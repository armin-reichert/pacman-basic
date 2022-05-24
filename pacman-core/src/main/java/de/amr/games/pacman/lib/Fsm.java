/*
MIT License

Copyright (c) 2021-22 Armin Reichert

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
import java.util.List;
import java.util.function.BiConsumer;

/**
 * A finite-state machine.
 * <p>
 * The states must be provided by an enumeration type that implements the {@link FsmState} interface. The data type
 * passed to the state lifecycle methods is specified by the CONTEXT type parameter.
 * <p>
 * State transitions are defined dynamically via the {@link #changeState} method calls. Each state change triggers an
 * event.
 * 
 * @param <STATE>   Enumeration type providing the states of this FSM
 * @param <CONTEXT> Type of the data provided to the state lifecycle methods {@link FsmState#onEnter},
 *                  {@link FsmState#onUpdate} and {@link FsmState#onExit}
 * 
 * @author Armin Reichert
 */
public abstract class Fsm<STATE extends FsmState<CONTEXT>, CONTEXT> {

	public boolean logging;

	private String name;
	private final STATE[] states;
	private STATE currentState;
	private STATE prevState;
	private final List<BiConsumer<STATE, STATE>> stateChangeListeners = new ArrayList<>();

	public Fsm(STATE[] states) {
		this.states = states;
		for (var state : states) {
			state.setFsm(this);
		}
		name = getClass().getSimpleName();
	}

	@Override
	public String toString() {
		return "FiniteStateMachine[%s: state=%s prev=%s]".formatted(name, currentState, prevState);
	}

	/**
	 * @return the context passed to the state lifecycle methods
	 */
	public abstract CONTEXT getContext();

	/**
	 * @return the current state
	 */
	public STATE state() {
		return currentState;
	}

	/**
	 * @return the previous state (may be null)
	 */
	public STATE prevState() {
		return prevState;
	}

	/**
	 * Adds a state change listener.
	 * 
	 * @param listener a state change listener
	 */
	public synchronized void addStateChangeListener(BiConsumer<STATE, STATE> listener) {
		stateChangeListeners.add(listener);
	}

	/**
	 * Removes a state change listener.
	 * 
	 * @param listener a state change listener
	 */
	public synchronized void removeStateChangeListener(BiConsumer<STATE, STATE> listener) {
		stateChangeListeners.remove(listener);
	}

	/**
	 * Resets the timer of each state to {@link TickTimer#INDEFINITE}.
	 */
	public void resetTimers() {
		for (var state : states) {
			state.timer().setDurationIndefinite();
		}
	}

	/**
	 * Initializes the state machine to the given state. All timers are reset. The initial state's entry hook method is
	 * executed but the current state's exit method isn't.
	 * 
	 * @param initialState the initial state
	 */
	public void reset(STATE initialState) {
		resetTimers();
		currentState = null;
		changeState(initialState);
	}

	/**
	 * Changes the machine's current state to the new state. Tne exit hook method of the current state is executed before
	 * entering the new state. The new state's entry hook method is executed and its timer is reset to
	 * {@link TickTimer#INDEFINITE} (TODO: implement this). After the state change, an event is published.
	 * <p>
	 * Trying to change to the current state (self loop) leads to a runtime exception. TODO: check this
	 * 
	 * @param newState the new state
	 */
	public void changeState(STATE newState) {
		if (newState == currentState) {
			throw new IllegalStateException("FiniteStateMachine: Self loop in state " + currentState);
		}
		if (currentState != null) {
			currentState.onExit(getContext());
			if (logging) {
				log("%s: Exit  state %s %s", name, currentState, currentState.timer());
			}
		}
		prevState = currentState;
		currentState = newState;
		// TODO: reset timer to 0
		currentState.onEnter(getContext());
		if (logging) {
			log("%s: Enter state %s %s", name, currentState, currentState.timer());
		}
		currentState.timer().start();
		stateChangeListeners.forEach(listener -> listener.accept(prevState, currentState));
	}

	/**
	 * Updates this FSM's state. Runs the {@link State#onUpdate} hook method (if defined) of the current state and
	 * advances the state timer.
	 */
	public void update() {
		try {
			currentState.onUpdate(getContext());
			currentState.timer().advance();
		} catch (Exception x) {
			x.printStackTrace();
			log("%s: Error updating state %s, %s", name, currentState, currentState.timer());
		}
	}

	/**
	 * Returns to the previous state.
	 */
	public void resumePreviousState() {
		if (prevState == null) {
			throw new IllegalStateException("State machine cannot resume previous state because there is none");
		}
		if (logging) {
			log("%s: Resume state %s, %s", name, prevState, prevState.timer());
		}
		changeState(prevState);
	}
}