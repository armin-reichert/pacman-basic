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

import static de.amr.games.pacman.lib.steering.Direction.LEFT;
import static de.amr.games.pacman.lib.steering.Direction.UP;
import static de.amr.games.pacman.model.common.world.World.t;

import de.amr.games.pacman.controller.common.GameState;
import de.amr.games.pacman.lib.anim.EntityAnimation;
import de.amr.games.pacman.lib.fsm.FsmState;
import de.amr.games.pacman.lib.timer.TickTimer;
import de.amr.games.pacman.model.common.actors.AnimKeys;
import de.amr.games.pacman.model.common.actors.Ghost;

public enum MsPacManIntroState implements FsmState<MsPacManIntroData> {

	START {
		@Override
		public void onEnter(MsPacManIntroData ctx) {
			ctx.game().gameScore().setShowContent(false);
			ctx.game().highScore().setShowContent(true);
			ctx.lightsTimer.restartIndefinitely();
			ctx.pac.setPosition(t(34), ctx.turningPoint.y());
			ctx.pac.setAbsSpeed(0);
			ctx.ghosts.forEach(ghost -> {
				ghost.enterStateHuntingPac();
				ghost.setMoveAndWishDir(LEFT);
				ghost.setPosition(t(34), ctx.turningPoint.y());
				ghost.setAbsSpeed(ctx.actorSpeed);
				ghost.show();
			});
			ctx.ghostIndex = 0;
		}

		@Override
		public void onUpdate(MsPacManIntroData ctx) {
			if (timer.tick() == 2) {
				ctx.creditVisible = true;
			} else if (timer.atSecond(1)) {
				controller.changeState(MsPacManIntroState.GHOSTS);
			}
			ctx.lightsTimer.advance();
		}
	},

	GHOSTS {
		@Override
		public void onEnter(MsPacManIntroData ctx) {
			ctx.ghosts.forEach(ghost -> ghost.selectRunnableAnimation(AnimKeys.GHOST_COLOR));
		}

		@Override
		public void onUpdate(MsPacManIntroData ctx) {
			ctx.lightsTimer.advance();
			Ghost ghost = ctx.ghosts.get(ctx.ghostIndex);
			ghost.move();
			ghost.animate();
			if (ghost.position().x() <= ctx.turningPoint.x()) {
				ghost.setMoveAndWishDir(UP);
			}
			if (ghost.position().y() <= ctx.redGhostEndPosition.y() + ghost.id() * 18) {
				ghost.setAbsSpeed(0);
				ghost.animation().ifPresent(EntityAnimation::stop);
				if (++ctx.ghostIndex == 4) {
					controller.changeState(MsPacManIntroState.MSPACMAN);
				}
			}
		}
	},

	MSPACMAN {
		@Override
		public void onEnter(MsPacManIntroData ctx) {
			ctx.pac.setMoveDir(LEFT);
			ctx.pac.setAbsSpeed(ctx.actorSpeed);
			ctx.pac.selectRunnableAnimation(AnimKeys.PAC_MUNCHING);
			ctx.pac.show();
		}

		@Override
		public void onUpdate(MsPacManIntroData ctx) {
			ctx.lightsTimer.advance();
			ctx.pac.move();
			ctx.pac.animate();
			if (ctx.pac.position().x() <= ctx.msPacManStopX) {
				ctx.pac.setAbsSpeed(0);
				ctx.pac.animation().ifPresent(EntityAnimation::reset);
				controller.changeState(MsPacManIntroState.READY_TO_PLAY);
			}
		}
	},

	READY_TO_PLAY {
		@Override
		public void onUpdate(MsPacManIntroData ctx) {
			if (timer.atSecond(2.0) && !ctx.game().hasCredit()) {
				ctx.gameController().changeState(GameState.READY);
				// show play scene in attract mode
				return;
			}
			if (timer.atSecond(5)) {
				ctx.gameController().boot();
				return;
			}
			ctx.lightsTimer.advance();
			ctx.blinking.animate();
		}
	};

	MsPacManIntroController controller;
	final TickTimer timer = new TickTimer("Timer-" + name());

	@Override
	public TickTimer timer() {
		return timer;
	}
}