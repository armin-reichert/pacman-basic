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

import de.amr.games.pacman.controller.mspacman.Intermission3Controller.Context;
import de.amr.games.pacman.lib.Direction;
import de.amr.games.pacman.lib.Fsm;
import de.amr.games.pacman.lib.FsmState;
import de.amr.games.pacman.lib.TickTimer;
import de.amr.games.pacman.lib.V2d;
import de.amr.games.pacman.model.common.GameEntity;
import de.amr.games.pacman.model.common.Pac;
import de.amr.games.pacman.model.mspacman.Flap;
import de.amr.games.pacman.model.mspacman.JuniorBag;

/**
 * @author Armin Reichert
 */
public enum Intermission3State implements FsmState<Context> {

	FLAP {
		@Override
		public void onEnter(Context $) {
			$.flap = new Flap();
			$.pacMan = new Pac("Pac-Man");
			$.msPacMan = new Pac("Ms. Pac-Man");
			$.stork = new GameEntity();
			$.bag = new JuniorBag();

			timer.setDurationSeconds(2).start();
			$.playIntermissionSound.run();

			$.flap.number = 3;
			$.flap.text = "JUNIOR";
			$.flap.setPosition(t(3), t(10));
			$.flap.show();
		}

		@Override
		public void onUpdate(Context $) {
			if (timer.atSecond(1)) {
				$.playFlapAnimation.run();
			} else if (timer.atSecond(2)) {
				$.flap.hide();
				controller.changeState(Intermission3State.ACTION);
			}
		}
	},
	ACTION {
		@Override
		public void onEnter(Context $) {
			timer.setDurationIndefinite().start();

			$.pacMan.setMoveDir(Direction.RIGHT);
			$.pacMan.setPosition(t(3), $.groundY - 4);
			$.pacMan.show();

			$.msPacMan.setMoveDir(Direction.RIGHT);
			$.msPacMan.setPosition(t(5), $.groundY - 4);
			$.msPacMan.show();

			$.stork.setPosition(t(30), t(12));
			$.stork.setVelocity(-0.8, 0);
			$.stork.show();

			$.bag.position = $.stork.position.plus(-14, 3);
			$.bag.velocity = $.stork.velocity;
			$.bag.acceleration = V2d.NULL;
			$.bag.open = false;
			$.bag.show();
			$.numBagBounces = 0;
		}

		@Override
		public void onUpdate(Context $) {
			$.stork.move();
			$.bag.move();

			// release bag from storks beak?
			if ((int) $.stork.position.x == t(20)) {
				$.bag.acceleration = new V2d(0, 0.04);
				$.stork.setVelocity(-1, 0);
			}

			// (closed) bag reaches ground for first time?
			if (!$.bag.open && $.bag.position.y > $.groundY) {
				++$.numBagBounces;
				if ($.numBagBounces < 3) {
					$.bag.setVelocity(-0.2f, -1f / $.numBagBounces);
					$.bag.setPosition($.bag.position.x, $.groundY);
				} else {
					$.bag.open = true;
					$.bag.velocity = V2d.NULL;
					controller.changeState(Intermission3State.DONE);
				}
			}
		}
	},

	DONE {
		@Override
		public void onEnter(Context $) {
			timer.setDurationSeconds(3).start();
		}

		@Override
		public void onUpdate(Context $) {
			$.stork.move();
			if (timer.hasExpired()) {
				controller.gameController.state().timer().expire();
			}
		}
	};

	protected Intermission3Controller controller;
	protected final TickTimer timer = new TickTimer("Timer:" + name());

	@Override
	public void setFsm(Fsm<? extends FsmState<Context>, Context> fsm) {
		controller = (Intermission3Controller) fsm;
	}

	@Override
	public TickTimer timer() {
		return timer;
	}
}