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
package de.amr.games.pacman.model.common.world;

import java.util.List;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import de.amr.games.pacman.lib.anim.AnimatedEntity;
import de.amr.games.pacman.lib.anim.AnimationMap;
import de.amr.games.pacman.lib.math.Vector2f;
import de.amr.games.pacman.lib.math.Vector2i;
import de.amr.games.pacman.lib.steering.Direction;

/**
 * Tiled world.
 * 
 * @author Armin Reichert
 */
public interface World extends AnimatedEntity {

	/** Tile size in pixels (8). */
	public static final int TS = 8;

	/** Half tile size in pixels (4). */
	public static final int HTS = 4;

	/**
	 * @param numTiles number of tiles
	 * @return pixels corresponding to the given number of tiles
	 */
	public static int t(int numTiles) {
		return numTiles * TS;
	}

	/**
	 * @param position a position
	 * @return tile containing given position
	 */
	public static Vector2i tileAt(Vector2f position) {
		return tileAt(position.x(), position.y());
	}

	/**
	 * @param x x position
	 * @param y y position
	 * @return tile containing given position
	 */
	public static Vector2i tileAt(float x, float y) {
		return new Vector2i((int) (x / TS), (int) (y / TS));
	}

	/**
	 * @param tile a tile
	 * @return position of the left-upper corner of given tile
	 */
	public static Vector2f originOfTile(Vector2i tile) {
		return tile.scaled(TS).toFloatVec();
	}

	/**
	 * @param tileX tile x coordinate
	 * @param tileY tile y coordinate
	 * @return position half tile right of tile origin
	 */
	public static Vector2f halfTileRightOf(int tileX, int tileY) {
		return new Vector2f(tileX * TS + HTS, tileY * TS);
	}

	/**
	 * @param tile tile
	 * @return position half tile right of tile origin
	 */
	public static Vector2f halfTileRightOf(Vector2i tile) {
		return halfTileRightOf(tile.x(), tile.y());
	}

	/**
	 * @return world size (number of tiles) in horizontal direction
	 */
	int numCols();

	/**
	 * @return world size (number of tiles) in vertical direction
	 */
	int numRows();

	/**
	 * @return tiles in order top-to-bottom, left-to-right
	 */
	default Stream<Vector2i> tiles() {
		return IntStream.range(0, numCols() * numRows()).mapToObj(this::tile);
	}

	/**
	 * @param tile a tile
	 * @return tile index in order top-to-bottom, left-to-right
	 */
	default int index(Vector2i tile) {
		return numCols() * tile.y() + tile.x();
	}

	/**
	 * @param index tile index in order top-to-bottom, left-to-right
	 * @return tile with given index
	 */
	default Vector2i tile(int index) {
		return new Vector2i(index % numCols(), index / numCols());
	}

	/**
	 * @param tile a tile
	 * @return if this tile is located inside the world bounds
	 */
	default boolean insideBounds(Vector2i tile) {
		return 0 <= tile.x() && tile.x() < numCols() && 0 <= tile.y() && tile.y() < numRows();
	}

	/**
	 * @return if this position is located inside the world bounds
	 */
	default boolean insideBounds(double x, double y) {
		return 0 <= x && x < numCols() * TS && 0 <= y && y < numRows() * TS;
	}

	/**
	 * @return list of all portals in this world
	 */
	List<Portal> portals();

	/**
	 * @param tile a tile
	 * @return if the tile is part of a portal
	 */
	boolean belongsToPortal(Vector2i tile);

	/**
	 * @param tile a tile
	 * @return if the tile is an intersection (waypoint)
	 */
	boolean isIntersection(Vector2i tile);

	/**
	 * @param tile a tile
	 * @return if the tile is a wall
	 */
	boolean isWall(Vector2i tile);

	/**
	 * @param tile a tile
	 * @return if the tile is part of a tunnel
	 */
	boolean isTunnel(Vector2i tile);

	/**
	 * @return start position of Pac-Man in this world
	 */
	Vector2f pacInitialPosition();

	/**
	 * @return start direction of Pac-Man in this world
	 */
	Direction pacInitialDirection();

	/**
	 * @return the ghost house in this world
	 */
	GhostHouse ghostHouse();

	/**
	 * @param ghostID ghost ID
	 * @return scatter target tile of ghost with given ID
	 */
	Vector2i ghostScatterTargetTile(byte ghostID);

	/**
	 * @param ghostID ghost ID
	 * @return initial position of ghost with given ID
	 */
	Vector2f ghostInitialPosition(byte ghostID);

	/**
	 * @param ghostID ghost ID
	 * @return initial direction of ghost with given ID
	 */
	Direction ghostInitialDirection(byte ghostID);

	/**
	 * @param ghostID ghost ID
	 * @return revival position of ghost with given ID
	 */
	Vector2f ghostRevivalPosition(byte ghostID);

	/**
	 * @param tile a tile
	 * @return tells if the tile contains food initially
	 */
	boolean isFoodTile(Vector2i tile);

	/**
	 * @param tile a tile
	 * @return if the tile contains an energizer initially
	 */
	boolean isEnergizerTile(Vector2i tile);

	/**
	 * @return all tiles containing an energizer initially
	 */
	Stream<Vector2i> energizerTiles();

	/**
	 * Removes food at given tile.
	 * 
	 * @param tile some tile
	 */
	void removeFood(Vector2i tile);

	/**
	 * @param tile some tile
	 * @return if there is food at the given tile
	 */
	boolean containsFood(Vector2i tile);

	/**
	 * @param tile some tile
	 * @return if there is eaten food at the given tile
	 */
	boolean containsEatenFood(Vector2i tile);

	/**
	 * @return number of uneaten pellets
	 */
	int uneatenFoodCount();

	/**
	 * @return number of eaten pellets
	 */
	int eatenFoodCount();

	default Stream<Vector2i> tilesContainingFood() {
		return tiles().filter(this::containsFood);
	}

	default Stream<Vector2i> tilesContainingEatenFood() {
		return tiles().filter(this::containsEatenFood);
	}

	void setAnimations(AnimationMap animationMap);
}