/*
Copyright (c) 2021-2023 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
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
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import de.amr.games.pacman.lib.Pulse;
import de.amr.games.pacman.lib.TileMap;
import de.amr.games.pacman.lib.Vector2f;
import de.amr.games.pacman.lib.Vector2i;

/**
 * The tiled world used in the Arcade versions of Pac-Man and Ms. Pac-Man.
 * <p>
 * Maze structure varies, but ghost house, ghost starting positions/directions and Pac-Man starting position/direction
 * are the same for each level/world.
 * 
 * @author Armin Reichert
 */
public class World {

	//@formatter:off
	public static final byte SPACE           = 0;
	public static final byte WALL            = 1;
	public static final byte TUNNEL          = 2;
	public static final byte PELLET          = 3;
	public static final byte ENERGIZER       = 4;
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
		halfTileRightOf(11, 17), halfTileRightOf(13, 17), halfTileRightOf(15, 17)
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

	public static Vector2f centerOfTile(int tileX, int tileY) {
		return v2f(TS * tileX + HTS, TS * tileY + HTS);
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
	private final List<Vector2i> energizerTiles;
	private final BitSet eaten;
	private final long totalFoodCount;
	private long uneatenFoodCount;
	private final List<Portal> portals;
	private final Pulse energizerBlinking;
	private final Pulse mazeFlashing;

	/**
	 * @param tileMapData byte-array of tile map data
	 */
	public World(byte[][] tileMapData) {
		tileMap = new TileMap(tileMapData);
		portals = buildPortals(tileMap);

		energizerTiles = tiles().filter(this::isEnergizerTile).collect(Collectors.toList());
		eaten = new BitSet(numCols() * numRows());
		totalFoodCount = tiles().filter(this::isFoodTile).count();
		uneatenFoodCount = totalFoodCount;

		// Animations
		energizerBlinking = new Pulse(10, true);
		mazeFlashing = new Pulse(10, false);
	}

	public House house() {
		return ARCADE_HOUSE;
	}

	public Pulse energizerBlinking() {
		return energizerBlinking;
	}

	public Pulse mazeFlashing() {
		return mazeFlashing;
	}

	/**
	 * @return tiles in order top-to-bottom, left-to-right
	 */
	public Stream<Vector2i> tiles() {
		return IntStream.range(0, numCols() * numRows()).mapToObj(this::tile);
	}

	public Stream<Vector2i> energizerTiles() {
		return energizerTiles.stream();
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

	private int index(Vector2i tile) {
		return numCols() * tile.y() + tile.x();
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
	public byte contentOrSpace(Vector2i tile) {
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


	public long totalFoodCount() {
		return totalFoodCount;
	}

	public long uneatenFoodCount() {
		return uneatenFoodCount;
	}

	public long eatenFoodCount() {
		return totalFoodCount - uneatenFoodCount;
	}

	public void removeFood(Vector2i tile) {
		checkTileNotNull(tile);
		if (insideBounds(tile) && hasFoodAt(tile)) {
			eaten.set(index(tile));
			--uneatenFoodCount;
		}
	}

	public boolean hasFoodAt(Vector2i tile) {
		checkTileNotNull(tile);
		if (insideBounds(tile)) {
			byte data = contentOrSpace(tile);
			return (data == World.PELLET || data == World.ENERGIZER) && !eaten.get(index(tile));
		}
		return false;
	}

	public boolean hasEatenFoodAt(Vector2i tile) {
		checkTileNotNull(tile);
		if (insideBounds(tile)) {
			return eaten.get(index(tile));
		}
		return false;
	}
}