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
import de.amr.games.pacman.lib.Fsm;
import de.amr.games.pacman.lib.FsmState;
import de.amr.games.pacman.lib.TickTimer;
import de.amr.games.pacman.model.common.GameEntity;
import de.amr.games.pacman.model.common.GameModel;
import de.amr.games.pacman.model.common.Ghost;
import de.amr.games.pacman.model.common.GhostState;
import de.amr.games.pacman.model.common.Pac;

public enum Intermission2State implements FsmState<Intermission2Context> {

	CHASING {
		@Override
		public void onEnter(Intermission2Context context) {
			timer.setDurationIndefinite().start();

			context.playIntermissionSound.run();

			context.pac = new Pac("Pac-Man");
			context.pac.setMoveDir(Direction.LEFT);
			context.pac.setPosition(t(30), t(20));
			context.pac.setSpeed(1.0);
			context.pac.show();

			context.blinky = new Ghost(GameModel.RED_GHOST, "Blinky");
			context.blinky.state = GhostState.HUNTING_PAC;
			context.blinky.setMoveDir(Direction.LEFT);
			context.blinky.setWishDir(Direction.LEFT);
			context.blinky.position = context.pac.position.plus(t(14), 0);
			context.blinky.setSpeed(1.0);
			context.blinky.show();

			context.nail = new GameEntity();
			context.nail.setPosition(t(14), t(20) - 1);
			context.nail.show();
		}

		@Override
		public void onUpdate(Intermission2Context context) {
			if (controller.nailDistance() == 0) {
				controller.changeState(STRETCHED);
				return;
			}
			context.pac.move();
			context.blinky.move();
		}
	},

	STRETCHED {
		@Override
		public void onUpdate(Intermission2Context context) {
			int stretching = controller.nailDistance() / 4;
			if (stretching == 3) {
				context.blinky.setSpeed(0);
				context.blinky.setMoveDir(Direction.UP);
				controller.changeState(Intermission2State.STUCK);
				return;
			}
			context.blinky.setSpeed(0.3 - 0.1 * stretching);
			context.blinky.move();
			context.pac.move();
		}
	},

	STUCK {
		@Override
		public void onUpdate(Intermission2Context context) {
			if (timer.atSecond(2)) {
				context.blinky.setMoveDir(Direction.RIGHT);
			} else if (timer.atSecond(6)) {
				controller.gameController.state().timer().expire();
				return;
			}
			context.blinky.move();
			context.pac.move();
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