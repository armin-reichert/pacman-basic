package de.amr.games.pacman;

import static de.amr.games.pacman.GameState.CHANGING_LEVEL;
import static de.amr.games.pacman.GameState.CHASING;
import static de.amr.games.pacman.GameState.GHOST_DYING;
import static de.amr.games.pacman.GameState.PACMAN_DYING;
import static de.amr.games.pacman.GameState.READY;
import static de.amr.games.pacman.GameState.SCATTERING;
import static de.amr.games.pacman.Timing.runFrame;
import static de.amr.games.pacman.Timing.sec;
import static de.amr.games.pacman.Timing.targetFPS;
import static de.amr.games.pacman.World.HOUSE_CENTER;
import static de.amr.games.pacman.World.HOUSE_ENTRY;
import static de.amr.games.pacman.World.HOUSE_LEFT;
import static de.amr.games.pacman.World.HOUSE_RIGHT;
import static de.amr.games.pacman.World.HTS;
import static de.amr.games.pacman.World.LOWER_LEFT_CORNER;
import static de.amr.games.pacman.World.LOWER_RIGHT_CORNER;
import static de.amr.games.pacman.World.PACMAN_HOME;
import static de.amr.games.pacman.World.PORTAL_LEFT;
import static de.amr.games.pacman.World.PORTAL_RIGHT;
import static de.amr.games.pacman.World.UPPER_LEFT_CORNER;
import static de.amr.games.pacman.World.UPPER_RIGHT_CORNER;
import static de.amr.games.pacman.World.WORLD_HEIGHT_TILES;
import static de.amr.games.pacman.World.WORLD_WIDTH_TILES;
import static de.amr.games.pacman.common.Direction.DOWN;
import static de.amr.games.pacman.common.Direction.LEFT;
import static de.amr.games.pacman.common.Direction.RIGHT;
import static de.amr.games.pacman.common.Direction.UP;
import static de.amr.games.pacman.common.Logging.log;
import static de.amr.games.pacman.entities.Creature.offset;
import static de.amr.games.pacman.entities.Creature.tile;
import static java.time.format.DateTimeFormatter.ISO_DATE_TIME;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.Optional;
import java.util.Properties;
import java.util.Random;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import de.amr.games.pacman.common.Direction;
import de.amr.games.pacman.common.V2f;
import de.amr.games.pacman.common.V2i;
import de.amr.games.pacman.entities.Creature;
import de.amr.games.pacman.entities.Ghost;
import de.amr.games.pacman.ui.PacManGameUI;

/**
 * A simple Pac-Man game with faithful behavior.
 * 
 * @author Armin Reichert
 * 
 * @see https://gameinternals.com/understanding-pac-man-ghost-behavior
 * @see https://www.gamasutra.com/view/feature/132330/the_pacman_dossier.php
 */
public class PacManGame implements Runnable {

	private static final float READY_STATE_SECONDS = 4;
	private static final int BLINKY = 0, PINKY = 1, INKY = 2, CLYDE = 3;
	private static final List<Direction> DIRECTION_PRIORITY = List.of(UP, LEFT, DOWN, RIGHT);
	private static final File HISCORE_DIR = new File(System.getProperty("user.home"));
	private static final File HISCORE_FILE = new File(HISCORE_DIR, "pacman-basic-hiscore.xml");

	private static boolean differsAtMost(float value, float target, float tolerance) {
		return Math.abs(value - target) <= tolerance;
	}

	private static final List<GameLevel> LEVELS = List.of(
	/*@formatter:off*/
	new GameLevel("Cherries",   100,  80,  71,  75, 40,  20,  80, 10,  85,  90, 79, 50, 6, 5),
	new GameLevel("Strawberry", 300,  90,  79,  85, 45,  30,  90, 15,  95,  95, 83, 55, 5, 5),
	new GameLevel("Peach",      500,  90,  79,  85, 45,  40,  90, 20,  95,  95, 83, 55, 4, 5),
	new GameLevel("Peach",      500,  90,  79,  85, 50,  40, 100, 20,  95,  95, 83, 55, 3, 5),
	new GameLevel("Apple",      700, 100,  87,  95, 50,  40, 100, 20, 105, 100, 87, 60, 2, 5),
	new GameLevel("Apple",      700, 100,  87,  95, 50,  50, 100, 25, 105, 100, 87, 60, 5, 5),
	new GameLevel("Grapes",    1000, 100,  87,  95, 50,  50, 100, 25, 105, 100, 87, 60, 2, 5),
	new GameLevel("Grapes",    1000, 100,  87,  95, 50,  50, 100, 25, 105, 100, 87, 60, 2, 5),
	new GameLevel("Galaxian",  2000, 100,  87,  95, 50,  60, 100, 30, 105, 100, 87, 60, 1, 3),
	new GameLevel("Galaxian",  2000, 100,  87,  95, 50,  60, 100, 30, 105, 100, 87, 60, 5, 5),
	new GameLevel("Bell",      3000, 100,  87,  95, 50,  60, 100, 30, 105, 100, 87, 60, 2, 5),
	new GameLevel("Bell",      3000, 100,  87,  95, 50,  80, 100, 40, 105, 100, 87, 60, 1, 3),
	new GameLevel("Key",       5000, 100,  87,  95, 50,  80, 100, 40, 105, 100, 87, 60, 1, 3),
	new GameLevel("Key",       5000, 100,  87,  95, 50,  80, 100, 40, 105, 100, 87, 60, 3, 5),
	new GameLevel("Key",       5000, 100,  87,  95, 50, 100, 100, 50, 105, 100, 87, 60, 1, 3),
	new GameLevel("Key",       5000, 100,  87,  95, 50, 100, 100, 50, 105,   0,  0,  0, 1, 3),
	new GameLevel("Key",       5000, 100,  87,  95, 50, 100, 100, 50, 105, 100, 87, 60, 0, 0),
	new GameLevel("Key",       5000, 100,  87,  95, 50, 100, 100, 50, 105,   0,   0, 0, 1, 0),
	new GameLevel("Key",       5000, 100,  87,  95, 50, 120, 100, 60, 105,   0,   0, 0, 0, 0),
	new GameLevel("Key",       5000, 100,  87,  95, 50, 120, 100, 60, 105,   0,   0, 0, 0, 0),
	new GameLevel("Key",       5000,  90,  79,  95, 50, 120, 100, 60, 105,   0,   0, 0, 0, 0)
	//@formatter:on
	);

	private static final long[][] SCATTERING_DURATION = {
		//@formatter:off
		{ sec(7), sec(7), sec(5), sec(5) },
		{ sec(7), sec(7), sec(5), 1      },
		{ sec(5), sec(5), sec(5), 1      },
		//@formatter:on
	};

	private static final long[][] CHASING_DURATION = {
		//@formatter:off
		{ sec(20), sec(20), sec(20),   Long.MAX_VALUE },
		{ sec(20), sec(20), sec(1033), Long.MAX_VALUE },
		{ sec(5),  sec(5),  sec(1037), Long.MAX_VALUE },
		//@formatter:on
	};

	private static int durationRowByLevel(int level) {
		return level == 1 ? 0 : level <= 4 ? 1 : 2;
	}

	public static GameLevel level(int level) {
		return level <= 21 ? LEVELS.get(level - 1) : LEVELS.get(20);
	}

	public GameLevel level() {
		return level(level);
	}

	public final World world = new World();
	public final Creature pacMan;
	public final Ghost[] ghosts = new Ghost[4];

	public GameState state;
	public GameState savedState;
	public int level;
	public int attackWave;
	public int lives;
	public int points;
	public int hiscore;
	public boolean newHiscore;
	public int ghostsKilledUsingEnergizer;
	public int mazeFlashesRemaining;
	public long pacManPowerTimer;
	public long bonusAvailableTimer;
	public long bonusConsumedTimer;

	public PacManGameUI ui;
	public boolean paused;

	public PacManGame() {
		pacMan = new Creature("Pac-Man", PACMAN_HOME);
		ghosts[BLINKY] = new Ghost("Blinky", HOUSE_ENTRY, UPPER_RIGHT_CORNER);
		ghosts[PINKY] = new Ghost("Pinky", HOUSE_CENTER, UPPER_LEFT_CORNER);
		ghosts[INKY] = new Ghost("Inky", HOUSE_LEFT, LOWER_RIGHT_CORNER);
		ghosts[CLYDE] = new Ghost("Clyde", HOUSE_RIGHT, LOWER_LEFT_CORNER);
	}

	public void exit() {
		if (newHiscore) {
			saveHiscore();
		}
	}

	@Override
	public void run() {
		reset();
		enterReadyState();
		while (true) {
			runFrame(() -> {
				readInput();
				if (!paused) {
					update();
				}
				ui.render();
			});
		}
	}

	private void reset() {
		loadHiscore();
		newHiscore = false;
		points = 0;
		lives = 3;
		setLevel(1);
	}

	private void setLevel(int n) {
		level = n;
		world.restoreFood();
		attackWave = 0;
		mazeFlashesRemaining = 0;
		ghostsKilledUsingEnergizer = 0;
		pacManPowerTimer = 0;
		bonusAvailableTimer = 0;
		bonusConsumedTimer = 0;
		for (GameState state : GameState.values()) {
			state.timer = 0;
		}
	}

	private void resetGuys() {
		pacMan.visible = true;
		pacMan.speed = 0;
		pacMan.dir = pacMan.wishDir = RIGHT;
		pacMan.placeAt(pacMan.homeTile.x, pacMan.homeTile.y, HTS, 0);
		pacMan.changedTile = true;
		pacMan.couldMove = true;
		pacMan.forcedOnTrack = true;
		pacMan.forcedTurningBack = false;
		pacMan.dead = false;

		for (Ghost ghost : ghosts) {
			ghost.visible = true;
			ghost.speed = 0;
			ghost.placeAt(ghost.homeTile.x, ghost.homeTile.y, HTS, 0);
			ghost.targetTile = null;
			ghost.changedTile = true;
			ghost.couldMove = true;
			ghost.forcedTurningBack = false;
			ghost.forcedOnTrack = ghost == ghosts[BLINKY];
			ghost.dead = false;
			ghost.frightened = false;
			ghost.enteringHouse = false;
			ghost.leavingHouse = false;
			ghost.bounty = 0;
		}
		ghosts[BLINKY].dir = ghosts[BLINKY].wishDir = LEFT;
		ghosts[PINKY].dir = ghosts[PINKY].wishDir = DOWN;
		ghosts[INKY].dir = ghosts[INKY].wishDir = UP;
		ghosts[CLYDE].dir = ghosts[CLYDE].wishDir = UP;

		bonusAvailableTimer = 0;
		bonusConsumedTimer = 0;
	}

	private void readInput() {
		if (ui.keyPressed("left")) {
			pacMan.wishDir = LEFT;
		} else if (ui.keyPressed("right")) {
			pacMan.wishDir = RIGHT;
		} else if (ui.keyPressed("up")) {
			pacMan.wishDir = UP;
		} else if (ui.keyPressed("down")) {
			pacMan.wishDir = DOWN;
		} else if (ui.keyPressed("d")) {
			ui.setDebugMode(!ui.isDebugMode());
		} else if (ui.keyPressed("e")) {
			eatAllNormalPellets();
		} else if (ui.keyPressed("x")) {
			ghostsKilledUsingEnergizer = 0;
			for (Ghost ghost : ghosts) {
				killGhost(ghost);
			}
			enterGhostDyingState();
		} else if (ui.keyPressed("s")) {
			targetFPS = targetFPS == 60 ? 30 : 60;
		} else if (ui.keyPressed("plus")) {
			setLevel(++level);
			enterReadyState();
		} else if (ui.keyPressed("p")) {
			paused = !paused;
		}
	}

	private void update() {
		switch (state) {
		case READY:
			runReadyState();
			break;
		case CHASING:
			runChasingState();
			break;
		case SCATTERING:
			runScatteringState();
			break;
		case CHANGING_LEVEL:
			runChangingLevelState();
			break;
		case PACMAN_DYING:
			runPacManDyingState();
			break;
		case GHOST_DYING:
			runGhostDyingState();
			break;
		case GAME_OVER:
			runGameOverState();
			break;
		default:
			throw new IllegalStateException("Illegal state: " + state);
		}
	}

	private void runReadyState() {
		if (READY.timer == 0) {
			exitReadyState();
			enterScatteringState();
			return;
		}
		if (READY.timer > sec(READY_STATE_SECONDS - 1.5f)) {
			--READY.timer;
			return;
		}
		letGhostBounce(ghosts[INKY]);
		letGhostBounce(ghosts[PINKY]);
		letGhostBounce(ghosts[CLYDE]);
		--READY.timer;
	}

	public void enterReadyState() {
		state = GameState.READY;
		READY.timer = sec(READY_STATE_SECONDS);
		SCATTERING.timer = 0;
		CHASING.timer = 0;
		attackWave = 0;
		resetGuys();
		ui.yellowMessage("Ready!");
		log("Game entered %s state", state);
	}

	private void exitReadyState() {
		ghosts[INKY].leavingHouse = ghosts[PINKY].leavingHouse = ghosts[CLYDE].leavingHouse = true;
		ghosts[BLINKY].leavingHouse = false;
		ui.clearMessage();
	}

	private void runScatteringState() {
		if (pacMan.dead) {
			enterPacManDyingState();
			return;
		}
		if (world.foodRemaining == 0) {
			enterChangingLevelState();
			return;
		}
		if (SCATTERING.timer == 0) {
			enterChasingState();
			return;
		}
		if (pacManPowerTimer == 0) {
			--SCATTERING.timer;
		}
		updatePacMan();
		for (Ghost ghost : ghosts) {
			updateGhost(ghost);
		}
		updateBonus();
		updateHiscore();
	}

	private void enterScatteringState() {
		state = GameState.SCATTERING;
		SCATTERING.timer = SCATTERING_DURATION[durationRowByLevel(level)][attackWave];
		forceGhostsTurningBack();
		log("Game entered %s state", state);
	}

	private void runChasingState() {
		if (pacMan.dead) {
			enterPacManDyingState();
			return;
		}
		if (world.foodRemaining == 0) {
			enterChangingLevelState();
			return;
		}
		if (CHASING.timer == 0) {
			++attackWave;
			enterScatteringState();
			return;
		}
		if (pacManPowerTimer == 0) {
			--CHASING.timer;
		}
		updatePacMan();
		for (Ghost ghost : ghosts) {
			updateGhost(ghost);
		}
		updateBonus();
		updateHiscore();
	}

	private void enterChasingState() {
		state = GameState.CHASING;
		CHASING.timer = CHASING_DURATION[durationRowByLevel(level)][attackWave];
		forceGhostsTurningBack();
		log("Game entered %s state", state);
	}

	private void runPacManDyingState() {
		if (PACMAN_DYING.timer == 0) {
			exitPacManDyingState();
			if (lives > 0) {
				enterReadyState();
				return;
			} else {
				enterGameOverState();
				return;
			}
		}
		if (PACMAN_DYING.timer == sec(2.5f) + 88) {
			for (Ghost ghost : ghosts) {
				ghost.visible = false;
			}
		}
		PACMAN_DYING.timer--;
	}

	private void enterPacManDyingState() {
		state = GameState.PACMAN_DYING;
		// 11 animation frames, 8 ticks each, 2 seconds before animation, 2 seconds after
		PACMAN_DYING.timer = sec(2) + 88 + sec(2);
		log("Game entered %s state", state);
	}

	private void exitPacManDyingState() {
		for (Ghost ghost : ghosts) {
			ghost.visible = true;
		}
	}

	private void runGhostDyingState() {
		if (GHOST_DYING.timer == 0) {
			exitGhostDyingState();
			return;
		}
		for (Ghost ghost : ghosts) {
			if (ghost.dead && ghost.bounty == 0) {
				updateGhost(ghost);
			}
		}
		--GHOST_DYING.timer;
	}

	private void enterGhostDyingState() {
		savedState = state;
		state = GameState.GHOST_DYING;
		GHOST_DYING.timer = sec(0.5f);
		pacMan.visible = false;
		log("Game entered %s state", state);
	}

	private void exitGhostDyingState() {
		for (Ghost ghost : ghosts) {
			if (ghost.dead && ghost.bounty > 0) {
				ghost.bounty = 0;
			}
		}
		pacMan.visible = true;
		state = savedState;
	}

	private void runChangingLevelState() {
		if (CHANGING_LEVEL.timer == 0) {
			log("Level %d complete, entering level %d", level, level + 1);
			setLevel(++level);
			enterReadyState();
			return;
		}
		if (CHANGING_LEVEL.timer == sec(2 + level().numFlashes)) {
			for (Ghost ghost : ghosts) {
				ghost.visible = false;
			}
		}
		--CHANGING_LEVEL.timer;
	}

	private void enterChangingLevelState() {
		state = GameState.CHANGING_LEVEL;
		CHANGING_LEVEL.timer = sec(4 + level().numFlashes);
		mazeFlashesRemaining = level().numFlashes;
		for (Ghost ghost : ghosts) {
			ghost.frightened = false;
			ghost.dead = false;
		}
		log("Game entered %s state", state);
	}

	private void runGameOverState() {
		if (ui.keyPressed("space")) {
			exitGameOverState();
			enterReadyState();
		}
	}

	private void enterGameOverState() {
		state = GameState.GAME_OVER;
		ui.redMessage("Game Over!");
		if (newHiscore) {
			saveHiscore();
		}
		log("Game entered %s state", state);
	}

	private void exitGameOverState() {
		reset();
		ui.clearMessage();
		log("Left game over state");
	}

	private void updatePacMan() {
		tryMoving(pacMan);

		// Pac-man power expiring?
		if (pacManPowerTimer > 0) {
			pacManPowerTimer--;
			if (pacManPowerTimer == 0) {
				for (Ghost ghost : ghosts) {
					ghost.frightened = false;
				}
			}
		}

		// food found?
		V2i tile = pacMan.tile();
		if (world.isFoodTile(tile.x, tile.y) && !world.hasEatenFood(tile.x, tile.y)) {
			world.eatFood(tile.x, tile.y);
			points += 10;
			// energizer found?
			if (world.isEnergizerTile(tile.x, tile.y)) {
				points += 40;
				pacManPowerTimer = sec(level().ghostFrightenedSeconds);
				if (level().ghostFrightenedSeconds > 0) {
					log("Pac-Man got power for %d seconds", level().ghostFrightenedSeconds);
					for (Ghost ghost : ghosts) {
						ghost.frightened = !ghost.dead;
					}
					forceGhostsTurningBack();
				}
				ghostsKilledUsingEnergizer = 0;
			}
		}

		// bonus found?
		if (bonusAvailableTimer > 0 && world.isBonusTile(tile.x, tile.y)) {
			bonusAvailableTimer = 0;
			bonusConsumedTimer = sec(2);
			points += level().bonusPoints;
			log("Pac-Man found bonus %s of value %d", level().bonusSymbol, level().bonusPoints);
		}
		// ghost at current tile?
		for (Ghost ghost : ghosts) {
			if (!pacMan.tile().equals(ghost.tile())) {
				continue;
			}
			// killing ghost?
			if (ghost.frightened) {
				killGhost(ghost);
				enterGhostDyingState();
				return;
			}
			// getting killed by ghost?
			if (pacManPowerTimer == 0 && !ghost.dead) {
				pacMan.dead = true;
				--lives;
				log("Pac-Man killed by %s at tile %s", ghost.name, ghost.tile());
				return;
			}
		}
	}

	private void killGhost(Ghost ghost) {
		if (ghost.dead) {
			return;
		}
		ghostsKilledUsingEnergizer++;
		ghost.dead = true;
		ghost.frightened = false;
		ghost.targetTile = HOUSE_ENTRY;
		ghost.bounty = (int) Math.pow(2, ghostsKilledUsingEnergizer) * 100;
		log("Ghost %s killed at tile %s, Pac-Man wins %d points", ghost.name, ghost.tile(), ghost.bounty);
	}

	private void updateBonus() {
		// bonus score reached?
		int eaten = World.TOTAL_FOOD_COUNT - world.foodRemaining;
		if (bonusAvailableTimer == 0 && (eaten == 70 || eaten == 170)) {
			bonusAvailableTimer = sec(9 + new Random().nextInt(1));
		}
		// bonus active and not consumed
		if (bonusAvailableTimer > 0) {
			--bonusAvailableTimer;
		}
		// bonus active and consumed
		if (bonusConsumedTimer > 0) {
			--bonusConsumedTimer;
		}
	}

	private void loadHiscore() {
		if (!HISCORE_FILE.exists()) {
			hiscore = 0;
			return;
		}
		try (FileInputStream in = new FileInputStream(HISCORE_FILE)) {
			Properties content = new Properties();
			content.loadFromXML(in);
			hiscore = Integer.parseInt(content.getProperty("points"));
			log("Hiscore file loaded: %s", HISCORE_FILE);
		} catch (Exception x) {
			log("Could not load hiscore file");
			x.printStackTrace(System.err);
		}
	}

	private void saveHiscore() {
		Properties content = new Properties();
		content.setProperty("points", String.valueOf(hiscore));
		content.setProperty("level", String.valueOf(level));
		content.setProperty("date", ZonedDateTime.now().format(ISO_DATE_TIME));
		try (FileOutputStream out = new FileOutputStream(HISCORE_FILE)) {
			content.storeToXML(out, "Pac-Man Hiscore");
			log("Hiscore file saved: %s", HISCORE_FILE);
		} catch (Exception x) {
			log("Could not save hiscore");
			x.printStackTrace(System.err);
		}
	}

	private void updateHiscore() {
		if (points > hiscore) {
			hiscore = points;
			newHiscore = true;
		}
	}

	private void updateGhost(Ghost ghost) {
		if (ghost.enteringHouse) {
			letGhostEnterHouse(ghost);
		} else if (ghost.leavingHouse) {
			letGhostLeaveHouse(ghost);
		} else if (ghost.dead) {
			letGhostReturnHome(ghost);
		} else if (state == GameState.SCATTERING) {
			ghost.targetTile = ghost.scatterTile;
			letGhostHeadForTargetTile(ghost);
		} else if (state == GameState.CHASING) {
			ghost.targetTile = currentChasingTarget(ghost);
			letGhostHeadForTargetTile(ghost);
		}
	}

	private V2i currentChasingTarget(Ghost ghost) {
		V2i pacManTile = pacMan.tile();
		switch (ghost.name) {
		case "Blinky": {
			return pacManTile;
		}
		case "Pinky": {
			V2i pacManTileAhead4 = pacManTile.sum(pacMan.dir.vec.scaled(4));
			if (pacMan.dir == UP) {
				// simulate overflow bug when Pac-Man is looking UP
				pacManTileAhead4 = pacManTileAhead4.sum(LEFT.vec.scaled(4));
			}
			return pacManTileAhead4;
		}
		case "Inky": {
			V2i pacManTileAhead2 = pacManTile.sum(pacMan.dir.vec.scaled(2));
			if (pacMan.dir == UP) {
				// simulate overflow bug when Pac-Man is looking UP
				pacManTileAhead2 = pacManTileAhead2.sum(LEFT.vec.scaled(2));
			}
			return ghosts[BLINKY].tile().scaled(-1).sum(pacManTileAhead2.scaled(2));
		}
		case "Clyde": {
			return ghosts[CLYDE].tile().distance(pacManTile) < 8 ? ghosts[CLYDE].scatterTile : pacManTile;
		}
		default:
			throw new IllegalArgumentException("Unknown ghost name: " + ghost.name);
		}
	}

	private void letGhostHeadForTargetTile(Ghost ghost) {
		newWishDir(ghost).ifPresent(dir -> ghost.wishDir = dir);
		tryMoving(ghost);
	}

	private void letGhostReturnHome(Ghost ghost) {
		if (atGhostHouseDoor(ghost)) {
			ghost.setOffset(HTS, 0);
			ghost.dir = ghost.wishDir = DOWN;
			ghost.forcedOnTrack = false;
			ghost.enteringHouse = true;
			ghost.targetTile = ghost == ghosts[BLINKY] ? HOUSE_CENTER : ghost.homeTile;
			return;
		}
		letGhostHeadForTargetTile(ghost);
	}

	private boolean atGhostHouseDoor(Creature guy) {
		return guy.at(HOUSE_ENTRY) && differsAtMost(guy.offset().x, HTS, 2);
	}

	private void letGhostEnterHouse(Ghost ghost) {
		V2f offset = ghost.offset();
		// target reached?
		if (ghost.at(ghost.targetTile) && offset.y >= 0) {
			ghost.wishDir = ghost.dir.inverse();
			ghost.dead = false;
			ghost.enteringHouse = false;
			ghost.leavingHouse = true;
			return;
		}
		// move sidewards towards target tile?
		if (ghost.at(HOUSE_CENTER) && offset.y >= 0) {
			ghost.wishDir = ghost.targetTile.x < HOUSE_CENTER.x ? LEFT : RIGHT;
		}
		ghost.couldMove = tryMoving(ghost, ghost.wishDir);
	}

	private void letGhostLeaveHouse(Ghost ghost) {
		V2f offset = ghost.offset();
		// house left?
		if (ghost.at(HOUSE_ENTRY) && differsAtMost(offset.y, 0, 1)) {
			ghost.setOffset(HTS, 0);
			ghost.dir = ghost.wishDir = LEFT;
			ghost.forcedOnTrack = true;
			ghost.leavingHouse = false;
			return;
		}
		// center of house reached?
		if (ghost.at(HOUSE_CENTER) && differsAtMost(offset.x, 3, 1)) {
			ghost.setOffset(HTS, 0);
			ghost.wishDir = UP;
			tryMoving(ghost);
			return;
		}
		// keep bouncing until ghost can move towards middle of house
		if (ghost.wishDir == UP || ghost.wishDir == DOWN) {
			if (ghost.at(ghost.homeTile)) {
				ghost.setOffset(HTS, 0);
				ghost.wishDir = ghost.homeTile.x < HOUSE_CENTER.x ? RIGHT : LEFT;
			} else {
				letGhostBounce(ghost);
			}
			return;
		}
		tryMoving(ghost);
	}

	private void setSpeed(Creature guy) {
		if (guy.name.equals("Pac-Man")) {
			guy.speed = level().pacManSpeed;
		} else {
			setGhostSpeed((Ghost) guy);
		}
	}

	private void setGhostSpeed(Ghost ghost) {
		if (ghost.leavingHouse) {
			ghost.speed = 0.5f * level().ghostSpeed;
		} else if (ghost.dead) {
			ghost.speed = 2f * level().ghostSpeed;
		} else if (world.isInsideTunnel(ghost.tile().x, ghost.tile().y)) {
			ghost.speed = level().ghostTunnelSpeed;
		} else if (ghost.frightened) {
			ghost.speed = level().frightenedGhostSpeed;
		} else {
			ghost.speed = level().ghostSpeed;
			if (ghost == ghosts[BLINKY]) {
				maybeSetElroySpeed(ghost);
			}
		}
	}

	private void maybeSetElroySpeed(Ghost blinky) {
		if (world.foodRemaining <= level().elroy2DotsLeft) {
			blinky.speed = level().elroy2Speed;
		} else if (world.foodRemaining <= level().elroy1DotsLeft) {
			blinky.speed = level().elroy1Speed;
		}
	}

	private Optional<Direction> newWishDir(Ghost ghost) {
		if (!ghost.changedTile) {
			return Optional.empty();
		}
		if (ghost.forcedTurningBack) {
			ghost.forcedTurningBack = false;
			return Optional.of(ghost.wishDir.inverse());
		}
		V2i tile = ghost.tile();
		if (world.isPortalTile(tile.x, tile.y)) {
			return Optional.empty();
		}
		if (ghost.frightened && world.isIntersectionTile(tile.x, tile.y)) {
			return Optional.of(randomMoveDir(ghost));
		}
		// use direction to neighbor with minimal distance to target
		double minDist = Double.MAX_VALUE;
		Direction minDistDir = null;
		for (Direction dir : DIRECTION_PRIORITY) {
			if (dir == ghost.dir.inverse()) {
				continue;
			}
			V2i neighbor = tile.sum(dir.vec);
			if (!canAccessTile(ghost, neighbor.x, neighbor.y)) {
				continue;
			}
			if (dir == UP && !ghost.dead && world.isUpwardsBlocked(neighbor.x, neighbor.y)) {
				continue;
			}
			double dist = neighbor.distance(ghost.targetTile);
			if (dist < minDist) {
				minDistDir = dir;
				minDist = dist;
			}
		}
		return Optional.ofNullable(minDistDir);
	}

	private void forceGhostsTurningBack() {
		for (Ghost ghost : ghosts) {
			if (!ghost.dead && !ghost.enteringHouse && !ghost.leavingHouse) {
				ghost.forcedTurningBack = true;
			}
		}
	}

	private void letGhostBounce(Ghost ghost) {
		tryMoving(ghost);
		if (!ghost.couldMove) {
			ghost.wishDir = ghost.wishDir.inverse();
		}
	}

	private void tryMoving(Creature guy) {
		setSpeed(guy);
		if (guy.speed == 0) {
			return;
		}

		// entering portal?
		if (guy.at(PORTAL_RIGHT) && guy.dir == RIGHT) {
			guy.placeAt(PORTAL_LEFT.x, PORTAL_LEFT.y, 0, 0);
			return;
		}
		if (guy.at(PORTAL_LEFT) && guy.dir == LEFT) {
			guy.placeAt(PORTAL_RIGHT.x, PORTAL_RIGHT.y, 0, 0);
			return;
		}

		guy.couldMove = tryMoving(guy, guy.wishDir);
		if (guy.couldMove) {
			guy.dir = guy.wishDir;
		} else {
			guy.couldMove = tryMoving(guy, guy.dir);
		}
	}

	private boolean tryMoving(Creature guy, Direction dir) {
		V2i tile = guy.tile();
		V2f offset = guy.offset();
		float pixelsMoving = guy.speed * 1.25f;

		// turns
		if (guy.forcedOnTrack && canAccessTile(guy, tile.x + dir.vec.x, tile.y + dir.vec.y)) {
			if (dir == LEFT || dir == RIGHT) {
				if (Math.abs(offset.y) > pixelsMoving) {
					return false;
				}
				guy.setOffset(offset.x, 0);
			} else if (dir == UP || dir == DOWN) {
				if (Math.abs(offset.x) > pixelsMoving) {
					return false;
				}
				guy.setOffset(0, offset.y);
			}
		}

		// 100% speed corresponds to 1.25 pixels/tick
		V2f velocity = new V2f(dir.vec).scaled(pixelsMoving);
		V2f newPosition = guy.position.sum(velocity);
		V2i newTile = tile(newPosition);
		V2f newOffset = offset(newPosition);

		if (!canAccessTile(guy, newTile.x, newTile.y)) {
			return false;
		}

		// avoid moving (partially) into inaccessible tile
		if (guy.at(newTile)) {
			if (!canAccessTile(guy, tile.x + dir.vec.x, tile.y + dir.vec.y)) {
				if (dir == RIGHT && newOffset.x > 0 || dir == LEFT && newOffset.x < 0) {
					guy.setOffset(0, offset.y);
					return false;
				}
				if (dir == DOWN && newOffset.y > 0 || dir == UP && newOffset.y < 0) {
					guy.setOffset(offset.x, 0);
					return false;
				}
			}
		}

		guy.placeAt(newTile.x, newTile.y, newOffset.x, newOffset.y);
		guy.changedTile = !guy.at(tile);
		return true;
	}

	private void eatAllNormalPellets() {
		for (int x = 0; x < WORLD_WIDTH_TILES; ++x) {
			for (int y = 0; y < WORLD_HEIGHT_TILES; ++y) {
				if (world.isFoodTile(x, y) && !world.hasEatenFood(x, y) && !world.isEnergizerTile(x, y)) {
					world.eatFood(x, y);
				}
			}
		}
	}

	private boolean canAccessTile(Creature guy, int x, int y) {
		if (!world.inMapRange(x, y)) {
			return world.isPortalTile(x, y);
		}
		if (world.isGhostHouseDoor(x, y)) {
			if (guy instanceof Ghost) {
				Ghost ghost = (Ghost) guy;
				return ghost.enteringHouse || ghost.leavingHouse;
			}
			return false;
		}
		return !world.isWall(x, y);
	}

	private Direction randomMoveDir(Creature guy) {
		//@formatter:off
		List<Direction> dirs = Stream.of(Direction.values())
			.filter(dir -> dir != guy.dir.inverse())
			.filter(dir -> {
				V2i neighbor = guy.tile().sum(dir.vec);
				return world.isAccessibleTile(neighbor.x, neighbor.y);
			})
			.collect(Collectors.toList());
		//@formatter:on
		return dirs.get(new Random().nextInt(dirs.size()));
	}

}