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
import de.amr.games.pacman.model.common.GameModel;
import de.amr.games.pacman.model.common.Ghost;
import de.amr.games.pacman.model.common.GhostState;
import de.amr.games.pacman.model.common.Pac;

public enum Intermission1State implements FsmState<Object> {

	CHASING_PACMAN {
		@Override
		public void onEnter(Object context) {
			timer.setSeconds(5).start();

			controller.pac = new Pac("Pac-Man");
			controller.pac.setMoveDir(Direction.LEFT);
			controller.pac.setPosition(t(30), t(20));
			controller.pac.setSpeed(1.0);
			controller.pac.show();

			controller.blinky = new Ghost(GameModel.RED_GHOST, "Blinky");
			controller.blinky.state = GhostState.HUNTING_PAC;
			controller.blinky.setMoveDir(Direction.LEFT);
			controller.blinky.setWishDir(Direction.LEFT);
			controller.blinky.position = controller.pac.position.plus(t(3) + 0.5, 0);
			controller.blinky.setSpeed(1.05);
			controller.blinky.show();
		}

		@Override
		public void onUpdate(Object context) {
			if (timer.ticked() < 60) {
				return;
			}
			if (timer.hasExpired()) {
				controller.changeState(CHASING_BLINKY);
				return;
			}
			controller.pac.move();
			controller.blinky.move();
		}
	},

	CHASING_BLINKY {
		@Override
		public void onEnter(Object context) {
			timer.setSeconds(7).start();
			controller.pac.setMoveDir(Direction.RIGHT);
			controller.pac.setPosition(-t(24), t(20));
			controller.pac.setSpeed(1.0);
			controller.blinky.state = GhostState.FRIGHTENED;
			controller.blinky.setMoveDir(Direction.RIGHT);
			controller.blinky.setWishDir(Direction.RIGHT);
			controller.blinky.setPosition(-t(1), t(20));
			controller.blinky.setSpeed(0.6);
		}

		@Override
		public void onUpdate(Object context) {
			if (timer.hasExpired()) {
				controller.gameController.state.timer().expire();
				return;
			}
			controller.pac.move();
			controller.blinky.move();
		}
	};

	protected Intermission1Controller controller;
	protected final TickTimer timer = new TickTimer("Timer:" + name());

	@Override
	public TickTimer timer() {
		return timer;
	}
}