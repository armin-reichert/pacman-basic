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
import de.amr.games.pacman.model.actors.Pac;

/**
 * Intermission scene 3: "Junior".
 * 
 * <p>
 * Pac-Man and Ms. Pac-Man gradually wait for a stork, who flies overhead with a little blue bundle. The stork drops the
 * bundle, which falls to the ground in front of Pac-Man and Ms. Pac-Man, and finally opens up to reveal a tiny Pac-Man.
 * (Played after rounds 9, 13, and 17)
 * 
 * @author Armin Reichert
 */
public class MsPacManIntermission3 extends Fsm<MsPacManIntermission3.State, MsPacManIntermission3.Context> {

	private final Context intermissionData;

	public MsPacManIntermission3(GameController gameController) {
		super(State.values());
		for (var state : states) {
			state.intermission = this;
		}
		this.intermissionData = new Context(gameController);
	}

	@Override
	public Context context() {
		return intermissionData;
	}

	public static class Context {
		public GameController gameController;
		public int groundY = TS * (24);
		public Clapperboard clapperboard;
		public Pac pacMan;
		public Pac msPacMan;
		public Entity stork;
		public Entity bag;
		public boolean bagOpen;
		public int numBagBounces;

		public Context(GameController gameController) {
			this.gameController = gameController;
		}
	}

	public enum State implements FsmState<Context> {

		FLAP {
			@Override
			public void onEnter(Context ctx) {
				timer.restartIndefinitely();
				ctx.clapperboard = new Clapperboard("3", "JUNIOR");
				ctx.clapperboard.setPosition(TS * (3), TS * (10));
				ctx.clapperboard.setVisible(true);
				ctx.pacMan = new Pac("Pac-Man");
				ctx.msPacMan = new Pac("Ms. Pac-Man");
				ctx.stork = new Entity();
				ctx.bag = new Entity();
				ctx.bagOpen = false;
			}

			@Override
			public void onUpdate(Context ctx) {
				if (timer.atSecond(1)) {
					GameEvents.publishSoundEvent(GameModel.SE_START_INTERMISSION_3);
					ctx.clapperboard.animation().ifPresent(Animated::restart);
				} else if (timer.atSecond(2)) {
					ctx.clapperboard.setVisible(false);
				} else if (timer.atSecond(3)) {
					intermission.changeState(State.ACTION);
				}
			}
		},

		ACTION {
			@Override
			public void onEnter(Context ctx) {
				timer.restartIndefinitely();

				ctx.pacMan.setMoveDir(Direction.RIGHT);
				ctx.pacMan.setPosition(TS * (3), ctx.groundY - 4);
				ctx.pacMan.selectAndResetAnimation(GameModel.AK_PAC_MUNCHING);
				ctx.pacMan.show();

				ctx.msPacMan.setMoveDir(Direction.RIGHT);
				ctx.msPacMan.setPosition(TS * (5), ctx.groundY - 4);
				ctx.msPacMan.selectAndResetAnimation(GameModel.AK_PAC_MUNCHING);
				ctx.msPacMan.show();

				ctx.stork.setPosition(TS * (30), TS * (12));
				ctx.stork.setVelocity(-0.8f, 0);
				ctx.stork.show();

				ctx.bag.setPosition(ctx.stork.position().plus(-14, 3));
				ctx.bag.setVelocity(ctx.stork.velocity());
				ctx.bag.setAcceleration(Vector2f.ZERO);
				ctx.bag.show();
				ctx.bagOpen = false;
				ctx.numBagBounces = 0;
			}

			@Override
			public void onUpdate(Context ctx) {
				ctx.stork.move();
				ctx.bag.move();

				// release bag from storks beak?
				if ((int) ctx.stork.position().x() == TS * (20)) {
					ctx.bag.setAcceleration(0, 0.04f);
					ctx.stork.setVelocity(-1, 0);
				}

				// (closed) bag reaches ground for first time?
				if (!ctx.bagOpen && ctx.bag.position().y() > ctx.groundY) {
					++ctx.numBagBounces;
					if (ctx.numBagBounces < 3) {
						ctx.bag.setVelocity(-0.2f, -1f / ctx.numBagBounces);
						ctx.bag.setPosition(ctx.bag.position().x(), ctx.groundY);
					} else {
						ctx.bagOpen = true;
						ctx.bag.setVelocity(Vector2f.ZERO);
						intermission.changeState(State.DONE);
					}
				}
			}
		},

		DONE {
			@Override
			public void onEnter(Context ctx) {
				timer.resetSeconds(3);
				timer.start();
			}

			@Override
			public void onUpdate(Context ctx) {
				ctx.stork.move();
				if (timer.hasExpired()) {
					ctx.gameController.terminateCurrentState();
				}
			}
		};

		protected MsPacManIntermission3 intermission;
		protected final TickTimer timer = new TickTimer("Timer-" + name());

		@Override
		public TickTimer timer() {
			return timer;
		}
	}
}