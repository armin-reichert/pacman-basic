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
 * A finite-state machine, a graph of vertices (states) connected by transitions. Transitions are
 * defined dynamically by the calls of the {@link #changeState(Enum)} method.
 * <p>
 * Each state transition triggers a state change event.
 * 
 * @author Armin Reichert
 * 
 * @param <STATE_ID> Enumeration type for identifying the states
 */
public class FiniteStateMachine<STATE_ID extends Enum<STATE_ID>> {

	@SuppressWarnings({ "unchecked" })
	private static <ID extends Enum<ID>> Map<ID, State> createStateMap(Class<ID> identifierType) {
		try {
			return EnumMap.class.getDeclaredConstructor(Class.class).newInstance(identifierType);
		} catch (Exception x) {
			throw new RuntimeException(x);
		}
	}

	public static class State {
		public final TickTimer timer;
		public Runnable onEnter, onUpdate, onExit;

		public State(String name) {
			timer = new TickTimer(name + "-timer");
		}
	}

	public static boolean logging = true;

	public STATE_ID previousStateID;
	public STATE_ID currentStateID;

	private final Map<STATE_ID, State> states_by_id;
	private final List<BiConsumer<STATE_ID, STATE_ID>> changeListeners = new ArrayList<>();

	public FiniteStateMachine(Class<STATE_ID> identifierClass, STATE_ID[] stateIdentifiers) {
		states_by_id = createStateMap(identifierClass);
		Stream.of(stateIdentifiers).forEach(id -> states_by_id.put(id, new State(id.name())));
	}

	public void configState(STATE_ID stateID, Runnable onEnter, Runnable onUpdate, Runnable onExit) {
		state(stateID).onEnter = onEnter;
		state(stateID).onUpdate = onUpdate;
		state(stateID).onExit = onExit;
	}

	public void addStateChangeListener(BiConsumer<STATE_ID, STATE_ID> listener) {
		changeListeners.add(listener);
	}

	public void removeStateChangeListener(BiConsumer<STATE_ID, STATE_ID> listener) {
		changeListeners.remove(listener);
	}

	public STATE_ID changeState(STATE_ID newStateID) {
		// before state machine is initialized, state object is null
		if (currentStateID != null) {
			if (logging) {
				log("Exit game state %s", currentStateID);
			}
			if (state(currentStateID).onExit != null) {
				state(currentStateID).onExit.run();
			}
		}
		previousStateID = currentStateID;
		currentStateID = newStateID;
		if (logging) {
			log("Enter game state %s", currentStateID);
		}
		if (state(currentStateID).onEnter != null) {
			state(currentStateID).onEnter.run();
		}
		fireStateChange(previousStateID, currentStateID);
		return currentStateID;
	}

	public TickTimer stateTimer() {
		return state(currentStateID).timer;
	}

	protected State state(STATE_ID id) {
		return states_by_id.get(id);
	}

	protected void fireStateChange(STATE_ID oldState, STATE_ID newState) {
		// copy list to avoid concurrent modification exceptions
		new ArrayList<>(changeListeners).stream().forEach(listener -> listener.accept(oldState, newState));
	}

	public void updateState() {
		try {
			if (state(currentStateID).onUpdate != null) {
				state(currentStateID).onUpdate.run();
			}
			state(currentStateID).timer.tick();
		} catch (Exception x) {
			Logging.log("Error updating state %s", currentStateID);
			x.printStackTrace();
		}
	}

	public void resumePreviousState() {
		if (previousStateID == null) {
			throw new IllegalStateException("State machine cannot resume previous state because there is none");
		}
		if (logging) {
			log("Resume game state %s", previousStateID);
		}
		changeState(previousStateID);
	}
}