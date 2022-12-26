/*
MIT License

Copyright (c) 2021-22 Armin Reichert

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

import static de.amr.games.pacman.lib.math.Vector2i.v2i;
import static de.amr.games.pacman.lib.steering.Direction.LEFT;
import static de.amr.games.pacman.lib.steering.Direction.UP;
import static de.amr.games.pacman.model.common.world.World.HTS;
import static de.amr.games.pacman.model.common.world.World.t;

import java.util.Arrays;
import java.util.List;

import de.amr.games.pacman.controller.common.GameController;
import de.amr.games.pacman.controller.common.GameState;
import de.amr.games.pacman.controller.common.SceneControllerContext;
import de.amr.games.pacman.controller.mspacman.MsPacManIntro.IntroData;
import de.amr.games.pacman.controller.mspacman.MsPacManIntro.IntroState;
import de.amr.games.pacman.lib.anim.EntityAnimation;
import de.amr.games.pacman.lib.anim.Pulse;
import de.amr.games.pacman.lib.fsm.Fsm;
import de.amr.games.pacman.lib.fsm.FsmState;
import de.amr.games.pacman.lib.math.Vector2i;
import de.amr.games.pacman.lib.timer.TickTimer;
import de.amr.games.pacman.model.common.actors.AnimKeys;
import de.amr.games.pacman.model.common.actors.Ghost;
import de.amr.games.pacman.model.common.actors.Pac;

/**
 * Intro scene of the Ms. Pac-Man game.
 * <p>
 * The ghosts and Ms. Pac-Man are introduced on a billboard and are marching in one after another.
 * 
 * @author Armin Reichert
 */
public class MsPacManIntro extends Fsm<IntroState, IntroData> {

	private final IntroData introData;

	public MsPacManIntro(GameController gameController) {
		states = IntroState.values();
		for (var state : states) {
			state.controller = this;
		}
		introData = new IntroData(gameController);
	}

	@Override
	public IntroData context() {
		return introData;
	}

	public static class IntroData extends SceneControllerContext {
		public final Vector2i redGhostEndPosition = v2i(t(8), t(11));
		public final Vector2i turningPoint = v2i(t(6), t(20)).plus(0, HTS);
		public final int msPacManStopX = t(15);
		public final Vector2i titlePosition = v2i(t(10), t(8));
		public final Pulse blinking = new Pulse(30, true);
		public final TickTimer lightsTimer = new TickTimer("lights-timer");
		public final float actorSpeed = 1.1f;
		public final Pac pac;
		public final List<Ghost> ghosts;
		public int ghostIndex = 0;
		public boolean creditVisible = false;

		public IntroData(GameController gameController) {
			super(gameController);
			pac = gameController.game().createPac();
			ghosts = Arrays.asList(gameController.game().createGhosts());
		}
	}

	public enum IntroState implements FsmState<MsPacManIntro.IntroData> {

		START {
			@Override
			public void onEnter(IntroData ctx) {
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
			public void onUpdate(IntroData ctx) {
				if (timer.tick() == 1) {
					ctx.game().gameScore().setVisible(true);
					ctx.game().highScore().setVisible(true);
				} else if (timer.tick() == 2) {
					ctx.creditVisible = true;
				} else if (timer.atSecond(1)) {
					controller.changeState(IntroState.GHOSTS);
				}
				ctx.lightsTimer.advance();
			}
		},

		GHOSTS {
			@Override
			public void onEnter(IntroData ctx) {
				ctx.ghosts.forEach(ghost -> ghost.selectRunnableAnimation(AnimKeys.GHOST_COLOR));
			}

			@Override
			public void onUpdate(IntroData ctx) {
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
						controller.changeState(IntroState.MSPACMAN);
					}
				}
			}
		},

		MSPACMAN {
			@Override
			public void onEnter(IntroData ctx) {
				ctx.pac.setMoveDir(LEFT);
				ctx.pac.setAbsSpeed(ctx.actorSpeed);
				ctx.pac.selectRunnableAnimation(AnimKeys.PAC_MUNCHING);
				ctx.pac.show();
			}

			@Override
			public void onUpdate(IntroData ctx) {
				ctx.lightsTimer.advance();
				ctx.pac.move();
				ctx.pac.animate();
				if (ctx.pac.position().x() <= ctx.msPacManStopX) {
					ctx.pac.setAbsSpeed(0);
					ctx.pac.animation().ifPresent(EntityAnimation::reset);
					controller.changeState(IntroState.READY_TO_PLAY);
				}
			}
		},

		READY_TO_PLAY {
			@Override
			public void onUpdate(IntroData ctx) {
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

		MsPacManIntro controller;
		final TickTimer timer = new TickTimer("Timer-" + name());

		@Override
		public TickTimer timer() {
			return timer;
		}
	}
}