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
package de.amr.games.pacman.controller.mspacman;

import de.amr.games.pacman.controller.common.GameController;
import de.amr.games.pacman.lib.FiniteStateMachine;

/**
 * Intro scene of the Ms. Pac-Man game.
 * <p>
 * The ghosts and Ms. Pac-Man are introduced on a billboard and are marching in one after another.
 * 
 * @author Armin Reichert
 */
public class IntroController extends FiniteStateMachine<IntroState, IntroContext> {

	public final GameController gameController;
	private final IntroContext context = new IntroContext();

	public IntroController(GameController gameController) {
		this.gameController = gameController;
		for (var state : IntroState.values()) {
			state.fsm = this;
		}
	}

	@Override
	public IntroContext getContext() {
		return context;
	}

	public void init() {
		// TODO fixme
		for (var state : IntroState.values()) {
			state.timer.set(0);
		}
		state = null;
		changeState(IntroState.BEGIN);
	}

	@Override
	public void updateState() {
		super.updateState();
		context.boardAnimationTimer.tick();
	}
}