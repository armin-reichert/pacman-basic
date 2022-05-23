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
import de.amr.games.pacman.lib.Fsm;

/**
 * Second intermission scene: Blinky pursues Pac but kicks a nail that tears his dress apart.
 * 
 * @author Armin Reichert
 */
public class Intermission2Controller extends Fsm<Intermission2State, Intermission2Context> {

	public final GameController gameController;
	public final Intermission2Context context = new Intermission2Context();

	public Intermission2Controller(GameController gameController) {
		super(Intermission2State.values());
		this.gameController = gameController;
	}

	@Override
	public Intermission2Context getContext() {
		return context;
	}

	public void init() {
		enterAsInitialState(Intermission2State.CHASING);
	}

	public int nailDistance() {
		return (int) (context.nail.position.x - context.blinky.position.x);
	}
}