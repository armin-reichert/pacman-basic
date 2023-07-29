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

	public static final byte T_SPACE     = 0;
	public static final byte T_WALL      = 1;
	public static final byte T_TUNNEL    = 2;
	public static final byte T_PELLET    = 3;
	public static final byte T_ENERGIZER = 4;

	public static final int ARCADE_TILES_X = 28;
	public static final int ARCADE_TILES_Y = 36;

	public static final House ARCADE_HOUSE;

	static {
		ARCADE_HOUSE= new House(
			v2i(10, 15), // top-left corner tile
			v2i(8, 5),   // size in tiles
			new Door(v2i(13, 15), v2i(14, 15))
		);
		ARCADE_HOUSE.setSeat("left", halfTileRightOf(11, 17));
		ARCADE_HOUSE.setSeat("middle", halfTileRightOf(13, 17));
		ARCADE_HOUSE.setSeat("right", halfTileRightOf(15, 17));
	}

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

	private static List<Portal> buildPortals(byte[][] tileMap) {
		int numRows = tileMap.length;
		int numCols = tileMap[0].length;
		var portals = new ArrayList<Portal>();
		int lastColumn = numCols - 1;
		for (int row = 0; row < numRows; ++row) {
			var leftBorderTile = v2i(0, row);
			var rightBorderTile = v2i(lastColumn, row);
			if (tileMap[row][0] == T_TUNNEL && tileMap[row][lastColumn] == T_TUNNEL) {
				portals.add(new Portal(leftBorderTile, rightBorderTile, 2));
			}
		}
		portals.trimToSize();
		return portals;
	}

	private static byte[][] validateTileMapData(byte[][] data) {
		if (data == null) {
			throw new IllegalArgumentException("Map data missing");
		}
		if (data.length == 0) {
			throw new IllegalArgumentException("Map data empty");
		}
		var firstRow = data[0];
		if (firstRow.length == 0) {
			throw new IllegalArgumentException("Map data empty");
		}
		for (int i = 0; i < data.length; ++i) {
			if (data[i].length != firstRow.length) {
				throw new IllegalArgumentException("Map has differently sized rows");
			}
		}
		return data;
	}


	private final byte[][] tileMap;
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
		tileMap = validateTileMapData(tileMapData);
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
		return ARCADE_TILES_X;
	}

	public int numRows() {
		return ARCADE_TILES_Y;
	}

	public List<Portal> portals() {
		return Collections.unmodifiableList(portals);
	}

	public boolean belongsToPortal(Vector2i tile) {
		checkTileNotNull(tile);
		return portals.stream().anyMatch(portal -> portal.contains(tile));
	}

	private byte contentOrSpace(Vector2i tile) {
		return insideBounds(tile) ? tileMap[tile.y()][tile.x()] : T_SPACE;
	}

	public boolean isWall(Vector2i tile) {
		checkTileNotNull(tile);
		return contentOrSpace(tile) == T_WALL;
	}

	public boolean isTunnel(Vector2i tile) {
		checkTileNotNull(tile);
		return contentOrSpace(tile) == T_TUNNEL;
	}

	public boolean isFoodTile(Vector2i tile) {
		checkTileNotNull(tile);
		byte data = contentOrSpace(tile);
		return data == T_PELLET || data == T_ENERGIZER;
	}

	public boolean isEnergizerTile(Vector2i tile) {
		checkTileNotNull(tile);
		return contentOrSpace(tile) == T_ENERGIZER;
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
		if (hasFoodAt(tile)) {
			eaten.set(index(tile));
			--uneatenFoodCount;
		}
	}

	public boolean hasFoodAt(Vector2i tile) {
		checkTileNotNull(tile);
		if (insideBounds(tile)) {
			byte data = tileMap[tile.y()][tile.x()];
			return (data == T_PELLET || data == T_ENERGIZER) && !eaten.get(index(tile));
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