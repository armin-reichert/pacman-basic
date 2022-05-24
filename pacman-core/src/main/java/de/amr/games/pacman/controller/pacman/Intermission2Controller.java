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
import de.amr.games.pacman.controller.pacman.Intermission2Controller.Context;
import de.amr.games.pacman.controller.pacman.Intermission2Controller.State;
import de.amr.games.pacman.lib.Direction;
import de.amr.games.pacman.lib.Fsm;
import de.amr.games.pacman.lib.FsmState;
import de.amr.games.pacman.lib.TickTimer;
import de.amr.games.pacman.model.common.GameEntity;
import de.amr.games.pacman.model.common.GameModel;
import de.amr.games.pacman.model.common.Ghost;
import de.amr.games.pacman.model.common.GhostState;
import de.amr.games.pacman.model.common.Pac;

/**
 * Second intermission scene: Blinky pursues Pac but kicks a nail that tears his dress apart.
 * 
 * @author Armin Reichert
 */
public class Intermission2Controller extends Fsm<State, Context> {

	public final GameController gameController;
	public final Context context = new Context();
	public Runnable playIntermissionSound;

	public Intermission2Controller(GameController gameController) {
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

	public int nailDistance() {
		return (int) (context.nail.position.x - context.blinky.position.x);
	}

	public static class Context {
		public Ghost blinky;
		public Pac pac;
		public GameEntity nail;
	}

	public enum State implements FsmState<Intermission2Controller.Context> {

		CHASING {
			@Override
			public void onEnter(Context $) {
				timer.setDurationIndefinite().start();
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
				$.blinky.position = $.pac.position.plus(t(14), 0);
				$.blinky.setSpeed(1.0);
				$.blinky.show();

				$.nail = new GameEntity();
				$.nail.setPosition(t(14), t(20) - 1);
				$.nail.show();
			}

			@Override
			public void onUpdate(Context $) {
				if (controller.nailDistance() == 0) {
					controller.changeState(STRETCHED);
					return;
				}
				$.pac.move();
				$.blinky.move();
			}
		},

		STRETCHED {
			@Override
			public void onUpdate(Context $) {
				int stretching = controller.nailDistance() / 4;
				if (stretching == 3) {
					$.blinky.setSpeed(0);
					$.blinky.setMoveDir(Direction.UP);
					controller.changeState(State.STUCK);
					return;
				}
				$.blinky.setSpeed(0.3 - 0.1 * stretching);
				$.blinky.move();
				$.pac.move();
			}
		},

		STUCK {
			@Override
			public void onUpdate(Context $) {
				if (timer.atSecond(2)) {
					$.blinky.setMoveDir(Direction.RIGHT);
				} else if (timer.atSecond(6)) {
					controller.gameController.state().timer().expire();
					return;
				}
				$.blinky.move();
				$.pac.move();
			}
		};

		protected Intermission2Controller controller;
		protected final TickTimer timer = new TickTimer("Timer-" + name());

		@Override
		public void setFsm(Fsm<? extends FsmState<Context>, Context> fsm) {
			controller = (Intermission2Controller) fsm;
		}

		@Override
		public TickTimer timer() {
			return timer;
		}
	}
}