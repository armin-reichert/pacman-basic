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

import static de.amr.games.pacman.lib.U.differsAtMost;
import static de.amr.games.pacman.lib.steering.Direction.LEFT;
import static de.amr.games.pacman.lib.steering.Direction.RIGHT;
import static de.amr.games.pacman.lib.steering.Direction.UP;
import static de.amr.games.pacman.model.common.Validator.checkNotNull;
import static de.amr.games.pacman.model.common.Validator.checkTileNotNull;

import java.util.ArrayList;
import java.util.BitSet;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import de.amr.games.pacman.lib.anim.AnimatedEntity;
import de.amr.games.pacman.lib.anim.AnimationMap;
import de.amr.games.pacman.lib.math.Vector2f;
import de.amr.games.pacman.lib.math.Vector2i;
import de.amr.games.pacman.lib.steering.Direction;
import de.amr.games.pacman.model.common.actors.Creature;

/**
 * The world used in the Arcade versions of Pac-Man and Ms. Pac-Man. Maze structure varies but ghost house
 * structure/position, ghost starting positions/directions and Pac-Man starting position/direction are the same for each
 * world.
 * 
 * @author Armin Reichert
 */
public class World implements AnimatedEntity {

	public static final int TILES_X = 28;
	public static final int TILES_Y = 36;

	//@formatter:off
	private static final byte TILE_SPACE           = 0;
	private static final byte TILE_WALL            = 1;
	private static final byte TILE_TUNNEL          = 2;
	private static final byte TILE_PELLET          = 3;
	private static final byte TILE_ENERGIZER       = 4;
	//@formatter:on

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
		checkNotNull(position);
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
	 * @param tileX tile x coordinate
	 * @param tileY tile y coordinate
	 * @return position half tile right of tile origin
	 */
	public static Vector2f halfTileRightOf(int tileX, int tileY) {
		return new Vector2f(tileX * TS + HTS, tileY * TS);
	}

	private final byte[][] tileMap;
	private AnimationMap animationMap;
	private final Vector2i houseTopLeftTile = new Vector2i(10, 15);
	private final Vector2i houseSize = new Vector2i(8, 5);
	private final Door houseDoor = new Door(new Vector2i(13, 15), 2);
	private final List<Vector2f> houseSeatPositions = List.of(//
			halfTileRightOf(11, 17), halfTileRightOf(13, 17), halfTileRightOf(15, 17));
	private List<Portal> portals;
	private List<Vector2i> energizerTiles;
	private int totalFoodCount;
	private int uneatenFoodCount;
	private final BitSet eatenSet = new BitSet(TILES_X * TILES_Y);

	/**
	 * @param tileMapData byte-array of tile map data
	 */
	public World(byte[][] tileMapData) {
		tileMap = validateTileMapData(tileMapData);
		energizerTiles = tiles().filter(this::isEnergizerTile).toList();
		totalFoodCount = (int) tiles().filter(this::isFoodTile).count();
		uneatenFoodCount = totalFoodCount;
		portals = findPortals();
	}

	private byte[][] validateTileMapData(byte[][] data) {
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
		for (int i = 0; i < data.length; ++i) {
			for (int j = 0; j < firstRow.length; ++j) {
				byte content = data[i][j];
				if (content < TILE_SPACE || content > TILE_ENERGIZER) {
					throw new IllegalArgumentException(
							"Map has invalid content '%s' at row %d column %d".formatted(content, i, j));
				}
			}
		}
		return data;
	}

	/**
	 * @return house size in tiles
	 */
	public Vector2i houseSize() {
		return houseSize;
	}

	/**
	 * @return house left-upper corner pixel position
	 */
	public Vector2i housePosition() {
		return houseTopLeftTile;
	}

	/**
	 * @return the unique house door
	 */
	public Door houseDoor() {
		return houseDoor;
	}

	/**
	 * @return the positions inside the house where ghosts can take a seat
	 */
	public List<Vector2f> houseSeatPositions() {
		return houseSeatPositions;
	}

	/**
	 * @param tile some tile
	 * @return tells if tile is occupied by a door
	 */
	public boolean houseDoorAt(Vector2i tile) {
		return houseDoor.contains(tile);
	}

	/**
	 * @param tile some tile
	 * @return tells if the given tile is part of this house
	 */
	public boolean houseContains(Vector2i tile) {
		Vector2i topLeft = housePosition();
		Vector2i bottomRightExclusive = topLeft.plus(houseSize());
		return tile.x() >= topLeft.x() && tile.x() < bottomRightExclusive.x() //
				&& tile.y() >= topLeft.y() && tile.y() < bottomRightExclusive.y();
	}

	/**
	 * Ghosts first move sidewards to the center, then they raise until the house entry/exit position outside is reached.
	 */
	public boolean leadOutsideHouse(Creature ghost) {
		var exitPosition = houseDoor.entryPosition();
		if (ghost.position().y() <= exitPosition.y()) {
			ghost.setPosition(exitPosition);
			return true;
		}
		if (differsAtMost(ghost.velocity().length() / 2, ghost.position().x(), exitPosition.x())) {
			// center reached: start rising
			ghost.setPosition(exitPosition.x(), ghost.position().y());
			ghost.setMoveAndWishDir(UP);
		} else {
			// move sidewards until middle axis is reached
			ghost.setMoveAndWishDir(ghost.position().x() < exitPosition.x() ? RIGHT : LEFT);
		}
		ghost.move();
		return false;
	}

	/**
	 * Ghost moves down on the vertical axis to the center, then returns or moves sidewards to its seat.
	 */
	public boolean leadInsideHouse(Creature ghost, Vector2f targetPosition) {
		var entryPosition = houseDoor.entryPosition();
		if (ghost.position().almostEquals(entryPosition, ghost.velocity().length() / 2, 0)
				&& ghost.moveDir() != Direction.DOWN) {
			// just reached door, start sinking
			ghost.setPosition(entryPosition);
			ghost.setMoveAndWishDir(Direction.DOWN);
		} else if (ghost.position().y() >= 17 * TS + HTS) {
			ghost.setPosition(ghost.position().x(), 17 * TS + HTS);
			if (targetPosition.x() < entryPosition.x()) {
				ghost.setMoveAndWishDir(LEFT);
			} else if (targetPosition.x() > entryPosition.x()) {
				ghost.setMoveAndWishDir(RIGHT);
			}
		}
		ghost.move();
		boolean reachedTarget = differsAtMost(1, ghost.position().x(), targetPosition.x())
				&& ghost.position().y() >= targetPosition.y();
		if (reachedTarget) {
			ghost.setPosition(targetPosition);
		}
		return reachedTarget;
	}

	private ArrayList<Portal> findPortals() {
		var portalList = new ArrayList<Portal>();
		for (int row = 0; row < numRows(); ++row) {
			if (content(row, 0) == TILE_TUNNEL && content(row, numCols() - 1) == TILE_TUNNEL) {
				portalList.add(new Portal(new Vector2i(0, row), new Vector2i(numCols() - 1, row), 2));
			}
		}
		portalList.trimToSize();
		return portalList;
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
		return new Vector2i(index % numCols(), index / numCols());
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

	/**
	 * @param tile some tile (may be outside world bound)
	 * @return the content at the given tile or empty space if outside world
	 */
	private byte content(Vector2i tile) {
		return content(tile.y(), tile.x(), TILE_SPACE);
	}

	private byte content(int row, int col) {
		if (!insideBounds(row, col)) {
			throwOutOfBoundsError(row, col);
		}
		return tileMap[row][col];
	}

	private byte content(int row, int col, byte defaultContent) {
		if (!insideBounds(row, col)) {
			return defaultContent;
		}
		return tileMap[row][col];
	}

	private boolean insideBounds(int row, int col) {
		return 0 <= row && row < numRows() && 0 <= col && col < numCols();
	}

	private void throwOutOfBoundsError(int row, int col) {
		throw new IndexOutOfBoundsException(
				"Coordinate (%d, %d) is outside of map bounds (%d rows, %d cols)".formatted(row, col, numRows(), numCols()));
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

	public boolean isWall(Vector2i tile) {
		checkTileNotNull(tile);
		return content(tile) == TILE_WALL;
	}

	public boolean isTunnel(Vector2i tile) {
		checkTileNotNull(tile);
		return content(tile) == TILE_TUNNEL;
	}

	public boolean isFoodTile(Vector2i tile) {
		checkTileNotNull(tile);
		byte data = content(tile);
		return data == TILE_PELLET || data == TILE_ENERGIZER;
	}

	public boolean isEnergizerTile(Vector2i tile) {
		checkTileNotNull(tile);
		byte data = content(tile);
		return data == TILE_ENERGIZER;
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
			byte data = content(tile);
			return (data == TILE_PELLET || data == TILE_ENERGIZER) && !eatenSet.get(index(tile));
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
		if (houseContains(tile)) {
			return false;
		}
		long numWallNeighbors = tile.neighbors().filter(this::isWall).count();
		long numDoorNeighbors = tile.neighbors().filter(houseDoor::contains).count();
		return numWallNeighbors + numDoorNeighbors < 2;
	}
}