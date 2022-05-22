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

package de.amr.games.pacman.controller.mspacman;

import static de.amr.games.pacman.model.common.world.World.t;

import de.amr.games.pacman.lib.Direction;
import de.amr.games.pacman.lib.FsmState;
import de.amr.games.pacman.lib.TickTimer;
import de.amr.games.pacman.model.common.Pac;
import de.amr.games.pacman.model.mspacman.Flap;

public enum Intermission2State implements FsmState<Object> {
	FLAP {
		@Override
		public void onEnter(Object context) {
			timer.setIndefinite().start();
			controller.playIntermissionSound.run();
			controller.flap = new Flap();
			controller.flap.number = 2;
			controller.flap.text = "THE CHASE";
			controller.flap.setPosition(t(3), t(10));
			controller.flap.show();
			controller.pacMan = new Pac("Pac-Man");
			controller.pacMan.setMoveDir(Direction.RIGHT);
			controller.msPacMan = new Pac("Ms. Pac-Man");
			controller.msPacMan.setMoveDir(Direction.RIGHT);
		}

		@Override
		public void onUpdate(Object context) {
			if (timer.isRunningSeconds(1)) {
				controller.playFlapAnimation.run();
			} else if (timer.isRunningSeconds(2)) {
				controller.flap.hide();
			} else if (timer.isRunningSeconds(3)) {
				controller.changeState(Intermission2State.CHASING);
			}
		}
	},

	CHASING {
		@Override
		public void onEnter(Object context) {
			timer.setIndefinite().start();
		}

		@Override
		public void onUpdate(Object context) {
			if (timer.isRunningSeconds(1.5)) {
				controller.pacMan.setPosition(-t(2), controller.upperY);
				controller.pacMan.setMoveDir(Direction.RIGHT);
				controller.pacMan.setSpeed(2.0);
				controller.pacMan.show();
				controller.msPacMan.setPosition(-t(8), controller.upperY);
				controller.msPacMan.setMoveDir(Direction.RIGHT);
				controller.msPacMan.setSpeed(2.0);
				controller.msPacMan.show();
			} else if (timer.isRunningSeconds(6)) {
				controller.pacMan.setPosition(t(36), controller.lowerY);
				controller.pacMan.setMoveDir(Direction.LEFT);
				controller.pacMan.setSpeed(2.0);
				controller.msPacMan.setPosition(t(30), controller.lowerY);
				controller.msPacMan.setMoveDir(Direction.LEFT);
				controller.msPacMan.setSpeed(2.0);
			} else if (timer.isRunningSeconds(10.5)) {
				controller.pacMan.setMoveDir(Direction.RIGHT);
				controller.pacMan.setSpeed(2.0);
				controller.msPacMan.setPosition(t(-8), controller.middleY);
				controller.msPacMan.setMoveDir(Direction.RIGHT);
				controller.msPacMan.setSpeed(2.0);
				controller.pacMan.setPosition(t(-2), controller.middleY);
			} else if (timer.isRunningSeconds(14.5)) {
				controller.pacMan.setPosition(t(42), controller.upperY);
				controller.pacMan.setMoveDir(Direction.LEFT);
				controller.pacMan.setSpeed(4.0);
				controller.msPacMan.setPosition(t(30), controller.upperY);
				controller.msPacMan.setMoveDir(Direction.LEFT);
				controller.msPacMan.setSpeed(4.0);
			} else if (timer.isRunningSeconds(15.5)) {
				controller.pacMan.setPosition(t(-2), controller.lowerY);
				controller.pacMan.setMoveDir(Direction.RIGHT);
				controller.pacMan.setSpeed(4.0);
				controller.msPacMan.setPosition(t(-14), controller.lowerY);
				controller.msPacMan.setMoveDir(Direction.RIGHT);
				controller.msPacMan.setSpeed(4.0);
			} else if (timer.isRunningSeconds(20)) {
				controller.gameController.state.timer().expire();
				return;
			}
			controller.pacMan.move();
			controller.msPacMan.move();
		}
	};

	protected Intermission2Controller controller;
	protected final TickTimer timer = new TickTimer("Timer:" + name());

	@Override
	public TickTimer timer() {
		return timer;
	}
}