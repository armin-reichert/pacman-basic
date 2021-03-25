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

	public static boolean logging = true;

	public S previousState;
	public S state;

	private final EnumMap<S, StateRepresentation> stateMap;
	private final List<BiConsumer<S, S>> changeListeners = new ArrayList<>();

	public FiniteStateMachine(EnumMap<S, StateRepresentation> stateMap, S[] stateIdentifiers) {
		this.stateMap = stateMap;
		Stream.of(stateIdentifiers).forEach(id -> stateMap.put(id, new StateRepresentation(id)));
	}

	public void configure(S gameState, Runnable onEnter, Runnable onUpdate, Runnable onExit) {
		stateObject(gameState).onEnter = onEnter;
		stateObject(gameState).onUpdate = onUpdate;
		stateObject(gameState).onExit = onExit;
	}

	public void addStateChangeListener(BiConsumer<S, S> listener) {
		changeListeners.add(listener);
	}

	public void removeStateChangeListener(BiConsumer<S, S> listener) {
		changeListeners.remove(listener);
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
		fireStateChange(previousState, state);
		return newState;
	}

	public TickTimer stateTimer() {
		return stateObject(state).timer;
	}

	private StateRepresentation stateObject(S id) {
		return stateMap.get(id);
	}

	private void fireStateChange(S oldState, S newState) {
		changeListeners.stream().forEach(listener -> listener.accept(oldState, newState));
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