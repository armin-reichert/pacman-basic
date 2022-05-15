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
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import de.amr.games.pacman.lib.Direction;
import de.amr.games.pacman.lib.V2i;
import de.amr.games.pacman.model.common.GameModel;

/**
 * Pac-Man and Ms. Pac-Man world as given in the original Arcade games. Implements all common stuff like ghost house
 * position, ghost and player start positions and direction etc. Concrete subclasses have (almost) only to provide their
 * specific world map.
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

	protected V2i size;
	protected byte[][] map;
	protected GhostHouse house;
	protected BitSet intersections;
	protected List<V2i> energizerTiles;
	protected int[] pelletsToEatForBonus = new int[2];
	protected List<Portal> portals;
	protected List<V2i> upwardsBlockedTiles = List.of();
	protected V2i bonusTile = v(0, 0);
	protected int totalFoodCount;
	protected int foodRemaining;

	protected ArcadeWorld(byte[][] map) {
		this.map = map;
		size = v(map[0].length, map.length);
		buildWorld();
	}

	protected ArcadeWorld(String[] mapText) {
		parseMap(mapText);
		buildWorld();
	}

	private void parseMap(String[] mapText) {
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
		printMap();
	}

	private void buildWorld() {
		house = new GhostHouse(v(10, 15), v(7, 4));
		house.doorLeft = v(13, 15);
		house.doorRight = v(14, 15);
		house.seatLeft = v(11, 17);
		house.seatCenter = v(13, 17);
		house.seatRight = v(15, 17);

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

	protected void printMap() {
		System.out.println(getClass());
		StringBuilder sb = new StringBuilder();
		sb.append("{\n");
		for (int row = 0; row < size.y; ++row) {
			sb.append("{");
			for (int col = 0; col < size.x; ++col) {
				byte b = map[row][col];
				sb.append(b).append(",");
			}
			sb.append("},\n");
		}
		sb.append("}\n");
		System.out.println(sb);
	}

	protected V2i computeMapSize(String[] mapText) {
		if (mapText == null) {
			throw new IllegalArgumentException("Map is missing");
		}
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
		default -> throw new IllegalArgumentException("IIlegal ghost ID: " + ghostID);
		};
	}

	@Override
	public V2i ghostScatterTile(int ghostID) {
		return switch (ghostID) {
		case GameModel.RED_GHOST -> v(25, 0);
		case GameModel.PINK_GHOST -> v(2, 0);
		case GameModel.CYAN_GHOST -> v(27, 34);
		case GameModel.ORANGE_GHOST -> v(0, 34);
		default -> throw new IllegalArgumentException("IIlegal ghost ID: " + ghostID);
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
		default -> throw new IllegalArgumentException("IIlegal bonus index: " + bonusIndex);
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