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

import de.amr.games.pacman.lib.Fsm;
import de.amr.games.pacman.lib.FsmState;
import de.amr.games.pacman.lib.TickTimer;
import de.amr.games.pacman.model.common.Ghost;
import de.amr.games.pacman.model.common.GhostState;

/**
 * @author Armin Reichert
 */
public enum IntroState implements FsmState<IntroContext> {

	BEGIN {
		@Override
		public void onEnter(IntroContext $) {
			$.lightsTimer.setDurationIndefinite().start();
			$.msPacMan.setMoveDir(LEFT);
			$.msPacMan.setPosition(t(36), $.turningPoint.y);
			$.msPacMan.setSpeed(0.95);
			$.msPacMan.show();
			for (Ghost ghost : $.ghosts) {
				ghost.state = GhostState.HUNTING_PAC;
				ghost.setMoveDir(LEFT);
				ghost.setWishDir(LEFT);
				ghost.setPosition(t(36), $.turningPoint.y);
				ghost.setSpeed(0.95);
				ghost.show();
			}
			$.ghostIndex = 0;
		}

		@Override
		public void onUpdate(IntroContext $) {
			$.lightsTimer.run();
			if (timer.atSecond(1)) {
				controller.changeState(IntroState.GHOSTS);
			}
		}
	},

	GHOSTS {
		@Override
		public void onUpdate(IntroContext $) {
			$.lightsTimer.run();
			Ghost ghost = $.ghosts[$.ghostIndex];
			ghost.move();
			if (ghost.moveDir() != UP && ghost.position.x <= $.turningPoint.x) {
				ghost.setMoveDir(UP);
				ghost.setWishDir(UP);
			}
			if (ghost.position.y <= $.lightsTopLeft.y + ghost.id * 18) {
				ghost.setSpeed(0);
				if (++$.ghostIndex == $.ghosts.length) {
					controller.changeState(IntroState.MSPACMAN);
				}
			}
		}
	},

	MSPACMAN {
		@Override
		public void onUpdate(IntroContext $) {
			$.lightsTimer.run();
			$.msPacMan.move();
			if ($.msPacMan.position.x <= t(14)) {
				$.msPacMan.setSpeed(0);
				controller.changeState(IntroState.READY_TO_PLAY);
			}
		}
	},

	READY_TO_PLAY {
		@Override
		public void onUpdate(IntroContext $) {
			$.lightsTimer.run();
			$.blinking.advance();
			if (timer.atSecond(5)) {
				controller.gameController.state().timer().expire();
			}
		}
	};

	protected IntroController controller;
	protected final TickTimer timer = new TickTimer("Timer:" + name());

	@Override
	public void setFsm(Fsm<? extends FsmState<IntroContext>, IntroContext> fsm) {
		controller = (IntroController) fsm;
	}

	@Override
	public TickTimer timer() {
		return timer;
	}
}