package de.amr.games.pacman.lib;

import static de.amr.games.pacman.lib.Logging.log;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.stream.Stream;

/**
 * A finite-state machine, a graph of vertices (states) connected by transitions. Transitions are
 * defined dynamically by the calls of the {@link #changeState(Enum)} method.
 * <p>
 * Each state transition triggers a state change event.
 * 
 * @author Armin Reichert
 * 
 * @param <S> Enumeration type for naming the states
 */
public class FiniteStateMachine<S extends Enum<S>> {

	@SuppressWarnings({ "unchecked" })
	private static <ID extends Enum<ID>> Map<ID, Vertex> createStateMap(Class<ID> identifierType) {
		try {
			return EnumMap.class.getDeclaredConstructor(Class.class).newInstance(identifierType);
		} catch (Exception x) {
			throw new RuntimeException(x);
		}
	}

	private static class Vertex {

		public final TickTimer timer;
		public Runnable onEnter, onUpdate, onExit;

		public Vertex(String name) {
			timer = new TickTimer(name + "-timer");
		}
	}

	public static boolean logging = true;

	public S previousState;
	public S state;

	private final Map<S, Vertex> stateMap;
	private final List<BiConsumer<S, S>> changeListeners = new ArrayList<>();

	public FiniteStateMachine(Class<S> identifierClass, S[] stateIdentifiers) {
		stateMap = createStateMap(identifierClass);
		Stream.of(stateIdentifiers).forEach(id -> stateMap.put(id, new Vertex(id.name())));
	}

	public void configure(S stateName, Runnable onEnter, Runnable onUpdate, Runnable onExit) {
		v(stateName).onEnter = onEnter;
		v(stateName).onUpdate = onUpdate;
		v(stateName).onExit = onExit;
	}

	public void addStateChangeListener(BiConsumer<S, S> listener) {
		changeListeners.add(listener);
	}

	public void removeStateChangeListener(BiConsumer<S, S> listener) {
		changeListeners.remove(listener);
	}

	public S changeState(S newState) {
		// before state machine is initialized, state object is null
		if (state != null) {
			if (logging) {
				log("Exit game state %s", state);
			}
			if (v(state).onExit != null) {
				v(state).onExit.run();
			}
		}
		previousState = state;
		state = newState;
		if (logging) {
			log("Enter game state %s", state);
		}
		// TODO maybe this should be configurable:
//		vertex(state).timer.reset();
//		vertex(state).timer.start();
		if (v(state).onEnter != null) {
			v(state).onEnter.run();
		}
		fireStateChange(previousState, state);
		return newState;
	}

	public TickTimer stateTimer() {
		return v(state).timer;
	}

	private Vertex v(S id) {
		return stateMap.get(id);
	}

	protected void fireStateChange(S oldState, S newState) {
		// copy list to avoid concurrent modification exceptions
		new ArrayList<>(changeListeners).stream().forEach(listener -> listener.accept(oldState, newState));
	}

	public void updateState() {
		try {
			if (v(state).onUpdate != null) {
				v(state).onUpdate.run();
			}
			v(state).timer.tick();
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