package de.amr.games.pacman;

import static de.amr.games.pacman.Creature.offset;
import static de.amr.games.pacman.Creature.tile;
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
import static de.amr.games.pacman.World.TOTAL_FOOD_COUNT;
import static de.amr.games.pacman.World.UPPER_LEFT_CORNER;
import static de.amr.games.pacman.World.UPPER_RIGHT_CORNER;
import static de.amr.games.pacman.World.WORLD_HEIGHT_TILES;
import static de.amr.games.pacman.World.WORLD_WIDTH_TILES;
import static de.amr.games.pacman.common.Direction.DOWN;
import static de.amr.games.pacman.common.Direction.LEFT;
import static de.amr.games.pacman.common.Direction.RIGHT;
import static de.amr.games.pacman.common.Direction.UP;

import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;
import java.util.Random;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import de.amr.games.pacman.common.Direction;
import de.amr.games.pacman.common.V2f;
import de.amr.games.pacman.common.V2i;

/**
 * A simple Pac-Man game with faithful behavior.
 * 
 * @author Armin Reichert
 */
public class PacManGame implements Runnable {

	private static final int BLINKY = 0, PINKY = 1, INKY = 2, CLYDE = 3;
	private static final List<Direction> DIRECTION_PRIORITY = List.of(UP, LEFT, DOWN, RIGHT);

	private static boolean differsAtMost(float value, float target, float tolerance) {
		return Math.abs(value - target) <= tolerance;
	}

	public static int FPS = 60;

	public static int sec(float seconds) {
		return (int) (seconds * FPS);
	}

	private static final DateTimeFormatter TIME_FORMAT = DateTimeFormatter.ofPattern("HH:mm:ss.SSS");

	public static void log(String msg, Object... args) {
		String timestamp = TIME_FORMAT.format(LocalTime.now());
		System.err.println(String.format("[%s] %s", timestamp, String.format(msg, args)));
	}

	private static final List<LevelData> LEVEL_DATA = List.of(
	/*@formatter:off*/
	LevelData.of("Cherries",   100,  80,  71,  75, 40,  20,  80, 10,  85,  90, 79, 50, 6, 5),
	LevelData.of("Strawberry", 300,  90,  79,  85, 45,  30,  90, 15,  95,  95, 83, 55, 5, 5),
	LevelData.of("Peach",      500,  90,  79,  85, 45,  40,  90, 20,  95,  95, 83, 55, 4, 5),
	LevelData.of("Peach",      500,  90,  79,  85, 50,  40, 100, 20,  95,  95, 83, 55, 3, 5),
	LevelData.of("Apple",      700, 100,  87,  95, 50,  40, 100, 20, 105, 100, 87, 60, 2, 5),
	LevelData.of("Apple",      700, 100,  87,  95, 50,  50, 100, 25, 105, 100, 87, 60, 5, 5),
	LevelData.of("Grapes",    1000, 100,  87,  95, 50,  50, 100, 25, 105, 100, 87, 60, 2, 5),
	LevelData.of("Grapes",    1000, 100,  87,  95, 50,  50, 100, 25, 105, 100, 87, 60, 2, 5),
	LevelData.of("Galaxian",  2000, 100,  87,  95, 50,  60, 100, 30, 105, 100, 87, 60, 1, 3),
	LevelData.of("Galaxian",  2000, 100,  87,  95, 50,  60, 100, 30, 105, 100, 87, 60, 5, 5),
	LevelData.of("Bell",      3000, 100,  87,  95, 50,  60, 100, 30, 105, 100, 87, 60, 2, 5),
	LevelData.of("Bell",      3000, 100,  87,  95, 50,  80, 100, 40, 105, 100, 87, 60, 1, 3),
	LevelData.of("Key",       5000, 100,  87,  95, 50,  80, 100, 40, 105, 100, 87, 60, 1, 3),
	LevelData.of("Key",       5000, 100,  87,  95, 50,  80, 100, 40, 105, 100, 87, 60, 3, 5),
	LevelData.of("Key",       5000, 100,  87,  95, 50, 100, 100, 50, 105, 100, 87, 60, 1, 3),
	LevelData.of("Key",       5000, 100,  87,  95, 50, 100, 100, 50, 105,   0,  0,  0, 1, 3),
	LevelData.of("Key",       5000, 100,  87,  95, 50, 100, 100, 50, 105, 100, 87, 60, 0, 0),
	LevelData.of("Key",       5000, 100,  87,  95, 50, 100, 100, 50, 105,   0,   0, 0, 1, 0),
	LevelData.of("Key",       5000, 100,  87,  95, 50, 120, 100, 60, 105,   0,   0, 0, 0, 0),
	LevelData.of("Key",       5000, 100,  87,  95, 50, 120, 100, 60, 105,   0,   0, 0, 0, 0),
	LevelData.of("Key",       5000,  90,  79,  95, 50, 120, 100, 60, 105,   0,   0, 0, 0, 0)
	//@formatter:on
	);

	public static LevelData levelData(int level) {
		return level <= 21 ? LEVEL_DATA.get(level - 1) : LEVEL_DATA.get(20);
	}

	public LevelData levelData() {
		return levelData(level);
	}

	private static final long[][] SCATTERING_TIMES = {
		//@formatter:off
		{ sec(7), sec(7), sec(5), sec(5) },
		{ sec(7), sec(7), sec(5), 1      },
		{ sec(5), sec(5), sec(5), 1      },
		//@formatter:on
	};

	private static final long[][] CHASING_TIMES = {
		//@formatter:off
		{ sec(20), sec(20), sec(20),   Long.MAX_VALUE },
		{ sec(20), sec(20), sec(1033), Long.MAX_VALUE },
		{ sec(5),  sec(5),  sec(1037), Long.MAX_VALUE },
		//@formatter:on
	};

	private static int timesTableRow(int level) {
		return level == 1 ? 0 : level <= 4 ? 1 : 2;
	}

	public final World world = new World();
	public final Creature pacMan;
	public final Ghost[] ghosts = new Ghost[4];
	public final FrameRateCounter fpsCount = new FrameRateCounter();

	public GameState state;
	public int level;
	public int attackWave;
	public int foodRemaining;
	public int lives;
	public int points;
	public int ghostsKilledUsingEnergizer;
	public int mazeFlashes;
	public long pacManPowerTimer;
	public long readyStateTimer;
	public long scatteringStateTimer;
	public long chasingStateTimer;
	public long changingLevelStateTimer;
	public long pacManDyingStateTimer;
	public long bonusAvailableTimer;
	public long bonusConsumedTimer;

	public PacManGameUI ui;

	public PacManGame() {
		pacMan = new Creature("Pac-Man", PACMAN_HOME);
		ghosts[BLINKY] = new Ghost("Blinky", HOUSE_ENTRY, UPPER_RIGHT_CORNER);
		ghosts[PINKY] = new Ghost("Pinky", HOUSE_CENTER, UPPER_LEFT_CORNER);
		ghosts[INKY] = new Ghost("Inky", HOUSE_LEFT, LOWER_RIGHT_CORNER);
		ghosts[CLYDE] = new Ghost("Clyde", HOUSE_RIGHT, LOWER_LEFT_CORNER);
	}

	@Override
	public void run() {
		reset();
		enterReadyState();
		while (true) {
			fpsCount.beginFrame();
			update();
			ui.render();
			long frameDuration = fpsCount.endFrame();
			adjustSpeed(frameDuration);
		}
	}

	private void adjustSpeed(long frameDuration) {
		long sleepTime = Math.max(1_000_000_000 / FPS - frameDuration, 0);
		if (sleepTime > 0) {
			try {
				Thread.sleep(sleepTime / 1_000_000); // milliseconds
			} catch (InterruptedException x) {
				x.printStackTrace();
			}
		}
	}

	private void reset() {
		points = 0;
		lives = 3;
		setLevel(1);
	}

	private void setLevel(int n) {
		level = n;
		world.restoreFood();
		foodRemaining = TOTAL_FOOD_COUNT;
		attackWave = 0;
		mazeFlashes = 0;
		ghostsKilledUsingEnergizer = 0;
		pacManPowerTimer = 0;
		readyStateTimer = 0;
		scatteringStateTimer = 0;
		chasingStateTimer = 0;
		changingLevelStateTimer = 0;
		pacManDyingStateTimer = 0;
		bonusAvailableTimer = 0;
		bonusConsumedTimer = 0;
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
			ghost.bountyTimer = 0;
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
			eatAllFood();
		} else if (ui.keyPressed("x")) {
			ghostsKilledUsingEnergizer = 0;
			for (Ghost ghost : ghosts) {
				killGhost(ghost);
			}
		} else if (ui.keyPressed("s")) {
			FPS = FPS == 60 ? 30 : 60;
		}
	}

	private void update() {
		readInput();
		switch (state) {
		case READY:
			runReadyState();
			return;
		case CHASING:
			runChasingState();
			return;
		case SCATTERING:
			runScatteringState();
			return;
		case CHANGING_LEVEL:
			runChangingLevelState();
			return;
		case PACMAN_DYING:
			runPacManDyingState();
			return;
		case GAME_OVER:
			runGameOverState();
			return;
		default:
			throw new IllegalStateException("Illegal state: " + state);
		}
	}

	private void runReadyState() {
		if (readyStateTimer == 0) {
			exitReadyState();
			enterScatteringState();
			return;
		}
		letGhostBounce(ghosts[INKY]);
		letGhostBounce(ghosts[PINKY]);
		letGhostBounce(ghosts[CLYDE]);
		--readyStateTimer;
	}

	public void enterReadyState() {
		state = GameState.READY;
		readyStateTimer = sec(3);
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
		if (foodRemaining == 0) {
			enterChangingLevelState();
			return;
		}
		if (scatteringStateTimer == 0) {
			enterChasingState();
			return;
		}
		if (pacManPowerTimer == 0) {
			--scatteringStateTimer;
		}
		updatePacMan();
		updateGhosts();
		updateBonus();
	}

	private void enterScatteringState() {
		state = GameState.SCATTERING;
		scatteringStateTimer = SCATTERING_TIMES[timesTableRow(level)][attackWave];
		forceLivingGhostsTurningBack();
		log("Game entered %s state", state);
	}

	private void runChasingState() {
		if (pacMan.dead) {
			enterPacManDyingState();
			return;
		}
		if (foodRemaining == 0) {
			enterChangingLevelState();
			return;
		}
		if (chasingStateTimer == 0) {
			++attackWave;
			enterScatteringState();
			return;
		}
		if (pacManPowerTimer == 0) {
			--chasingStateTimer;
		}
		updatePacMan();
		updateGhosts();
		updateBonus();
	}

	private void enterChasingState() {
		state = GameState.CHASING;
		chasingStateTimer = CHASING_TIMES[timesTableRow(level)][attackWave];
		forceLivingGhostsTurningBack();
		log("Game entered %s state", state);
	}

	private void runPacManDyingState() {
		if (pacManDyingStateTimer == 0) {
			exitPacManDyingState();
			if (lives > 0) {
				enterReadyState();
				return;
			} else {
				enterGameOverState();
				return;
			}
		}
		if (pacManDyingStateTimer == sec(2.5f) + 88) {
			for (Ghost ghost : ghosts) {
				ghost.visible = false;
			}
		}
		pacManDyingStateTimer--;
	}

	private void enterPacManDyingState() {
		state = GameState.PACMAN_DYING;
		// 11 animation frames, 8 ticks each, 2 seconds before animation, 2 seconds after
		pacManDyingStateTimer = sec(2) + 88 + sec(2);
		log("Game entered %s state", state);
	}

	private void exitPacManDyingState() {
		for (Ghost ghost : ghosts) {
			ghost.visible = true;
		}
	}

	private void runChangingLevelState() {
		if (changingLevelStateTimer == 0) {
			log("Level %d complete, entering level %d", level, level + 1);
			setLevel(++level);
			enterReadyState();
			return;
		}
		--changingLevelStateTimer;
	}

	private void enterChangingLevelState() {
		state = GameState.CHANGING_LEVEL;
		changingLevelStateTimer = sec(7);
		mazeFlashes = levelData().numFlashes();
		for (Ghost ghost : ghosts) {
			ghost.visible = false;
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
			foodRemaining--;
			points += 10;
			// energizer found?
			if (world.isEnergizerTile(tile.x, tile.y)) {
				points += 40;
				pacManPowerTimer = sec(levelData().ghostFrightenedSeconds());
				log("Pac-Man got power for %d seconds", levelData().ghostFrightenedSeconds());
				for (Ghost ghost : ghosts) {
					ghost.frightened = !ghost.dead;
				}
				ghostsKilledUsingEnergizer = 0;
				forceLivingGhostsTurningBack();
			}

			// bonus reached?
			if (bonusAvailableTimer == 0 && (foodRemaining == 70 || foodRemaining == 170)) {
				bonusAvailableTimer = sec(9 + new Random().nextInt(1));
			}
		}

		// bonus found?
		if (bonusAvailableTimer > 0 && world.isBonusTile(tile.x, tile.y)) {
			bonusAvailableTimer = 0;
			bonusConsumedTimer = sec(3);
			points += levelData().bonusPoints();
			log("Pac-Man found bonus %s of value %d", levelData().bonusSymbol(), levelData().bonusPoints());
		}

		// ghost at current tile?
		for (Ghost ghost : ghosts) {
			if (!pacMan.tile().equals(ghost.tile())) {
				continue;
			}
			// killing ghost?
			if (ghost.frightened) {
				killGhost(ghost);
			}
			// getting killed by ghost?
			if (pacManPowerTimer == 0 && !ghost.dead) {
				pacMan.dead = true;
				--lives;
				log("Pac-Man killed by %s at tile %s", ghost.name, ghost.tile());
				break;
			}
		}
	}

	private void killGhost(Ghost ghost) {
		if (ghost.dead) {
			return;
		}
		ghost.dead = true;
		ghost.frightened = false;
		ghost.targetTile = HOUSE_ENTRY;
		ghostsKilledUsingEnergizer++;
		ghost.bounty = (int) Math.pow(2, ghostsKilledUsingEnergizer) * 100;
		ghost.bountyTimer = sec(0.5f);
		log("Ghost %s killed at tile %s, Pac-Man wins %d points", ghost.name, ghost.tile(), ghost.bounty);
	}

	private void updateBonus() {
		if (bonusAvailableTimer > 0) {
			--bonusAvailableTimer;
		}
		if (bonusConsumedTimer > 0) {
			--bonusConsumedTimer;
		}
	}

	private void updateGhosts() {
		for (int i = 0; i < 4; ++i) {
			Ghost ghost = ghosts[i];
			if (ghost.bountyTimer > 0) {
				--ghost.bountyTimer;
			} else if (ghost.enteringHouse) {
				letGhostEnterHouse(ghost);
			} else if (ghost.leavingHouse) {
				letGhostLeaveHouse(ghost);
			} else if (ghost.dead) {
				letGhostReturnHome(ghost);
			} else if (state == GameState.SCATTERING) {
				letGhostScatter(ghost);
			} else if (state == GameState.CHASING) {
				letGhostChase(ghost);
			}
		}
	}

	private void letGhostScatter(Ghost ghost) {
		ghost.targetTile = ghost.scatterTile;
		letGhostHeadForTargetTile(ghost);
	}

	private void letGhostChase(Ghost ghost) {
		ghost.targetTile = currentChasingTarget(ghost);
		letGhostHeadForTargetTile(ghost);
	}

	private V2i currentChasingTarget(Ghost ghost) {
		switch (ghost.name) {
		case "Blinky": {
			return pacMan.tile();
		}
		case "Pinky": {
			V2i fourTilesAheadPacMan = pacMan.tile().sum(pacMan.dir.vec.scaled(4));
			// simulate offset bug when Pac-Man is looking UP
			return pacMan.dir == UP ? fourTilesAheadPacMan.sum(LEFT.vec.scaled(4)) : fourTilesAheadPacMan;
		}
		case "Inky": {
			V2i blinkyTile = ghosts[BLINKY].tile();
			V2i twoTilesAheadPacMan = pacMan.tile().sum(pacMan.dir.vec.scaled(2));
			return twoTilesAheadPacMan.scaled(2).sum(blinkyTile.scaled(-1));
		}
		case "Clyde": {
			return ghosts[CLYDE].tile().distance(pacMan.tile()) < 8 ? ghosts[CLYDE].scatterTile : pacMan.tile();
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
			log("%s starts entering house", ghost);
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
			log("%s reached ghost house target and starts leaving house", ghost);
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
			ghost.wishDir = LEFT;
			ghost.forcedOnTrack = true;
			ghost.leavingHouse = false;
			log("%s has left house", ghost);
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
			guy.speed = levelData().pacManSpeed();
		} else {
			setGhostSpeed((Ghost) guy);
		}
	}

	private void setGhostSpeed(Ghost ghost) {
		if (ghost.bountyTimer > 0) {
			ghost.speed = 0;
		} else if (ghost.enteringHouse) {
			ghost.speed = levelData().ghostSpeed();
		} else if (ghost.leavingHouse) {
			ghost.speed = 0.5f * levelData().ghostSpeed();
		} else if (ghost.dead) {
			ghost.speed = 1f * levelData().ghostSpeed();
		} else if (world.isInsideTunnel(ghost.tile().x, ghost.tile().y)) {
			ghost.speed = levelData().ghostTunnelSpeed();
		} else if (ghost.frightened) {
			ghost.speed = levelData().frightenedGhostSpeed();
		} else {
			ghost.speed = levelData().ghostSpeed();
			if (ghost == ghosts[BLINKY]) {
				maybeSetElroySpeed(ghost);
			}
		}
	}

	private void maybeSetElroySpeed(Ghost blinky) {
		if (foodRemaining <= levelData().elroy2DotsLeft()) {
			blinky.speed = levelData().elroy2Speed();
		} else if (foodRemaining <= levelData().elroy1DotsLeft()) {
			blinky.speed = levelData().elroy1Speed();
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

	private void forceLivingGhostsTurningBack() {
		for (Ghost ghost : ghosts) {
			if (!ghost.dead) {
				ghost.forcedTurningBack = true;
			}
		}
	}

	private void letGhostBounce(Ghost ghost) {
		if (!ghost.couldMove) {
			ghost.wishDir = ghost.wishDir.inverse();
		}
		tryMoving(ghost);
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
		// turns
		if (guy.forcedOnTrack && canAccessTile(guy, tile.x + dir.vec.x, tile.y + dir.vec.y)) {
			if (dir == LEFT || dir == RIGHT) {
				if (Math.abs(offset.y) > 1) {
					return false;
				}
				guy.setOffset(offset.x, 0);
			} else if (dir == UP || dir == DOWN) {
				if (Math.abs(offset.x) > 1) {
					return false;
				}
				guy.setOffset(0, offset.y);
			}
		}

		// 100% speed corresponds to 1.25 pixels/tick
		V2f velocity = new V2f(dir.vec).scaled(1.25f * guy.speed);
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

	private void eatAllFood() {
		for (int x = 0; x < WORLD_WIDTH_TILES; ++x) {
			for (int y = 0; y < WORLD_HEIGHT_TILES; ++y) {
				if (world.isFoodTile(x, y) && !world.hasEatenFood(x, y)) {
					world.eatFood(x, y);
					foodRemaining = 0;
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