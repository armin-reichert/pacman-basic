package de.amr.games.pacman.lib;

import static de.amr.games.pacman.lib.Logging.log;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.stream.Stream;

/**
 * Finite-state machine.
 * 
 * @author Armin Reichert
 */
public class FiniteStateMachine<S extends Enum<S>> {

	private static class StateChangeListener<S> {

		private final BiConsumer<S, S> handler;

		public StateChangeListener(BiConsumer<S, S> handler) {
			this.handler = handler;
		}
	}

	public boolean logging = true;

	public S previousState;
	public S state;

	private final EnumMap<S, StateRepresentation> stateMap;
	private final List<StateChangeListener<S>> changeListeners = new ArrayList<>();

	public FiniteStateMachine(EnumMap<S, StateRepresentation> stateMap, S[] stateIdentifiers) {
		this.stateMap = stateMap;
		Stream.of(stateIdentifiers).forEach(id -> stateMap.put(id, new StateRepresentation(id)));
	}

	public void configure(S gameState, Runnable onEnter, Runnable onUpdate, Runnable onExit) {
		stateObject(gameState).onEnter = onEnter;
		stateObject(gameState).onUpdate = onUpdate;
		stateObject(gameState).onExit = onExit;
	}

	public void addStateChangeListener(BiConsumer<S, S> handler) {
		changeListeners.add(new StateChangeListener<S>(handler));
	}

	public void removeStateChangeListener(BiConsumer<S, S> handler) {
		changeListeners.removeIf(entry -> entry.handler == handler);
	}

	public S changeState(S newState) {
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

	public TickTimer stateTimer() {
		return stateObject(state).timer;
	}

	private StateRepresentation stateObject(S id) {
		return stateMap.get(id);
	}

	private void fireStateChange() {
		// TODO find solution instead of workaround to avoid ConcurrentModificationException
		List<StateChangeListener<S>> changeListenersBefore = new ArrayList<>(changeListeners);
		changeListenersBefore.stream().forEach(listener -> listener.handler.accept(previousState, state));
	}

	public void updateState() {
		try {
			if (stateObject(state).onUpdate != null) {
				stateObject(state).onUpdate.run();
			}
			TickTimer stateTimer = stateObject(state).timer;
			if (!stateTimer.isRunning() && !stateTimer.hasExpired()) { // TODO check this
				stateTimer.start();
			}
			stateTimer.tick();
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