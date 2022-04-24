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

import java.util.BitSet;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import de.amr.games.pacman.lib.Direction;
import de.amr.games.pacman.lib.V2i;
import de.amr.games.pacman.model.common.GameModel;

/**
 * @author Armin Reichert
 */
public abstract class SimpleWorld implements World {

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
	protected int[] pelletsToEatForBonus = new int[2];
	protected List<Portal> portals = List.of();
	protected List<V2i> upwardsBlockedTiles = List.of();
	protected BitSet intersections = new BitSet();
	protected GhostHouse house;
	protected BitSet eaten = new BitSet();
	protected int totalFoodCount;
	protected int foodRemaining;

	protected SimpleWorld(String[] map) {
		this.map = map;
		buildGhostHouse();
		computeIntersections();
	}

	protected void computeIntersections() {
		tiles() //
				.filter(tile -> !house.contains(tile)) //
				.filter(tile -> !isDoor(tile.plus(Direction.DOWN.vec))) //
				.filter(tile -> neighbors(tile).filter(Predicate.not(this::isWall)).count() > 2) //
				.map(this::index) //
				.forEach(intersections::set);
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
		if (insideWorld(tile)) {
			return map[tile.y].charAt(tile.x);
		}
		return SPACE;
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
		return V2i.NULL;
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
		return Collections.unmodifiableList(portals);
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
		return intersections.get(index(tile));
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
		return tiles().filter(this::isEnergizerTile).collect(Collectors.toList());
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
		return isFoodTile(tile) && !isFoodEaten(tile);
	}

	@Override
	public boolean isFoodEaten(V2i tile) {
		return eaten.get(index(tile));
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