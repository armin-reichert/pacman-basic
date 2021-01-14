package de.amr.games.pacman.world;

import static de.amr.games.pacman.lib.Direction.DOWN;
import static de.amr.games.pacman.lib.Direction.LEFT;
import static de.amr.games.pacman.lib.Direction.UP;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.stream.Stream;

import de.amr.games.pacman.core.PacManGameLevel;
import de.amr.games.pacman.lib.Direction;
import de.amr.games.pacman.lib.V2i;

/**
 * Ms. Pac-Man game world. Has 6 maze variants.
 * 
 * TODO: lots of details still missing
 * 
 * @author Armin Reichert
 */
public class MsPacManWorld extends AbstractPacManGameWorld {

	public static final byte CHERRIES = 0, STRAWBERRY = 1, ORANGE = 2, PRETZEL = 3, APPLE = 4, PEAR = 5, BANANA = 6;

	/*@formatter:off*/
	// TODO make levels confom to Ms.Pac-Man game
	public static final PacManGameLevel[] LEVELS = {
	/* 1*/ new PacManGameLevel(CHERRIES,   100,  80, 75, 40,  20,  80, 10,  85,  90, 50, 6, 5),
	/* 2*/ new PacManGameLevel(STRAWBERRY, 200,  90, 85, 45,  30,  90, 15,  95,  95, 55, 5, 5),
	/* 3*/ new PacManGameLevel(ORANGE,      500,  90, 85, 45,  40,  90, 20,  95,  95, 55, 4, 5),
	/* 4*/ new PacManGameLevel(PRETZEL,    700,  90, 85, 45,  40,  90, 20,  95,  95, 55, 3, 5),
	/* 5*/ new PacManGameLevel(APPLE,     1000, 100, 95, 50,  40, 100, 20, 105, 100, 60, 2, 5),
	/* 6*/ new PacManGameLevel(PEAR,      2000, 100, 95, 50,  50, 100, 25, 105, 100, 60, 5, 5),
	/* 7*/ new PacManGameLevel(BANANA,    5000, 100, 95, 50,  50, 100, 25, 105, 100, 60, 2, 5),
//	/* 8*/ new PacManGameLevel(APPLE,     1000, 100, 95, 50,  50, 100, 25, 105, 100, 60, 2, 5),
//	/* 9*/ new PacManGameLevel(PEAR,      2000, 100, 95, 50,  60, 100, 30, 105, 100, 60, 1, 3),
//	/*10*/ new PacManGameLevel(PEAR,      2000, 100, 95, 50,  60, 100, 30, 105, 100, 60, 5, 5),
//	/*11*/ new PacManGameLevel(BANANA,    3000, 100, 95, 50,  60, 100, 30, 105, 100, 60, 2, 5),
//	/*12*/ new PacManGameLevel(BANANA,    3000, 100, 95, 50,  80, 100, 40, 105, 100, 60, 1, 3),
//	/*13*/ new PacManGameLevel(BANANA,    5000, 100, 95, 50,  80, 100, 40, 105, 100, 60, 1, 3),
//	/*14*/ new PacManGameLevel(BANANA,    5000, 100, 95, 50,  80, 100, 40, 105, 100, 60, 3, 5),
//	/*15*/ new PacManGameLevel(BANANA,    5000, 100, 95, 50, 100, 100, 50, 105, 100, 60, 1, 3),
//	/*16*/ new PacManGameLevel(BANANA,    5000, 100, 95, 50, 100, 100, 50, 105, 100, 60, 1, 3),
//	/*17*/ new PacManGameLevel(BANANA,    5000, 100, 95, 50, 100, 100, 50, 105,   0,  0, 0, 0),
//	/*18*/ new PacManGameLevel(BANANA,    5000, 100, 95, 50, 100, 100, 50, 105, 100, 60, 1, 3),
//	/*19*/ new PacManGameLevel(BANANA,    5000, 100, 95, 50, 120, 100, 60, 105,   0,  0, 0, 0),
//	/*20*/ new PacManGameLevel(BANANA,    5000, 100, 95, 50, 120, 100, 60, 105,   0,  0, 0, 0),
//	/*21*/ new PacManGameLevel(BANANA,    5000,  90, 95, 50, 120, 100, 60, 105,   0,  0, 0, 0)
	};
	/*@formatter:on*/

	private static final V2i HOUSE_ENTRY = new V2i(13, 14);
	private static final V2i HOUSE_CENTER = new V2i(13, 17);
	private static final V2i HOUSE_LEFT = new V2i(11, 17);
	private static final V2i HOUSE_RIGHT = new V2i(15, 17);
	private static final V2i PAC_HOME = new V2i(13, 26);

	private static final String[] GHOST_NAMES = { "Blinky", "Pinky", "Inky", "Sue" };
	private static final V2i[] GHOST_HOME_TILES = { HOUSE_ENTRY, HOUSE_CENTER, HOUSE_LEFT, HOUSE_RIGHT };
	private static final V2i[] GHOST_SCATTER_TILES = { new V2i(25, 0), new V2i(2, 0), new V2i(27, 35), new V2i(27, 35) };
	private static final Direction[] GHOST_START_DIRECTIONS = { LEFT, UP, DOWN, DOWN };

	private final Random rnd = new Random();
	private final Map<Integer, Byte> symbolAtLevel = new HashMap<>();
	private int mapIndex; // 1-6

	public MsPacManWorld() {
	}

	private void selectMap(int mapIndex) {
		if (mapIndex < 1 || mapIndex > 6) {
			throw new IllegalArgumentException("Illegal map index: " + mapIndex);
		}
		this.mapIndex = mapIndex;
		// Map #5 is the same as #3, only different color, same for #6 vs. #4
		int fileIndex = mapIndex == 5 ? 3 : mapIndex == 6 ? 4 : mapIndex;
		map = loadMap("/worlds/mspacman/map" + fileIndex + ".txt");
		findPortals();
		findFoodTiles();
	}

	@Override
	public void setLevel(int levelNumber) {
		if (levelNumber < 1) {
			throw new IllegalArgumentException("Illegal level number: " + levelNumber);
		}
		if (levelNumber <= 2) {
			selectMap(1); // pink maze
		} else if (levelNumber <= 5) {
			selectMap(2); // light blue maze
		} else if (levelNumber <= 9) {
			selectMap(3); // orange maze
		} else if (levelNumber <= 13) {
			selectMap(4); // dark blue maze
		} else {
			int mapIndex = (levelNumber - 14) % 8 < 4 ? 5 : 6; // TODO correct?
			selectMap(mapIndex);
		}
	}

	public int getMapIndex() {
		return mapIndex;
	}

	@Override
	public PacManGameLevel levelData(int levelNumber) {
		if (levelNumber <= 7) {
			return LEVELS[levelNumber - 1];
		}
		byte symbol = -1;
		if (symbolAtLevel.containsKey(levelNumber)) {
			symbol = symbolAtLevel.get(levelNumber);
		} else {
			symbol = (byte) rnd.nextInt(7);
			symbolAtLevel.put(levelNumber, symbol);
		}
		return new PacManGameLevel(LEVELS[6], symbol);
	}

	@Override
	public String pacName() {
		return "Ms. Pac-Man";
	}

	@Override
	public Direction pacStartDirection() {
		return LEFT;
	}

	@Override
	public V2i pacHome() {
		return PAC_HOME;
	}

	@Override
	public String ghostName(int ghost) {
		return GHOST_NAMES[ghost];
	}

	@Override
	public Direction ghostStartDirection(int ghost) {
		return GHOST_START_DIRECTIONS[ghost];
	}

	@Override
	public V2i ghostHome(int ghost) {
		return GHOST_HOME_TILES[ghost];
	}

	@Override
	public V2i ghostScatterTile(int ghost) {
		return GHOST_SCATTER_TILES[ghost];
	}

	@Override
	public V2i houseEntry() {
		return HOUSE_ENTRY;
	}

	@Override
	public V2i houseCenter() {
		return HOUSE_CENTER;
	}

	@Override
	public V2i houseLeft() {
		return HOUSE_LEFT;
	}

	@Override
	public V2i houseRight() {
		return HOUSE_RIGHT;
	}

	private boolean isInsideGhostHouse(int x, int y) {
		return x >= 10 && x <= 17 && y >= 15 && y <= 22;
	}

	@Override
	public boolean isTunnel(int x, int y) {
		return false;
	}

	@Override
	public boolean isUpwardsBlocked(int x, int y) {
		return false; // ghosts can travel all paths
	}

	@Override
	public boolean isIntersection(int x, int y) {
		if (isInsideGhostHouse(x, y) || isGhostHouseDoor(x, y + 1)) {
			return false;
		}
		return Stream.of(Direction.values()).filter(dir -> isAccessible(x + dir.vec.x, y + dir.vec.y)).count() >= 3;
	}
}