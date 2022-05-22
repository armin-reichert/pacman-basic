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

import static de.amr.games.pacman.lib.Direction.LEFT;
import static de.amr.games.pacman.lib.Direction.UP;
import static de.amr.games.pacman.model.common.world.World.t;

import de.amr.games.pacman.lib.FsmState;
import de.amr.games.pacman.lib.TickTimer;
import de.amr.games.pacman.model.common.Ghost;
import de.amr.games.pacman.model.common.GhostState;

public enum IntroState implements FsmState<Object> {

	BEGIN {
		@Override
		public void onEnter(Object context) {
			controller.boardAnimationTimer.setIndefinite().start();
			controller.msPacMan.setMoveDir(LEFT);
			controller.msPacMan.setPosition(t(36), controller.turningPoint.y);
			controller.msPacMan.setSpeed(0.95);
			controller.msPacMan.show();
			for (Ghost ghost : controller.ghosts) {
				ghost.state = GhostState.HUNTING_PAC;
				ghost.setMoveDir(LEFT);
				ghost.setWishDir(LEFT);
				ghost.setPosition(t(36), controller.turningPoint.y);
				ghost.setSpeed(0.95);
				ghost.show();
			}
			controller.ghostIndex = 0;
		}

		@Override
		public void onUpdate(Object context) {
			if (timer.isRunningSeconds(1)) {
				controller.changeState(IntroState.GHOSTS);
			}
		}
	},

	GHOSTS {
		@Override
		public void onUpdate(Object context) {
			Ghost ghost = controller.ghosts[controller.ghostIndex];
			ghost.move();
			if (ghost.moveDir() != UP && ghost.position.x <= controller.turningPoint.x) {
				ghost.setMoveDir(UP);
				ghost.setWishDir(UP);
			}
			if (ghost.position.y <= controller.boardTopLeft.y + ghost.id * 18) {
				ghost.setSpeed(0);
				if (++controller.ghostIndex == controller.ghosts.length) {
					controller.changeState(IntroState.MSPACMAN);
				}
			}
		}
	},

	MSPACMAN {
		@Override
		public void onUpdate(Object context) {
			controller.msPacMan.move();
			if (controller.msPacMan.position.x <= t(14)) {
				controller.msPacMan.setSpeed(0);
				controller.changeState(IntroState.READY);
			}
		}
	},

	READY {
		@Override
		public void onUpdate(Object context) {
			controller.blinking.advance();
			if (timer.isRunningSeconds(5)) {
				controller.gameController.state.timer().expire();
			}
		}
	};

	protected IntroController controller;
	protected final TickTimer timer = new TickTimer("Timer:" + name());

	@Override
	public TickTimer timer() {
		return timer;
	}

}