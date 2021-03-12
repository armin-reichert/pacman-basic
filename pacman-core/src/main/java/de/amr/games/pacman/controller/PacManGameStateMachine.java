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

		public PacManGameStateListener(PacManGameState state, Consumer<PacManGameState> consumer) {
			this.state = state;
			handler = consumer;
		}
	}

	public boolean logging = true;
	public PacManGameState previousState;
	public PacManGameState state;

	private final List<PacManGameStateListener> entryListeners = new ArrayList<>();
	private final List<PacManGameStateListener> exitListeners = new ArrayList<>();

	public void addStateEntryListener(PacManGameState subscribedState, Consumer<PacManGameState> subscriber) {
		entryListeners.add(new PacManGameStateListener(subscribedState, subscriber));
	}

	public void removeStateEntryListener(Consumer<PacManGameState> subscriber) {
		Iterator<PacManGameStateListener> it = entryListeners.iterator();
		while (it.hasNext()) {
			PacManGameStateListener listener = it.next();
			if (listener.handler == subscriber) {
				it.remove();
				return;
			}
		}
	}

	public void addStateExitListener(PacManGameState subscribedState, Consumer<PacManGameState> subscriber) {
		exitListeners.add(new PacManGameStateListener(subscribedState, subscriber));
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
		exitListeners.stream().filter(l -> l.state.equals(state)).forEach(listener -> listener.handler.accept(state));
		previousState = state;
		state = newState;
		if (logging) {
			log("Enter game state %s", newState);
		}
		if (newState.onEnter != null) {
			newState.onEnter.run();
		}
		entryListeners.stream().filter(l -> l.state.equals(state)).forEach(listener -> listener.handler.accept(state));
		newState.timer.start();
		return newState;
	}

	public void updateState() {
		try {
			if (state.onUpdate != null) {
				state.onUpdate.run();
			}
			state.timer.tick();
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