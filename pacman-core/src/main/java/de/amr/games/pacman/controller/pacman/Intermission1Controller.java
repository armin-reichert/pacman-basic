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

import static de.amr.games.pacman.model.common.actors.Ghost.RED_GHOST;
import static de.amr.games.pacman.model.common.world.World.t;

import de.amr.games.pacman.controller.common.GameController;
import de.amr.games.pacman.controller.pacman.Intermission1Controller.Context;
import de.amr.games.pacman.controller.pacman.Intermission1Controller.State;
import de.amr.games.pacman.lib.Direction;
import de.amr.games.pacman.lib.TickTimer;
import de.amr.games.pacman.lib.fsm.Fsm;
import de.amr.games.pacman.lib.fsm.FsmState;
import de.amr.games.pacman.model.common.GameModel;
import de.amr.games.pacman.model.common.GameSound;
import de.amr.games.pacman.model.common.actors.Ghost;
import de.amr.games.pacman.model.common.actors.GhostState;
import de.amr.games.pacman.model.common.actors.Pac;

/**
 * First intermission scene: Blinky chases Pac-Man and is then chased by a huge Pac-Man.
 * 
 * @author Armin Reichert
 */
public class Intermission1Controller extends Fsm<State, Context> {

	public final GameController gameController;
	public final Context context = new Context();

	public Intermission1Controller(GameController gameController) {
		super(State.values());
		this.gameController = gameController;
		context.game = gameController.game();
	}

	@Override
	public Context context() {
		return context;
	}

	public void init() {
		restartInInitialState(State.CHASING_PACMAN);
	}

	public static class Context {
		public GameModel game;
		public Ghost blinky;
		public Pac pac;
	}

	public enum State implements FsmState<Context> {

		CHASING_PACMAN {
			@Override
			public void onEnter(Context $) {
				timer.resetIndefinitely();
				timer.start();

				$.pac = new Pac("Pac-Man");
				$.pac.setMoveDir(Direction.LEFT);
				$.pac.setPosition(t(29), t(20));
				$.pac.setAbsSpeed(1.0);
				$.pac.show();

				$.blinky = new Ghost(RED_GHOST, "Blinky");
				$.blinky.state = GhostState.HUNTING_PAC;
				$.blinky.setBothDirs(Direction.LEFT);
				$.blinky.setPosition($.pac.position.plus(t(3), 0));
				$.blinky.setAbsSpeed(1.04);
				$.blinky.show();

				$.game.sounds().ifPresent(snd -> snd.loop(GameSound.INTERMISSION_1, 2));
			}

			@Override
			public void onUpdate(Context $) {
				if (timer.tick() < 8) {
					return;
				}
				if (timer.atSecond(5)) {
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
				timer.resetSeconds(7);
				timer.start();
				$.pac.setMoveDir(Direction.RIGHT);
				$.pac.setPosition(t(-14), t(20));
				$.pac.setAbsSpeed(1);
				$.blinky.state = GhostState.FRIGHTENED;
				$.blinky.setMoveDir(Direction.RIGHT);
				$.blinky.setWishDir(Direction.RIGHT);
				$.blinky.position = $.pac.position.plus(t(13), 0);
				$.blinky.setAbsSpeed(0.75);
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

		Intermission1Controller controller;
		TickTimer timer = new TickTimer("Timer-" + name());

		@Override
		public void setOwner(Fsm<? extends FsmState<Context>, Context> owner) {
			controller = (Intermission1Controller) owner;
		}

		@Override
		public TickTimer timer() {
			return timer;
		}
	}
}