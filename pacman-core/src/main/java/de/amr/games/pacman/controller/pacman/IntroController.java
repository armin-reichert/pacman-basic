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

import static de.amr.games.pacman.model.common.actors.Ghost.CYAN_GHOST;
import static de.amr.games.pacman.model.common.actors.Ghost.ORANGE_GHOST;
import static de.amr.games.pacman.model.common.actors.Ghost.PINK_GHOST;
import static de.amr.games.pacman.model.common.actors.Ghost.RED_GHOST;
import static de.amr.games.pacman.model.common.world.World.t;

import java.util.Arrays;
import java.util.Optional;
import java.util.stream.Stream;

import de.amr.games.pacman.controller.common.GameController;
import de.amr.games.pacman.controller.common.GameState;
import de.amr.games.pacman.controller.pacman.IntroController.Context;
import de.amr.games.pacman.controller.pacman.IntroController.State;
import de.amr.games.pacman.lib.Direction;
import de.amr.games.pacman.lib.TickTimer;
import de.amr.games.pacman.lib.animation.SingleSpriteAnimation;
import de.amr.games.pacman.lib.animation.SpriteAnimations;
import de.amr.games.pacman.lib.fsm.Fsm;
import de.amr.games.pacman.lib.fsm.FsmState;
import de.amr.games.pacman.model.common.actors.Ghost;
import de.amr.games.pacman.model.common.actors.GhostState;
import de.amr.games.pacman.model.common.actors.Pac;

/**
 * Intro scene of the PacMan game.
 * <p>
 * The ghosts are presented one after another, then Pac-Man is chased by the ghosts, turns the card and hunts the ghosts
 * himself.
 * 
 * @author Armin Reichert
 */
public class IntroController extends Fsm<State, Context> {

	public final Context $;

	public IntroController(GameController gameController) {
		super(State.values());
		$ = new Context(gameController);
	}

	@Override
	public Context context() {
		return $;
	}

	public static class Context {
		public final int left = 4;
		public final double speed = 1.15;
		public final SingleSpriteAnimation<Boolean> blinking = SingleSpriteAnimation.pulse(10);
		public final String nicknames[] = { "Blinky", "Pinky", "Inky", "Clyde" };
		public final String characters[] = { "SHADOW", "SPEEDY", "BASHFUL", "POKEY" };
		public boolean creditVisible = false;
		public boolean titleVisible = false;
		public boolean[] pictureVisible = { false, false, false, false };
		public boolean[] nicknameVisible = { false, false, false, false };
		public boolean[] characterVisible = { false, false, false, false };
		public Pac pacMan;
		public Ghost[] ghosts;
		public int ghostIndex;
		public long ghostKilledTime;

		public final GameController gameController;

		public Context(GameController gameController) {
			this.gameController = gameController;
		}
	}

	public enum State implements FsmState<Context> {

		START {
			@Override
			public void onEnter(Context $) {
				$.gameController.game().scores.gameScore.showContent = false;
				$.gameController.game().scores.highScore.showContent = true;
				$.ghostIndex = 0;
				Arrays.fill($.pictureVisible, false);
				Arrays.fill($.nicknameVisible, false);
				Arrays.fill($.characterVisible, false);
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
				if (timer.tick() == 1) {
					$.gameController.game().scores.gameScore.visible = true;
					$.gameController.game().scores.highScore.visible = true;
				} else if (timer.tick() == 2) {
					$.creditVisible = true;
				} else if (timer.tick() == 3) {
					$.titleVisible = true;
				} else if (timer.atSecond(1)) {
					changeState(State.PRESENTING_GHOSTS);
				}
			}
		},

		PRESENTING_GHOSTS {
			@Override
			public void onUpdate(Context $) {
				if (timer.atSecond(0)) {
					$.pictureVisible[$.ghostIndex] = true;
				} else if (timer.atSecond(1.0)) {
					$.characterVisible[$.ghostIndex] = true;
				} else if (timer.atSecond(1.5)) {
					$.nicknameVisible[$.ghostIndex] = true;
				} else if (timer.atSecond(2.0)) {
					if (++$.ghostIndex < 4) {
						timer.resetIndefinitely(); // resets timer to 0 too!
					}
				} else if (timer.atSecond(2.5)) {
					changeState(State.SHOWING_POINTS);
				}
			}
		},

		SHOWING_POINTS {
			@Override
			public void onEnter(Context $) {
				$.blinking.stop();
			}

			@Override
			public void onUpdate(Context $) {
				if (timer.atSecond(1)) {
					changeState(State.CHASING_PAC);
				}
			}
		},

		CHASING_PAC {
			@Override
			public void onEnter(Context $) {
				timer.resetIndefinitely();
				timer.start();
				$.pacMan.setPosition(t(36), t(20));
				$.pacMan.setMoveDir(Direction.LEFT);
				$.pacMan.setAbsSpeed($.speed);
				$.pacMan.show();
				$.pacMan.animations().ifPresent(anim -> anim.ensureRunning());
				for (Ghost ghost : $.ghosts) {
					ghost.state = GhostState.HUNTING_PAC;
					ghost.position = $.pacMan.position.plus(16 * (ghost.id + 1), 0);
					ghost.setBothDirs(Direction.LEFT);
					ghost.setAbsSpeed($.speed);
					ghost.show();
					ghost.animations().ifPresent(SpriteAnimations::ensureRunning);
				}
			}

			@Override
			public void onUpdate(Context $) {
				// Pac-Man reaches the energizer
				if ($.pacMan.position.x <= t($.left)) {
					changeState(State.CHASING_GHOSTS);
				}
				// ghosts already reverse direction before Pac-man eats the energizer and turns right!
				else if ($.pacMan.position.x <= t($.left) + 4) {
					for (Ghost ghost : $.ghosts) {
						ghost.enterFrightenedMode();
						ghost.setBothDirs(Direction.RIGHT);
						ghost.setAbsSpeed(0.5 * $.speed);
						ghost.move();
					}
					$.pacMan.move();
				}
				// keep moving
				else {
					// wait 1 sec before blinking
					if (timer.atSecond(1)) {
						$.blinking.run();
					}
					$.blinking.advance();
					$.pacMan.move();
					for (Ghost ghost : $.ghosts) {
						ghost.move();
					}
				}
			}
		},

		CHASING_GHOSTS {
			@Override
			public void onEnter(Context $) {
				timer.resetIndefinitely();
				timer.start();
				$.ghostKilledTime = timer.tick();
				$.pacMan.setMoveDir(Direction.RIGHT);
				$.pacMan.setAbsSpeed($.speed);
			}

			@Override
			public void onUpdate(Context $) {

				// When the last ghost has been killed, leave state
				if (Stream.of($.ghosts).allMatch(ghost -> ghost.is(GhostState.DEAD))) {
					$.pacMan.hide();
					changeState(READY_TO_PLAY);
					return;
				}

				// check if Pac-Man kills a ghost
				Optional<Ghost> killedGhost = Stream.of($.ghosts).filter(ghost -> ghost.state != GhostState.DEAD)
						.filter($.pacMan::sameTile).findFirst();
				killedGhost.ifPresent(victim -> {
					$.ghostKilledTime = timer.tick();
					victim.state = GhostState.DEAD;
					victim.killIndex = victim.id;
					$.pacMan.hide();
					$.pacMan.setAbsSpeed(0);
					Stream.of($.ghosts).forEach(ghost -> ghost.setAbsSpeed(0));
				});

				// After 1 sec, Pac-Man and the surviving ghosts get visible again and move on
				if (timer.tick() - $.ghostKilledTime == TickTimer.sec_to_ticks(1)) {
					$.pacMan.show();
					$.pacMan.setAbsSpeed($.speed);
					for (Ghost ghost : $.ghosts) {
						if (ghost.state != GhostState.DEAD) {
							ghost.show();
							ghost.setAbsSpeed(0.5 * $.speed);
							ghost.animations().ifPresent(SpriteAnimations::ensureRunning);
						} else {
							ghost.hide();
						}
					}
				}

				$.pacMan.move();
				for (Ghost ghost : $.ghosts) {
					ghost.move();
				}
				$.blinking.advance();
			}
		},

		READY_TO_PLAY {
			@Override
			public void onUpdate(Context $) {
				if (timer.atSecond(1)) {
					$.ghosts[3].hide();
					if ($.gameController.game().credit == 0) {
						$.gameController.changeState(GameState.READY);
						return;
					}
				}
				if (timer.atSecond(5)) {
					$.gameController.restartIntro();
					return;
				}
			}
		};

		protected Fsm<FsmState<Context>, Context> controller;
		protected final TickTimer timer = new TickTimer("Timer-" + name());

		@Override
		public void setOwner(Fsm<FsmState<Context>, Context> fsm) {
			controller = fsm;
		}

		@Override
		public Fsm<FsmState<Context>, Context> getOwner() {
			return controller;
		}

		@Override
		public TickTimer timer() {
			return timer;
		}
	}
}