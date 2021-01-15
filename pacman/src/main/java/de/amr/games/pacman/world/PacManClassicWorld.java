package de.amr.games.pacman.world;

import static de.amr.games.pacman.lib.Direction.DOWN;
import static de.amr.games.pacman.lib.Direction.LEFT;
import static de.amr.games.pacman.lib.Direction.UP;

import java.util.stream.Stream;

import de.amr.games.pacman.core.PacManGameLevel;
import de.amr.games.pacman.lib.Direction;
import de.amr.games.pacman.lib.V2i;

/**
 * The game world used by the classic Pac-Man game.
 * 
 * @author Armin Reichert
 */
public class PacManClassicWorld extends AbstractPacManGameWorld {

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

	public static final V2i BONUS_TILE    = new V2i(13, 20);
	
	static final V2i HOUSE_ENTRY  = new V2i(13, 14);
	static final V2i HOUSE_CENTER = new V2i(13, 17);
	static final V2i HOUSE_LEFT   = new V2i(11, 17);
	static final V2i HOUSE_RIGHT  = new V2i(15, 17);
	static final V2i PAC_HOME     = new V2i(13, 26);

	static final String[]    GHOST_NAMES = { "Blinky", "Pinky", "Inky", "Clyde" };
	static final V2i[]       GHOST_HOME_TILES = { HOUSE_ENTRY, HOUSE_CENTER, HOUSE_LEFT, HOUSE_RIGHT };
	static final V2i[]       GHOST_SCATTER_TILES = { new V2i(25, 0), new V2i(2, 0), new V2i(27, 35), new V2i(27, 35) };
	static final Direction[] GHOST_START_DIRECTIONS = { LEFT, UP, DOWN, DOWN };

	/*@formatter:on*/

	public PacManClassicWorld() {
		loadMap("/worlds/classic/map.txt");
	}

	@Override
	public PacManGameLevel levelData(int levelNumber) {
		return LEVELS[levelNumber <= 21 ? levelNumber - 1 : 20];
	}

	@Override
	public void setLevel(int levelNumber) {
		restoreFood();
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
		return isTile(x, y, 12, 13) || isTile(x, y, 15, 13) || isTile(x, y, 12, 25) || isTile(x, y, 15, 25);
	}

	@Override
	public boolean isIntersection(int x, int y) {
		if (isInsideGhostHouse(x, y) || isGhostHouseDoor(x, y + 1)) {
			return false;
		}
		return Stream.of(Direction.values()).filter(dir -> isAccessible(x + dir.vec.x, y + dir.vec.y)).count() >= 3;
	}
}