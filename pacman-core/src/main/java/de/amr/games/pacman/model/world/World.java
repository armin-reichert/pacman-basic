/*
MIT License

Copyright (c) 2021-2023 Armin Reichert

Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files (the "Software"), to deal
in the Software without restriction, including without limitation the rights
to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
copies of the Software, and to permit persons to whom the Software is
furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all
copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
SOFTWARE.
 */
package de.amr.games.pacman.model.world;

import static de.amr.games.pacman.lib.Globals.HTS;
import static de.amr.games.pacman.lib.Globals.TS;
import static de.amr.games.pacman.lib.Globals.checkNotNull;
import static de.amr.games.pacman.lib.Globals.checkTileNotNull;
import static de.amr.games.pacman.lib.Globals.v2f;
import static de.amr.games.pacman.lib.Globals.v2i;

import java.util.ArrayList;
import java.util.BitSet;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import de.amr.games.pacman.lib.TileMap;
import de.amr.games.pacman.lib.anim.AnimationMap;
import de.amr.games.pacman.lib.math.Vector2f;
import de.amr.games.pacman.lib.math.Vector2i;
import de.amr.games.pacman.model.AnimatedEntity;
import de.amr.games.pacman.model.actors.Entity;

/**
 * The tiled world used in the Arcade versions of Pac-Man and Ms. Pac-Man.
 * <p>
 * Maze structure varies, but ghost house, ghost starting positions/directions and Pac-Man starting position/direction
 * are the same for each level/world.
 * 
 * @author Armin Reichert
 */
public class World extends Entity implements AnimatedEntity {

	//@formatter:off
	private static final byte SPACE           = 0;
	private static final byte WALL            = 1;
	private static final byte TUNNEL          = 2;
	private static final byte PELLET          = 3;
	private static final byte ENERGIZER       = 4;
	//@formatter:on

	/** World size in x-direction in tiles. */
	public static final int TILES_X = 28;

	/** World size in y-direction in tiles. */
	public static final int TILES_Y = 36;

	/**
	 * The ghosthouse as it looks in the Arcade version of Pac-Man and Ms. Pac-Man.
	 */
	//@formatter:off
	private static final House ARCADE_HOUSE = new House(
		v2i(10, 15), // top-left corner
		v2i(8, 5),   // size in tiles
		new Door(v2i(13, 15), v2i(14, 15)),
		List.of(halfTileRightOf(11, 17), halfTileRightOf(13, 17), halfTileRightOf(15, 17)),
		halfTileRightOf(13, 17).plus(0, HTS) // center position
	);
	//@formatter:on

	/**
	 * @param position a position
	 * @return tile containing given position
	 */
	public static Vector2i tileAt(Vector2f position) {
		checkNotNull(position);
		return tileAt(position.x(), position.y());
	}

	/**
	 * @param x x position
	 * @param y y position
	 * @return tile containing given position
	 */
	public static Vector2i tileAt(float x, float y) {
		return v2i((int) (x / TS), (int) (y / TS));
	}

	/**
	 * @param tileX tile x coordinate
	 * @param tileY tile y coordinate
	 * @return position half tile right of tile origin
	 */
	public static Vector2f halfTileRightOf(int tileX, int tileY) {
		return v2f(TS * tileX + HTS, TS * tileY);
	}

	private static List<Portal> buildPortals(TileMap tileMap) {
		var portals = new ArrayList<Portal>();
		int lastColumn = tileMap.numCols() - 1;
		for (int row = 0; row < tileMap.numRows(); ++row) {
			var leftBorderTile = v2i(0, row);
			var rightBorderTile = v2i(lastColumn, row);
			if (tileMap.content(row, 0) == TUNNEL && tileMap.content(row, lastColumn) == TUNNEL) {
				portals.add(new Portal(leftBorderTile, rightBorderTile, 2));
			}
		}
		portals.trimToSize();
		return portals;
	}

	private final TileMap tileMap;
	private final List<Portal> portals;
	private final List<Vector2i> energizerTiles;
	private final BitSet eatenSet;
	private final int totalFoodCount;
	private int uneatenFoodCount;
	private AnimationMap animationMap;

	/**
	 * @param tileMapData byte-array of tile map data
	 */
	public World(byte[][] tileMapData) {
		tileMap = new TileMap(tileMapData);
		portals = buildPortals(tileMap);
		eatenSet = new BitSet(tileMap.numCols() * tileMap.numRows());
		energizerTiles = tiles().filter(this::isEnergizerTile).toList();
		totalFoodCount = (int) tiles().filter(this::isFoodTile).count();
		uneatenFoodCount = totalFoodCount;
	}

	@Override
	public Entity entity() {
		return this;
	}

	public House house() {
		return ARCADE_HOUSE;
	}

	/**
	 * @return tiles in order top-to-bottom, left-to-right
	 */
	public Stream<Vector2i> tiles() {
		return IntStream.range(0, numCols() * numRows()).mapToObj(this::tile);
	}

	/**
	 * @param tile a tile
	 * @return tile index in order top-to-bottom, left-to-right
	 */
	public int index(Vector2i tile) {
		return numCols() * tile.y() + tile.x();
	}

	/**
	 * @param index tile index in order top-to-bottom, left-to-right
	 * @return tile with given index
	 */
	public Vector2i tile(int index) {
		return v2i(index % numCols(), index / numCols());
	}

	/**
	 * @param tile a tile
	 * @return if this tile is located inside the world bounds
	 */
	public boolean insideBounds(Vector2i tile) {
		return 0 <= tile.x() && tile.x() < numCols() && 0 <= tile.y() && tile.y() < numRows();
	}

	/**
	 * @return if this position is located inside the world bounds
	 */
	public boolean insideBounds(double x, double y) {
		return 0 <= x && x < numCols() * TS && 0 <= y && y < numRows() * TS;
	}

	@Override
	public Optional<AnimationMap> animations() {
		return Optional.ofNullable(animationMap);
	}

	public void setAnimations(AnimationMap animationMap) {
		this.animationMap = animationMap;
	}

	public int numCols() {
		return TILES_X;
	}

	public int numRows() {
		return TILES_Y;
	}

	public List<Portal> portals() {
		return Collections.unmodifiableList(portals);
	}

	public boolean belongsToPortal(Vector2i tile) {
		checkTileNotNull(tile);
		return portals.stream().anyMatch(portal -> portal.contains(tile));
	}

	/**
	 * @param tile some tile (may be outside world bound)
	 * @return the content at the given tile or empty space if outside world
	 */
	private byte contentOrSpace(Vector2i tile) {
		// Note: content is stored row-wise, so use (y,x) to index content
		return tileMap.content(tile.y(), tile.x(), SPACE);
	}

	public boolean isWall(Vector2i tile) {
		checkTileNotNull(tile);
		return contentOrSpace(tile) == WALL;
	}

	public boolean isTunnel(Vector2i tile) {
		checkTileNotNull(tile);
		return contentOrSpace(tile) == TUNNEL;
	}

	public boolean isFoodTile(Vector2i tile) {
		checkTileNotNull(tile);
		byte data = contentOrSpace(tile);
		return data == PELLET || data == ENERGIZER;
	}

	public boolean isEnergizerTile(Vector2i tile) {
		checkTileNotNull(tile);
		return contentOrSpace(tile) == ENERGIZER;
	}

	public Stream<Vector2i> energizerTiles() {
		return energizerTiles.stream();
	}

	public void removeFood(Vector2i tile) {
		checkTileNotNull(tile);
		if (insideBounds(tile) && containsFood(tile)) {
			eatenSet.set(index(tile));
			--uneatenFoodCount;
		}
	}

	public boolean containsFood(Vector2i tile) {
		checkTileNotNull(tile);
		if (insideBounds(tile)) {
			byte data = contentOrSpace(tile);
			return (data == PELLET || data == ENERGIZER) && !eatenSet.get(index(tile));
		}
		return false;
	}

	public boolean containsEatenFood(Vector2i tile) {
		checkTileNotNull(tile);
		if (insideBounds(tile)) {
			return eatenSet.get(index(tile));
		}
		return false;
	}

	public int uneatenFoodCount() {
		return uneatenFoodCount;
	}

	public int eatenFoodCount() {
		return totalFoodCount - uneatenFoodCount;
	}

	public boolean isIntersection(Vector2i tile) {
		checkTileNotNull(tile);

		if (tile.x() <= 0 || tile.x() >= numCols() - 1) {
			return false; // exclude portal entries and tiles outside of the map
		}
		if (ARCADE_HOUSE.contains(tile)) {
			return false;
		}
		long numWallNeighbors = tile.neighbors().filter(this::isWall).count();
		long numDoorNeighbors = tile.neighbors().filter(ARCADE_HOUSE.door()::occupies).count();
		return numWallNeighbors + numDoorNeighbors < 2;
	}
}