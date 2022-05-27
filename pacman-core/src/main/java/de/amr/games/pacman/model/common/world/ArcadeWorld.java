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

import static de.amr.games.pacman.lib.Logging.log;
import static de.amr.games.pacman.model.common.Ghost.CYAN_GHOST;
import static de.amr.games.pacman.model.common.Ghost.ORANGE_GHOST;
import static de.amr.games.pacman.model.common.Ghost.PINK_GHOST;
import static de.amr.games.pacman.model.common.Ghost.RED_GHOST;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.BitSet;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import de.amr.games.pacman.lib.Direction;
import de.amr.games.pacman.lib.V2i;

/**
 * Implements all stuff that is common to the original Arcade worlds like ghost house position, ghost and player start
 * positions and direction etc.
 * 
 * @author Armin Reichert
 */
public class ArcadeWorld implements World {

	public static int TILES_X = 28;
	public static int TILES_Y = 36;

	//@formatter:off
	public static final byte SPACE           = 0;
	public static final byte WALL            = 1;
	public static final byte TUNNEL          = 2;
	public static final byte PELLET          = 3;
	public static final byte ENERGIZER       = 4;
	public static final byte PELLET_EATEN    = 5;
	public static final byte ENERGIZER_EATEN = 6;
	//@formatter:on

	protected static V2i v(int x, int y) {
		return new V2i(x, y);
	}

	private static byte[][] copyArray2D(byte[][] arr) {
		return Arrays.stream(arr).map(byte[]::clone).toArray(byte[][]::new);
	}

	protected V2i size;
	protected final byte[][] map;
	protected final V2i leftLowerTarget = v(0, 34);
	protected final V2i rightLowerTarget = v(27, 34);
	protected final V2i leftUpperTarget = v(2, 0);
	protected final V2i rightUpperTarget = v(25, 0);
	protected final V2i pacHome = v(13, 26);
	protected final GhostHouse house;
	protected final BitSet intersections;
	protected final List<V2i> energizerTiles;
	protected final int[] pelletsToEatForBonus = new int[2];
	protected final List<Portal> portals;
	protected final int totalFoodCount;

	// subclasses may override these:
	protected List<V2i> upwardsBlockedTiles = List.of();
	protected V2i bonusTile = v(0, 0);

	protected int foodRemaining;

	protected ArcadeWorld(byte[][] map) {
		this.map = copyArray2D(map);
		size = v(map[0].length, map.length); // cols x rows!

		house = new GhostHouse(v(10, 15), v(7, 4));
		house.doorTileLeft = v(13, 15);
		house.doorTileRight = v(14, 15);
		house.seatTileLeft = v(11, 17);
		house.seatTileMiddle = v(13, 17);
		house.seatTileRight = v(15, 17);

		ArrayList<Portal> portalList = new ArrayList<>();
		for (int row = 0; row < size.y; ++row) {
			if (map[row][0] == TUNNEL && map[row][size.x - 1] == TUNNEL) {
				portalList.add(new Portal(new V2i(-1, row), new V2i(size.x, row)));
			}
		}
		portalList.trimToSize();
		portals = Collections.unmodifiableList(portalList);

		intersections = new BitSet();
		tiles() //
				.filter(tile -> !house.contains(tile)) //
				.filter(tile -> tile.x > 0 && tile.x < numCols() - 1) //
				.filter(tile -> tile.neighbors().filter(nb -> isWall(nb) || house.isDoor(nb)).count() <= 1) //
				.map(this::index) //
				.forEach(intersections::set);

		energizerTiles = tiles().filter(this::isEnergizerTile).collect(Collectors.toUnmodifiableList());
		totalFoodCount = (int) tiles().filter(this::isFoodTile).count();
		foodRemaining = totalFoodCount;
	}

	protected byte map(V2i tile) {
		return insideWorld(tile) ? map[tile.y][tile.x] : SPACE;
	}

	@Override
	public int numCols() {
		return size.x;
	}

	@Override
	public int numRows() {
		return size.y;
	}

	@Override
	public Direction playerStartDirection() {
		return Direction.LEFT;
	}

	@Override
	public V2i playerHomeTile() {
		return pacHome;
	}

	@Override
	public Direction ghostStartDirection(int ghostID) {
		return switch (ghostID) {
		case RED_GHOST -> Direction.LEFT;
		case PINK_GHOST -> Direction.DOWN;
		case CYAN_GHOST -> Direction.UP;
		case ORANGE_GHOST -> Direction.UP;
		default -> throw new IllegalArgumentException("IIlegal ghost ID: " + ghostID);
		};
	}

	@Override
	public V2i ghostScatterTile(int ghostID) {
		return switch (ghostID) {
		case RED_GHOST -> rightUpperTarget;
		case PINK_GHOST -> leftUpperTarget;
		case CYAN_GHOST -> rightLowerTarget;
		case ORANGE_GHOST -> leftLowerTarget;
		default -> throw new IllegalArgumentException("IIlegal ghost ID: " + ghostID);
		};
	}

	@Override
	public V2i bonusTile() {
		return bonusTile;
	}

	@Override
	public int pelletsToEatForBonus(int bonusIndex) {
		return pelletsToEatForBonus[bonusIndex];
	}

	@Override
	public GhostHouse ghostHouse() {
		return house;
	}

	@Override
	public List<Portal> portals() {
		return portals;
	}

	@Override
	public boolean isPortal(V2i tile) {
		return portals.stream().anyMatch(portal -> portal.left.equals(tile) || portal.right.equals(tile));
	}

	@Override
	public boolean isOneWayDown(V2i tile) {
		return insideWorld(tile) && upwardsBlockedTiles.contains(tile);
	}

	@Override
	public boolean isIntersection(V2i tile) {
		return insideWorld(tile) && intersections.get(index(tile));
	}

	@Override
	public boolean isWall(V2i tile) {
		return map(tile) == WALL;
	}

	@Override
	public boolean isTunnel(V2i tile) {
		return map(tile) == TUNNEL;
	}

	@Override
	public boolean isFoodTile(V2i tile) {
		byte data = map(tile);
		return data == PELLET || data == PELLET_EATEN || data == ENERGIZER || data == ENERGIZER_EATEN;
	}

	@Override
	public boolean isEnergizerTile(V2i tile) {
		byte data = map(tile);
		return data == ENERGIZER || data == ENERGIZER_EATEN;
	}

	@Override
	public Stream<V2i> energizerTiles() {
		return energizerTiles.stream();
	}

	@Override
	public void removeFood(V2i tile) {
		byte data = map(tile);
		if (data == ENERGIZER) {
			map[tile.y][tile.x] = ENERGIZER_EATEN;
			--foodRemaining;
		} else if (data == PELLET) {
			map[tile.y][tile.x] = PELLET_EATEN;
			--foodRemaining;
		}
	}

	@Override
	public boolean containsFood(V2i tile) {
		byte data = map(tile);
		return data == PELLET || data == ENERGIZER;
	}

	@Override
	public boolean containsEatenFood(V2i tile) {
		byte data = map(tile);
		return data == PELLET_EATEN || data == ENERGIZER_EATEN;
	}

	@Override
	public int foodRemaining() {
		return foodRemaining;
	}

	@Override
	public int eatenFoodCount() {
		return totalFoodCount - foodRemaining;
	}

	@Override
	public void resetFood() {
		for (int row = 0; row < size.y; ++row) {
			for (int col = 0; col < size.x; ++col) {
				if (map[row][col] == PELLET_EATEN) {
					map[row][col] = PELLET;
				} else if (map[row][col] == ENERGIZER_EATEN) {
					map[row][col] = ENERGIZER;
				}
			}
		}
		foodRemaining = totalFoodCount;
		long energizerCount = tiles().filter(this::isEnergizerTile).count();
		log("Food restored (%d pellets, %d energizers)", totalFoodCount - energizerCount, energizerCount);
	}
}