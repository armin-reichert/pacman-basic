package de.amr.games.pacman.controller;

import static de.amr.games.pacman.lib.Logging.log;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.function.BiConsumer;
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

		public StateListener(PacManGameState state, Consumer<PacManGameState> consumer, double seconds) {
			this.state = state;
			handler = consumer;
			this.ticks = Math.round(seconds * 60);
		}
	}

	private static class StateChangeListener {

		final BiConsumer<PacManGameState, PacManGameState> handler;

		public StateChangeListener(BiConsumer<PacManGameState, PacManGameState> handler) {
			this.handler = handler;
		}
	}

	public boolean logging = true;
	public PacManGameState previousState;
	public PacManGameState state;

	private final List<StateListener> tickListeners = new ArrayList<>();
	private final List<StateChangeListener> changeListeners = new ArrayList<>();

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
		if (state != null) {
			if (logging) {
				log("Exit game state %s", state);
			}
			if (state.onExit != null) {
				state.onExit.run();
			}
		}
		previousState = state;
		state = newState;
		if (logging) {
			log("Enter game state %s", state);
		}
		if (state.onEnter != null) {
			state.onEnter.run();
		}
		fireStateChange();
		state.timer.start();
		return newState;
	}

	private void fireStateChange() {
		// TODO find solution instead of workaround to avoid ConcurrentModificationException
		List<StateChangeListener> changeListenersBefore = new ArrayList<>(changeListeners);
		changeListenersBefore.stream().forEach(listener -> listener.handler.accept(previousState, state));
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