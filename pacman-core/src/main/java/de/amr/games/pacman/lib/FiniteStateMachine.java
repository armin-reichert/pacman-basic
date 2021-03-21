package de.amr.games.pacman.lib;

import static de.amr.games.pacman.lib.Logging.log;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.Iterator;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.stream.Stream;

/**
 * Finite-state machine.
 * 
 * @author Armin Reichert
 */
public class FiniteStateMachine<S extends Enum<S>> {

	private static class StateListener<S> {

		private final S state;
		private final Consumer<S> handler;
		private final long ticks;

		public StateListener(S state, Consumer<S> handler, double seconds) {
			this.state = state;
			this.handler = handler;
			this.ticks = Math.round(seconds * 60);
		}
	}

	private static class StateChangeListener<S> {

		private final BiConsumer<S, S> handler;

		public StateChangeListener(BiConsumer<S, S> handler) {
			this.handler = handler;
		}
	}

	public boolean logging = true;

	public S previousState;
	public S state;

	private final EnumMap<S, StateRepresentation> stateObjects;
	private final List<StateListener<S>> tickListeners = new ArrayList<>();
	private final List<StateChangeListener<S>> changeListeners = new ArrayList<>();

	public FiniteStateMachine(EnumMap<S, StateRepresentation> stateObjects) {
		this.stateObjects = stateObjects;
	}

	protected void createStates(Stream<S> stateIdentifiers) {
		stateIdentifiers.forEach(id -> stateObjects.put(id, new StateRepresentation(id)));
	}

	public void configure(S gameState, Runnable onEnter, Runnable onUpdate, Runnable onExit) {
		stateObject(gameState).onEnter = onEnter;
		stateObject(gameState).onUpdate = onUpdate;
		stateObject(gameState).onExit = onExit;
	}

	public void addStateTimeListener(S gameState, Consumer<S> handler, double seconds) {
		tickListeners.add(new StateListener<S>(gameState, handler, seconds));
	}

	public void removeStateTimeListener(Consumer<S> handler) {
		removeListener(handler, tickListeners);
	}

	public void addStateChangeListener(BiConsumer<S, S> handler) {
		changeListeners.add(new StateChangeListener<S>(handler));
	}

	public void removeStateChangeListener(BiConsumer<S, S> handler) {
		changeListeners.removeIf(entry -> entry.handler == handler);
	}

	private void removeListener(Consumer<S> handler, List<StateListener<S>> list) {
		Iterator<StateListener<S>> it = list.iterator();
		while (it.hasNext()) {
			StateListener<S> listener = it.next();
			if (listener.handler == handler) {
				it.remove();
				return;
			}
		}
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

	public TickTimer timer() {
		return stateObject(state).timer;
	}

	private StateRepresentation stateObject(S id) {
		return stateObjects.get(id);
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