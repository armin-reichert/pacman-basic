package de.amr.games.pacman.world;

import static de.amr.games.pacman.lib.Direction.DOWN;
import static de.amr.games.pacman.lib.Direction.LEFT;
import static de.amr.games.pacman.lib.Direction.UP;

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

	public static final short[] BONUS_POINTS = { 100, 200, 500, 700, 1000, 2000, 5000 };

	/*@formatter:off*/
	// TODO make levels confom to real Ms.Pac-Man game
	public static final PacManGameLevel[] LEVELS = {
	/* 1*/ new PacManGameLevel(CHERRIES,    80, 75, 40,  20,  80, 10,  85,  90, 50, 6, 5),
	/* 2*/ new PacManGameLevel(STRAWBERRY,  90, 85, 45,  30,  90, 15,  95,  95, 55, 5, 5),
	/* 3*/ new PacManGameLevel(ORANGE,      90, 85, 45,  40,  90, 20,  95,  95, 55, 4, 5),
	/* 4*/ new PacManGameLevel(PRETZEL,     90, 85, 45,  40,  90, 20,  95,  95, 55, 3, 5),
	/* 5*/ new PacManGameLevel(APPLE,      100, 95, 50,  40, 100, 20, 105, 100, 60, 2, 5),
	/* 6*/ new PacManGameLevel(PEAR,       100, 95, 50,  50, 100, 25, 105, 100, 60, 5, 5),
	/* 7*/ new PacManGameLevel(BANANA,     100, 95, 50,  50, 100, 25, 105, 100, 60, 2, 5),
	};

	static final V2i HOUSE_ENTRY  = new V2i(13, 14);
	static final V2i HOUSE_CENTER = new V2i(13, 17);
	static final V2i HOUSE_LEFT   = new V2i(11, 17);
	static final V2i HOUSE_RIGHT  = new V2i(15, 17);
	static final V2i PAC_HOME     = new V2i(13, 26);

	static final String[]    GHOST_NAMES = { "Blinky", "Pinky", "Inky", "Sue" };
	static final V2i[]       GHOST_HOME_TILES = { HOUSE_ENTRY, HOUSE_CENTER, HOUSE_LEFT, HOUSE_RIGHT };
	static final V2i[]       GHOST_SCATTER_TILES = { new V2i(25, 0), new V2i(2, 0), new V2i(27, 35), new V2i(27, 35) };
	static final Direction[] GHOST_START_DIRECTIONS = { LEFT, UP, DOWN, DOWN };

	/*@formatter:on*/

	private final Random rnd = new Random();

	private int mapIndex; // 1-6

	private void selectMap(int mapIndex) {
		if (mapIndex < 1 || mapIndex > 6) {
			throw new IllegalArgumentException("Illegal map index: " + mapIndex);
		}
		this.mapIndex = mapIndex;
		// Map #5 is the same as #3, only different color, same for #6 vs. #4
		int fileIndex = mapIndex == 5 ? 3 : mapIndex == 6 ? 4 : mapIndex;
		loadMap("/worlds/mspacman/map" + fileIndex + ".txt");
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
	public PacManGameLevel createLevel(int levelNumber) {
		int index = levelNumber <= 7 ? levelNumber - 1 : 6;
		PacManGameLevel level = LEVELS[index];
		if (levelNumber > 7) {
			level.bonusSymbol = (byte) rnd.nextInt(7);
		}
		return level;
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