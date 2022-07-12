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

import static de.amr.games.pacman.model.common.actors.Ghost.PINK_GHOST;
import static de.amr.games.pacman.model.common.world.World.t;

import de.amr.games.pacman.controller.common.GameController;
import de.amr.games.pacman.controller.mspacman.Intermission1Controller.Context;
import de.amr.games.pacman.controller.mspacman.Intermission1Controller.State;
import de.amr.games.pacman.lib.Direction;
import de.amr.games.pacman.lib.TickTimer;
import de.amr.games.pacman.lib.V2d;
import de.amr.games.pacman.lib.animation.EntityAnimation;
import de.amr.games.pacman.lib.fsm.Fsm;
import de.amr.games.pacman.lib.fsm.FsmState;
import de.amr.games.pacman.model.common.GameSound;
import de.amr.games.pacman.model.common.actors.AnimKeys;
import de.amr.games.pacman.model.common.actors.Entity;
import de.amr.games.pacman.model.common.actors.Ghost;
import de.amr.games.pacman.model.common.actors.Pac;
import de.amr.games.pacman.model.mspacman.Flap;

/**
 * Intermission scene 1: "They meet".
 * <p>
 * Pac-Man leads Inky and Ms. Pac-Man leads Pinky. Soon, the two Pac-Men are about to collide, they quickly move
 * upwards, causing Inky and Pinky to collide and vanish. Finally, Pac-Man and Ms. Pac-Man face each other at the top of
 * the screen and a big pink heart appears above them. (Played after round 2)
 * 
 * @author Armin Reichert
 */
public class Intermission1Controller extends Fsm<State, Context> {

	public final Context ctx;

	public Intermission1Controller(GameController gameController) {
		super(State.values());
		for (var state : states) {
			state.controller = this;
		}
		ctx = new Context(gameController);
	}

	@Override
	public Context context() {
		return ctx;
	}

	public static class Context {

		public Context(GameController gameController) {
			this.gameController = gameController;
		}

		public final GameController gameController;
		public final int upperY = t(12);
		public final int middleY = t(18);
		public final int lowerY = t(24);
		public final float pacSpeedChased = 1.125f;
		public final float pacSpeedRising = 0.75f;
		public final float ghostSpeedAfterColliding = 0.3f;
		public final float ghostSpeedChasing = 1.25f;
		public Flap flap;
		public Pac pacMan;
		public Pac msPac;
		public Ghost pinky;
		public Ghost inky;
		public Entity heart;
	}

	public enum State implements FsmState<Context> {

		FLAP {
			@Override
			public void onEnter(Context ctx) {
				timer.resetSeconds(2);
				timer.start();
				ctx.flap = new Flap();
				ctx.flap.number = 1;
				ctx.flap.text = "THEY MEET";
				ctx.flap.setPosition(t(3), t(10));
				ctx.flap.show();

				ctx.pacMan = new Pac("Pac-Man");
				ctx.pacMan.setMoveDir(Direction.RIGHT);
				ctx.pacMan.setPosition(-t(2), ctx.upperY);
				ctx.pacMan.setAnimation(AnimKeys.PAC_MUNCHING);
				ctx.pacMan.show();

				ctx.inky = new Ghost(Ghost.CYAN_GHOST, "Inky");
				ctx.inky.setMoveDir(Direction.RIGHT);
				ctx.inky.setWishDir(Direction.RIGHT);
				ctx.inky.setPosition(ctx.pacMan.getPosition().minus(t(6), 0));
				ctx.inky.setAnimation(AnimKeys.GHOST_COLOR);
				ctx.inky.show();

				ctx.msPac = new Pac("Ms. Pac-Man");
				ctx.msPac.setMoveDir(Direction.LEFT);
				ctx.msPac.setPosition(t(30), ctx.lowerY);
				ctx.msPac.setAnimation(AnimKeys.PAC_MUNCHING);
				ctx.msPac.show();

				ctx.pinky = new Ghost(PINK_GHOST, "Pinky");
				ctx.pinky.setMoveDir(Direction.LEFT);
				ctx.pinky.setWishDir(Direction.LEFT);
				ctx.pinky.setPosition(ctx.msPac.getPosition().plus(t(6), 0));
				ctx.pinky.setAnimation(AnimKeys.GHOST_COLOR);
				ctx.pinky.show();

				ctx.heart = new Entity();
			}

			@Override
			public void onUpdate(Context ctx) {
				if (timer.atSecond(1)) {
					ctx.gameController.sounds().play(GameSound.INTERMISSION_1);
					ctx.flap.animation().ifPresent(EntityAnimation::restart);
				}
				if (timer.hasExpired()) {
					controller.changeState(State.CHASED_BY_GHOSTS);
				}
			}
		},

		CHASED_BY_GHOSTS {
			@Override
			public void onEnter(Context ctx) {
				ctx.flap.hide();
				ctx.pacMan.setAbsSpeed(ctx.pacSpeedChased);
				ctx.msPac.setAbsSpeed(ctx.pacSpeedChased);
				ctx.inky.setAbsSpeed(ctx.ghostSpeedChasing);
				ctx.pinky.setAbsSpeed(ctx.ghostSpeedChasing);
			}

			@Override
			public void onUpdate(Context ctx) {
				if (ctx.inky.getPosition().x() > t(30)) {
					controller.changeState(State.COMING_TOGETHER);
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
			public void onEnter(Context ctx) {
				ctx.msPac.setPosition(t(-3), ctx.middleY);
				ctx.msPac.setMoveDir(Direction.RIGHT);

				ctx.pinky.setPosition(ctx.msPac.getPosition().minus(t(5), 0));
				ctx.pinky.setMoveDir(Direction.RIGHT);
				ctx.pinky.setWishDir(Direction.RIGHT);

				ctx.pacMan.setPosition(t(31), ctx.middleY);
				ctx.pacMan.setMoveDir(Direction.LEFT);

				ctx.inky.setPosition(ctx.pacMan.getPosition().plus(t(5), 0));
				ctx.inky.setMoveDir(Direction.LEFT);
				ctx.inky.setWishDir(Direction.LEFT);
			}

			@Override
			public void onUpdate(Context ctx) {
				// Pac-Man and Ms. Pac-Man reach end position?
				if (ctx.pacMan.moveDir() == Direction.UP && ctx.pacMan.getPosition().y() < ctx.upperY) {
					controller.changeState(State.IN_HEAVEN);
				}
				// Pac-Man and Ms. Pac-Man meet?
				else if (ctx.pacMan.moveDir() == Direction.LEFT
						&& ctx.pacMan.getPosition().x() - ctx.msPac.getPosition().x() < t(2)) {
					ctx.pacMan.setMoveDir(Direction.UP);
					ctx.pacMan.setAbsSpeed(ctx.pacSpeedRising);
					ctx.msPac.setMoveDir(Direction.UP);
					ctx.msPac.setAbsSpeed(ctx.pacSpeedRising);
				}
				// Inky and Pinky collide?
				else if (ctx.inky.moveDir() == Direction.LEFT
						&& ctx.inky.getPosition().x() - ctx.pinky.getPosition().x() < t(2)) {
					ctx.inky.setMoveDir(Direction.RIGHT);
					ctx.inky.setWishDir(Direction.RIGHT);
					ctx.inky.setAbsSpeed(ctx.ghostSpeedAfterColliding);
					ctx.inky.setVelocity(ctx.inky.getVelocity().minus(0, 2.0));
					ctx.inky.setAcceleration(0, 0.4);

					ctx.pinky.setMoveDir(Direction.LEFT);
					ctx.pinky.setWishDir(Direction.LEFT);
					ctx.pinky.setAbsSpeed(ctx.ghostSpeedAfterColliding);
					ctx.pinky.setVelocity(ctx.pinky.getVelocity().minus(0, 2.0));
					ctx.pinky.setAcceleration(0, 0.4);
				} else {
					ctx.pacMan.move();
					ctx.pacMan.advanceAnimation();
					ctx.msPac.move();
					ctx.msPac.advanceAnimation();
					ctx.inky.move();
					ctx.inky.advanceAnimation();
					if (ctx.inky.getPosition().y() > ctx.middleY) {
						ctx.inky.setPosition(ctx.inky.getPosition().x(), ctx.middleY);
						ctx.inky.setAcceleration(V2d.NULL);
					}
					ctx.pinky.move();
					ctx.pinky.advanceAnimation();
					if (ctx.pinky.getPosition().y() > ctx.middleY) {
						ctx.pinky.setPosition(ctx.pinky.getPosition().x(), ctx.middleY);
						ctx.pinky.setAcceleration(V2d.NULL);
					}
				}
			}
		},

		IN_HEAVEN {
			@Override
			public void onEnter(Context ctx) {
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
				ctx.heart.setPosition((ctx.pacMan.getPosition().x() + ctx.msPac.getPosition().x()) / 2,
						ctx.pacMan.getPosition().y() - t(2));
				ctx.heart.show();
			}

			@Override
			public void onUpdate(Context ctx) {
				if (timer.hasExpired()) {
					ctx.gameController.state().timer().expire();
				}
			}
		};

		protected Intermission1Controller controller;
		protected final TickTimer timer = new TickTimer("Timer-" + name());

		@Override
		public TickTimer timer() {
			return timer;
		}
	}
}