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

	static class PacManGameStateListener {

		final PacManGameState state;
		final Consumer<PacManGameState> handler;
		final long time;

		public PacManGameStateListener(PacManGameState state, Consumer<PacManGameState> consumer) {
			this(state, consumer, 0);
		}

		public PacManGameStateListener(PacManGameState state, Consumer<PacManGameState> consumer, double seconds) {
			this.state = state;
			handler = consumer;
			this.time = Math.round(seconds * 60);
		}
	}

	public boolean logging = true;
	public PacManGameState previousState;
	public PacManGameState state;

	private final List<PacManGameStateListener> entryListeners = new ArrayList<>();
	private final List<PacManGameStateListener> exitListeners = new ArrayList<>();
	private final List<PacManGameStateListener> timeListeners = new ArrayList<>();

	public void addStateEntryListener(PacManGameState subscribedState, Consumer<PacManGameState> handler) {
		entryListeners.add(new PacManGameStateListener(subscribedState, handler));
	}

	public void removeStateEntryListener(Consumer<PacManGameState> handler) {
		Iterator<PacManGameStateListener> it = entryListeners.iterator();
		while (it.hasNext()) {
			PacManGameStateListener listener = it.next();
			if (listener.handler == handler) {
				it.remove();
				return;
			}
		}
	}

	public void addStateTimeListener(PacManGameState subscribedState, Consumer<PacManGameState> handler, double time) {
		timeListeners.add(new PacManGameStateListener(subscribedState, handler, time));
	}

	public void removeStateTimeListener(Consumer<PacManGameState> handler) {
		Iterator<PacManGameStateListener> it = timeListeners.iterator();
		while (it.hasNext()) {
			PacManGameStateListener subscriber = it.next();
			if (subscriber.handler == handler) {
				it.remove();
				return;
			}
		}
	}

	public void addStateExitListener(PacManGameState subscribedState, Consumer<PacManGameState> handler) {
		exitListeners.add(new PacManGameStateListener(subscribedState, handler));
	}

	public void removeStateExitListener(Consumer<PacManGameState> subscriber) {
		Iterator<PacManGameStateListener> it = exitListeners.iterator();
		while (it.hasNext()) {
			PacManGameStateListener listener = it.next();
			if (listener.handler == subscriber) {
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
		state.onEnter.run(); // assuming INTRO state has onEnter action
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
			timeListeners.stream().filter(l -> l.state == state).filter(l -> l.time == state.timer.ticked())
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