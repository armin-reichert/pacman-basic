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
import static de.amr.games.pacman.lib.Misc.trim;

import java.util.ArrayList;
import java.util.BitSet;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;
import java.util.stream.Stream;

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
public abstract class ArcadeWorld implements World {

	//@formatter:off
	public static final char SPACE         = ' ';
	public static final char WALL          = '#';
	public static final char TUNNEL        = 'T';
	public static final char DOOR_LEFT     = 'L';
	public static final char DOOR_RIGHT    = 'R';
	public static final char PELLET        = '.';
	public static final char ENERGIZER     = '*';
	//@formatter:on

	protected static V2i v(int x, int y) {
		return new V2i(x, y);
	}

	protected String[] map;
	protected List<V2i> energizerTiles;
	protected V2i bonusTile = v(0, 0);
	protected int[] pelletsToEatForBonus = new int[2];
	protected List<Portal> portals = List.of();
	protected List<V2i> upwardsBlockedTiles = List.of();
	protected BitSet intersections = new BitSet();
	protected GhostHouse house;
	protected BitSet eaten = new BitSet();
	protected int totalFoodCount;
	protected int foodRemaining;

	protected ArcadeWorld(String[] map) {
		this.map = map;
		buildGhostHouse();
		buildPortals();
		findIntersections();
		resetFood();
		energizerTiles = tiles().filter(this::isEnergizerTile).collect(Collectors.toUnmodifiableList());
	}

	protected void findIntersections() {
		intersections = new BitSet();
		tiles() //
				.filter(tile -> !house.contains(tile)) //
				.filter(tile -> tile.x > 0 && tile.x < numCols() - 1) //
				.filter(tile -> neighbors(tile).filter(nb -> isWall(nb) || isDoor(nb)).count() <= 1) //
				.map(this::index) //
				.forEach(intersections::set);
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
		house.entry = v(13, 14);
		house.seatLeft = v(11, 17);
		house.seatCenter = v(13, 17);
		house.seatRight = v(15, 17);
		house.doorTiles = trim(tiles().filter(this::isDoor).collect(Collectors.toList()));
	}

	protected char map(V2i tile) {
		return insideWorld(tile) ? map[tile.y].charAt(tile.x) : SPACE;
	}

	protected boolean isDoor(V2i tile) {
		return isLeftDoorWing(tile) || isRightDoorWing(tile);
	}

	protected Stream<V2i> neighbors(V2i tile) {
		return Stream.of(Direction.values()).map(dir -> tile.plus(dir.vec));
	}

	@Override
	public int numCols() {
		return 28;
	}

	@Override
	public int numRows() {
		return 36;
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
	public Collection<Portal> portals() {
		return portals;
	}

	@Override
	public Portal randomPortal() {
		return portals.get(new Random().nextInt(portals.size()));
	}

	@Override
	public boolean isPortal(V2i tile) {
		return portals.stream().anyMatch(portal -> portal.left.equals(tile) || portal.right.equals(tile));
	}

	@Override
	public boolean isOneWayDown(V2i tile) {
		return upwardsBlockedTiles.contains(tile);
	}

	@Override
	public boolean isIntersection(V2i tile) {
		if (insideWorld(tile)) {
			return intersections.get(index(tile));
		}
		return false;
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
	public GhostHouse ghostHouse() {
		return house;
	}

	@Override
	public boolean isLeftDoorWing(V2i tile) {
		return map(tile) == DOOR_LEFT;
	}

	@Override
	public boolean isRightDoorWing(V2i tile) {
		return map(tile) == DOOR_RIGHT;
	}

	@Override
	public boolean isFoodTile(V2i tile) {
		return map(tile) == PELLET || map(tile) == ENERGIZER;
	}

	@Override
	public boolean isEnergizerTile(V2i tile) {
		return map(tile) == ENERGIZER;
	}

	@Override
	public Collection<V2i> energizerTiles() {
		return energizerTiles;
	}

	@Override
	public void removeFood(V2i tile) {
		if (containsFood(tile)) {
			eaten.set(index(tile));
			--foodRemaining;
		}
	}

	@Override
	public boolean containsFood(V2i tile) {
		if (insideWorld(tile)) {
			return isFoodTile(tile) && !isFoodEaten(tile);
		}
		return false;
	}

	@Override
	public boolean isFoodEaten(V2i tile) {
		if (insideWorld(tile)) {
			return eaten.get(index(tile));
		}
		return false;
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
		totalFoodCount = (int) tiles().filter(this::isFoodTile).count();
		foodRemaining = totalFoodCount;
		eaten = new BitSet();
		long energizerCount = tiles().filter(this::isEnergizerTile).count();
		log("Total food: %d (%d pellets, %d energizers)", totalFoodCount, totalFoodCount - energizerCount, energizerCount);
	}
}