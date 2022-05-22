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

public enum Intermission1State implements FsmState<Object> {
	FLAP {
		@Override
		public void onEnter(Object context) {
			timer.setSeconds(2).start();
			controller.playIntermissionSound.run();

			controller.flap = new Flap();
			controller.flap.number = 1;
			controller.flap.text = "THEY MEET";
			controller.flap.setPosition(t(3), t(10));
			controller.flap.show();

			controller.pacMan = new Pac("Pac-Man");
			controller.pacMan.setMoveDir(Direction.RIGHT);
			controller.pacMan.setPosition(-t(2), controller.upperY);
			controller.pacMan.show();

			controller.inky = new Ghost(GameModel.CYAN_GHOST, "Inky");
			controller.inky.setMoveDir(Direction.RIGHT);
			controller.inky.setWishDir(Direction.RIGHT);
			controller.inky.position = controller.pacMan.position.minus(t(6), 0);
			controller.inky.show();

			controller.msPac = new Pac("Ms. Pac-Man");
			controller.msPac.setMoveDir(Direction.LEFT);
			controller.msPac.setPosition(t(30), controller.lowerY);
			controller.msPac.show();

			controller.pinky = new Ghost(GameModel.PINK_GHOST, "Pinky");
			controller.pinky.setMoveDir(Direction.LEFT);
			controller.pinky.setWishDir(Direction.LEFT);
			controller.pinky.position = controller.msPac.position.plus(t(6), 0);
			controller.pinky.show();

			controller.heart = new GameEntity();
		}

		@Override
		public void onUpdate(Object context) {
			if (timer.isRunningSeconds(1)) {
				controller.playFlapAnimation.run();
			}
			if (timer.hasExpired()) {
				controller.changeState(Intermission1State.CHASED_BY_GHOSTS);
			}
		}
	},

	CHASED_BY_GHOSTS {
		@Override
		public void onEnter(Object context) {
			controller.flap.hide();
			controller.pacMan.setSpeed(0.9);
			controller.msPac.setSpeed(0.9);
			controller.inky.setSpeed(1);
			controller.pinky.setSpeed(1);
		}

		@Override
		public void onUpdate(Object context) {
			if (controller.inky.position.x > t(30)) {
				controller.changeState(Intermission1State.COMING_TOGETHER);
				return;
			}
			controller.inky.move();
			controller.pacMan.move();
			controller.pinky.move();
			controller.msPac.move();
		}
	},

	COMING_TOGETHER {
		@Override
		public void onEnter(Object context) {
			controller.msPac.setPosition(t(-3), controller.middleY);
			controller.msPac.setMoveDir(Direction.RIGHT);

			controller.pinky.position = controller.msPac.position.minus(t(5), 0);
			controller.pinky.setMoveDir(Direction.RIGHT);
			controller.pinky.setWishDir(Direction.RIGHT);

			controller.pacMan.setPosition(t(31), controller.middleY);
			controller.pacMan.setMoveDir(Direction.LEFT);

			controller.inky.position = controller.pacMan.position.plus(t(5), 0);
			controller.inky.setMoveDir(Direction.LEFT);
			controller.inky.setWishDir(Direction.LEFT);
		}

		@Override
		public void onUpdate(Object context) {
			// Pac-Man and Ms. Pac-Man reach end position?
			if (controller.pacMan.moveDir() == Direction.UP && controller.pacMan.position.y < controller.upperY) {
				controller.changeState(Intermission1State.IN_HEAVEN);
				return;
			}
			// Pac-Man and Ms. Pac-Man meet?
			else if (controller.pacMan.moveDir() == Direction.LEFT
					&& controller.pacMan.position.x - controller.msPac.position.x < t(2)) {
				controller.pacMan.setMoveDir(Direction.UP);
				controller.pacMan.setSpeed(0.75);
				controller.msPac.setMoveDir(Direction.UP);
				controller.msPac.setSpeed(0.75);
			}
			// Inky and Pinky collide?
			else if (controller.inky.moveDir() == Direction.LEFT
					&& controller.inky.position.x - controller.pinky.position.x < t(2)) {
				controller.inky.setMoveDir(Direction.RIGHT);
				controller.inky.setWishDir(Direction.RIGHT);
				controller.inky.setSpeed(0.3);
				controller.inky.velocity = controller.inky.velocity.minus(0, 2.0);
				controller.inky.acceleration = new V2d(0, 0.4);

				controller.pinky.setMoveDir(Direction.LEFT);
				controller.pinky.setWishDir(Direction.LEFT);
				controller.pinky.setSpeed(0.3);
				controller.pinky.velocity = controller.pinky.velocity.minus(0, 2.0);
				controller.pinky.acceleration = new V2d(0, 0.4);
			} else {
				controller.pacMan.move();
				controller.msPac.move();
				controller.inky.move();
				if (controller.inky.position.y > controller.middleY) {
					controller.inky.setPosition(controller.inky.position.x, controller.middleY);
					controller.inky.acceleration = V2d.NULL;
				}
				controller.pinky.move();
				if (controller.pinky.position.y > controller.middleY) {
					controller.pinky.setPosition(controller.pinky.position.x, controller.middleY);
					controller.pinky.acceleration = V2d.NULL;
				}
			}
		}
	},

	IN_HEAVEN {
		@Override
		public void onEnter(Object context) {
			timer.setSeconds(3).start();
			controller.pacMan.setSpeed(0);
			controller.pacMan.setMoveDir(Direction.LEFT);
			controller.msPac.setSpeed(0);
			controller.msPac.setMoveDir(Direction.RIGHT);
			controller.inky.setSpeed(0);
			controller.inky.hide();
			controller.pinky.setSpeed(0);
			controller.pinky.hide();
			controller.heart.setPosition((controller.pacMan.position.x + controller.msPac.position.x) / 2,
					controller.pacMan.position.y - t(2));
			controller.heart.show();
		}

		@Override
		public void onUpdate(Object context) {
			if (timer.hasExpired()) {
				controller.gameController.state.timer().expire();
			}
		}
	};

	protected Intermission1Controller controller;
	protected final TickTimer timer = new TickTimer("Timer:" + name());

	@Override
	public TickTimer timer() {
		return timer;
	}
}