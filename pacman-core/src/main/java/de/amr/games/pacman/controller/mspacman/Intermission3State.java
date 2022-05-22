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
import de.amr.games.pacman.lib.V2d;
import de.amr.games.pacman.model.common.GameEntity;
import de.amr.games.pacman.model.common.Pac;
import de.amr.games.pacman.model.mspacman.Flap;
import de.amr.games.pacman.model.mspacman.JuniorBag;

public enum Intermission3State implements FsmState<Object> {
	FLAP {
		@Override
		public void onEnter(Object context) {
			controller.flap = new Flap();
			controller.pacMan = new Pac("Pac-Man");
			controller.msPacMan = new Pac("Ms. Pac-Man");
			controller.stork = new GameEntity();
			controller.bag = new JuniorBag();

			timer.setSeconds(2).start();
			controller.playIntermissionSound.run();

			controller.flap.number = 3;
			controller.flap.text = "JUNIOR";
			controller.flap.setPosition(t(3), t(10));
			controller.flap.show();
		}

		@Override
		public void onUpdate(Object context) {
			if (timer.isRunningSeconds(1)) {
				controller.playFlapAnimation.run();
			} else if (timer.isRunningSeconds(2)) {
				controller.flap.hide();
				controller.changeState(Intermission3State.ACTION);
			}
		}
	},
	ACTION {
		@Override
		public void onEnter(Object context) {
			timer.setIndefinite().start();

			controller.pacMan.setMoveDir(Direction.RIGHT);
			controller.pacMan.setPosition(t(3), controller.groundY - 4);
			controller.pacMan.show();

			controller.msPacMan.setMoveDir(Direction.RIGHT);
			controller.msPacMan.setPosition(t(5), controller.groundY - 4);
			controller.msPacMan.show();

			controller.stork.setPosition(t(30), t(12));
			controller.stork.setVelocity(-0.8, 0);
			controller.stork.show();

			controller.bag.position = controller.stork.position.plus(-14, 3);
			controller.bag.velocity = controller.stork.velocity;
			controller.bag.acceleration = V2d.NULL;
			controller.bag.open = false;
			controller.bag.show();
			controller.numBagBounces = 0;
		}

		@Override
		public void onUpdate(Object context) {
			controller.stork.move();
			controller.bag.move();

			// release bag from storks beak?
			if ((int) controller.stork.position.x == t(20)) {
				controller.bag.acceleration = new V2d(0, 0.04);
				controller.stork.setVelocity(-1, 0);
			}

			// (closed) bag reaches ground for first time?
			if (!controller.bag.open && controller.bag.position.y > controller.groundY) {
				++controller.numBagBounces;
				if (controller.numBagBounces < 3) {
					controller.bag.setVelocity(-0.2f, -1f / controller.numBagBounces);
					controller.bag.setPosition(controller.bag.position.x, controller.groundY);
				} else {
					controller.bag.open = true;
					controller.bag.velocity = V2d.NULL;
					controller.changeState(Intermission3State.DONE);
				}
			}
		}
	},

	DONE {
		@Override
		public void onEnter(Object context) {
			timer.setSeconds(3).start();
		}

		@Override
		public void onUpdate(Object context) {
			controller.stork.move();
			if (timer.hasExpired()) {
				controller.gameController.state.timer().expire();
			}
		}
	};

	protected Intermission3Controller controller;
	protected final TickTimer timer = new TickTimer("Timer:" + name());

	@Override
	public TickTimer timer() {
		return timer;
	}

}