/*
Copyright (c) 2021-2023 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.games.pacman.controller;

import static de.amr.games.pacman.lib.Globals.checkGameVariant;
import static de.amr.games.pacman.lib.Globals.checkNotNull;

import java.util.ArrayList;
import java.util.Collection;
import java.util.function.Predicate;

import org.tinylog.Logger;

import de.amr.games.pacman.event.GameEvent;
import de.amr.games.pacman.event.GameEventListener;
import de.amr.games.pacman.event.GameStateChangeEvent;
import de.amr.games.pacman.event.SoundEvent;
import de.amr.games.pacman.lib.Fsm;
import de.amr.games.pacman.lib.RuleBasedSteering;
import de.amr.games.pacman.lib.Vector2i;
import de.amr.games.pacman.model.GameModel;
import de.amr.games.pacman.model.GameVariant;

/**
 * Controller (in the sense of MVC) for both (Pac-Man, Ms. Pac-Man) game variants.
 * <p>
 * A finite-state machine with states defined in {@link GameState}. The game data are stored in the model of the
 * selected game, see {@link GameModel}. Scene selection is not controlled by this class but left to the specific user
 * interface implementations.
 * <p>
 * <li>Exact level data for Ms. Pac-Man still not available. Any hints appreciated!
 * <li>Multiple players (1up, 2up) not implemented.</li>
 * </ul>
 * 
 * @author Armin Reichert
 * 
 * @see <a href="https://github.com/armin-reichert">GitHub</a>
 * @see <a href="https://pacman.holenet.info">Jamey Pittman: The Pac-Man Dossier</a>
 * @see <a href= "https://gameinternals.com/understanding-pac-man-ghost-behavior">Chad Birch: Understanding ghost
 *      behavior</a>
 * @see <a href="http://superpacman.com/mspacman/">Ms. Pac-Man</a>
 */
public class GameController extends Fsm<GameState, GameModel> {

	private static GameController it;

	/**
	 * Creates the game controller singleton and sets the current game model to the given game variant.
	 *
	 * @param variant game variant to select
	 */
	public static void create(GameVariant variant) {
		if (it != null) {
			throw new IllegalStateException("Game controller already created");
		}
		checkGameVariant(variant);
		it = new GameController(variant);
		Logger.info("Game controller created, selected game variant: {}", it.game.variant());
	}

	/**
	 * @return the game controller singleton
	 */
	public static GameController it() {
		if (it == null) {
			throw new IllegalStateException("Game Controller cannot be accessed before it has been created");
		}
		return it;
	}

	private GameModel game;
	private Steering autopilot = new RuleBasedSteering();
	private Steering manualPacSteering = Steering.NONE;
	private boolean autoControlled;

	private GameController(GameVariant variant) {
		super(GameState.values());
		GameController.it = this;
		game = new GameModel(variant);
		// map FSM state change events to "game state change" events
		addStateChangeListener((oldState, newState) -> publishGameEvent(new GameStateChangeEvent(game, oldState, newState)));
	}

	@Override
	public GameModel context() {
		return game;
	}

	public GameModel game() {
		return game;
	}

	public boolean isAutoControlled() {
		return autoControlled;
	}

	public void setAutoControlled(boolean autoControlled) {
		this.autoControlled = autoControlled;
	}

	public void toggleAutoControlled() {
		autoControlled = !autoControlled;
	}

	public Steering steering() {
		return autoControlled ? autopilot : manualPacSteering;
	}

	public Steering getManualPacSteering() {
		return manualPacSteering;
	}

	public void setManualPacSteering(Steering steering) {
		checkNotNull(steering);
		this.manualPacSteering = steering;
	}

	// Game commands

	/**
	 * Creates a new game as specified by the given variant and reboots. Keeps immunity and credit.
	 * 
	 * @param variant Pac-Man or Ms. Pac-Man
	 */
	public void selectGameVariant(GameVariant variant) {
		boolean immune = game.isImmune();
		int credit = game.credit();
		game = new GameModel(variant);
		game.setImmune(immune);
		game.setCredit(credit);
		restart(GameState.BOOT);
	}

	/**
	 * Adds credit (simulates insertion of a coin) and switches to the credit scene.
	 */
	public void addCredit() {
		if (!game.isPlaying()) {
			boolean added = game.changeCredit(1);
			if (added) {
				publishSoundEvent(SoundEvent.CREDIT_ADDED);
			}
			if (state() != GameState.CREDIT) {
				changeState(GameState.CREDIT);
			}
		}
	}

	public void startPlaying() {
		if ((state() == GameState.INTRO || state() == GameState.CREDIT) && game.hasCredit()) {
			changeState(GameState.READY);
		}
	}

	public void startCutscenesTest(int cutSceneNumber) {
		if (state() == GameState.INTRO) {
			game.intermissionTestNumber = cutSceneNumber;
			changeState(GameState.INTERMISSION_TEST);
		}
	}

	public void cheatEatAllPellets() {
		if (game.isPlaying() && state() == GameState.HUNTING) {
			game.level().ifPresent(level -> {
				var world = level.world();
				world.tiles().filter(Predicate.not(world::isEnergizerTile)).forEach(world::removeFood);
				publishGameEventOfType(GameEvent.PAC_FINDS_FOOD);
				if (world.uneatenFoodCount() == 0) {
					changeState(GameState.LEVEL_COMPLETE);
				}
			});
		}
	}

	public void cheatKillAllEatableGhosts() {
		if (game.isPlaying() && state() == GameState.HUNTING) {
			game.level().ifPresent(level -> {
				level.killAllHuntingAndFrightenedGhosts();
				changeState(GameState.GHOST_DYING);
			});
		}
	}

	public void cheatEnterNextLevel() {
		if (game.isPlaying() && state() == GameState.HUNTING) {
			game.level().ifPresent(level -> {
				var world = level.world();
				world.tiles().forEach(world::removeFood);
				changeState(GameState.LEVEL_COMPLETE);
			});
		}
	}

	// Events

	private final Collection<GameEventListener> subscribers = new ArrayList<>();
	private boolean soundEventsEnabled = true;

	public static void setSoundEventsEnabled(boolean enabled) {
		it.soundEventsEnabled = enabled;
		Logger.info("Sound events {}", enabled ? "enabled" : "disabled");
	}

	public static void addListener(GameEventListener subscriber) {
		checkNotNull(subscriber);
		it.subscribers.add(subscriber);
	}

	public static void removeListener(GameEventListener subscriber) {
		checkNotNull(subscriber);
		it.subscribers.remove(subscriber);
	}

	public static void publishGameEvent(GameEvent event) {
		checkNotNull(event);
		Logger.trace("Publish game event: {}", event);
		it.subscribers.forEach(subscriber -> subscriber.onGameEvent(event));
	}

	public static void publishGameEvent(byte type, Vector2i tile) {
		checkNotNull(tile);
		publishGameEvent(new GameEvent(it.game, type, tile));
	}

	public static void publishGameEventOfType(byte type) {
		publishGameEvent(new GameEvent(it.game, type, null));
	}

	public static void publishSoundEvent(byte soundEventID) {
		checkNotNull(soundEventID);
		if (it.soundEventsEnabled) {
			publishGameEvent(new SoundEvent(it.game, soundEventID));
		}
	}
}