package de.amr.games.pacman.controller;

import static de.amr.games.pacman.lib.Logging.log;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.Iterator;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.stream.Stream;

import de.amr.games.pacman.lib.Logging;
import de.amr.games.pacman.lib.TickTimer;

/**
 * Finite-state machine controlling the Pac-Man and Ms. Pac-Man game play.
 * 
 * @author Armin Reichert
 */
public class PacManGameStateMachine {

	private static class StateListener {

		private final PacManGameState state;
		private final Consumer<PacManGameState> handler;
		private final long ticks;

		public StateListener(PacManGameState state, Consumer<PacManGameState> handler, double seconds) {
			this.state = state;
			this.handler = handler;
			this.ticks = Math.round(seconds * 60);
		}
	}

	private static class StateChangeListener {

		private final BiConsumer<PacManGameState, PacManGameState> handler;

		public StateChangeListener(BiConsumer<PacManGameState, PacManGameState> handler) {
			this.handler = handler;
		}
	}

	public boolean logging = true;

	public PacManGameState previousState;
	public PacManGameState state;

	private final EnumMap<PacManGameState, GameState> stateObjects = new EnumMap<>(PacManGameState.class);
	private final List<StateListener> tickListeners = new ArrayList<>();
	private final List<StateChangeListener> changeListeners = new ArrayList<>();

	public PacManGameStateMachine() {
		Stream.of(PacManGameState.values()).forEach(id -> stateObjects.put(id, new GameState(id)));
	}

	public void configure(PacManGameState gameState, Runnable onEnter, Runnable onUpdate, Runnable onExit) {
		stateObject(gameState).onEnter = onEnter;
		stateObject(gameState).onUpdate = onUpdate;
		stateObject(gameState).onExit = onExit;
	}

	public void addStateTimeListener(PacManGameState gameState, Consumer<PacManGameState> handler, double seconds) {
		tickListeners.add(new StateListener(gameState, handler, seconds));
	}

	public void removeStateTimeListener(Consumer<PacManGameState> handler) {
		removeListener(handler, tickListeners);
	}

	public void addStateChangeListener(BiConsumer<PacManGameState, PacManGameState> handler) {
		changeListeners.add(new StateChangeListener(handler));
	}

	public void removeStateChangeListener(BiConsumer<PacManGameState, PacManGameState> handler) {
		changeListeners.removeIf(entry -> entry.handler == handler);
	}

	private void removeListener(Consumer<PacManGameState> handler, List<StateListener> list) {
		Iterator<StateListener> it = list.iterator();
		while (it.hasNext()) {
			StateListener listener = it.next();
			if (listener.handler == handler) {
				it.remove();
				return;
			}
		}
	}

	public PacManGameState changeState(PacManGameState newState) {
		// when not yet initialized, state object is NULL
		if (state != null) {
			if (logging) {
				log("Exit game state %s", state);
			}
			if (stateObject(state).onExit != null) {
				stateObject(state).onExit.run();
			}
		}
		previousState = state;
		state = newState;
		if (logging) {
			log("Enter game state %s", state);
		}
		stateObject(state).timer.reset();
		stateObject(state).timer.start();
		if (stateObject(state).onEnter != null) {
			stateObject(state).onEnter.run();
		}
		fireStateChange();
		return newState;
	}

	public TickTimer timer() {
		return stateObject(state).timer;
	}

	private GameState stateObject(PacManGameState id) {
		return stateObjects.get(id);
	}

	private void fireStateChange() {
		// TODO find solution instead of workaround to avoid ConcurrentModificationException
		List<StateChangeListener> changeListenersBefore = new ArrayList<>(changeListeners);
		changeListenersBefore.stream().forEach(listener -> listener.handler.accept(previousState, state));
	}

	public void updateState() {
		try {
			if (stateObject(state).onUpdate != null) {
				stateObject(state).onUpdate.run();
			}
			if (!stateObject(state).timer.isRunning()) {
				stateObject(state).timer.start();
			}
			stateObject(state).timer.tick();
			tickListeners.stream()//
					.filter(listener -> listener.state == state)//
					.filter(listener -> listener.ticks == stateObject(state).timer.ticked())//
					.forEach(listener -> listener.handler.accept(state));
		} catch (Exception x) {
			Logging.log("Error updating state %s", state);
			x.printStackTrace();
		}
	}

	public void resumePreviousState() {
		if (previousState == null) {
			throw new IllegalStateException("State machine cannot resume previous state because there is none");
		}
		if (logging) {
			log("Resume game state %s", previousState);
		}
		changeState(previousState);
	}
}