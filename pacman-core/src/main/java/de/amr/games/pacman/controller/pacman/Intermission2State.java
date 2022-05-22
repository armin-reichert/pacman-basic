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

package de.amr.games.pacman.controller.pacman;

import static de.amr.games.pacman.model.common.world.World.t;

import de.amr.games.pacman.lib.Direction;
import de.amr.games.pacman.lib.FsmState;
import de.amr.games.pacman.lib.TickTimer;
import de.amr.games.pacman.model.common.GameEntity;
import de.amr.games.pacman.model.common.GameModel;
import de.amr.games.pacman.model.common.Ghost;
import de.amr.games.pacman.model.common.GhostState;
import de.amr.games.pacman.model.common.Pac;

public enum Intermission2State implements FsmState<Object> {
	CHASING {
		@Override
		public void onEnter(Object context) {
			controller.startStateTimer();

			controller.pac = new Pac("Pac-Man");
			controller.pac.setMoveDir(Direction.LEFT);
			controller.pac.setPosition(t(30), t(20));
			controller.pac.setSpeed(1.0);
			controller.pac.show();

			controller.blinky = new Ghost(GameModel.RED_GHOST, "Blinky");
			controller.blinky.state = GhostState.HUNTING_PAC;
			controller.blinky.setMoveDir(Direction.LEFT);
			controller.blinky.setWishDir(Direction.LEFT);
			controller.blinky.position = controller.pac.position.plus(t(14), 0);
			controller.blinky.setSpeed(1.0);
			controller.blinky.show();

			controller.nail = new GameEntity();
			controller.nail.setPosition(t(14), t(20) - 1);
			controller.nail.show();
		}

		@Override
		public void onUpdate(Object context) {
			if (controller.nailDistance() == 0) {
				controller.changeState(STRETCHED);
				return;
			}
			controller.pac.move();
			controller.blinky.move();
		}
	},

	STRETCHED {
		@Override
		public void onUpdate(Object context) {
			int stretching = controller.nailDistance() / 4;
			if (stretching == 3) {
				controller.blinky.setSpeed(0);
				controller.blinky.setMoveDir(Direction.UP);
				controller.changeState(Intermission2State.STUCK);
				return;
			}
			controller.blinky.setSpeed(0.3 - 0.1 * stretching);
			controller.blinky.move();
			controller.pac.move();
		}
	},

	STUCK {
		@Override
		public void onUpdate(Object context) {
			if (timer.isRunningSeconds(2)) {
				controller.blinky.setMoveDir(Direction.RIGHT);
			} else if (timer.isRunningSeconds(6)) {
				controller.gameController.state.timer().expire();
				return;
			}
			controller.blinky.move();
			controller.pac.move();
		}
	};

	protected Intermission2Controller controller;
	protected final TickTimer timer = new TickTimer("Timer:" + name());

	@Override
	public TickTimer timer() {
		return timer;
	}
}