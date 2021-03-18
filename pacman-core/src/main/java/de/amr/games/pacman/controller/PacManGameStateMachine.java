package de.amr.games.pacman.controller;

import static de.amr.games.pacman.controller.PacManGameState.INTRO;
import static de.amr.games.pacman.lib.Logging.log;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.function.Consumer;

import de.amr.games.pacman.lib.Logging;

/**
 * Finite-state machine controlling the Pac-Man and Ms. Pac-Man game play.
 * 
 * @author Armin Reichert
 */
public class PacManGameStateMachine {

	private static class StateListener {

		final PacManGameState state;
		final Consumer<PacManGameState> handler;
		final long ticks;

		public StateListener(PacManGameState state, Consumer<PacManGameState> consumer) {
			this(state, consumer, 0);
		}

		public StateListener(PacManGameState state, Consumer<PacManGameState> consumer, double seconds) {
			this.state = state;
			handler = consumer;
			this.ticks = Math.round(seconds * 60);
		}
	}

	public boolean logging = true;
	public PacManGameState previousState;
	public PacManGameState state;

	private final List<StateListener> entryListeners = new ArrayList<>();
	private final List<StateListener> exitListeners = new ArrayList<>();
	private final List<StateListener> tickListeners = new ArrayList<>();

	public void addStateEntryListener(PacManGameState gameState, Consumer<PacManGameState> handler) {
		entryListeners.add(new StateListener(gameState, handler));
	}

	public void removeStateEntryListener(Consumer<PacManGameState> handler) {
		removeListener(handler, entryListeners);
	}

	public void addStateTimeListener(PacManGameState gameState, Consumer<PacManGameState> handler, double seconds) {
		tickListeners.add(new StateListener(gameState, handler, seconds));
	}

	public void removeStateTimeListener(Consumer<PacManGameState> handler) {
		removeListener(handler, tickListeners);
	}

	public void addStateExitListener(PacManGameState gameState, Consumer<PacManGameState> handler) {
		exitListeners.add(new StateListener(gameState, handler));
	}

	public void removeStateExitListener(Consumer<PacManGameState> handler) {
		removeListener(handler, exitListeners);
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

	public void init() {
		state = INTRO;
		previousState = null;
		if (logging) {
			log("Initialize game state machine, enter state %s", state);
		}
		if (state.onEnter != null) {
			state.onEnter.run();
		}
		state.timer.start();
	}

	public PacManGameState changeState(PacManGameState newState) {
		if (state == null) {
			throw new IllegalStateException("State machine not initialized");
		}
		if (logging) {
			log("Exit game state %s", state);
		}
		if (state.onExit != null) {
			state.onExit.run();
		}
		exitListeners.stream().filter(l -> l.state.equals(state)).forEach(l -> l.handler.accept(state));
		previousState = state;
		state = newState;
		if (logging) {
			log("Enter game state %s", newState);
		}
		if (newState.onEnter != null) {
			newState.onEnter.run();
		}
		entryListeners.stream().filter(l -> l.state.equals(state)).forEach(l -> l.handler.accept(state));
		newState.timer.start();
		return newState;
	}

	public void updateState() {
		try {
			if (state.onUpdate != null) {
				state.onUpdate.run();
			}
			state.timer.tick();
			tickListeners.stream().filter(l -> l.state == state).filter(l -> l.ticks == state.timer.ticked())
					.forEach(l -> l.handler.accept(state));
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