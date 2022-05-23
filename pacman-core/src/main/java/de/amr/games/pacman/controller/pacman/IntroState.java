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

import de.amr.games.pacman.controller.pacman.IntroContext.GhostPortrait;
import de.amr.games.pacman.lib.Direction;
import de.amr.games.pacman.lib.Fsm;
import de.amr.games.pacman.lib.FsmState;
import de.amr.games.pacman.lib.TickTimer;
import de.amr.games.pacman.model.common.Ghost;
import de.amr.games.pacman.model.common.GhostState;
import de.amr.games.pacman.model.common.Pac;

public enum IntroState implements FsmState<IntroContext> {

	BEGIN {
		@Override
		public void onEnter(IntroContext context) {
			context.portraits = new GhostPortrait[] { //
					new GhostPortrait(RED_GHOST, "Blinky", "SHADOW", 7), //
					new GhostPortrait(PINK_GHOST, "Pinky", "SPEEDY", 10), //
					new GhostPortrait(CYAN_GHOST, "Inky", "BASHFUL", 13), //
					new GhostPortrait(ORANGE_GHOST, "Clyde", "POKEY", 16), //
			};
			context.pacMan = new Pac("Pac-Man");
			context.ghosts = new Ghost[] { //
					new Ghost(RED_GHOST, "Blinky"), //
					new Ghost(PINK_GHOST, "Pinky"), //
					new Ghost(CYAN_GHOST, "Inky"), //
					new Ghost(ORANGE_GHOST, "Clyde"), //
			};
		}

		@Override
		public void onUpdate(IntroContext context) {
			if (timer.atSecond(1)) {
				context.selectGhost(0);
				controller.changeState(IntroState.PRESENTING_GHOSTS);
			}
		}
	},

	PRESENTING_GHOSTS {
		@Override
		public void onUpdate(IntroContext context) {
			if (timer.atSecond(1.0)) {
				context.portraits[context.ghostIndex].characterVisible = true;
			} else if (timer.atSecond(1.5)) {
				context.portraits[context.ghostIndex].nicknameVisible = true;
			} else if (timer.atSecond(2.0)) {
				if (context.ghostIndex < 3) {
					context.selectGhost(context.ghostIndex + 1);
					timer.setDurationIndefinite().start();
				}
			} else if (timer.atSecond(2.75)) {
				context.fastBlinking.restart();
				context.fastBlinking.advance();
				controller.changeState(IntroState.SHOWING_POINTS);
			}
		}
	},

	SHOWING_POINTS {
		@Override
		public void onUpdate(IntroContext context) {
			if (timer.atSecond(2)) {
				controller.changeState(IntroState.CHASING_PAC);
			}
		}
	},

	CHASING_PAC {
		@Override
		public void onEnter(IntroContext context) {
			timer.setDurationIndefinite().start();
			context.pacMan.show();
			context.pacMan.setSpeed(1);
			context.pacMan.setPosition(t(28), t(20));
			context.pacMan.setMoveDir(Direction.LEFT);
			for (Ghost ghost : context.ghosts) {
				ghost.position = context.pacMan.position.plus(24 + ghost.id * 16, 0);
				ghost.setWishDir(Direction.LEFT);
				ghost.setMoveDir(Direction.LEFT);
				ghost.setSpeed(1.05);
				ghost.show();
				ghost.state = GhostState.HUNTING_PAC;
			}
		}

		@Override
		public void onUpdate(IntroContext context) {
			if (context.pacMan.position.x < t(2)) {
				controller.changeState(IntroState.CHASING_GHOSTS);
				return;
			}
			context.pacMan.move();
			for (Ghost ghost : context.ghosts) {
				ghost.move();
			}
			context.fastBlinking.animate();
		}
	},

	CHASING_GHOSTS {
		@Override
		public void onEnter(IntroContext context) {
			timer.setDurationIndefinite().start();
			for (Ghost ghost : context.ghosts) {
				ghost.state = GhostState.FRIGHTENED;
				ghost.setWishDir(Direction.RIGHT);
				ghost.setMoveDir(Direction.RIGHT);
				ghost.setSpeed(0.6);
			}
			context.ghostKilledTime = timer.tick();
		}

		@Override
		public void onUpdate(IntroContext context) {
			if (timer.tick() < 8) {
				for (Ghost ghost : context.ghosts) {
					ghost.move();
				}
				return;
			}
			if (timer.tick() == 8) {
				context.pacMan.setMoveDir(Direction.RIGHT);
				context.pacMan.setSpeed(1);
			}
			if (context.pacMan.position.x > t(29)) {
				context.slowBlinking.restart();
				controller.changeState(IntroState.READY_TO_PLAY);
				return;
			}
			// check if Pac-Man kills a ghost
			Optional<Ghost> killedGhost = Stream.of(context.ghosts).filter(ghost -> ghost.state != GhostState.DEAD)
					.filter(context.pacMan::meets).findFirst();
			killedGhost.ifPresent(victim -> {
				context.ghostKilledTime = timer.tick();
				victim.state = GhostState.DEAD;
				victim.bounty = List.of(200, 400, 800, 1600).get(victim.id);
				context.pacMan.hide();
				context.pacMan.setSpeed(0);
				Stream.of(context.ghosts).forEach(ghost -> ghost.setSpeed(0));
			});
			// After some time, Pac-Man and the surviving ghosts get visible and move again
			if (timer.tick() - context.ghostKilledTime == sec_to_ticks(1)) {
				context.pacMan.show();
				context.pacMan.setSpeed(1.0);
				for (Ghost ghost : context.ghosts) {
					if (ghost.state == GhostState.DEAD) {
						ghost.hide();
					} else {
						ghost.show();
						ghost.setSpeed(0.6);
					}
				}
				context.ghostKilledTime = timer.tick();
			}
			// When the last ghost has been killed, make Pac-Man invisible
			if (Stream.of(context.ghosts).allMatch(ghost -> ghost.state == GhostState.DEAD)) {
				context.pacMan.hide();
			}
			context.pacMan.move();
			for (Ghost ghost : context.ghosts) {
				ghost.move();
			}
			context.fastBlinking.animate();
		}
	},

	READY_TO_PLAY {
		@Override
		public void onUpdate(IntroContext context) {
			context.slowBlinking.animate();
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