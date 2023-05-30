/*
Copyright (c) 2021-2023 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.games.pacman.controller;

import static de.amr.games.pacman.lib.Globals.TS;

import de.amr.games.pacman.event.GameEvents;
import de.amr.games.pacman.lib.anim.Animated;
import de.amr.games.pacman.lib.fsm.Fsm;
import de.amr.games.pacman.lib.fsm.FsmState;
import de.amr.games.pacman.lib.math.Vector2f;
import de.amr.games.pacman.lib.steering.Direction;
import de.amr.games.pacman.lib.timer.TickTimer;
import de.amr.games.pacman.model.GameModel;
import de.amr.games.pacman.model.actors.Clapperboard;
import de.amr.games.pacman.model.actors.Entity;
import de.amr.games.pacman.model.actors.Ghost;
import de.amr.games.pacman.model.actors.GhostAnimations;
import de.amr.games.pacman.model.actors.Pac;
import de.amr.games.pacman.model.actors.PacAnimations;

/**
 * Intermission scene 1: "They meet".
 * <p>
 * Pac-Man leads Inky and Ms. Pac-Man leads Pinky. Soon, the two Pac-Men are about to collide, they quickly move
 * upwards, causing Inky and Pinky to collide and vanish. Finally, Pac-Man and Ms. Pac-Man face each other at the top of
 * the screen and a big pink heart appears above them. (Played after round 2)
 * 
 * @author Armin Reichert
 */
public class MsPacManIntermission1 extends Fsm<MsPacManIntermission1.State, MsPacManIntermission1.Context> {

	private final Context context;

	public MsPacManIntermission1(GameController gameController) {
		super(State.values());
		for (var state : states) {
			state.intermission = this;
		}
		context = new Context(gameController);
	}

	@Override
	public Context context() {
		return context;
	}

	public static class Context {
		public GameController gameController;
		public int upperY = TS * (12);
		public int middleY = TS * (18);
		public int lowerY = TS * (24);
		public float pacSpeedChased = 1.125f;
		public float pacSpeedRising = 0.75f;
		public float ghostSpeedAfterColliding = 0.3f;
		public float ghostSpeedChasing = 1.25f;
		public Clapperboard clapperboard;
		public Pac pacMan;
		public Pac msPac;
		public Ghost pinky;
		public Ghost inky;
		public Entity heart;

		public Context(GameController gameController) {
			this.gameController = gameController;
		}
	}

	public enum State implements FsmState<MsPacManIntermission1.Context> {

		FLAP {
			@Override
			public void onEnter(Context ctx) {
				timer.resetSeconds(2);
				timer.start();
				ctx.clapperboard = new Clapperboard("1", "THEY MEET");
				ctx.clapperboard.setPosition(TS * (3), TS * (10));
				ctx.clapperboard.setVisible(true);

				ctx.pacMan = new Pac("Pac-Man");
				ctx.pacMan.setMoveDir(Direction.RIGHT);
				ctx.pacMan.setPosition(-TS * (2), ctx.upperY);
				ctx.pacMan.selectAnimation(PacAnimations.PAC_MUNCHING);
				ctx.pacMan.show();

				ctx.inky = new Ghost(GameModel.CYAN_GHOST, "Inky");
				ctx.inky.setMoveAndWishDir(Direction.RIGHT);
				ctx.inky.setPosition(ctx.pacMan.position().minus(TS * (6), 0));
				ctx.inky.selectAnimation(GhostAnimations.GHOST_NORMAL);
				ctx.inky.show();

				ctx.msPac = new Pac("Ms. Pac-Man");
				ctx.msPac.setMoveDir(Direction.LEFT);
				ctx.msPac.setPosition(TS * (30), ctx.lowerY);
				ctx.msPac.selectAnimation(PacAnimations.PAC_MUNCHING);
				ctx.msPac.show();

				ctx.pinky = new Ghost(GameModel.PINK_GHOST, "Pinky");
				ctx.pinky.setMoveAndWishDir(Direction.LEFT);
				ctx.pinky.setPosition(ctx.msPac.position().plus(TS * (6), 0));
				ctx.pinky.selectAnimation(GhostAnimations.GHOST_NORMAL);
				ctx.pinky.show();

				ctx.heart = new Entity();
			}

			@Override
			public void onUpdate(Context ctx) {
				if (timer.atSecond(1)) {
					GameEvents.publishSoundEvent(GameModel.SE_START_INTERMISSION_1);
					ctx.clapperboard.animation().ifPresent(Animated::restart);
				}
				if (timer.hasExpired()) {
					ctx.clapperboard.setVisible(false);
					intermission.changeState(State.CHASED_BY_GHOSTS);
				}
			}
		},

		CHASED_BY_GHOSTS {
			@Override
			public void onEnter(Context ctx) {
				ctx.pacMan.setPixelSpeed(ctx.pacSpeedChased);
				ctx.msPac.setPixelSpeed(ctx.pacSpeedChased);
				ctx.inky.setPixelSpeed(ctx.ghostSpeedChasing);
				ctx.pinky.setPixelSpeed(ctx.ghostSpeedChasing);
			}

			@Override
			public void onUpdate(Context ctx) {
				if (ctx.inky.position().x() > TS * (30)) {
					intermission.changeState(State.COMING_TOGETHER);
					return;
				}
				ctx.pacMan.move();
				ctx.msPac.move();
				ctx.inky.move();
				ctx.pinky.move();
			}
		},

		COMING_TOGETHER {
			@Override
			public void onEnter(Context ctx) {
				ctx.msPac.setPosition(TS * (-3), ctx.middleY);
				ctx.msPac.setMoveDir(Direction.RIGHT);

				ctx.pinky.setPosition(ctx.msPac.position().minus(TS * (5), 0));
				ctx.pinky.setMoveAndWishDir(Direction.RIGHT);

				ctx.pacMan.setPosition(TS * (31), ctx.middleY);
				ctx.pacMan.setMoveDir(Direction.LEFT);

				ctx.inky.setPosition(ctx.pacMan.position().plus(TS * (5), 0));
				ctx.inky.setMoveAndWishDir(Direction.LEFT);
			}

			@Override
			public void onUpdate(Context ctx) {
				// Pac-Man and Ms. Pac-Man reach end position?
				if (ctx.pacMan.moveDir() == Direction.UP && ctx.pacMan.position().y() < ctx.upperY) {
					intermission.changeState(State.IN_HEAVEN);
				}
				// Pac-Man and Ms. Pac-Man meet?
				else if (ctx.pacMan.moveDir() == Direction.LEFT
						&& ctx.pacMan.position().x() - ctx.msPac.position().x() < TS * (2)) {
					ctx.pacMan.setMoveDir(Direction.UP);
					ctx.pacMan.setPixelSpeed(ctx.pacSpeedRising);
					ctx.msPac.setMoveDir(Direction.UP);
					ctx.msPac.setPixelSpeed(ctx.pacSpeedRising);
				}
				// Inky and Pinky collide?
				else if (ctx.inky.moveDir() == Direction.LEFT
						&& ctx.inky.position().x() - ctx.pinky.position().x() < TS * (2)) {
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
					ctx.msPac.move();
					ctx.inky.move();
					ctx.pinky.move();
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
			public void onEnter(Context ctx) {
				timer.resetSeconds(3);
				timer.start();
				ctx.pacMan.setPixelSpeed(0);
				ctx.pacMan.setMoveDir(Direction.LEFT);
				ctx.pacMan.stopAnimation();
				ctx.pacMan.animations().ifPresent(PacAnimations::resetSelected);
				ctx.msPac.setPixelSpeed(0);
				ctx.msPac.setMoveDir(Direction.RIGHT);
				ctx.msPac.stopAnimation();
				ctx.msPac.animations().ifPresent(PacAnimations::resetSelected);
				ctx.inky.setPixelSpeed(0);
				ctx.inky.hide();
				ctx.pinky.setPixelSpeed(0);
				ctx.pinky.hide();
				ctx.heart.setPosition((ctx.pacMan.position().x() + ctx.msPac.position().x()) / 2,
						ctx.pacMan.position().y() - TS * (2));
				ctx.heart.show();
			}

			@Override
			public void onUpdate(Context ctx) {
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