/*
MIT License

Copyright (c) 2022 Armin Reichert

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
import static de.amr.games.pacman.model.common.GameModel.CYAN_GHOST;
import static de.amr.games.pacman.model.common.GameModel.ORANGE_GHOST;
import static de.amr.games.pacman.model.common.GameModel.PINK_GHOST;
import static de.amr.games.pacman.model.common.GameModel.RED_GHOST;
import static de.amr.games.pacman.model.common.world.World.t;

import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

import de.amr.games.pacman.lib.Direction;
import de.amr.games.pacman.lib.Fsm;
import de.amr.games.pacman.lib.FsmState;
import de.amr.games.pacman.lib.TickTimer;
import de.amr.games.pacman.model.common.Ghost;
import de.amr.games.pacman.model.common.GhostState;
import de.amr.games.pacman.model.common.Pac;

/**
 * @author Armin Reichert
 */
public enum IntroState implements FsmState<IntroContext> {

	BEGIN {
		@Override
		public void onEnter(IntroContext $) {
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
		public void onUpdate(IntroContext $) {
			if (timer.atSecond(1)) {
				$.selectGhost(0);
				controller.changeState(IntroState.PRESENTING_GHOSTS);
			}
		}
	},

	PRESENTING_GHOSTS {
		@Override
		public void onUpdate(IntroContext $) {
			if (timer.atSecond(1.0)) {
				$.characterVisible[$.ghostIndex] = true;
			} else if (timer.atSecond(1.5)) {
				$.nicknameVisible[$.ghostIndex] = true;
			} else if (timer.atSecond(2.0)) {
				if ($.ghostIndex < 3) {
					$.selectGhost($.ghostIndex + 1);
					timer.setDurationIndefinite().start();
				}
			} else if (timer.atSecond(2.75)) {
				$.fastBlinking.restart();
				$.fastBlinking.advance();
				controller.changeState(IntroState.SHOWING_POINTS);
			}
		}
	},

	SHOWING_POINTS {
		@Override
		public void onUpdate(IntroContext $) {
			if (timer.atSecond(2)) {
				controller.changeState(IntroState.CHASING_PAC);
			}
		}
	},

	CHASING_PAC {
		@Override
		public void onEnter(IntroContext $) {
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
		public void onUpdate(IntroContext $) {
			if ($.pacMan.position.x <= t(3)) {
				controller.changeState(IntroState.CHASING_GHOSTS);
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
		public void onEnter(IntroContext $) {
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
		public void onUpdate(IntroContext $) {
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
				controller.changeState(IntroState.READY_TO_PLAY);
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
		public void onUpdate(IntroContext $) {
			$.slowBlinking.animate();
			if (timer.atSecond(5)) {
				controller.gameController.state().timer().expire();
			}
		}
	};

	protected IntroController controller;
	protected final TickTimer timer = new TickTimer("Timer:" + name());

	@Override
	public void setFsm(Fsm<? extends FsmState<IntroContext>, IntroContext> fsm) {
		controller = (IntroController) fsm;
	}

	@Override
	public TickTimer timer() {
		return timer;
	}
}