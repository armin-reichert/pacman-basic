/*
MIT License

Copyright (c) 2021-22 Armin Reichert

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
package de.amr.games.pacman.model.common.world;

import java.util.List;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import de.amr.games.pacman.lib.V2d;
import de.amr.games.pacman.lib.V2i;

/**
 * Interface for accessing the game world.
 * 
 * @author Armin Reichert
 */
public interface World {

	/** Tile size in pixels (8). */
	public static final int TS = 8;

	/** Half tile size in pixels (4). */
	public static final int HTS = 4;

	/**
	 * @param numTiles number of tiles
	 * @return Pixels corresponding to the given number of tiles.
	 */
	public static int t(int numTiles) {
		return numTiles * TS;
	}

	/** Tile position of a given pixel position. */
	public static V2i tile(V2d position) {
		return new V2i((int) position.x / TS, (int) position.y / TS);
	}

	/** Tile offset of a given pixel position. */
	public static V2d offset(V2d position) {
		return position.minus(new V2d(tile(position).scaled(TS)));
	}

	/**
	 * @return Number of tiles in horizontal direction.
	 */
	int numCols();

	/**
	 * @return Number of tiles in vertical direction.
	 */
	int numRows();

	/**
	 * @return All tiles in the world in order top-to-bottom, left-to-right.
	 */
	default Stream<V2i> tiles() {
		return IntStream.range(0, numCols() * numRows()).mapToObj(this::tile);
	}

	/**
	 * @param tile a tile
	 * @return Tile index in order top-to-bottom, left-to-right.
	 */
	default int index(V2i tile) {
		return numCols() * tile.y + tile.x;
	}

	/**
	 * @param index tile index in order top-to-bottom, left-to-right
	 * @return tile as vector
	 */
	default V2i tile(int index) {
		return new V2i(index % numCols(), index / numCols());
	}

	/**
	 * @param tile a tile
	 * @return tells if this tile is located inside the world bounds
	 */
	default boolean insideMap(V2i tile) {
		return 0 <= tile.x && tile.x < numCols() && 0 <= tile.y && tile.y < numRows();
	}

	/**
	 * @return portals inside this world
	 */
	List<Portal> portals();

	/**
	 * @param tile a tile
	 * @return Tells if the tile is part of a portal. The x-(tile-)position of a portal tile is either {@code -1} (left
	 *         portal) or {@code numCols()} (right portal).
	 */
	boolean isPortal(V2i tile);

	/**
	 * @param tile a tile
	 * @return Tells if the tile is an intersection (waypoint).
	 */
	default boolean isIntersection(V2i tile) {
		if (tile.x <= 0 || tile.x >= numCols() - 1) {
			return false; // exclude portal entries and tiles outside of the map
		}
		if (ghostHouse().contains(tile)) {
			return false;
		}
		long numWallNeighbors = tile.neighbors().filter(this::isWall).count();
		long numDoorNeighbors = tile.neighbors().filter(ghostHouse()::isDoor).count();
		return numWallNeighbors + numDoorNeighbors < 2;
	}

	/**
	 * @param tile a tile
	 * @return Tells if the tile is a wall.
	 */
	boolean isWall(V2i tile);

	/**
	 * @param tile a tile
	 * @return Tells if the tile is part of a tunnel.
	 */
	boolean isTunnel(V2i tile);

	/**
	 * @return the ghost house in this world
	 */
	GhostHouse ghostHouse();

	/**
	 * @param tile a tile
	 * @return Tells if the tile contains food initially.
	 */
	boolean isFoodTile(V2i tile);

	/**
	 * @param tile a tile
	 * @return Tells if the tile contains an energizer initially.
	 */
	boolean isEnergizerTile(V2i tile);

	/**
	 * @return All tiles containing an energizer initially.
	 */
	Stream<V2i> energizerTiles();

	/**
	 * Removes food at given tile.
	 * 
	 * @param tile some tile
	 */
	void removeFood(V2i tile);

	/**
	 * @param tile some tile
	 * @return Return {@code true} if there is food at the given tile.
	 */
	boolean containsFood(V2i tile);

	/**
	 * @param tile some tile
	 * @return Returns {@code true} if there is eaten food at the given tile.
	 */
	boolean containsEatenFood(V2i tile);

	/**
	 * @return Number of uneaten pellets remaining
	 */
	int foodRemaining();

	/**
	 * @return Number of pellets eaten.
	 */
	int eatenFoodCount();

	/**
	 * Resets the food in this world.
	 */
	void resetFood();
}