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
import de.amr.games.pacman.lib.fsm.Fsm;
import de.amr.games.pacman.lib.fsm.FsmState;
import de.amr.games.pacman.model.common.GameModel;
import de.amr.games.pacman.model.common.GameSound;
import de.amr.games.pacman.model.common.actors.Entity;
import de.amr.games.pacman.model.common.actors.Pac;
import de.amr.games.pacman.model.mspacman.Flap;

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

	public final GameController gameController;
	public final Context context;

	public Intermission3Controller(GameController gameController) {
		super(State.values());
		this.gameController = gameController;
		this.context = new Context(gameController.game());
	}

	@Override
	public Context context() {
		return context;
	}

	public static class Context {
		public final GameModel game;
		public final int groundY = t(24);
		public Flap flap;
		public Pac pacMan;
		public Pac msPacMan;
		public Entity stork;
		public Entity bag;
		public boolean bagOpen;
		public int numBagBounces;

		public Context(GameModel game) {
			this.game = game;
		}
	}

	public enum State implements FsmState<Context> {

		FLAP {
			@Override
			public void onEnter(Context $) {
				timer.setIndefinite();
				timer.start();
				$.game.sounds().ifPresent(snd -> snd.play(GameSound.INTERMISSION_3));
				$.flap = new Flap();
				$.flap.number = 3;
				$.flap.text = "JUNIOR";
				$.flap.setPosition(t(3), t(10));
				$.flap.show();
				$.pacMan = new Pac("Pac-Man");
				$.msPacMan = new Pac("Ms. Pac-Man");
				$.stork = new Entity();
				$.bag = new Entity();
				$.bagOpen = false;
			}

			@Override
			public void onUpdate(Context $) {
				if (timer.atSecond(1)) {
					if ($.flap.animation != null) {
						$.flap.animation.restart();
					}
				} else if (timer.atSecond(2)) {
					$.flap.hide();
				} else if (timer.atSecond(3)) {
					controller.changeState(State.ACTION);
				}
			}
		},

		ACTION {
			@Override
			public void onEnter(Context $) {
				timer.setIndefinite();
				timer.start();

				$.pacMan.setMoveDir(Direction.RIGHT);
				$.pacMan.setPosition(t(3), $.groundY - 4);
				$.pacMan.show();

				$.msPacMan.setMoveDir(Direction.RIGHT);
				$.msPacMan.setPosition(t(5), $.groundY - 4);
				$.msPacMan.show();

				$.stork.setPosition(t(30), t(12));
				$.stork.setVelocity(-0.8, 0);
				$.stork.show();

				$.bag.position = $.stork.position.plus(-14, 3);
				$.bag.velocity = $.stork.velocity;
				$.bag.acceleration = V2d.NULL;
				$.bag.show();
				$.bagOpen = false;
				$.numBagBounces = 0;
			}

			@Override
			public void onUpdate(Context $) {
				$.stork.move();
				$.bag.move();

				// release bag from storks beak?
				if ((int) $.stork.position.x == t(20)) {
					$.bag.acceleration = new V2d(0, 0.04);
					$.stork.setVelocity(-1, 0);
				}

				// (closed) bag reaches ground for first time?
				if (!$.bagOpen && $.bag.position.y > $.groundY) {
					++$.numBagBounces;
					if ($.numBagBounces < 3) {
						$.bag.setVelocity(-0.2f, -1f / $.numBagBounces);
						$.bag.setPosition($.bag.position.x, $.groundY);
					} else {
						$.bagOpen = true;
						$.bag.velocity = V2d.NULL;
						controller.changeState(State.DONE);
					}
				}
			}
		},

		DONE {
			@Override
			public void onEnter(Context $) {
				timer.setSeconds(3);
				timer.start();
			}

			@Override
			public void onUpdate(Context $) {
				$.stork.move();
				if (timer.hasExpired()) {
					controller.gameController.state().timer().expire();
				}
			}
		};

		protected Intermission3Controller controller;
		protected final TickTimer timer = new TickTimer("Timer-" + name());

		@Override
		public void setOwner(Fsm<? extends FsmState<Context>, Context> fsm) {
			controller = (Intermission3Controller) fsm;
		}

		@Override
		public TickTimer timer() {
			return timer;
		}
	}
}