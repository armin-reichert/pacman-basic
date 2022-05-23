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
import de.amr.games.pacman.model.common.GameModel;
import de.amr.games.pacman.model.common.Ghost;
import de.amr.games.pacman.model.common.Pac;
import de.amr.games.pacman.model.mspacman.Flap;

public enum Intermission1State implements FsmState<Intermission1Context> {

	FLAP {
		@Override
		public void onEnter(Intermission1Context context) {
			timer.setDurationSeconds(2).start();
			context.playIntermissionSound.run();

			context.flap = new Flap();
			context.flap.number = 1;
			context.flap.text = "THEY MEET";
			context.flap.setPosition(t(3), t(10));
			context.flap.show();

			context.pacMan = new Pac("Pac-Man");
			context.pacMan.setMoveDir(Direction.RIGHT);
			context.pacMan.setPosition(-t(2), context.upperY);
			context.pacMan.show();

			context.inky = new Ghost(GameModel.CYAN_GHOST, "Inky");
			context.inky.setMoveDir(Direction.RIGHT);
			context.inky.setWishDir(Direction.RIGHT);
			context.inky.position = context.pacMan.position.minus(t(6), 0);
			context.inky.show();

			context.msPac = new Pac("Ms. Pac-Man");
			context.msPac.setMoveDir(Direction.LEFT);
			context.msPac.setPosition(t(30), context.lowerY);
			context.msPac.show();

			context.pinky = new Ghost(GameModel.PINK_GHOST, "Pinky");
			context.pinky.setMoveDir(Direction.LEFT);
			context.pinky.setWishDir(Direction.LEFT);
			context.pinky.position = context.msPac.position.plus(t(6), 0);
			context.pinky.show();

			context.heart = new GameEntity();
		}

		@Override
		public void onUpdate(Intermission1Context context) {
			if (timer.atSecond(1)) {
				context.playFlapAnimation.run();
			}
			if (timer.hasExpired()) {
				fsm.changeState(Intermission1State.CHASED_BY_GHOSTS);
			}
		}
	},

	CHASED_BY_GHOSTS {
		@Override
		public void onEnter(Intermission1Context context) {
			context.flap.hide();
			context.pacMan.setSpeed(0.9);
			context.msPac.setSpeed(0.9);
			context.inky.setSpeed(1);
			context.pinky.setSpeed(1);
		}

		@Override
		public void onUpdate(Intermission1Context context) {
			if (context.inky.position.x > t(30)) {
				fsm.changeState(Intermission1State.COMING_TOGETHER);
				return;
			}
			context.inky.move();
			context.pacMan.move();
			context.pinky.move();
			context.msPac.move();
		}
	},

	COMING_TOGETHER {
		@Override
		public void onEnter(Intermission1Context context) {
			context.msPac.setPosition(t(-3), context.middleY);
			context.msPac.setMoveDir(Direction.RIGHT);

			context.pinky.position = context.msPac.position.minus(t(5), 0);
			context.pinky.setMoveDir(Direction.RIGHT);
			context.pinky.setWishDir(Direction.RIGHT);

			context.pacMan.setPosition(t(31), context.middleY);
			context.pacMan.setMoveDir(Direction.LEFT);

			context.inky.position = context.pacMan.position.plus(t(5), 0);
			context.inky.setMoveDir(Direction.LEFT);
			context.inky.setWishDir(Direction.LEFT);
		}

		@Override
		public void onUpdate(Intermission1Context context) {
			// Pac-Man and Ms. Pac-Man reach end position?
			if (context.pacMan.moveDir() == Direction.UP && context.pacMan.position.y < context.upperY) {
				fsm.changeState(Intermission1State.IN_HEAVEN);
				return;
			}
			// Pac-Man and Ms. Pac-Man meet?
			else if (context.pacMan.moveDir() == Direction.LEFT
					&& context.pacMan.position.x - context.msPac.position.x < t(2)) {
				context.pacMan.setMoveDir(Direction.UP);
				context.pacMan.setSpeed(0.75);
				context.msPac.setMoveDir(Direction.UP);
				context.msPac.setSpeed(0.75);
			}
			// Inky and Pinky collide?
			else if (context.inky.moveDir() == Direction.LEFT && context.inky.position.x - context.pinky.position.x < t(2)) {
				context.inky.setMoveDir(Direction.RIGHT);
				context.inky.setWishDir(Direction.RIGHT);
				context.inky.setSpeed(0.3);
				context.inky.velocity = context.inky.velocity.minus(0, 2.0);
				context.inky.acceleration = new V2d(0, 0.4);

				context.pinky.setMoveDir(Direction.LEFT);
				context.pinky.setWishDir(Direction.LEFT);
				context.pinky.setSpeed(0.3);
				context.pinky.velocity = context.pinky.velocity.minus(0, 2.0);
				context.pinky.acceleration = new V2d(0, 0.4);
			} else {
				context.pacMan.move();
				context.msPac.move();
				context.inky.move();
				if (context.inky.position.y > context.middleY) {
					context.inky.setPosition(context.inky.position.x, context.middleY);
					context.inky.acceleration = V2d.NULL;
				}
				context.pinky.move();
				if (context.pinky.position.y > context.middleY) {
					context.pinky.setPosition(context.pinky.position.x, context.middleY);
					context.pinky.acceleration = V2d.NULL;
				}
			}
		}
	},

	IN_HEAVEN {
		@Override
		public void onEnter(Intermission1Context context) {
			timer.setDurationSeconds(3).start();
			context.pacMan.setSpeed(0);
			context.pacMan.setMoveDir(Direction.LEFT);
			context.msPac.setSpeed(0);
			context.msPac.setMoveDir(Direction.RIGHT);
			context.inky.setSpeed(0);
			context.inky.hide();
			context.pinky.setSpeed(0);
			context.pinky.hide();
			context.heart.setPosition((context.pacMan.position.x + context.msPac.position.x) / 2,
					context.pacMan.position.y - t(2));
			context.heart.show();
		}

		@Override
		public void onUpdate(Intermission1Context context) {
			if (timer.hasExpired()) {
				fsm.gameController.state().timer().expire();
			}
		}
	};

	protected Intermission1Controller fsm;
	protected final TickTimer timer = new TickTimer("Timer:" + name());

	@Override
	public TickTimer timer() {
		return timer;
	}
}