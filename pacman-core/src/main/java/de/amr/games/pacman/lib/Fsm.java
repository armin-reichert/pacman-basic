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
 * A finite-state machine, a graph of vertices (states) connected by transitions.
 * <p>
 * Transitions are defined dynamically by the {@link #changeState(Enum)} method calls. Each state transition triggers a
 * state change event.
 * 
 * @author Armin Reichert
 */
public abstract class Fsm<STATE extends FsmState<CONTEXT>, CONTEXT> {

	public boolean logging;

	public static void nop() {
	}

	private String name;
	private STATE state;
	private STATE prevState;

	protected final List<BiConsumer<STATE, STATE>> stateChangeListeners = new ArrayList<>();

	public Fsm(STATE[] states) {
		name = getClass().getSimpleName();
		for (var state : states) {
			state.setFsm(this);
		}
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

	public void enterAsInitialState(STATE newState) {
		state = null;
		changeState(newState);
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
	public void updateState() {
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