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

import static de.amr.games.pacman.model.common.world.World.t;

import de.amr.games.pacman.controller.common.GameController;
import de.amr.games.pacman.controller.mspacman.Intermission3Controller.Context;
import de.amr.games.pacman.controller.mspacman.Intermission3Controller.State;
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
import de.amr.games.pacman.model.common.actors.Pac;
import de.amr.games.pacman.model.mspacman.Clapperboard;

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
public class Intermission3Controller extends Fsm<State, Context> {

	public static class Context {
		public final GameController gameController;
		public final GameModel game;
		public final int groundY = t(24);
		public Clapperboard clapperboard;
		public Pac pacMan;
		public Pac msPacMan;
		public Entity stork;
		public Entity bag;
		public boolean bagOpen;
		public int numBagBounces;

		public Context(GameController gameController) {
			this.gameController = gameController;
			this.game = gameController.game();
		}
	}

	public enum State implements FsmState<Context> {

		FLAP {
			@Override
			public void onEnter(Context ctx) {
				timer.resetIndefinitely();
				timer.start();
				ctx.clapperboard = new Clapperboard(3, "JUNIOR");
				ctx.clapperboard.setPosition(t(3), t(10));
				ctx.clapperboard.show();
				ctx.pacMan = new Pac("Pac-Man");
				ctx.msPacMan = new Pac("Ms. Pac-Man");
				ctx.stork = new Entity();
				ctx.bag = new Entity();
				ctx.bagOpen = false;
			}

			@Override
			public void onUpdate(Context ctx) {
				if (timer.atSecond(1)) {
					ctx.gameController.sounds().play(GameSound.INTERMISSION_3);
					ctx.clapperboard.animation().ifPresent(EntityAnimation::restart);
				} else if (timer.atSecond(2)) {
					ctx.clapperboard.hide();
				} else if (timer.atSecond(3)) {
					controller.changeState(State.ACTION);
				}
			}
		},

		ACTION {
			@Override
			public void onEnter(Context ctx) {
				timer.resetIndefinitely();
				timer.start();

				ctx.pacMan.setMoveDir(Direction.RIGHT);
				ctx.pacMan.setPosition(t(3), ctx.groundY - 4);
				ctx.pacMan.selectAndRunAnimation(AnimKeys.PAC_MUNCHING);
				ctx.pacMan.show();
				ctx.pacMan.animation(AnimKeys.PAC_MUNCHING).ifPresent(EntityAnimation::reset);

				ctx.msPacMan.setMoveDir(Direction.RIGHT);
				ctx.msPacMan.setPosition(t(5), ctx.groundY - 4);
				ctx.msPacMan.selectAndRunAnimation(AnimKeys.PAC_MUNCHING);
				ctx.msPacMan.show();
				ctx.msPacMan.animation(AnimKeys.PAC_MUNCHING).ifPresent(EntityAnimation::reset);

				ctx.stork.setPosition(t(30), t(12));
				ctx.stork.setVelocity(-0.8, 0);
				ctx.stork.show();

				ctx.bag.setPosition(ctx.stork.getPosition().plus(-14, 3));
				ctx.bag.setVelocity(ctx.stork.getVelocity());
				ctx.bag.setAcceleration(V2d.NULL);
				ctx.bag.show();
				ctx.bagOpen = false;
				ctx.numBagBounces = 0;
			}

			@Override
			public void onUpdate(Context ctx) {
				ctx.stork.move();
				ctx.bag.move();

				// release bag from storks beak?
				if ((int) ctx.stork.getPosition().x() == t(20)) {
					ctx.bag.setAcceleration(0, 0.04);
					ctx.stork.setVelocity(-1, 0);
				}

				// (closed) bag reaches ground for first time?
				if (!ctx.bagOpen && ctx.bag.getPosition().y() > ctx.groundY) {
					++ctx.numBagBounces;
					if (ctx.numBagBounces < 3) {
						ctx.bag.setVelocity(-0.2f, -1f / ctx.numBagBounces);
						ctx.bag.setPosition(ctx.bag.getPosition().x(), ctx.groundY);
					} else {
						ctx.bagOpen = true;
						ctx.bag.setVelocity(V2d.NULL);
						controller.changeState(State.DONE);
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

		protected Intermission3Controller controller;
		protected final TickTimer timer = new TickTimer("Timer-" + name(), GameModel.FPS);

		@Override
		public TickTimer timer() {
			return timer;
		}
	}

	public final Context ctx;

	public Intermission3Controller(GameController gameController) {
		states = State.values();
		for (var state : states) {
			state.controller = this;
		}
		this.ctx = new Context(gameController);
	}

	@Override
	public Context context() {
		return ctx;
	}
}