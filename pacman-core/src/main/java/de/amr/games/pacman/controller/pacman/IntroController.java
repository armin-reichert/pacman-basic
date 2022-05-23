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
package de.amr.games.pacman.controller.pacman;

import de.amr.games.pacman.controller.common.GameController;
import de.amr.games.pacman.lib.FiniteStateMachine;

/**
 * Intro scene of the PacMan game.
 * <p>
 * The ghosts are presented one after another, then Pac-Man is chased by the ghosts, turns the card and hunts the ghosts
 * himself.
 * 
 * @author Armin Reichert
 */
public class IntroController extends FiniteStateMachine<IntroState, IntroContext> {

	public final IntroContext context = new IntroContext();

	public IntroController(GameController gameController) {
		context.gameController = gameController;
		for (var state : IntroState.values()) {
			state.controller = this;
		}
		logging = true;
	}

	@Override
	public IntroContext getContext() {
		return context;
	}

	public void init() {
		// TODO fixme
		for (var s : IntroState.values()) {
			s.timer.set(0);
		}
		enterAsInitialState(IntroState.BEGIN);
	}

	public void selectGhost(int index) {
		context.ghostIndex = index;
		context.portraits[context.ghostIndex].ghost.show();
	}
}