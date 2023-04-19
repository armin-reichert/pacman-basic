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

import static de.amr.games.pacman.model.common.actors.Ghost.ID_PINK_GHOST;
import static de.amr.games.pacman.model.common.world.World.toPx;

import de.amr.games.pacman.controller.common.GameController;
import de.amr.games.pacman.controller.mspacman.MsPacManIntermission1.IntermissionData;
import de.amr.games.pacman.controller.mspacman.MsPacManIntermission1.IntermissionState;
import de.amr.games.pacman.event.GameEvents;
import de.amr.games.pacman.lib.anim.Animated;
import de.amr.games.pacman.lib.fsm.Fsm;
import de.amr.games.pacman.lib.fsm.FsmState;
import de.amr.games.pacman.lib.math.Vector2f;
import de.amr.games.pacman.lib.steering.Direction;
import de.amr.games.pacman.lib.timer.TickTimer;
import de.amr.games.pacman.model.common.GameModel;
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

	public static class IntermissionData {
		public final GameController gameController;
		public final int upperY = toPx(12);
		public final int middleY = toPx(18);
		public final int lowerY = toPx(24);
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
			this.gameController = gameController;
		}
	}

	public enum IntermissionState implements FsmState<IntermissionData> {

		FLAP {
			@Override
			public void onEnter(IntermissionData ctx) {
				timer.resetSeconds(2);
				timer.start();
				ctx.clapperboard = new Clapperboard(1, "THEY MEET");
				ctx.clapperboard.setPosition(toPx(3), toPx(10));
				ctx.clapperboard.setVisible(true);

				ctx.pacMan = new Pac("Pac-Man");
				ctx.pacMan.setMoveDir(Direction.RIGHT);
				ctx.pacMan.setPosition(-toPx(2), ctx.upperY);
				ctx.pacMan.selectAndRunAnimation(GameModel.AK_PAC_MUNCHING);
				ctx.pacMan.show();

				ctx.inky = new Ghost(Ghost.ID_CYAN_GHOST, "Inky");
				ctx.inky.setMoveAndWishDir(Direction.RIGHT);
				ctx.inky.setPosition(ctx.pacMan.position().minus(toPx(6), 0));
				ctx.inky.selectAndRunAnimation(GameModel.AK_GHOST_COLOR);
				ctx.inky.show();

				ctx.msPac = new Pac("Ms. Pac-Man");
				ctx.msPac.setMoveDir(Direction.LEFT);
				ctx.msPac.setPosition(toPx(30), ctx.lowerY);
				ctx.msPac.selectAndRunAnimation(GameModel.AK_PAC_MUNCHING);
				ctx.msPac.show();

				ctx.pinky = new Ghost(ID_PINK_GHOST, "Pinky");
				ctx.pinky.setMoveAndWishDir(Direction.LEFT);
				ctx.pinky.setPosition(ctx.msPac.position().plus(toPx(6), 0));
				ctx.pinky.selectAndRunAnimation(GameModel.AK_GHOST_COLOR);
				ctx.pinky.show();

				ctx.heart = new Entity();
			}

			@Override
			public void onUpdate(IntermissionData ctx) {
				if (timer.atSecond(1)) {
					GameEvents.publishSoundEvent(GameModel.SE_START_INTERMISSION_1);
					ctx.clapperboard.animation().ifPresent(Animated::restart);
				}
				if (timer.hasExpired()) {
					ctx.clapperboard.setVisible(false);
					intermission.changeState(IntermissionState.CHASED_BY_GHOSTS);
				}
			}
		},

		CHASED_BY_GHOSTS {
			@Override
			public void onEnter(IntermissionData ctx) {
				ctx.pacMan.setPixelSpeed(ctx.pacSpeedChased);
				ctx.msPac.setPixelSpeed(ctx.pacSpeedChased);
				ctx.inky.setPixelSpeed(ctx.ghostSpeedChasing);
				ctx.pinky.setPixelSpeed(ctx.ghostSpeedChasing);
			}

			@Override
			public void onUpdate(IntermissionData ctx) {
				if (ctx.inky.position().x() > toPx(30)) {
					intermission.changeState(IntermissionState.COMING_TOGETHER);
					return;
				}
				ctx.inky.move();
				ctx.inky.animate();
				ctx.pacMan.move();
				ctx.pacMan.animate();
				ctx.pinky.move();
				ctx.pinky.animate();
				ctx.msPac.move();
				ctx.msPac.animate();
			}
		},

		COMING_TOGETHER {
			@Override
			public void onEnter(IntermissionData ctx) {
				ctx.msPac.setPosition(toPx(-3), ctx.middleY);
				ctx.msPac.setMoveDir(Direction.RIGHT);

				ctx.pinky.setPosition(ctx.msPac.position().minus(toPx(5), 0));
				ctx.pinky.setMoveAndWishDir(Direction.RIGHT);

				ctx.pacMan.setPosition(toPx(31), ctx.middleY);
				ctx.pacMan.setMoveDir(Direction.LEFT);

				ctx.inky.setPosition(ctx.pacMan.position().plus(toPx(5), 0));
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
						&& ctx.pacMan.position().x() - ctx.msPac.position().x() < toPx(2)) {
					ctx.pacMan.setMoveDir(Direction.UP);
					ctx.pacMan.setPixelSpeed(ctx.pacSpeedRising);
					ctx.msPac.setMoveDir(Direction.UP);
					ctx.msPac.setPixelSpeed(ctx.pacSpeedRising);
				}
				// Inky and Pinky collide?
				else if (ctx.inky.moveDir() == Direction.LEFT && ctx.inky.position().x() - ctx.pinky.position().x() < toPx(2)) {
					ctx.inky.setMoveAndWishDir(Direction.RIGHT);
					ctx.inky.setPixelSpeed(ctx.ghostSpeedAfterColliding);
					ctx.inky.setVelocity(ctx.inky.velocity().minus(0, 2.0f));
					ctx.inky.setAcceleration(0, 0.4f);

					ctx.pinky.setMoveAndWishDir(Direction.LEFT);
					ctx.pinky.setPixelSpeed(ctx.ghostSpeedAfterColliding);
					ctx.pinky.setVelocity(ctx.pinky.velocity().minus(0, 2.0f));
					ctx.pinky.setAcceleration(0, 0.4f);
				} else {
					ctx.pacMan.move();
					ctx.pacMan.animate();
					ctx.msPac.move();
					ctx.msPac.animate();
					ctx.inky.move();
					ctx.inky.animate();
					ctx.pinky.move();
					ctx.pinky.animate();
					if (ctx.inky.position().y() > ctx.middleY) {
						ctx.inky.setPosition(ctx.inky.position().x(), ctx.middleY);
						ctx.inky.setAcceleration(Vector2f.ZERO);
					}
					if (ctx.pinky.position().y() > ctx.middleY) {
						ctx.pinky.setPosition(ctx.pinky.position().x(), ctx.middleY);
						ctx.pinky.setAcceleration(Vector2f.ZERO);
					}
				}
			}
		},

		IN_HEAVEN {
			@Override
			public void onEnter(IntermissionData ctx) {
				timer.resetSeconds(3);
				timer.start();
				ctx.pacMan.setPixelSpeed(0);
				ctx.pacMan.setMoveDir(Direction.LEFT);
				ctx.pacMan.animation(GameModel.AK_PAC_MUNCHING).ifPresent(Animated::reset);
				ctx.msPac.setPixelSpeed(0);
				ctx.msPac.setMoveDir(Direction.RIGHT);
				ctx.msPac.animation(GameModel.AK_PAC_MUNCHING).ifPresent(Animated::reset);
				ctx.inky.setPixelSpeed(0);
				ctx.inky.hide();
				ctx.pinky.setPixelSpeed(0);
				ctx.pinky.hide();
				ctx.heart.setPosition((ctx.pacMan.position().x() + ctx.msPac.position().x()) / 2,
						ctx.pacMan.position().y() - toPx(2));
				ctx.heart.show();
			}

			@Override
			public void onUpdate(IntermissionData ctx) {
				if (timer.hasExpired()) {
					ctx.gameController.terminateCurrentState();
				}
			}
		};

		// common fields of each state
		MsPacManIntermission1 intermission;
		TickTimer timer = new TickTimer("Timer-" + name());

		@Override
		public TickTimer timer() {
			return timer;
		}
	}
}