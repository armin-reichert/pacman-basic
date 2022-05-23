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

public enum Intermission3State implements FsmState<Intermission3Context> {

	FLAP {
		@Override
		public void onEnter(Intermission3Context context) {
			context.flap = new Flap();
			context.pacMan = new Pac("Pac-Man");
			context.msPacMan = new Pac("Ms. Pac-Man");
			context.stork = new GameEntity();
			context.bag = new JuniorBag();

			timer.setSecond(2).start();
			context.playIntermissionSound.run();

			context.flap.number = 3;
			context.flap.text = "JUNIOR";
			context.flap.setPosition(t(3), t(10));
			context.flap.show();
		}

		@Override
		public void onUpdate(Intermission3Context context) {
			if (timer.atSecond(1)) {
				context.playFlapAnimation.run();
			} else if (timer.atSecond(2)) {
				context.flap.hide();
				fsm.changeState(Intermission3State.ACTION);
			}
		}
	},
	ACTION {
		@Override
		public void onEnter(Intermission3Context context) {
			timer.setIndefinite().start();

			context.pacMan.setMoveDir(Direction.RIGHT);
			context.pacMan.setPosition(t(3), context.groundY - 4);
			context.pacMan.show();

			context.msPacMan.setMoveDir(Direction.RIGHT);
			context.msPacMan.setPosition(t(5), context.groundY - 4);
			context.msPacMan.show();

			context.stork.setPosition(t(30), t(12));
			context.stork.setVelocity(-0.8, 0);
			context.stork.show();

			context.bag.position = context.stork.position.plus(-14, 3);
			context.bag.velocity = context.stork.velocity;
			context.bag.acceleration = V2d.NULL;
			context.bag.open = false;
			context.bag.show();
			context.numBagBounces = 0;
		}

		@Override
		public void onUpdate(Intermission3Context context) {
			context.stork.move();
			context.bag.move();

			// release bag from storks beak?
			if ((int) context.stork.position.x == t(20)) {
				context.bag.acceleration = new V2d(0, 0.04);
				context.stork.setVelocity(-1, 0);
			}

			// (closed) bag reaches ground for first time?
			if (!context.bag.open && context.bag.position.y > context.groundY) {
				++context.numBagBounces;
				if (context.numBagBounces < 3) {
					context.bag.setVelocity(-0.2f, -1f / context.numBagBounces);
					context.bag.setPosition(context.bag.position.x, context.groundY);
				} else {
					context.bag.open = true;
					context.bag.velocity = V2d.NULL;
					fsm.changeState(Intermission3State.DONE);
				}
			}
		}
	},

	DONE {
		@Override
		public void onEnter(Intermission3Context context) {
			timer.setSecond(3).start();
		}

		@Override
		public void onUpdate(Intermission3Context context) {
			context.stork.move();
			if (timer.hasExpired()) {
				fsm.gameController.state().timer().expire();
			}
		}
	};

	protected Intermission3Controller fsm;
	protected final TickTimer timer = new TickTimer("Timer:" + name());

	@Override
	public TickTimer timer() {
		return timer;
	}

}