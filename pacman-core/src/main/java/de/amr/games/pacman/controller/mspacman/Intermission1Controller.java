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
import de.amr.games.pacman.lib.animation.ThingAnimation;
import de.amr.games.pacman.lib.fsm.Fsm;
import de.amr.games.pacman.lib.fsm.FsmState;
import de.amr.games.pacman.model.common.GameModel;
import de.amr.games.pacman.model.common.GameSound;
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

	public final Context $;

	public Intermission1Controller(GameController gameController) {
		super(State.values());
		this.$ = new Context(gameController);
	}

	@Override
	public Context context() {
		return $;
	}

	public static class Context {
		public final GameModel game;
		public final int upperY = t(12), middleY = t(18), lowerY = t(24);
		public final float pacSpeedChased = 1.125f;
		public final float pacSpeedRising = 0.75f;
		public final float ghostSpeedAfterColliding = 0.3f;
		public final float ghostSpeedChasing = 1.25f;
		public Flap flap;
		public Pac pacMan, msPac;
		public Ghost pinky, inky;
		public Entity heart;

		public final GameController gameController;

		public Context(GameController gameController) {
			this.gameController = gameController;
			this.game = gameController.game();
		}
	}

	public enum State implements FsmState<Context> {

		FLAP {
			@Override
			public void onEnter(Context $) {
				timer.resetSeconds(2);
				timer.start();
				$.flap = new Flap();
				$.flap.number = 1;
				$.flap.text = "THEY MEET";
				$.flap.setPosition(t(3), t(10));
				$.flap.show();

				$.pacMan = new Pac("Pac-Man");
				$.pacMan.setMoveDir(Direction.RIGHT);
				$.pacMan.setPosition(-t(2), $.upperY);
				$.pacMan.show();

				$.inky = new Ghost(Ghost.CYAN_GHOST, "Inky");
				$.inky.setMoveDir(Direction.RIGHT);
				$.inky.setWishDir(Direction.RIGHT);
				$.inky.position = $.pacMan.position.minus(t(6), 0);
				$.inky.show();

				$.msPac = new Pac("Ms. Pac-Man");
				$.msPac.setMoveDir(Direction.LEFT);
				$.msPac.setPosition(t(30), $.lowerY);
				$.msPac.show();

				$.pinky = new Ghost(PINK_GHOST, "Pinky");
				$.pinky.setMoveDir(Direction.LEFT);
				$.pinky.setWishDir(Direction.LEFT);
				$.pinky.position = $.msPac.position.plus(t(6), 0);
				$.pinky.show();

				$.heart = new Entity();
			}

			@Override
			public void onUpdate(Context $) {
				if (timer.atSecond(1)) {
					$.game.sounds().ifPresent(snd -> snd.play(GameSound.INTERMISSION_1));
					if ($.flap.animation != null) {
						$.flap.animation.restart();
					}
				}
				if (timer.hasExpired()) {
					changeState(State.CHASED_BY_GHOSTS);
				}
			}
		},

		CHASED_BY_GHOSTS {
			@Override
			public void onEnter(Context $) {
				$.flap.hide();
				$.pacMan.setAbsSpeed($.pacSpeedChased);
				$.msPac.setAbsSpeed($.pacSpeedChased);
				$.inky.setAbsSpeed($.ghostSpeedChasing);
				$.pinky.setAbsSpeed($.ghostSpeedChasing);
			}

			@Override
			public void onUpdate(Context $) {
				if ($.inky.position.x > t(30)) {
					changeState(State.COMING_TOGETHER);
					return;
				}
				$.inky.move();
				$.pacMan.move();
				$.pinky.move();
				$.msPac.move();
			}
		},

		COMING_TOGETHER {
			@Override
			public void onEnter(Context $) {
				$.msPac.setPosition(t(-3), $.middleY);
				$.msPac.setMoveDir(Direction.RIGHT);

				$.pinky.position = $.msPac.position.minus(t(5), 0);
				$.pinky.setMoveDir(Direction.RIGHT);
				$.pinky.setWishDir(Direction.RIGHT);

				$.pacMan.setPosition(t(31), $.middleY);
				$.pacMan.setMoveDir(Direction.LEFT);

				$.inky.position = $.pacMan.position.plus(t(5), 0);
				$.inky.setMoveDir(Direction.LEFT);
				$.inky.setWishDir(Direction.LEFT);
			}

			@Override
			public void onUpdate(Context $) {
				// Pac-Man and Ms. Pac-Man reach end position?
				if ($.pacMan.moveDir() == Direction.UP && $.pacMan.position.y < $.upperY) {
					changeState(State.IN_HEAVEN);
					return;
				}
				// Pac-Man and Ms. Pac-Man meet?
				else if ($.pacMan.moveDir() == Direction.LEFT && $.pacMan.position.x - $.msPac.position.x < t(2)) {
					$.pacMan.setMoveDir(Direction.UP);
					$.pacMan.setAbsSpeed($.pacSpeedRising);
					$.msPac.setMoveDir(Direction.UP);
					$.msPac.setAbsSpeed($.pacSpeedRising);
				}
				// Inky and Pinky collide?
				else if ($.inky.moveDir() == Direction.LEFT && $.inky.position.x - $.pinky.position.x < t(2)) {
					$.inky.setMoveDir(Direction.RIGHT);
					$.inky.setWishDir(Direction.RIGHT);
					$.inky.setAbsSpeed($.ghostSpeedAfterColliding);
					$.inky.velocity = $.inky.velocity.minus(0, 2.0);
					$.inky.acceleration = new V2d(0, 0.4);

					$.pinky.setMoveDir(Direction.LEFT);
					$.pinky.setWishDir(Direction.LEFT);
					$.pinky.setAbsSpeed($.ghostSpeedAfterColliding);
					$.pinky.velocity = $.pinky.velocity.minus(0, 2.0);
					$.pinky.acceleration = new V2d(0, 0.4);
				} else {
					$.pacMan.move();
					$.msPac.move();
					$.inky.move();
					if ($.inky.position.y > $.middleY) {
						$.inky.setPosition($.inky.position.x, $.middleY);
						$.inky.acceleration = V2d.NULL;
					}
					$.pinky.move();
					if ($.pinky.position.y > $.middleY) {
						$.pinky.setPosition($.pinky.position.x, $.middleY);
						$.pinky.acceleration = V2d.NULL;
					}
				}
			}
		},

		IN_HEAVEN {
			@Override
			public void onEnter(Context $) {
				timer.resetSeconds(3);
				timer.start();
				$.pacMan.setAbsSpeed(0);
				$.pacMan.setMoveDir(Direction.LEFT);
				$.pacMan.animation("pac-anim-munching").ifPresent(ThingAnimation::reset);
				$.msPac.setAbsSpeed(0);
				$.msPac.setMoveDir(Direction.RIGHT);
				$.msPac.animation("pac-anim-munching").ifPresent(ThingAnimation::reset);
				$.inky.setAbsSpeed(0);
				$.inky.hide();
				$.pinky.setAbsSpeed(0);
				$.pinky.hide();
				$.heart.setPosition(($.pacMan.position.x + $.msPac.position.x) / 2, $.pacMan.position.y - t(2));
				$.heart.show();
			}

			@Override
			public void onUpdate(Context $) {
				if (timer.hasExpired()) {
					$.gameController.state().timer().expire();
				}
			}
		};

		protected Fsm<FsmState<Context>, Context> controller;
		protected final TickTimer timer = new TickTimer("Timer-" + name());

		@Override
		public void setOwner(Fsm<FsmState<Context>, Context> fsm) {
			controller = fsm;
		}

		@Override
		public Fsm<FsmState<Context>, Context> getOwner() {
			return controller;
		}

		@Override
		public TickTimer timer() {
			return timer;
		}
	}
}