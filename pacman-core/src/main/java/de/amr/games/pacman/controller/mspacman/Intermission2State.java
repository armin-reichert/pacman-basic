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
import de.amr.games.pacman.lib.Fsm;
import de.amr.games.pacman.lib.FsmState;
import de.amr.games.pacman.lib.TickTimer;
import de.amr.games.pacman.model.common.Pac;
import de.amr.games.pacman.model.mspacman.Flap;

public enum Intermission2State implements FsmState<Intermission2Context> {

	FLAP {
		@Override
		public void onEnter(Intermission2Context context) {
			timer.setDurationIndefinite().start();
			context.playIntermissionSound.run();
			context.flap = new Flap();
			context.flap.number = 2;
			context.flap.text = "THE CHASE";
			context.flap.setPosition(t(3), t(10));
			context.flap.show();
			context.pacMan = new Pac("Pac-Man");
			context.pacMan.setMoveDir(Direction.RIGHT);
			context.msPacMan = new Pac("Ms. Pac-Man");
			context.msPacMan.setMoveDir(Direction.RIGHT);
		}

		@Override
		public void onUpdate(Intermission2Context context) {
			if (timer.atSecond(1)) {
				context.playFlapAnimation.run();
			} else if (timer.atSecond(2)) {
				context.flap.hide();
			} else if (timer.atSecond(3)) {
				controller.changeState(Intermission2State.CHASING);
			}
		}
	},

	CHASING {
		@Override
		public void onEnter(Intermission2Context context) {
			timer.setDurationIndefinite().start();
		}

		@Override
		public void onUpdate(Intermission2Context context) {
			if (timer.atSecond(1.5)) {
				context.pacMan.setPosition(-t(2), context.upperY);
				context.pacMan.setMoveDir(Direction.RIGHT);
				context.pacMan.setSpeed(2.0);
				context.pacMan.show();
				context.msPacMan.setPosition(-t(8), context.upperY);
				context.msPacMan.setMoveDir(Direction.RIGHT);
				context.msPacMan.setSpeed(2.0);
				context.msPacMan.show();
			} else if (timer.atSecond(6)) {
				context.pacMan.setPosition(t(36), context.lowerY);
				context.pacMan.setMoveDir(Direction.LEFT);
				context.pacMan.setSpeed(2.0);
				context.msPacMan.setPosition(t(30), context.lowerY);
				context.msPacMan.setMoveDir(Direction.LEFT);
				context.msPacMan.setSpeed(2.0);
			} else if (timer.atSecond(10.5)) {
				context.pacMan.setMoveDir(Direction.RIGHT);
				context.pacMan.setSpeed(2.0);
				context.msPacMan.setPosition(t(-8), context.middleY);
				context.msPacMan.setMoveDir(Direction.RIGHT);
				context.msPacMan.setSpeed(2.0);
				context.pacMan.setPosition(t(-2), context.middleY);
			} else if (timer.atSecond(14.5)) {
				context.pacMan.setPosition(t(42), context.upperY);
				context.pacMan.setMoveDir(Direction.LEFT);
				context.pacMan.setSpeed(4.0);
				context.msPacMan.setPosition(t(30), context.upperY);
				context.msPacMan.setMoveDir(Direction.LEFT);
				context.msPacMan.setSpeed(4.0);
			} else if (timer.atSecond(15.5)) {
				context.pacMan.setPosition(t(-2), context.lowerY);
				context.pacMan.setMoveDir(Direction.RIGHT);
				context.pacMan.setSpeed(4.0);
				context.msPacMan.setPosition(t(-14), context.lowerY);
				context.msPacMan.setMoveDir(Direction.RIGHT);
				context.msPacMan.setSpeed(4.0);
			} else if (timer.atSecond(20)) {
				controller.gameController.state().timer().expire();
				return;
			}
			context.pacMan.move();
			context.msPacMan.move();
		}
	};

	protected Intermission2Controller controller;
	protected final TickTimer timer = new TickTimer("Timer:" + name());

	@Override
	public void setFsm(Fsm<? extends FsmState<Intermission2Context>, Intermission2Context> fsm) {
		controller = (Intermission2Controller) fsm;
	}

	@Override
	public TickTimer timer() {
		return timer;
	}
}