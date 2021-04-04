package de.amr.games.pacman.lib;

import static de.amr.games.pacman.lib.Logging.log;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.stream.Stream;

/**
 * Finite-state machine, a graph of vertices (states) connected by transitions.
 * Transitions are not defined explicitly but implicitly defined by calls of the
 * {@link #changeState(Enum)} method.
 * <p>
 * Each transition triggers firing of a state change event.
 * 
 * @author Armin Reichert
 * 
 * @param <S> Type (enum) for identifying the states
 */
public class FiniteStateMachine<S extends Enum<S>> {

	@SuppressWarnings({ "unchecked" })
	private static <STATE_KEY extends Enum<STATE_KEY>> Map<STATE_KEY, Vertex> createStateMap(Class<STATE_KEY> keyClass,
			STATE_KEY[] stateKeys) {
		try {
			EnumMap<STATE_KEY, Vertex> enumMap = EnumMap.class.getDeclaredConstructor(Class.class)
					.newInstance(keyClass);
			return enumMap;
		} catch (InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException
				| NoSuchMethodException | SecurityException x) {
			throw new RuntimeException(x);
		}
	}

	private static class Vertex {

		public final TickTimer timer = new TickTimer();
		public Runnable onEnter, onUpdate, onExit;
	}

	public static boolean logging = true;

	public S previousState;
	public S state;

	private final Map<S, Vertex> stateMap;
	private final List<BiConsumer<S, S>> changeListeners = new ArrayList<>();

	public FiniteStateMachine(Class<S> enumClass, S[] stateIdentifiers) {
		stateMap = createStateMap(enumClass, stateIdentifiers);
		Stream.of(stateIdentifiers).forEach(id -> stateMap.put(id, new Vertex()));
	}

	public void configure(S gameState, Runnable onEnter, Runnable onUpdate, Runnable onExit) {
		vertex(gameState).onEnter = onEnter;
		vertex(gameState).onUpdate = onUpdate;
		vertex(gameState).onExit = onExit;
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
			if (vertex(state).onExit != null) {
				vertex(state).onExit.run();
			}
		}
		previousState = state;
		state = newState;
		if (logging) {
			log("Enter game state %s", state);
		}
		vertex(state).timer.reset();
		vertex(state).timer.start();
		if (vertex(state).onEnter != null) {
			vertex(state).onEnter.run();
		}
		fireStateChange(previousState, state);
		return newState;
	}

	public TickTimer stateTimer() {
		return vertex(state).timer;
	}

	private Vertex vertex(S id) {
		return stateMap.get(id);
	}

	protected void fireStateChange(S oldState, S newState) {
		// create copy to avoid concurrent modification
		new ArrayList<>(changeListeners).stream().forEach(listener -> listener.accept(oldState, newState));
	}

	public void updateState() {
		try {
			if (vertex(state).onUpdate != null) {
				vertex(state).onUpdate.run();
			}
			TickTimer stateTimer = vertex(state).timer;
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