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
import de.amr.games.pacman.controller.pacman.Intermission3Controller.Context;
import de.amr.games.pacman.controller.pacman.Intermission3Controller.State;
import de.amr.games.pacman.lib.Direction;
import de.amr.games.pacman.lib.Fsm;
import de.amr.games.pacman.lib.FsmState;
import de.amr.games.pacman.lib.TickTimer;
import de.amr.games.pacman.model.common.GameModel;
import de.amr.games.pacman.model.common.Ghost;
import de.amr.games.pacman.model.common.GhostState;
import de.amr.games.pacman.model.common.Pac;

/**
 * Third intermission scene: Blinky in shred dress chases Pac-Man, comes back half-naked drawing its dress over the
 * floor.
 * 
 * @author Armin Reichert
 */
public class Intermission3Controller extends Fsm<State, Context> {

	public final GameController gameController;
	public final Context context = new Context();

	public Intermission3Controller(GameController gameController) {
		super(State.values());
		this.gameController = gameController;
	}

	@Override
	public Context getContext() {
		return context;
	}

	public void init() {
		reset(State.CHASING);
	}

	public class Context {
		public Runnable playIntermissionSound = Fsm::nop;
		public Ghost blinky;
		public Pac pac;
	}

	public enum State implements FsmState<Context> {

		CHASING {
			@Override
			public void onEnter(Context $) {
				timer.setDurationIndefinite().start();
				$.playIntermissionSound.run();

				$.pac = new Pac("Pac-Man");
				$.pac.setMoveDir(Direction.LEFT);
				$.pac.setPosition(t(40), t(20));
				$.pac.setSpeed(1.2);
				$.pac.show();

				$.blinky = new Ghost(GameModel.RED_GHOST, "Blinky");
				$.blinky.state = GhostState.HUNTING_PAC;
				$.blinky.setMoveDir(Direction.LEFT);
				$.blinky.setWishDir(Direction.LEFT);
				$.blinky.position = $.pac.position.plus(t(8), 0);
				$.blinky.setSpeed(1.2);
				$.blinky.show();
			}

			@Override
			public void onUpdate(Context $) {
				if ($.blinky.position.x <= -t(15)) {
					$.pac.setSpeed(0);
					$.blinky.setMoveDir(Direction.RIGHT);
					$.blinky.setWishDir(Direction.RIGHT);
					controller.changeState(RETURNING);
					return;
				}
				$.pac.move();
				$.blinky.move();
			}
		},

		RETURNING {
			@Override
			public void onUpdate(Context $) {
				if ($.blinky.position.x > t(53)) {
					controller.gameController.state().timer().expire();
					return;
				}
				$.pac.move();
				$.blinky.move();
			}
		};

		protected Intermission3Controller controller;
		protected final TickTimer timer = new TickTimer("Timer:" + name());

		@Override
		public void setFsm(Fsm<? extends FsmState<Context>, Context> fsm) {
			controller = (Intermission3Controller) fsm;
		}

		@Override
		public TickTimer timer() {
			return timer;
		}
	}
}