/*
MIT License

Copyright (c) 2021 Armin Reichert

Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files (the "Software"), to deal
in the Software without restriction, including without limitation the rights
to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
copies of the Software, and to permit persons to whom the Software is
furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all
copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
SOFTWARE.
 */
package de.amr.games.pacman.lib;

import static de.amr.games.pacman.lib.Logging.log;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.stream.Stream;

/**
 * A finite-state machine, a graph of vertices (states) connected by transitions. Transitions are defined dynamically by
 * the calls of the {@link #changeState(Enum)} method.
 * <p>
 * Each state transition triggers a state change event.
 * 
 * @author Armin Reichert
 * 
 * @param <STATE_ID> Enumeration type for identifying the states
 */
public class FiniteStateMachine<STATE_ID extends Enum<STATE_ID>> {

	public static final Runnable NOP = () -> {
	};

	public static class State {
		public final TickTimer timer;
		public Runnable onEnter, onUpdate, onExit;

		public State(String name) {
			timer = new TickTimer("Timer:" + name);
		}
	}

	public static boolean logging = true;

	public STATE_ID previousState;
	public STATE_ID state;

	private final Map<STATE_ID, State> statesByID;
	private final List<BiConsumer<STATE_ID, STATE_ID>> stateChangeListeners = new ArrayList<>();

	@SuppressWarnings("unchecked")
	public FiniteStateMachine(STATE_ID[] stateIdentifiers) {
		if (stateIdentifiers.length == 0) {
			throw new IllegalArgumentException("State identifier set must not be empty");
		}
		try {
			Class<?> identifierClass = stateIdentifiers[0].getClass();
			statesByID = EnumMap.class.getDeclaredConstructor(Class.class).newInstance(identifierClass);
			Stream.of(stateIdentifiers).forEach(id -> statesByID.put(id, new State(id.name())));
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	protected String name() {
		return getClass().getSimpleName();
	}

	public void configState(STATE_ID stateID, Runnable onEnter, Runnable onUpdate, Runnable onExit) {
		state(stateID).onEnter = onEnter;
		state(stateID).onUpdate = onUpdate;
		state(stateID).onExit = onExit;
	}

	public void addStateChangeListener(BiConsumer<STATE_ID, STATE_ID> listener) {
		stateChangeListeners.add(listener);
	}

	public void removeStateChangeListener(BiConsumer<STATE_ID, STATE_ID> listener) {
		stateChangeListeners.remove(listener);
	}

	public STATE_ID changeState(STATE_ID newStateID) {
		// before state machine is initialized, state object is null
		if (state != null) {
			if (state(state).onExit != null) {
				state(state).onExit.run();
			}
			if (logging) {
				log("%s: Exited state %s %s", name(), state, state(state).timer);
			}
		}
		previousState = state;
		state = newStateID;
		if (logging) {
			log("%s: Entering state %s %s", name(), state, state(state).timer);
		}
		if (state(state).onEnter != null) {
			state(state).onEnter.run();
		}
		if (logging) {
			log("%s: Entered state %s %s", name(), state, state(state).timer);
		}
		fireStateChange(previousState, state);
		return state;
	}

	public TickTimer stateTimer() {
		return state(state).timer;
	}

	protected State state(STATE_ID id) {
		return statesByID.get(id);
	}

	private void fireStateChange(STATE_ID oldState, STATE_ID newState) {
		stateChangeListeners.stream().forEach(listener -> listener.accept(oldState, newState));
	}

	public void updateState() {
		try {
			if (state(state).onUpdate != null) {
				state(state).onUpdate.run();
			}
			state(state).timer.tick();
		} catch (Exception x) {
			Logging.log("%s: Error updating state %s, timer: %s", name(), state, state(state).timer);
			x.printStackTrace();
		}
	}

	public void resumePreviousState() {
		if (previousState == null) {
			throw new IllegalStateException("State machine cannot resume previous state because there is none");
		}
		if (logging) {
			log("%s: Resume state %s, timer: %s", name(), previousState, state(previousState).timer);
		}
		changeState(previousState);
	}
}