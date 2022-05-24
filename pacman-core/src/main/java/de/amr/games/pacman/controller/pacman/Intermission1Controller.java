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
package de.amr.games.pacman.controller.pacman;

import static de.amr.games.pacman.model.common.world.World.t;

import de.amr.games.pacman.controller.common.GameController;
import de.amr.games.pacman.controller.pacman.Intermission1Controller.Context;
import de.amr.games.pacman.controller.pacman.Intermission1Controller.State;
import de.amr.games.pacman.lib.Direction;
import de.amr.games.pacman.lib.Fsm;
import de.amr.games.pacman.lib.FsmState;
import de.amr.games.pacman.lib.TickTimer;
import de.amr.games.pacman.model.common.GameModel;
import de.amr.games.pacman.model.common.Ghost;
import de.amr.games.pacman.model.common.GhostState;
import de.amr.games.pacman.model.common.Pac;

/**
 * First intermission scene: Blinky chases Pac-Man and is then chased by a huge Pac-Man.
 * 
 * @author Armin Reichert
 */
public class Intermission1Controller extends Fsm<State, Context> {

	public final GameController gameController;
	public final Context context = new Context();
	public Runnable playIntermissionSound;

	public Intermission1Controller(GameController gameController) {
		super(State.values());
		this.gameController = gameController;
	}

	@Override
	public Context getContext() {
		return context;
	}

	public void init() {
		reset(State.CHASING_PACMAN);
	}

	public static class Context {
		public Ghost blinky;
		public Pac pac;
	}

	public enum State implements FsmState<Context> {

		CHASING_PACMAN {
			@Override
			public void onEnter(Context $) {
				timer.setDurationSeconds(5).start();
				if (controller.playIntermissionSound != null) {
					controller.playIntermissionSound.run();
				}

				$.pac = new Pac("Pac-Man");
				$.pac.setMoveDir(Direction.LEFT);
				$.pac.setPosition(t(30), t(20));
				$.pac.setSpeed(1.0);
				$.pac.show();

				$.blinky = new Ghost(GameModel.RED_GHOST, "Blinky");
				$.blinky.state = GhostState.HUNTING_PAC;
				$.blinky.setMoveDir(Direction.LEFT);
				$.blinky.setWishDir(Direction.LEFT);
				$.blinky.position = $.pac.position.plus(t(3) + 0.5, 0);
				$.blinky.setSpeed(1.05);
				$.blinky.show();
			}

			@Override
			public void onUpdate(Context $) {
				if (timer.tick() < 60) {
					return;
				}
				if (timer.hasExpired()) {
					controller.changeState(CHASING_BLINKY);
					return;
				}
				$.pac.move();
				$.blinky.move();
			}
		},

		CHASING_BLINKY {
			@Override
			public void onEnter(Context $) {
				timer.setDurationSeconds(7).start();
				$.pac.setMoveDir(Direction.RIGHT);
				$.pac.setPosition(-t(24), t(20));
				$.pac.setSpeed(1.0);
				$.blinky.state = GhostState.FRIGHTENED;
				$.blinky.setMoveDir(Direction.RIGHT);
				$.blinky.setWishDir(Direction.RIGHT);
				$.blinky.setPosition(-t(1), t(20));
				$.blinky.setSpeed(0.6);
			}

			@Override
			public void onUpdate(Context $) {
				if (timer.hasExpired()) {
					controller.gameController.state().timer().expire();
					return;
				}
				$.pac.move();
				$.blinky.move();
			}
		};

		protected Intermission1Controller controller;
		protected final TickTimer timer = new TickTimer("Timer-" + name());

		@Override
		public void setFsm(Fsm<? extends FsmState<Context>, Context> fsm) {
			controller = (Intermission1Controller) fsm;
		}

		@Override
		public TickTimer timer() {
			return timer;
		}
	}
}