/*
MIT License

Copyright (c) 2021 Armin Reichert

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

import java.util.Collection;
import java.util.Optional;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import de.amr.games.pacman.lib.Direction;
import de.amr.games.pacman.lib.V2d;
import de.amr.games.pacman.lib.V2i;

/**
 * Interface for accessing the game world.
 * 
 * @author Armin Reichert
 */
public interface World {

	/** Tile size in pixels. */
	public static final int TS = 8;

	/** Half tile size in pixels. */
	public static final int HTS = 4;

	/** Pixels corresponding to the given number of tiles. */
	public static int t(int numTiles) {
		return numTiles * TS;
	}

	/** Tile position of a given pixel position. */
	public static V2i tile(V2d position) {
		return new V2i((int) position.x / TS, (int) position.y / TS);
	}

	/** Tile offset of a given pixel position. */
	public static V2d offset(V2d position) {
		return new V2d(position.x - (int) (position.x / TS) * TS, position.y - (int) (position.y / TS) * TS);
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
	 * 
	 * @return optional map of the world
	 */
	Optional<WorldMap> getMap();

	/**
	 * @return all tiles in the world in order top to bottom, left-to-right.
	 */
	default Stream<V2i> tiles() {
		int w = numCols(), h = numRows();
		return IntStream.range(0, w * h).mapToObj(this::tile);
	}

	/**
	 * @param tile a tile
	 * @return tile index in order top-to-bottom, left-to-right.
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
	default boolean insideWorld(V2i tile) {
		return 0 <= tile.x && tile.x < numCols() && 0 <= tile.y && tile.y < numRows();
	}

	/**
	 * @return the player's home tile
	 */
	V2i playerHomeTile();

	/**
	 * @param ghostID ghost ID (0-3)
	 * @return ghost scattering target tile (an inaccessible tile)
	 */
	V2i ghostScatterTile(int ghostID);

	/**
	 * @return player start direction
	 */
	Direction playerStartDirection();

	/**
	 * @param ghostID ghost ID (0-3)
	 * @return ghost start direction
	 */
	Direction ghostStartDirection(int ghostID);

	/**
	 * @return portals inside this world
	 */
	Collection<Portal> portals();

	/**
	 * @return a random portal
	 */
	Portal randomPortal();

	/**
	 * @param tile a tile
	 * @return tells if the tile is a portal
	 */
	boolean isPortal(V2i tile);

	/**
	 * @param tile a tile
	 * @return tells if the tile can only be traversed from top to bottom
	 */
	boolean isOneWayDown(V2i tile);

	/**
	 * @param tile a tile
	 * @return tells if the tile is an intersection (waypoint)
	 */
	boolean isIntersection(V2i tile);

	/**
	 * @param tile a tile
	 * @return tells if the tile is a wall
	 */
	boolean isWall(V2i tile);

	/**
	 * @param tile a tile
	 * @return tells if the tile is part of a tunnel
	 */
	boolean isTunnel(V2i tile);

	/**
	 * @return the ghost house in this world
	 */
	GhostHouse ghostHouse();

	/**
	 * @param tile a tile
	 * @return tells if there is a ghost house door at this tile
	 */
	boolean isGhostHouseDoor(V2i tile);

	/**
	 * @param tile a tile
	 * @return tells if the tile may contain food (not if it currently contains food!)
	 */
	boolean isFoodTile(V2i tile);

	/**
	 * @param tile a tile
	 * @return tells if the tile may contain an energizer
	 */
	boolean isEnergizerTile(V2i tile);

	/**
	 * @return all energizer tiles in the world
	 */
	Collection<V2i> energizerTiles();

	/**
	 * @return bonus location in case this is fixed
	 */
	V2i bonusTile();

	/**
	 * 
	 * @param bonusIndex first or second bonus (index 0 or 1)
	 * @return number of pellets to eat for earning bonus
	 */
	int pelletsToEatForBonus(int bonusIndex);

	/**
	 * Removed food at given tile.
	 * 
	 * @param tile some tile
	 */
	void removeFood(V2i tile);

	/**
	 * @param tile some tile
	 * @return {@code true} if there is food at the given tile
	 */
	boolean containsFood(V2i tile);

	/**
	 * @param tile some tile
	 * @return {@code true} if there is eaten food at the given tile
	 */
	boolean isFoodEaten(V2i tile);

	/**
	 * @return number of pellets remaining
	 */
	int foodRemaining();

	/**
	 * @return number of pellets eaten
	 */
	int eatenFoodCount();

	/**
	 * Resets the food in this world.
	 */
	void resetFood();
}