/*
MIT License

Copyright (c) 2021-2023 Armin Reichert

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

import static de.amr.games.pacman.controller.mspacman.MsPacManIntroData.BLINKY_END_TILE;
import static de.amr.games.pacman.controller.mspacman.MsPacManIntroData.GUYS_SPEED;
import static de.amr.games.pacman.controller.mspacman.MsPacManIntroData.MS_PACMAN_STOP_X;
import static de.amr.games.pacman.controller.mspacman.MsPacManIntroData.TURNING_POSITION;
import static de.amr.games.pacman.lib.steering.Direction.LEFT;
import static de.amr.games.pacman.lib.steering.Direction.UP;
import static de.amr.games.pacman.model.common.world.World.t;

import de.amr.games.pacman.controller.common.GameState;
import de.amr.games.pacman.lib.anim.Animated;
import de.amr.games.pacman.lib.fsm.FsmState;
import de.amr.games.pacman.lib.timer.TickTimer;
import de.amr.games.pacman.model.common.GameModel;
import de.amr.games.pacman.model.common.actors.Ghost;

/**
 * States of Ms. Pac-Man intro scene.
 * 
 * @author Armin Reichert
 */
public enum MsPacManIntroState implements FsmState<MsPacManIntroData> {

	START {
		@Override
		public void onEnter(MsPacManIntroData ctx) {
			ctx.lightsTimer.restartIndefinitely();
			ctx.msPacMan.setPosition(t(34), TURNING_POSITION.y());
			ctx.msPacMan.setMoveDir(LEFT);
			ctx.msPacMan.setPixelSpeed(GUYS_SPEED);
			ctx.msPacMan.selectAndRunAnimation(GameModel.AK_PAC_MUNCHING);
			ctx.msPacMan.show();
			ctx.ghosts.forEach(ghost -> {
				ghost.setPosition(t(34), TURNING_POSITION.y());
				ghost.setMoveAndWishDir(LEFT);
				ghost.setPixelSpeed(GUYS_SPEED);
				ghost.enterStateHuntingPac();
				ghost.show();
			});
			ctx.ghostIndex = 0;
		}

		@Override
		public void onUpdate(MsPacManIntroData ctx) {
			if (timer.tick() == 2) {
				ctx.creditVisible = true;
			} else if (timer.atSecond(1)) {
				intro.changeState(MsPacManIntroState.GHOSTS);
			}
			ctx.lightsTimer.advance();
		}
	},

	GHOSTS {
		@Override
		public void onUpdate(MsPacManIntroData ctx) {
			ctx.lightsTimer.advance();
			Ghost ghost = ctx.ghosts.get(ctx.ghostIndex);
			ghost.move();
			ghost.animate();
			if (ghost.position().x() <= TURNING_POSITION.x()) {
				ghost.setMoveAndWishDir(UP);
			}
			if (ghost.position().y() <= BLINKY_END_TILE.y() + ghost.id() * 18) {
				ghost.setPixelSpeed(0);
				ghost.animation().ifPresent(Animated::reset);
				if (++ctx.ghostIndex == 4) {
					intro.changeState(MsPacManIntroState.MSPACMAN);
				}
			}
		}
	},

	MSPACMAN {
		@Override
		public void onUpdate(MsPacManIntroData ctx) {
			ctx.lightsTimer.advance();
			ctx.msPacMan.move();
			ctx.msPacMan.animate();
			if (ctx.msPacMan.position().x() <= MS_PACMAN_STOP_X) {
				ctx.msPacMan.setPixelSpeed(0);
				ctx.msPacMan.animation().ifPresent(Animated::reset);
				intro.changeState(MsPacManIntroState.READY_TO_PLAY);
			}
		}
	},

	READY_TO_PLAY {
		@Override
		public void onUpdate(MsPacManIntroData ctx) {
			if (timer.atSecond(2.0) && !ctx.gameController.game().hasCredit()) {
				ctx.gameController.changeState(GameState.READY);
				// go into demo mode
				return;
			}
			if (timer.atSecond(5)) {
				ctx.gameController.changeState(GameState.CREDIT);
				return;
			}
			ctx.lightsTimer.advance();
			ctx.blinking.animate();
		}
	};

	MsPacManIntroController intro;
	final TickTimer timer = new TickTimer("Timer-" + name());

	@Override
	public TickTimer timer() {
		return timer;
	}
}