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

import static de.amr.games.pacman.model.common.world.World.toPx;

import de.amr.games.pacman.controller.common.GameController;
import de.amr.games.pacman.controller.mspacman.MsPacManIntermission3.IntermissionData;
import de.amr.games.pacman.controller.mspacman.MsPacManIntermission3.IntermissionState;
import de.amr.games.pacman.event.GameEvents;
import de.amr.games.pacman.lib.anim.Animated;
import de.amr.games.pacman.lib.fsm.Fsm;
import de.amr.games.pacman.lib.fsm.FsmState;
import de.amr.games.pacman.lib.math.Vector2f;
import de.amr.games.pacman.lib.steering.Direction;
import de.amr.games.pacman.lib.timer.TickTimer;
import de.amr.games.pacman.model.common.GameModel;
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
public class MsPacManIntermission3 extends Fsm<IntermissionState, IntermissionData> {

	private final IntermissionData intermissionData;

	public MsPacManIntermission3(GameController gameController) {
		states = IntermissionState.values();
		for (var state : states) {
			state.intermission = this;
		}
		this.intermissionData = new IntermissionData(gameController);
	}

	@Override
	public IntermissionData context() {
		return intermissionData;
	}

	public static class IntermissionData {
		public final GameController gameController;
		public final int groundY = toPx(24);
		public Clapperboard clapperboard;
		public Pac pacMan;
		public Pac msPacMan;
		public Entity stork;
		public Entity bag;
		public boolean bagOpen;
		public int numBagBounces;

		public IntermissionData(GameController gameController) {
			this.gameController = gameController;
		}
	}

	public enum IntermissionState implements FsmState<IntermissionData> {

		FLAP {
			@Override
			public void onEnter(IntermissionData ctx) {
				timer.restartIndefinitely();
				ctx.clapperboard = new Clapperboard(3, "JUNIOR");
				ctx.clapperboard.setPosition(toPx(3), toPx(10));
				ctx.clapperboard.setVisible(true);
				ctx.pacMan = new Pac("Pac-Man");
				ctx.msPacMan = new Pac("Ms. Pac-Man");
				ctx.stork = new Entity();
				ctx.bag = new Entity();
				ctx.bagOpen = false;
			}

			@Override
			public void onUpdate(IntermissionData ctx) {
				if (timer.atSecond(1)) {
					GameEvents.publishSoundEvent(GameModel.SE_START_INTERMISSION_3);
					ctx.clapperboard.animation().ifPresent(Animated::restart);
				} else if (timer.atSecond(2)) {
					ctx.clapperboard.setVisible(false);
				} else if (timer.atSecond(3)) {
					intermission.changeState(IntermissionState.ACTION);
				}
			}
		},

		ACTION {
			@Override
			public void onEnter(IntermissionData ctx) {
				timer.restartIndefinitely();

				ctx.pacMan.setMoveDir(Direction.RIGHT);
				ctx.pacMan.setPosition(toPx(3), ctx.groundY - 4);
				ctx.pacMan.selectAndResetAnimation(GameModel.AK_PAC_MUNCHING);
				ctx.pacMan.show();

				ctx.msPacMan.setMoveDir(Direction.RIGHT);
				ctx.msPacMan.setPosition(toPx(5), ctx.groundY - 4);
				ctx.msPacMan.selectAndResetAnimation(GameModel.AK_PAC_MUNCHING);
				ctx.msPacMan.show();

				ctx.stork.setPosition(toPx(30), toPx(12));
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
			public void onUpdate(IntermissionData ctx) {
				ctx.stork.move();
				ctx.bag.move();

				// release bag from storks beak?
				if ((int) ctx.stork.position().x() == toPx(20)) {
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
						intermission.changeState(IntermissionState.DONE);
					}
				}
			}
		},

		DONE {
			@Override
			public void onEnter(IntermissionData ctx) {
				timer.resetSeconds(3);
				timer.start();
			}

			@Override
			public void onUpdate(IntermissionData ctx) {
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