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

import static de.amr.games.pacman.lib.TickTimer.sec_to_ticks;
import static de.amr.games.pacman.model.common.Ghost.CYAN_GHOST;
import static de.amr.games.pacman.model.common.Ghost.ORANGE_GHOST;
import static de.amr.games.pacman.model.common.Ghost.PINK_GHOST;
import static de.amr.games.pacman.model.common.Ghost.RED_GHOST;
import static de.amr.games.pacman.model.common.world.World.t;

import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

import de.amr.games.pacman.controller.common.GameController;
import de.amr.games.pacman.controller.pacman.IntroController.Context;
import de.amr.games.pacman.controller.pacman.IntroController.State;
import de.amr.games.pacman.lib.Direction;
import de.amr.games.pacman.lib.Fsm;
import de.amr.games.pacman.lib.FsmState;
import de.amr.games.pacman.lib.TickTimer;
import de.amr.games.pacman.lib.TimedSeq;
import de.amr.games.pacman.model.common.Ghost;
import de.amr.games.pacman.model.common.GhostState;
import de.amr.games.pacman.model.common.Pac;

/**
 * Intro scene of the PacMan game.
 * <p>
 * The ghosts are presented one after another, then Pac-Man is chased by the ghosts, turns the card and hunts the ghosts
 * himself.
 * 
 * @author Armin Reichert
 */
public class IntroController extends Fsm<State, Context> {

	public final GameController gameController;
	public final Context context = new Context();

	public IntroController(GameController gameController) {
		super(State.values());
		this.gameController = gameController;
		logging = true;
	}

	@Override
	public Context getContext() {
		return context;
	}

	public static class Context {
		public final TimedSeq<Boolean> fastBlinking = TimedSeq.pulse().frameDuration(10);
		public final TimedSeq<Boolean> slowBlinking = TimedSeq.pulse().frameDuration(30);
		public final String nicknames[] = { "Blinky", "Pinky", "Inky", "Clyde" };
		public final String characters[] = { "SHADOW", "SPEEDY", "BASHFUL", "POKEY" };
		public final boolean[] pictureVisible = { false, false, false, false };
		public final boolean[] nicknameVisible = { false, false, false, false };
		public final boolean[] characterVisible = { false, false, false, false };
		public Pac pacMan;
		public Ghost[] ghosts;
		public int ghostIndex;
		public long ghostKilledTime;
	}

	public enum State implements FsmState<Context> {

		BEGIN {
			@Override
			public void onEnter(Context $) {
				for (int id = 0; id < 4; ++id) {
					$.pictureVisible[id] = false;
					$.nicknameVisible[id] = false;
					$.characterVisible[id] = false;
				}
				$.pacMan = new Pac("Pac-Man");
				$.ghosts = new Ghost[] { //
						new Ghost(RED_GHOST, "Blinky"), //
						new Ghost(PINK_GHOST, "Pinky"), //
						new Ghost(CYAN_GHOST, "Inky"), //
						new Ghost(ORANGE_GHOST, "Clyde"), //
				};
			}

			@Override
			public void onUpdate(Context $) {
				if (timer.atSecond(1)) {
					$.ghostIndex = 0;
					$.pictureVisible[0] = true;
					controller.changeState(State.PRESENTING_GHOSTS);
				}
			}
		},

		PRESENTING_GHOSTS {
			@Override
			public void onUpdate(Context $) {
				if (timer.atSecond(1.0)) {
					$.characterVisible[$.ghostIndex] = true;
				} else if (timer.atSecond(1.5)) {
					$.nicknameVisible[$.ghostIndex] = true;
				} else if (timer.atSecond(2.0)) {
					if ($.ghostIndex < 3) {
						$.ghostIndex++;
						$.pictureVisible[$.ghostIndex] = true;
						timer.setDurationIndefinite().start();
					}
				} else if (timer.atSecond(2.75)) {
					$.fastBlinking.restart();
					$.fastBlinking.advance();
					controller.changeState(State.SHOWING_POINTS);
				}
			}
		},

		SHOWING_POINTS {
			@Override
			public void onUpdate(Context $) {
				if (timer.atSecond(2)) {
					controller.changeState(State.CHASING_PAC);
				}
			}
		},

		CHASING_PAC {
			@Override
			public void onEnter(Context $) {
				timer.setDurationIndefinite().start();
				$.pacMan.show();
				$.pacMan.setSpeed(1);
				$.pacMan.setPosition(t(28), t(20));
				$.pacMan.setMoveDir(Direction.LEFT);
				for (Ghost ghost : $.ghosts) {
					ghost.position = $.pacMan.position.plus(24 + ghost.id * 16, 0);
					ghost.setWishDir(Direction.LEFT);
					ghost.setMoveDir(Direction.LEFT);
					ghost.setSpeed(1.05);
					ghost.show();
					ghost.state = GhostState.HUNTING_PAC;
				}
			}

			@Override
			public void onUpdate(Context $) {
				if ($.pacMan.position.x <= t(3)) {
					controller.changeState(State.CHASING_GHOSTS);
					return;
				}
				$.pacMan.move();
				for (Ghost ghost : $.ghosts) {
					ghost.move();
				}
				$.fastBlinking.animate();
			}
		},

		CHASING_GHOSTS {
			@Override
			public void onEnter(Context $) {
				timer.setDurationIndefinite().start();
				for (Ghost ghost : $.ghosts) {
					ghost.state = GhostState.FRIGHTENED;
					ghost.setWishDir(Direction.RIGHT);
					ghost.setMoveDir(Direction.RIGHT);
					ghost.setSpeed(0.6);
				}
				$.ghostKilledTime = timer.tick();
			}

			@Override
			public void onUpdate(Context $) {
				if (timer.tick() < 8) {
					for (Ghost ghost : $.ghosts) {
						ghost.move();
					}
					return;
				}
				if (timer.tick() == 8) {
					$.pacMan.setMoveDir(Direction.RIGHT);
					$.pacMan.setSpeed(1);
				}
				if ($.pacMan.position.x > t(29)) {
					$.slowBlinking.restart();
					if (controller.gameController.credit() == 0) {
						controller.gameController.state().timer().expire();
					} else {
						controller.changeState(State.READY_TO_PLAY);
					}
					return;
				}
				// check if Pac-Man kills a ghost
				Optional<Ghost> killedGhost = Stream.of($.ghosts).filter(ghost -> ghost.state != GhostState.DEAD)
						.filter($.pacMan::meets).findFirst();
				killedGhost.ifPresent(victim -> {
					$.ghostKilledTime = timer.tick();
					victim.state = GhostState.DEAD;
					victim.bounty = List.of(200, 400, 800, 1600).get(victim.id);
					$.pacMan.hide();
					$.pacMan.setSpeed(0);
					Stream.of($.ghosts).forEach(ghost -> ghost.setSpeed(0));
				});
				// After some time, Pac-Man and the surviving ghosts get visible and move again
				if (timer.tick() - $.ghostKilledTime == sec_to_ticks(1)) {
					$.pacMan.show();
					$.pacMan.setSpeed(1.0);
					for (Ghost ghost : $.ghosts) {
						if (ghost.state == GhostState.DEAD) {
							ghost.hide();
						} else {
							ghost.show();
							ghost.setSpeed(0.6);
						}
					}
					$.ghostKilledTime = timer.tick();
				}
				// When the last ghost has been killed, make Pac-Man invisible
				if (Stream.of($.ghosts).allMatch(ghost -> ghost.state == GhostState.DEAD)) {
					$.pacMan.hide();
				}
				$.pacMan.move();
				for (Ghost ghost : $.ghosts) {
					ghost.move();
				}
				$.fastBlinking.animate();
			}
		},

		READY_TO_PLAY {
			@Override
			public void onUpdate(Context $) {
				$.slowBlinking.animate();
				if (timer.atSecond(5)) {
					controller.gameController.state().timer().expire();
				}
			}
		};

		protected IntroController controller;
		protected final TickTimer timer = new TickTimer("Timer-" + name());

		@Override
		public void setFsm(Fsm<? extends FsmState<Context>, Context> fsm) {
			controller = (IntroController) fsm;
		}

		@Override
		public TickTimer timer() {
			return timer;
		}
	}
}