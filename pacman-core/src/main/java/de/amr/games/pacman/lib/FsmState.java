/*
MIT License

Copyright (c) 2022 Armin Reichert

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

/**
 * Interface implemented by all states (enums) of a FSM. Each state has a timer and a reference to its FSM.
 *
 * @author Armin Reichert
 */
public interface FsmState<CONTEXT> {

	/**
	 * Sets the reference to the FSM owning this state.
	 * 
	 * @param fsm the FSM owning this state
	 */
	void setFsm(Fsm<? extends FsmState<CONTEXT>, CONTEXT> fsm);

	/**
	 * The hook method that gets executed when the state is entered.
	 * 
	 * @param context the "context" (data type provided to the state)
	 */
	default void onEnter(CONTEXT context) {
	}

	/**
	 * The hook method that gets executed when the state is updated.
	 * 
	 * @param context the "context" (data type provided to the state)
	 */
	void onUpdate(CONTEXT context);

	/**
	 * The hook method that gets executed when the state is exited.
	 * 
	 * @param context the "context" (data type provided to the state)
	 */
	default void onExit(CONTEXT context) {
	}

	/**
	 * @return the timer of this state
	 */
	TickTimer timer();
}