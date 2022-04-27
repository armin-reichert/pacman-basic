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
package de.amr.games.pacman.model.common.world;

import static de.amr.games.pacman.lib.Logging.log;

import java.util.ArrayList;
import java.util.BitSet;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import de.amr.games.pacman.lib.Direction;
import de.amr.games.pacman.lib.V2i;
import de.amr.games.pacman.model.common.GameModel;

/**
 * Base class for Pac-Man and Ms. Pac-Man worlds as given in the original Arcade versions. Implements all common stuff
 * like ghost house position, ghost and player start positions and direction etc. Concrete subclasses have (almost) only
 * to provide their specific world map.
 * 
 * @author Armin Reichert
 */
public class ArcadeWorld implements World {

	//@formatter:off
	public static final byte SPACE           = 0; // ' '
	public static final byte WALL            = 1; // '#'
	public static final byte TUNNEL          = 2; // 'T'
	public static final byte PELLET          = 3; // '.'
	public static final byte ENERGIZER       = 4; // '*'
	public static final byte PELLET_EATEN    = 5;
	public static final byte ENERGIZER_EATEN = 6;
	//@formatter:on

	protected static V2i v(int x, int y) {
		return new V2i(x, y);
	}

	protected final V2i size;
	protected final byte[][] map;
	protected List<V2i> energizerTiles;
	protected V2i bonusTile = v(0, 0);
	protected int[] pelletsToEatForBonus = new int[2];
	protected List<Portal> portals = List.of();
	protected List<V2i> upwardsBlockedTiles = List.of();
	protected BitSet intersections = new BitSet();
	protected GhostHouse house;
	protected int totalFoodCount;
	protected int foodRemaining;

	protected ArcadeWorld(String[] mapText) {
		size = computeMapSize(mapText);
		map = new byte[size.y][size.x];
		for (int row = 0; row < size.y; ++row) {
			for (int col = 0; col < size.x; ++col) {
				char ch = mapText[row].charAt(col);
				map[row][col] = switch (ch) {
				case ' ' -> SPACE;
				case '#' -> WALL;
				case 'T' -> TUNNEL;
				case '.' -> PELLET;
				case '*' -> ENERGIZER;
				default -> throw new IllegalArgumentException(String.format("Illegal map character '%c' ", ch));
				};
			}
		}
		buildGhostHouse();
		buildPortals();
		findIntersections();
		resetFood();
		energizerTiles = tiles().filter(this::isEnergizerTile).collect(Collectors.toUnmodifiableList());
	}

	protected V2i computeMapSize(String[] mapText) {
		int size_y = mapText.length;
		if (size_y == 0) {
			throw new IllegalArgumentException("Map is empty");
		}
		int size_x = mapText[0].length();
		if (size_x == 0) {
			throw new IllegalArgumentException("Map is empty");
		}
		for (int row = 1; row < size_y; ++row) {
			if (mapText[row].length() != size_x) {
				throw new IllegalArgumentException(
						String.format("Map row %d has wrong length %d, should be %d", row, mapText[row].length(), size_x));
			}
		}
		return v(size_x, size_y);
	}

	protected void findIntersections() {
		intersections = new BitSet();
		tiles() //
				.filter(tile -> !house.contains(tile)) //
				.filter(tile -> tile.x > 0 && tile.x < numCols() - 1) //
				.filter(tile -> World.neighbors(tile).filter(nb -> isWall(nb) || isDoor(nb)).count() <= 1) //
				.map(this::index) //
				.forEach(intersections::set);
	}

	private boolean isDoor(V2i tile) {
		return tile.equals(house.leftDoor) || tile.equals(house.rightDoor);
	}

	protected void buildPortals() {
		portals = new ArrayList<>(3);
		for (int y = 0; y < numRows(); ++y) {
			V2i leftBorder = v(0, y), rightBorder = v(numCols() - 1, y);
			if (map(leftBorder) == TUNNEL && map(rightBorder) == TUNNEL) {
				portals.add(new Portal(new V2i(-1, y), new V2i(numCols(), y)));
			}
		}
		portals = Collections.unmodifiableList(portals);
	}

	protected void buildGhostHouse() {
		house = new GhostHouse(v(10, 15), v(7, 4));
		house.leftDoor = v(13, 15);
		house.rightDoor = v(14, 15);
		house.seatLeft = v(11, 17);
		house.seatCenter = v(13, 17);
		house.seatRight = v(15, 17);
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
		return v(13, 26);
	}

	@Override
	public Direction ghostStartDirection(int ghostID) {
		return switch (ghostID) {
		case GameModel.RED_GHOST -> Direction.LEFT;
		case GameModel.PINK_GHOST -> Direction.DOWN;
		case GameModel.CYAN_GHOST -> Direction.UP;
		case GameModel.ORANGE_GHOST -> Direction.UP;
		default -> throw new IllegalArgumentException();
		};
	}

	@Override
	public V2i ghostScatterTile(int ghostID) {
		return switch (ghostID) {
		case GameModel.RED_GHOST -> v(25, 0);
		case GameModel.PINK_GHOST -> v(2, 0);
		case GameModel.CYAN_GHOST -> v(27, 34);
		case GameModel.ORANGE_GHOST -> v(0, 34);
		default -> throw new IllegalArgumentException();
		};
	}

	@Override
	public V2i bonusTile() {
		return bonusTile;
	}

	@Override
	public int pelletsToEatForBonus(int bonusIndex) {
		return switch (bonusIndex) {
		case 0 -> pelletsToEatForBonus[0];
		case 1 -> pelletsToEatForBonus[1];
		default -> throw new IllegalArgumentException();
		};
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
	public Collection<V2i> energizerTiles() {
		return energizerTiles;
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
		tiles().forEach(tile -> {
			if (map(tile) == PELLET_EATEN) {
				map[tile.y][tile.x] = PELLET;
			} else if (map(tile) == ENERGIZER_EATEN) {
				map[tile.y][tile.x] = ENERGIZER;
			}
		});
		totalFoodCount = (int) tiles().filter(this::isFoodTile).count();
		foodRemaining = totalFoodCount;
		long energizerCount = tiles().filter(this::isEnergizerTile).count();
		log("Food restored (%d pellets, %d energizers)", totalFoodCount - energizerCount, energizerCount);
	}
}