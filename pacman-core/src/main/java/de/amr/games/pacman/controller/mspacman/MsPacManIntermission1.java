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

import static de.amr.games.pacman.model.common.actors.Ghost.ID_PINK_GHOST;
import static de.amr.games.pacman.model.common.world.World.t;

import de.amr.games.pacman.controller.common.GameController;
import de.amr.games.pacman.controller.common.SceneControllerContext;
import de.amr.games.pacman.controller.mspacman.MsPacManIntermission1.IntermissionData;
import de.amr.games.pacman.controller.mspacman.MsPacManIntermission1.IntermissionState;
import de.amr.games.pacman.lib.Direction;
import de.amr.games.pacman.lib.TickTimer;
import de.amr.games.pacman.lib.V2d;
import de.amr.games.pacman.lib.animation.EntityAnimation;
import de.amr.games.pacman.lib.fsm.Fsm;
import de.amr.games.pacman.lib.fsm.FsmState;
import de.amr.games.pacman.model.common.GameModel;
import de.amr.games.pacman.model.common.GameSound;
import de.amr.games.pacman.model.common.actors.AnimKeys;
import de.amr.games.pacman.model.common.actors.Entity;
import de.amr.games.pacman.model.common.actors.Ghost;
import de.amr.games.pacman.model.common.actors.Pac;
import de.amr.games.pacman.model.mspacman.Clapperboard;

/**
 * Intermission scene 1: "They meet".
 * <p>
 * Pac-Man leads Inky and Ms. Pac-Man leads Pinky. Soon, the two Pac-Men are about to collide, they quickly move
 * upwards, causing Inky and Pinky to collide and vanish. Finally, Pac-Man and Ms. Pac-Man face each other at the top of
 * the screen and a big pink heart appears above them. (Played after round 2)
 * 
 * @author Armin Reichert
 */
public class MsPacManIntermission1 extends Fsm<IntermissionState, IntermissionData> {

	private final IntermissionData intermissionData;

	public MsPacManIntermission1(GameController gameController) {
		states = IntermissionState.values();
		for (var state : states) {
			state.intermission = this;
		}
		intermissionData = new IntermissionData(gameController);
	}

	@Override
	public IntermissionData context() {
		return intermissionData;
	}

	public static class IntermissionData extends SceneControllerContext {
		public final int upperY = t(12);
		public final int middleY = t(18);
		public final int lowerY = t(24);
		public final float pacSpeedChased = 1.125f;
		public final float pacSpeedRising = 0.75f;
		public final float ghostSpeedAfterColliding = 0.3f;
		public final float ghostSpeedChasing = 1.25f;
		public Clapperboard clapperboard;
		public Pac pacMan;
		public Pac msPac;
		public Ghost pinky;
		public Ghost inky;
		public Entity heart;

		public IntermissionData(GameController gameController) {
			super(gameController);
		}
	}

	public enum IntermissionState implements FsmState<IntermissionData> {

		FLAP {
			@Override
			public void onEnter(IntermissionData ctx) {
				timer.resetSeconds(2);
				timer.start();
				ctx.clapperboard = new Clapperboard(1, "THEY MEET");
				ctx.clapperboard.setPosition(t(3), t(10));
				ctx.clapperboard.show();

				ctx.pacMan = new Pac("Pac-Man");
				ctx.pacMan.setMoveDir(Direction.RIGHT);
				ctx.pacMan.setPosition(-t(2), ctx.upperY);
				ctx.pacMan.selectAndEnsureRunningAnimation(AnimKeys.PAC_MUNCHING);
				ctx.pacMan.show();

				ctx.inky = new Ghost(Ghost.ID_CYAN_GHOST, "Inky");
				ctx.inky.setMoveAndWishDir(Direction.RIGHT);
				ctx.inky.setPosition(ctx.pacMan.position().minus(t(6), 0));
				ctx.inky.selectAndEnsureRunningAnimation(AnimKeys.GHOST_COLOR);
				ctx.inky.show();

				ctx.msPac = new Pac("Ms. Pac-Man");
				ctx.msPac.setMoveDir(Direction.LEFT);
				ctx.msPac.setPosition(t(30), ctx.lowerY);
				ctx.msPac.selectAndEnsureRunningAnimation(AnimKeys.PAC_MUNCHING);
				ctx.msPac.show();

				ctx.pinky = new Ghost(ID_PINK_GHOST, "Pinky");
				ctx.pinky.setMoveAndWishDir(Direction.LEFT);
				ctx.pinky.setPosition(ctx.msPac.position().plus(t(6), 0));
				ctx.pinky.selectAndEnsureRunningAnimation(AnimKeys.GHOST_COLOR);
				ctx.pinky.show();

				ctx.heart = new Entity();
			}

			@Override
			public void onUpdate(IntermissionData ctx) {
				if (timer.atSecond(1)) {
					ctx.gameController().sounds().play(GameSound.INTERMISSION_1);
					ctx.clapperboard.selectedAnimation().ifPresent(EntityAnimation::restart);
				}
				if (timer.hasExpired()) {
					ctx.clapperboard.hide();
					intermission.changeState(IntermissionState.CHASED_BY_GHOSTS);
				}
			}
		},

		CHASED_BY_GHOSTS {
			@Override
			public void onEnter(IntermissionData ctx) {
				ctx.pacMan.setAbsSpeed(ctx.pacSpeedChased);
				ctx.msPac.setAbsSpeed(ctx.pacSpeedChased);
				ctx.inky.setAbsSpeed(ctx.ghostSpeedChasing);
				ctx.pinky.setAbsSpeed(ctx.ghostSpeedChasing);
			}

			@Override
			public void onUpdate(IntermissionData ctx) {
				if (ctx.inky.position().x() > t(30)) {
					intermission.changeState(IntermissionState.COMING_TOGETHER);
					return;
				}
				ctx.inky.move();
				ctx.inky.advanceAnimation();
				ctx.pacMan.move();
				ctx.pacMan.advanceAnimation();
				ctx.pinky.move();
				ctx.pinky.advanceAnimation();
				ctx.msPac.move();
				ctx.msPac.advanceAnimation();
			}
		},

		COMING_TOGETHER {
			@Override
			public void onEnter(IntermissionData ctx) {
				ctx.msPac.setPosition(t(-3), ctx.middleY);
				ctx.msPac.setMoveDir(Direction.RIGHT);

				ctx.pinky.setPosition(ctx.msPac.position().minus(t(5), 0));
				ctx.pinky.setMoveAndWishDir(Direction.RIGHT);

				ctx.pacMan.setPosition(t(31), ctx.middleY);
				ctx.pacMan.setMoveDir(Direction.LEFT);

				ctx.inky.setPosition(ctx.pacMan.position().plus(t(5), 0));
				ctx.inky.setMoveAndWishDir(Direction.LEFT);
			}

			@Override
			public void onUpdate(IntermissionData ctx) {
				// Pac-Man and Ms. Pac-Man reach end position?
				if (ctx.pacMan.moveDir() == Direction.UP && ctx.pacMan.position().y() < ctx.upperY) {
					intermission.changeState(IntermissionState.IN_HEAVEN);
				}
				// Pac-Man and Ms. Pac-Man meet?
				else if (ctx.pacMan.moveDir() == Direction.LEFT
						&& ctx.pacMan.position().x() - ctx.msPac.position().x() < t(2)) {
					ctx.pacMan.setMoveDir(Direction.UP);
					ctx.pacMan.setAbsSpeed(ctx.pacSpeedRising);
					ctx.msPac.setMoveDir(Direction.UP);
					ctx.msPac.setAbsSpeed(ctx.pacSpeedRising);
				}
				// Inky and Pinky collide?
				else if (ctx.inky.moveDir() == Direction.LEFT && ctx.inky.position().x() - ctx.pinky.position().x() < t(2)) {
					ctx.inky.setMoveAndWishDir(Direction.RIGHT);
					ctx.inky.setAbsSpeed(ctx.ghostSpeedAfterColliding);
					ctx.inky.setVelocity(ctx.inky.velocity().minus(0, 2.0));
					ctx.inky.setAcceleration(0, 0.4);

					ctx.pinky.setMoveAndWishDir(Direction.LEFT);
					ctx.pinky.setAbsSpeed(ctx.ghostSpeedAfterColliding);
					ctx.pinky.setVelocity(ctx.pinky.velocity().minus(0, 2.0));
					ctx.pinky.setAcceleration(0, 0.4);
				} else {
					ctx.pacMan.move();
					ctx.pacMan.advanceAnimation();
					ctx.msPac.move();
					ctx.msPac.advanceAnimation();
					ctx.inky.move();
					ctx.inky.advanceAnimation();
					ctx.pinky.move();
					ctx.pinky.advanceAnimation();
					if (ctx.inky.position().y() > ctx.middleY) {
						ctx.inky.setPosition(ctx.inky.position().x(), ctx.middleY);
						ctx.inky.setAcceleration(V2d.NULL);
					}
					if (ctx.pinky.position().y() > ctx.middleY) {
						ctx.pinky.setPosition(ctx.pinky.position().x(), ctx.middleY);
						ctx.pinky.setAcceleration(V2d.NULL);
					}
				}
			}
		},

		IN_HEAVEN {
			@Override
			public void onEnter(IntermissionData ctx) {
				timer.resetSeconds(3);
				timer.start();
				ctx.pacMan.setAbsSpeed(0);
				ctx.pacMan.setMoveDir(Direction.LEFT);
				ctx.pacMan.animation(AnimKeys.PAC_MUNCHING).ifPresent(EntityAnimation::reset);
				ctx.msPac.setAbsSpeed(0);
				ctx.msPac.setMoveDir(Direction.RIGHT);
				ctx.msPac.animation(AnimKeys.PAC_MUNCHING).ifPresent(EntityAnimation::reset);
				ctx.inky.setAbsSpeed(0);
				ctx.inky.hide();
				ctx.pinky.setAbsSpeed(0);
				ctx.pinky.hide();
				ctx.heart.setPosition((ctx.pacMan.position().x() + ctx.msPac.position().x()) / 2,
						ctx.pacMan.position().y() - t(2));
				ctx.heart.show();
			}

			@Override
			public void onUpdate(IntermissionData ctx) {
				if (timer.hasExpired()) {
					ctx.gameController().terminateCurrentState();
				}
			}
		};

		// common fields of each state
		MsPacManIntermission1 intermission;
		TickTimer timer = new TickTimer("Timer-" + name(), GameModel.FPS);

		@Override
		public TickTimer timer() {
			return timer;
		}
	}
}