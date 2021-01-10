package de.amr.games.pacman.worlds.classic;

import java.util.ArrayList;
import java.util.BitSet;
import java.util.List;
import java.util.stream.Stream;

import de.amr.games.pacman.core.PacManGameLevel;
import de.amr.games.pacman.lib.Direction;
import de.amr.games.pacman.lib.MapReader;
import de.amr.games.pacman.lib.V2i;
import de.amr.games.pacman.worlds.PacManGameWorld;

/**
 * The game world used by the classic Pac-Man game.
 * 
 * @author Armin Reichert
 */
public class PacManClassicWorld implements PacManGameWorld {

	public static final byte BLINKY = 0, PINKY = 1, INKY = 2, CLYDE = 3;

	public static final byte CHERRIES = 0, STRAWBERRY = 1, PEACH = 2, APPLE = 3, GRAPES = 4, GALAXIAN = 5, BELL = 6,
			KEY = 7;

	/*@formatter:off*/
	public static final PacManGameLevel[] LEVELS = {
	/* 1*/ new PacManGameLevel(CHERRIES,   100,  80, 75, 40,  20,  80, 10,  85,  90, 50, 6, 5),
	/* 2*/ new PacManGameLevel(STRAWBERRY, 300,  90, 85, 45,  30,  90, 15,  95,  95, 55, 5, 5),
	/* 3*/ new PacManGameLevel(PEACH,      500,  90, 85, 45,  40,  90, 20,  95,  95, 55, 4, 5),
	/* 4*/ new PacManGameLevel(PEACH,      500,  90, 85, 45,  40,  90, 20,  95,  95, 55, 3, 5),
	/* 5*/ new PacManGameLevel(APPLE,      700, 100, 95, 50,  40, 100, 20, 105, 100, 60, 2, 5),
	/* 6*/ new PacManGameLevel(APPLE,      700, 100, 95, 50,  50, 100, 25, 105, 100, 60, 5, 5),
	/* 7*/ new PacManGameLevel(GRAPES,    1000, 100, 95, 50,  50, 100, 25, 105, 100, 60, 2, 5),
	/* 8*/ new PacManGameLevel(GRAPES,    1000, 100, 95, 50,  50, 100, 25, 105, 100, 60, 2, 5),
	/* 9*/ new PacManGameLevel(GALAXIAN,  2000, 100, 95, 50,  60, 100, 30, 105, 100, 60, 1, 3),
	/*10*/ new PacManGameLevel(GALAXIAN,  2000, 100, 95, 50,  60, 100, 30, 105, 100, 60, 5, 5),
	/*11*/ new PacManGameLevel(BELL,      3000, 100, 95, 50,  60, 100, 30, 105, 100, 60, 2, 5),
	/*12*/ new PacManGameLevel(BELL,      3000, 100, 95, 50,  80, 100, 40, 105, 100, 60, 1, 3),
	/*13*/ new PacManGameLevel(KEY,       5000, 100, 95, 50,  80, 100, 40, 105, 100, 60, 1, 3),
	/*14*/ new PacManGameLevel(KEY,       5000, 100, 95, 50,  80, 100, 40, 105, 100, 60, 3, 5),
	/*15*/ new PacManGameLevel(KEY,       5000, 100, 95, 50, 100, 100, 50, 105, 100, 60, 1, 3),
	/*16*/ new PacManGameLevel(KEY,       5000, 100, 95, 50, 100, 100, 50, 105, 100, 60, 1, 3),
	/*17*/ new PacManGameLevel(KEY,       5000, 100, 95, 50, 100, 100, 50, 105,   0,  0, 0, 0),
	/*18*/ new PacManGameLevel(KEY,       5000, 100, 95, 50, 100, 100, 50, 105, 100, 60, 1, 3),
	/*19*/ new PacManGameLevel(KEY,       5000, 100, 95, 50, 120, 100, 60, 105,   0,  0, 0, 0),
	/*20*/ new PacManGameLevel(KEY,       5000, 100, 95, 50, 120, 100, 60, 105,   0,  0, 0, 0),
	/*21*/ new PacManGameLevel(KEY,       5000,  90, 95, 50, 120, 100, 60, 105,   0,  0, 0, 0)
	};
	/*@formatter:on*/

	private final byte[][] map;
	private final V2i size = new V2i(28, 36);
	private final List<V2i> portalsLeft = new ArrayList<>(3);
	private final List<V2i> portalsRight = new ArrayList<>(3);
	private final V2i pacManHome = new V2i(13, 26);
	private final V2i scatterTileTopLeft = new V2i(2, 0);
	private final V2i scatterTileTopRight = new V2i(25, 0);
	private final V2i scatterTileBottomLeft = new V2i(0, 35);
	private final V2i scatterTileBottomRight = new V2i(27, 35);
	private final V2i houseEntry = new V2i(13, 14);
	private final V2i houseCenter = new V2i(13, 17);
	private final V2i houseLeft = new V2i(11, 17);
	private final V2i houseRight = new V2i(15, 17);
	private final V2i bonusTile = new V2i(13, 20);
	private final int totalFoodCount;
	private final BitSet eaten = new BitSet();

	private int foodRemaining;

	public PacManClassicWorld() {
		map = MapReader.readMap("/worlds/pacMan_classic/map.txt");
		int food = 0;
		for (int x = 0; x < size.x; ++x) {
			for (int y = 0; y < size.y; ++y) {
				if (map[y][x] == FOOD) {
					++food;
				}
			}
		}
		for (int y = 0; y < size.y; ++y) {
			if (map[y][0] == SPACE && map[y][size.x - 1] == SPACE) {
				portalsLeft.add(new V2i(0, y));
				portalsRight.add(new V2i(size.x - 1, y));
			}
		}
		totalFoodCount = food;
		foodRemaining = totalFoodCount;
	}

	@Override
	public PacManGameLevel level(int levelNumber) {
		return LEVELS[levelNumber <= 21 ? levelNumber - 1 : 20];
	}

	@Override
	public V2i sizeInTiles() {
		return size;
	}

	@Override
	public int numPortals() {
		return portalsLeft.size();
	}

	@Override
	public V2i portalLeft(int i) {
		return portalsLeft.get(i);
	}

	@Override
	public V2i portalRight(int i) {
		return portalsRight.get(i);
	}

	@Override
	public String pacName() {
		return "Pac-Man";
	}

	@Override
	public Direction pacStartDirection() {
		return Direction.RIGHT;
	}

	@Override
	public V2i pacHome() {
		return pacManHome;
	}

	@Override
	public String ghostName(int ghost) {
		switch (ghost) {
		case 0:
			return "Blinky";
		case 1:
			return "Pinky";
		case 2:
			return "Inky";
		case 3:
			return "Clyde";
		default:
			throw new IllegalArgumentException("Illegal ghost ID: " + ghost);
		}
	}

	@Override
	public Direction ghostStartDirection(int ghost) {
		switch (ghost) {
		case 0:
			return Direction.LEFT;
		case 1:
			return Direction.UP;
		case 2:
		case 3:
			return Direction.DOWN;
		default:
			throw new IllegalArgumentException("Illegal ghost ID: " + ghost);
		}
	}

	@Override
	public V2i ghostHome(int ghost) {
		switch (ghost) {
		case 0:
			return houseEntry;
		case 1:
			return houseCenter;
		case 2:
			return houseLeft;
		case 3:
			return houseRight;
		default:
			throw new IllegalArgumentException("Illegal ghost ID: " + ghost);
		}
	}

	@Override
	public V2i ghostScatterTile(int ghost) {
		switch (ghost) {
		case 0:
			return scatterTileTopRight;
		case 1:
			return scatterTileTopLeft;
		case 2:
			return scatterTileBottomRight;
		case 3:
			return scatterTileBottomLeft;
		default:
			throw new IllegalArgumentException("Illegal ghost ID: " + ghost);
		}
	}

	@Override
	public V2i houseEntry() {
		return houseEntry;
	}

	@Override
	public V2i houseCenter() {
		return houseCenter;
	}

	@Override
	public V2i houseLeft() {
		return houseLeft;
	}

	@Override
	public V2i houseRight() {
		return houseRight;
	}

	@Override
	public V2i bonusTile() {
		return bonusTile;
	}

	@Override
	public int totalFoodCount() {
		return totalFoodCount;
	}

	@Override
	public int foodRemaining() {
		return foodRemaining;
	}

	private int tileIndex(int x, int y) {
		return y * size.x + x;
	}

	private boolean isTile(int x, int y, int xx, int yy) {
		return x == xx && y == yy;
	}

	private byte map(int x, int y) {
		return map[y][x];
	}

	@Override
	public boolean isWall(int x, int y) {
		return inMapRange(x, y) && map(x, y) == WALL;
	}

	@Override
	public boolean isGhostHouseDoor(int x, int y) {
		return isTile(x, y, 13, 15) || isTile(x, y, 14, 15);
	}

	public boolean isInsideGhostHouse(int x, int y) {
		return x >= 10 && x <= 17 && y >= 15 && y <= 22;
	}

	@Override
	public boolean isTunnel(int x, int y) {
		return y == 17 && (x <= 5 || x >= 21);
	}

	@Override
	public boolean isUpwardsBlocked(int x, int y) {
		return isTile(x, y, 12, 13) || isTile(x, y, 15, 13) || isTile(x, y, 12, 25) || isTile(x, y, 15, 25);
	}

	@Override
	public boolean isFoodTile(int x, int y) {
		return inMapRange(x, y) && map(x, y) == FOOD;
	}

	@Override
	public boolean isEnergizerTile(int x, int y) {
		return isTile(x, y, 1, 6) || isTile(x, y, 26, 6) || isTile(x, y, 1, 26) || isTile(x, y, 26, 26);
	}

	@Override
	public boolean isIntersection(int x, int y) {
		if (isInsideGhostHouse(x, y) || isGhostHouseDoor(x, y + 1)) {
			return false;
		}
		return Stream.of(Direction.values()).filter(dir -> isAccessible(x + dir.vec.x, y + dir.vec.y)).count() >= 3;
	}

	@Override
	public boolean isAccessible(int x, int y) {
		return !isWall(x, y) || isPortal(x, y);
	}

	@Override
	public boolean isPortal(int x, int y) {
		for (int i = 0; i < numPortals(); ++i) {
			if (isTile(x, y, portalsLeft.get(i).x, portalsLeft.get(i).y)
					|| isTile(x, y, portalsRight.get(i).x, portalsRight.get(i).y)) {
				return true;
			}
		}
		return false;
	}

	@Override
	public boolean foodRemoved(int x, int y) {
		return eaten.get(tileIndex(x, y));
	}

	@Override
	public void removeFood(int x, int y) {
		if (!foodRemoved(x, y)) {
			eaten.set(tileIndex(x, y));
			--foodRemaining;
		}
	}

	@Override
	public void restoreFood() {
		eaten.clear();
		foodRemaining = totalFoodCount;
	}
}