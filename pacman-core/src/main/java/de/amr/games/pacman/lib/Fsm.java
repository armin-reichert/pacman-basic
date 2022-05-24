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
 * State transitions are defined dynamically via the {@link #changeState(Enum)} method calls. Each state change triggers
 * an event.
 * 
 * @param <STATE>   enumeration type providing the states of this FSM
 * @param <CONTEXT> data provided to the state lifecycle methods {@link FsmState#onEnter}, {@link FsmState#onUpdate} and
 *                  {@link FsmState#onExit}
 * 
 * @author Armin Reichert
 */
public abstract class Fsm<STATE extends FsmState<CONTEXT>, CONTEXT> {

	public boolean logging;

	private String name;
	private final STATE[] states;
	private STATE state;
	private STATE prevState;

	protected final List<BiConsumer<STATE, STATE>> stateChangeListeners = new ArrayList<>();

	public Fsm(STATE[] states) {
		this.states = states;
		for (var state : states) {
			state.setFsm(this);
		}
		name = getClass().getSimpleName();
	}

	@Override
	public String toString() {
		return "FiniteStateMachine[%s: state=%s prev=%s]".formatted(name, state, prevState);
	}

	public abstract CONTEXT getContext();

	public STATE state() {
		return state;
	}

	public STATE prevState() {
		return prevState;
	}

	public void resetTimers() {
		for (var state : states) {
			state.timer().setDurationIndefinite();
		}
	}

	public void reset(STATE initialState) {
		resetTimers();
		state = null;
		changeState(initialState);
	}

	public void changeState(STATE newState) {
		if (newState == state) {
			throw new IllegalStateException("FiniteStateMachine: Self loop in state " + state);
		}
		if (state != null) {
			state.onExit(getContext());
			if (logging) {
				log("%s: Exit  state %s %s", name, state, state.timer());
			}
		}
		prevState = state;
		state = newState;
		state.onEnter(getContext());
		state.timer().start();
		if (logging) {
			log("%s: Enter state %s %s", name, state, state.timer());
		}
		stateChangeListeners.stream().forEach(listener -> listener.accept(prevState, state));
	}

	/**
	 * Runs the {@link State#onUpdate} hook method (if defined) of the current state and ticks the state timer.
	 */
	public void update() {
		try {
			state.onUpdate(getContext());
			state.timer().run();
		} catch (Exception x) {
			log("%s: Error updating state %s, timer: %s", name, state, state.timer());
			x.printStackTrace();
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
			log("%s: Resume state %s, timer: %s", name, prevState, prevState.timer());
		}
		changeState(prevState);
	}
}