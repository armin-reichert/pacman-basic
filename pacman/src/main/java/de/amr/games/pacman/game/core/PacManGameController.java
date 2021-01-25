package de.amr.games.pacman.game.core;

import static de.amr.games.pacman.game.core.PacManGameState.CHANGING_LEVEL;
import static de.amr.games.pacman.game.core.PacManGameState.GAME_OVER;
import static de.amr.games.pacman.game.core.PacManGameState.GHOST_DYING;
import static de.amr.games.pacman.game.core.PacManGameState.INTRO;
import static de.amr.games.pacman.game.core.PacManGameState.PACMAN_DYING;
import static de.amr.games.pacman.game.core.PacManGameState.READY;
import static de.amr.games.pacman.game.creatures.GhostState.DEAD;
import static de.amr.games.pacman.game.creatures.GhostState.ENTERING_HOUSE;
import static de.amr.games.pacman.game.creatures.GhostState.FRIGHTENED;
import static de.amr.games.pacman.game.creatures.GhostState.HUNTING;
import static de.amr.games.pacman.game.creatures.GhostState.LEAVING_HOUSE;
import static de.amr.games.pacman.game.creatures.GhostState.LOCKED;
import static de.amr.games.pacman.game.heaven.God.clock;
import static de.amr.games.pacman.game.heaven.God.random;
import static de.amr.games.pacman.game.worlds.PacManClassicWorld.BLINKY;
import static de.amr.games.pacman.game.worlds.PacManClassicWorld.CLYDE;
import static de.amr.games.pacman.game.worlds.PacManClassicWorld.INKY;
import static de.amr.games.pacman.game.worlds.PacManClassicWorld.PINKY;
import static de.amr.games.pacman.game.worlds.PacManGameWorld.HTS;
import static de.amr.games.pacman.lib.Direction.LEFT;
import static de.amr.games.pacman.lib.Direction.UP;
import static de.amr.games.pacman.lib.Logging.log;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import de.amr.games.pacman.game.creatures.Ghost;
import de.amr.games.pacman.game.creatures.Pac;
import de.amr.games.pacman.lib.V2i;
import de.amr.games.pacman.ui.api.KeyboardPacController;
import de.amr.games.pacman.ui.api.PacManGameSound;
import de.amr.games.pacman.ui.api.PacManGameUI;

/**
 * Pac-Man and Ms. Pac-Man game with original "AI", levels, timers.
 * <p>
 * Missing:
 * <ul>
 * <li>Pac-Man "cornering"</li>
 * <li>Intermission scenes</li>
 * <li>Multiple players</li>
 * </ul>
 * 
 * @author Armin Reichert
 * 
 * @see <a href="https://pacman.holenet.info">Jamey Pittman: The Pac-Man Dossier</a>
 * @see <a href="https://gameinternals.com/understanding-pac-man-ghost-behavior">Chad Birch:
 *      Understanding ghost behavior</a>
 * @see <a href="http://superpacman.com/mspacman/">Ms. Pac-Man</a>
 */
public class PacManGameController implements Runnable {

	public PacManGameModel game;
	public PacManGameUI ui;

	public Consumer<Pac> pacController;

	// controller state
	public boolean paused;
	public boolean started;
	public PacManGameState state;
	public PacManGameState stateBefore;
	public byte huntingPhase;
	public byte mazeFlashesRemaining;

	public void newPacManClassicGame() {
		game = PacManGameModel.newPacManClassicGame();
		log("Game variant is Pac-Man");
		reset();
		enterIntroState();
		log("State is '%s' for %s", stateDescription(), ticksDescription(state.duration));
	}

	public void newMsPacManGame() {
		game = PacManGameModel.newMsPacManGame();
		log("Game variant is Ms. Pac-Man");
		reset();
		enterIntroState();
		log("State is '%s' for %s", stateDescription(), ticksDescription(state.duration));
	}

	private void reset() {
		started = false;
		game.levelNumber = 0;
		game.score = 0;
		game.lives = 3;
		game.levelSymbols = new ArrayList<>();
		nextLevel();
		game.hiscore.load();
		if (ui != null) {
			ui.clearMessage();
		}
	}

	@Override
	public void run() {
		while (true) {
			clock.tick(this::step);
		}
	}

	private void step() {
		if (!paused) {
			readInput();
			updateState();
			updateSound();
		}
		ui.render();
	}

	public void exit() {
		if (game.hiscore.changed) {
			game.hiscore.save();
			log("Hiscore saved to " + game.hiscore.file);
		}
		log("Game exits.");
	}

	private void readInput() {
		if (ui.keyPressed("a")) {
			if (pacController instanceof Autopilot) {
				pacController = new KeyboardPacController(ui);
				log("Pac-Man autopilot mode is off");
			} else {
				pacController = new Autopilot(game);
				log("Pac-Man autopilot mode is on");
			}
		}
		if (ui.keyPressed("i")) {
			game.pac.immune = !game.pac.immune;
			log("%s is %s", game.pac.name, game.pac.immune ? "immune against ghosts" : "vulnerable by ghosts");
		}
		if (ui.keyPressed("escape")) {
			reset();
			ui.stopAllSounds();
			enterIntroState();
		}
	}

	private void nextLevel() {
		huntingPhase = 0;
		mazeFlashesRemaining = 0;
		game.initLevel(game.levelNumber + 1);
		game.levelSymbols.add(game.level.bonusSymbol);
	}

	private void resetGuys() {
		game.pac.placeAt(game.world.pacHome(), HTS, 0);
		game.pac.dir = game.pac.wishDir = game.world.pacStartDirection();
		game.pac.visible = true;
		game.pac.speed = 0;
		game.pac.targetTile = null; // used in autopilot mode
		game.pac.couldMove = true;
		game.pac.forcedOnTrack = true;
		game.pac.dead = false;
		game.pac.powerTicksLeft = 0;
		game.pac.restingTicksLeft = 0;
		game.pac.starvingTicks = 0;
		game.pac.collapsingTicksLeft = 0;

		for (Ghost ghost : game.ghosts) {
			ghost.placeAt(game.world.ghostHome(ghost.id), HTS, 0);
			ghost.dir = ghost.wishDir = game.world.ghostStartDirection(ghost.id);
			ghost.visible = true;
			ghost.speed = 0;
			ghost.targetTile = null;
			ghost.couldMove = true;
			ghost.forcedDirection = ghost.id == BLINKY;
			ghost.forcedOnTrack = ghost.id == BLINKY;
			ghost.state = LOCKED;
			ghost.bounty = 0;
			// these are only reset when entering level:
//		ghost.dotCounter = 0;
//		ghost.elroyMode = 0;
		}

		game.bonus.visible = false;
		game.bonus.speed = 0;
		game.bonus.changedTile = true;
		game.bonus.couldMove = true;
		game.bonus.forcedOnTrack = true;
		game.bonus.edibleTicksLeft = 0;
		game.bonus.eatenTicksLeft = 0;
	}

	// BEGIN STATE-MACHINE

	private PacManGameState changeState(Runnable onExit, Runnable onEntry) {
		log("Exit state '%s'", stateDescription());
		onExit.run();
		onEntry.run();
		log("Enter state '%s' for %s", stateDescription(), ticksDescription(state.duration));
		return state;
	}

	private void updateState() {
		switch (state) {
		case INTRO:
			runIntroState();
			break;
		case READY:
			runReadyState();
			break;
		case HUNTING:
			runHuntingState();
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

	public String stateDescription() {
		if (state == PacManGameState.HUNTING) {
			String phaseName = inScatteringPhase() ? "Scattering" : "Chasing";
			int phase = huntingPhase / 2;
			return String.format("%s-%s (%d of 4)", state, phaseName, phase + 1);
		}
		return state.name();
	}

	private String ticksDescription(long ticks) {
		return ticks == Long.MAX_VALUE ? "indefinite time" : ticks + " ticks";
	}

	// INTRO

	private void enterIntroState() {
		state = INTRO;
		state.setDuration(Long.MAX_VALUE);
	}

	private PacManGameState runIntroState() {
		if (ui.keyPressed("v")) {
			// toggle game variant
			if (game.variant == PacManGameModel.CLASSIC) {
				newMsPacManGame();
			} else {
				newPacManClassicGame();
			}
			// update UI
			ui.setGameController(this);
			return state;
		}
		if (ui.keyPressed("space")) {
			return changeState(this::exitIntroState, this::enterReadyState);
		}
		return state.run();
	}

	private void exitIntroState() {
	}

	// READY

	private void enterReadyState() {
		state = READY;
		state.setDuration(clock.sec(started ? 3 : 6));
		resetGuys();
		ui.stopAllSounds();
		for (Ghost ghost : game.ghosts) {
			ghost.visible = false;
		}
	}

	private PacManGameState runReadyState() {
		if (state.hasExpired()) {
			started = true;
			return changeState(this::exitReadyState, this::enterHuntingState);
		}
		if (state.running == clock.sec(0.5)) {
			ui.showMessage(ui.getString("READY"), false);
			for (Ghost ghost : game.ghosts) {
				ghost.visible = true;
			}
		}
		if (!started && state.running == clock.sec(1)) {
			ui.playSound(PacManGameSound.GAME_READY);
		}
		return state.run();
	}

	private void exitReadyState() {
		ui.clearMessage();
	}

	// HUNTING

	static final short[][] HUNTING_PHASE_DURATION = {
		//@formatter:off
		{ 7, 20, 7, 20, 5,   20,  5, Short.MAX_VALUE },
		{ 7, 20, 7, 20, 5, 1033, -1, Short.MAX_VALUE },
		{ 5, 20, 5, 20, 5, 1037, -1, Short.MAX_VALUE },
		//@formatter:on
	};

	private long huntingPhaseDuration(int phase) {
		int row = game.levelNumber == 1 ? 0 : game.levelNumber <= 4 ? 1 : 2;
		return huntingTicks(HUNTING_PHASE_DURATION[row][phase]);
	}

	private long huntingTicks(short duration) {
		if (duration == -1) {
			return 1; // -1 means a single tick
		}
		if (duration == Short.MAX_VALUE) {
			return Long.MAX_VALUE;
		}
		return clock.sec(duration);
	}

	private boolean inScatteringPhase() {
		return huntingPhase % 2 == 0;
	}

	private static PacManGameSound siren(int huntingPhase) {
		switch (huntingPhase / 2) {
		case 0:
			return PacManGameSound.GHOST_SIREN_1;
		case 1:
			return PacManGameSound.GHOST_SIREN_2;
		case 2:
			return PacManGameSound.GHOST_SIREN_3;
		case 3:
			return PacManGameSound.GHOST_SIREN_4;
		default:
			throw new IllegalArgumentException("Illegal hunting phase: " + huntingPhase);
		}
	}

	private void startHuntingPhase(int phase) {
		huntingPhase = (byte) phase;
		state.setDuration(huntingPhaseDuration(huntingPhase));
		if (inScatteringPhase()) {
			if (huntingPhase >= 2) {
				ui.stopSound(siren(huntingPhase - 2));
			}
			ui.loopSound(siren(huntingPhase)); // TODO not clear when which siren should play
		}
		log("Hunting phase %d started, state is now %s", phase, stateDescription());
	}

	private void enterHuntingState() {
		state = PacManGameState.HUNTING;
		startHuntingPhase(0);
	}

	private PacManGameState runHuntingState() {

		// Cheats
		if (ui.keyPressed("e")) {
			eatAllNormalPellets();
		}
		if (ui.keyPressed("x")) {
			killAllGhosts();
			return changeState(this::exitHuntingState, this::enterGhostDyingState);
		}
		if (ui.keyPressed("l")) {
			if (game.lives < Byte.MAX_VALUE) {
				game.lives++;
			}
		}
		if (ui.keyPressed("n")) {
			return changeState(this::exitHuntingState, this::enterChangingLevelState);
		}

		// Level completed?
		if (game.world.foodRemaining() == 0) {
			return changeState(this::exitHuntingState, this::enterChangingLevelState);
		}

		// Can Pac kill ghost(s)?
		List<Ghost> prey = Stream.of(game.ghosts).filter(ghost -> ghost.is(FRIGHTENED) && ghost.meets(game.pac))
				.collect(Collectors.toList());
		if (!prey.isEmpty()) {
			prey.forEach(this::killGhost);
			return changeState(this::exitHuntingState, this::enterGhostDyingState);
		}

		// Pac killed by ghost?
		Optional<Ghost> killer = Stream.of(game.ghosts).filter(ghost -> ghost.is(HUNTING) && ghost.meets(game.pac))
				.findAny();
		if (!game.pac.immune && killer.isPresent()) {
			onPacKilled(killer.get());
			return changeState(this::exitHuntingState, this::enterPacManDyingState);
		}

		// Hunting phase complete?
		if (state.hasExpired()) {
			startHuntingPhase(++huntingPhase);
			for (Ghost ghost : game.ghosts) {
				if (ghost.state == HUNTING) {
					ghost.forceTurningBack();
				}
			}
		}

		// Can Pac move?
		pacController.accept(game.pac);
		if (game.pac.restingTicksLeft > 0) {
			game.pac.restingTicksLeft--;
		} else {
			game.pac.speed = game.pac.powerTicksLeft == 0 ? game.level.pacSpeed : game.level.pacSpeedPowered;
			game.pac.tryMoving();
		}

		// Does Pac find food?
		V2i pacLocation = game.pac.tile();
		if (game.world.containsFood(pacLocation)) {
			onPacFoundFood(pacLocation);
		} else {
			game.pac.starvingTicks++;
		}

		// Is Pac losing power?
		if (game.pac.powerTicksLeft > 0) {
			game.pac.powerTicksLeft--;
			if (game.pac.powerTicksLeft == 0) {
				for (Ghost ghost : game.ghosts) {
					if (ghost.is(FRIGHTENED)) {
						ghost.state = HUNTING;
					}
				}
			}
		}

		tryReleasingGhosts();

		for (Ghost ghost : game.ghosts) {
			if (ghost.is(HUNTING)) {
				// In Ms. Pac-Man, Blinky and Pinky move randomly during *first* scatter phase
				if (game.variant == PacManGameModel.MS_PACMAN && (ghost.id == BLINKY || ghost.id == PINKY)
						&& huntingPhase == 0) {
					ghost.targetTile = null; // move randomly
				} else if (inScatteringPhase() && ghost.elroy == 0) {
					// Elroy chases also in scatter phase
					ghost.targetTile = game.world.ghostScatterTile(ghost.id);
				} else {
					setGhostChasingTarget(ghost);
				}
			}
			ghost.update(game.level);
		}

		game.bonus.update();
		if (game.bonus.edibleTicksLeft > 0 && game.pac.meets(game.bonus)) {
			log("Pac-Man found bonus (%d) of value %d", game.bonus.symbol, game.bonus.points);
			game.bonus.eatAndDisplayValue(clock.sec(2));
			score(game.bonus.points);
			ui.playSound(PacManGameSound.PACMAN_EAT_BONUS);
		}

		// hunting state timer is suspended if Pac has power
		if (game.pac.powerTicksLeft == 0) {
			state.run();
		}

		return state;
	}

	private void exitHuntingState() {
	}

	// PACMAN_DYING

	private void enterPacManDyingState() {
		state = PACMAN_DYING;
		state.setDuration(clock.sec(6));
		game.pac.speed = 0;
		for (Ghost ghost : game.ghosts) {
			ghost.state = HUNTING; // TODO just want ghost to be rendered colorful
		}
		game.bonus.edibleTicksLeft = game.bonus.eatenTicksLeft = 0;
		ui.stopAllSounds();
	}

	private PacManGameState runPacManDyingState() {
		if (state.hasExpired()) {
			game.lives -= 1;
			if (game.lives > 0) {
				return changeState(this::exitPacManDyingState, this::enterReadyState);
			} else {
				return changeState(this::exitPacManDyingState, this::enterGameOverState);
			}
		}
		if (state.running == clock.sec(1.5)) {
			for (Ghost ghost : game.ghosts) {
				ghost.visible = false;
			}
		}
		if (state.running == clock.sec(2.5)) {
			game.pac.collapsingTicksLeft = clock.sec(1.5);
			ui.playSound(PacManGameSound.PACMAN_DEATH);
		}
		if (game.pac.collapsingTicksLeft > 1) {
			// count down until 1 such that animation stays at last frame until state expires
			game.pac.collapsingTicksLeft--;
		}
		return state.run();
	}

	private void exitPacManDyingState() {
		game.pac.collapsingTicksLeft = 0;
		for (Ghost ghost : game.ghosts) {
			ghost.visible = true;
		}
	}

	// GHOST_DYING

	private void enterGhostDyingState() {
		stateBefore = state;
		state = GHOST_DYING;
		state.setDuration(clock.sec(1));
		game.pac.visible = false;
		ui.playSound(PacManGameSound.GHOST_EATEN);
	}

	private PacManGameState runGhostDyingState() {
		if (state.hasExpired()) {
			log("Resume state '%s'", stateBefore);
			return changeState(this::exitGhostDyingState, () -> state = stateBefore);
		}
		pacController.accept(game.pac);
		for (Ghost ghost : game.ghosts) {
			if (ghost.is(DEAD) && ghost.bounty == 0 || ghost.is(ENTERING_HOUSE)) {
				ghost.update(game.level);
			}
		}
		return state.run();
	}

	private void exitGhostDyingState() {
		for (Ghost ghost : game.ghosts) {
			if (ghost.is(DEAD) && ghost.bounty > 0) {
				ghost.bounty = 0;
			}
		}
		game.pac.visible = true;
		ui.loopSound(PacManGameSound.GHOST_EYES);
	}

	// CHANGING_LEVEL

	private void enterChangingLevelState() {
		state = CHANGING_LEVEL;
		state.setDuration(clock.sec(game.level.numFlashes + 3));
		game.bonus.edibleTicksLeft = game.bonus.eatenTicksLeft = 0;
		game.pac.speed = 0;
		ui.stopAllSounds();
	}

	private PacManGameState runChangingLevelState() {
		if (state.hasExpired()) {
			return changeState(this::exitChangingLevelState, this::enterReadyState);
		}
		if (state.running == clock.sec(2)) {
			for (Ghost ghost : game.ghosts) {
				ghost.visible = false;
			}
		}
		if (state.running == clock.sec(3)) {
			mazeFlashesRemaining = game.level.numFlashes;
		}
		return state.run();
	}

	private void exitChangingLevelState() {
		log("Level %d complete, entering level %d", game.levelNumber, game.levelNumber + 1);
		nextLevel();
	}

	// GAME_OVER

	private void enterGameOverState() {
		state = GAME_OVER;
		state.setDuration(clock.sec(30));
		for (Ghost ghost : game.ghosts) {
			ghost.speed = 0;
		}
		game.pac.speed = 0;
		if (game.hiscore.changed) {
			game.hiscore.save();
			log("Hiscore saved to " + game.hiscore.file);
		}
		ui.showMessage(ui.getString("GAME_OVER"), true);
	}

	private PacManGameState runGameOverState() {
		if (state.hasExpired() || ui.keyPressed("space")) {
			return changeState(this::exitGameOverState, this::enterIntroState);
		}
		return state.run();
	}

	private void exitGameOverState() {
		reset();
	}

	// END STATE-MACHINE

	private void onPacFoundFood(V2i pacLocation) {
		game.world.removeFood(pacLocation);
		game.pac.starvingTicks = 0;
		if (game.world.isEnergizerTile(pacLocation)) {
			game.pac.restingTicksLeft = 3;
			game.ghostBounty = 200;
			letPacFrightenGhosts(game.level.ghostFrightenedSeconds);
			score(50);
		} else {
			game.pac.restingTicksLeft = 1;
			score(10);
		}

		// Bonus gets edible?
		if (game.world.eatenFoodCount() == 70 || game.world.eatenFoodCount() == 170) {
			long ticks = game.variant == PacManGameModel.CLASSIC ? clock.sec(9 + random.nextFloat()) : Long.MAX_VALUE;
			game.bonus.activate(game.level.bonusSymbol, ticks);
		}

		// Blinky becomes Elroy?
		if (game.world.foodRemaining() == game.level.elroy1DotsLeft) {
			game.ghosts[BLINKY].elroy = 1;
			log("Blinky becomes Cruise Elroy 1");
		} else if (game.world.foodRemaining() == game.level.elroy2DotsLeft) {
			game.ghosts[BLINKY].elroy = 2;
			log("Blinky becomes Cruise Elroy 2");
		}
		updateGhostDotCounters();
		ui.playSound(PacManGameSound.PACMAN_MUNCH);
	}

	private void letPacFrightenGhosts(int seconds) {
		game.pac.powerTicksLeft = clock.sec(seconds);
		if (seconds > 0) {
			log("Pac-Man got power for %d seconds", seconds);
			for (Ghost ghost : game.ghosts) {
				if (ghost.is(HUNTING)) {
					ghost.state = FRIGHTENED;
					ghost.wishDir = ghost.dir.opposite();
					ghost.forcedDirection = true;
				}
			}
			ui.loopSound(PacManGameSound.PACMAN_POWER);
		}
	}

	private void onPacKilled(Ghost killer) {
		log("%s killed by %s at tile %s", game.pac.name, killer.name, killer.tile());
		game.pac.dead = true;
		byte elroyMode = game.ghosts[BLINKY].elroy;
		if (elroyMode > 0) {
			game.ghosts[BLINKY].elroy = (byte) -elroyMode; // negative value means "disabled"
			log("Blinky Elroy mode %d disabled", elroyMode);
		}
		game.globalDotCounter = 0;
		game.globalDotCounterEnabled = true;
		log("Global dot counter reset and enabled");
	}

	private int pacStarvingTimeLimit() {
		return game.levelNumber < 5 ? clock.sec(4) : clock.sec(3);
	}

	// Ghosts

	private void killGhost(Ghost ghost) {
		ghost.state = DEAD;
		ghost.targetTile = game.world.houseEntry();
		ghost.bounty = game.ghostBounty;
		score(ghost.bounty);
		if (++game.level.numGhostsKilled == 16) {
			score(12000);
		}
		game.ghostBounty *= 2;
		log("Ghost %s killed at tile %s, Pac-Man wins %d points", ghost.name, ghost.tile(), ghost.bounty);
	}

	private void setGhostChasingTarget(Ghost ghost) {
		switch (ghost.id) {
		case 0: {
			// BLINKY
			ghost.targetTile = game.pac.tile();
			break;
		}
		case 1: {
			// PINKY
			V2i pacAhead4 = game.pac.tile().sum(game.pac.dir.vec.scaled(4));
			if (game.pac.dir == UP) { // simulate overflow bug when Pac-Man is looking UP
				pacAhead4 = pacAhead4.sum(LEFT.vec.scaled(4));
			}
			ghost.targetTile = pacAhead4;
			break;
		}
		case 2: {
			// INKY
			V2i pacAhead2 = game.pac.tile().sum(game.pac.dir.vec.scaled(2));
			if (game.pac.dir == UP) { // simulate overflow bug when Pac-Man is looking UP
				pacAhead2 = pacAhead2.sum(LEFT.vec.scaled(2));
			}
			ghost.targetTile = game.ghosts[BLINKY].tile().scaled(-1).sum(pacAhead2.scaled(2));
			break;
		}
		case 3: {
			// CLYDE, SUE
			ghost.targetTile = game.ghosts[3].tile().euclideanDistance(game.pac.tile()) < 8 ? game.world.ghostScatterTile(3)
					: game.pac.tile();
			break;
		}
		default:
			throw new IllegalArgumentException("Unknown ghost id: " + ghost.id);
		}
	}

	// Ghost house

	private void tryReleasingGhosts() {
		if (game.ghosts[BLINKY].is(LOCKED)) {
			game.ghosts[BLINKY].state = HUNTING;
		}
		preferredLockedGhostInsideHouse().ifPresent(ghost -> {
			if (game.globalDotCounterEnabled && game.globalDotCounter >= ghostGlobalDotLimit(ghost)) {
				releaseGhost(ghost, "Global dot counter (%d) reached limit (%d)", game.globalDotCounter,
						ghostGlobalDotLimit(ghost));
			} else if (!game.globalDotCounterEnabled && ghost.dotCounter >= ghostPrivateDotLimit(ghost)) {
				releaseGhost(ghost, "%s's dot counter (%d) reached limit (%d)", ghost.name, ghost.dotCounter,
						ghostPrivateDotLimit(ghost));
			} else if (game.pac.starvingTicks >= pacStarvingTimeLimit()) {
				releaseGhost(ghost, "%s has been starving for %d ticks", game.pac.name, game.pac.starvingTicks);
				game.pac.starvingTicks = 0;
			}
		});
	}

	private void releaseGhost(Ghost ghost, String reason, Object... args) {
		ghost.state = LEAVING_HOUSE;
		if (ghost.id == CLYDE && game.ghosts[BLINKY].elroy < 0) {
			game.ghosts[BLINKY].elroy = (byte) -game.ghosts[BLINKY].elroy; // resume Elroy mode
			log("Blinky Elroy mode %d resumed", game.ghosts[BLINKY].elroy);
		}
		log("Ghost %s released: %s", ghost.name, String.format(reason, args));
	}

	private Optional<Ghost> preferredLockedGhostInsideHouse() {
		return Stream.of(PINKY, INKY, CLYDE).map(id -> game.ghosts[id]).filter(ghost -> ghost.is(LOCKED)).findFirst();
	}

	private int ghostPrivateDotLimit(Ghost ghost) {
		if (ghost.id == INKY) {
			return game.levelNumber == 1 ? 30 : 0;
		}
		if (ghost.id == CLYDE) {
			return game.levelNumber == 1 ? 60 : game.levelNumber == 2 ? 50 : 0;
		}
		return 0;
	}

	private int ghostGlobalDotLimit(Ghost ghost) {
		return ghost.id == PINKY ? 7 : ghost.id == INKY ? 17 : Integer.MAX_VALUE;
	}

	private void updateGhostDotCounters() {
		if (game.globalDotCounterEnabled) {
			if (game.ghosts[CLYDE].is(LOCKED) && game.globalDotCounter == 32) {
				game.globalDotCounterEnabled = false;
				game.globalDotCounter = 0;
				log("Global dot counter disabled and reset, Clyde was in house when counter reached 32");
			} else {
				++game.globalDotCounter;
			}
		} else {
			preferredLockedGhostInsideHouse().ifPresent(ghost -> ++ghost.dotCounter);
		}
	}

	private void score(int points) {
		int oldscore = game.score;
		game.score += points;
		if (oldscore < 10000 && game.score >= 10000) {
			game.lives++;
			ui.playSound(PacManGameSound.EXTRA_LIFE);
			log("Extra life! Now we have %d lives", game.lives);
		}
		game.hiscore.update(game.score, game.levelNumber);
	}

	private void updateSound() {
		if (Stream.of(game.ghosts).noneMatch(ghost -> ghost.is(DEAD))) {
			ui.stopSound(PacManGameSound.GHOST_EYES);
		}
		if (Stream.of(game.ghosts).noneMatch(ghost -> ghost.is(FRIGHTENED))) {
			ui.stopSound(PacManGameSound.PACMAN_POWER);
		}
	}

	private void eatAllNormalPellets() {
		for (int x = 0; x < game.world.sizeInTiles().x; ++x) {
			for (int y = 0; y < game.world.sizeInTiles().y; ++y) {
				if (game.world.containsFood(x, y) && !game.world.isEnergizerTile(x, y)) {
					game.world.removeFood(x, y);
				}
			}
		}
	}

	private void killAllGhosts() {
		game.ghostBounty = 200;
		for (Ghost ghost : game.ghosts) {
			if (ghost.is(HUNTING) || ghost.is(FRIGHTENED)) {
				killGhost(ghost);
			}
		}
	}

	public static boolean differsAtMost(float value, float target, float tolerance) {
		return Math.abs(value - target) <= tolerance;
	}
}