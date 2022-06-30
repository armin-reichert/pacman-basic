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

import static de.amr.games.pacman.lib.TickTimer.secToTicks;
import static de.amr.games.pacman.model.common.actors.Ghost.CYAN_GHOST;
import static de.amr.games.pacman.model.common.actors.Ghost.ORANGE_GHOST;
import static de.amr.games.pacman.model.common.actors.Ghost.PINK_GHOST;
import static de.amr.games.pacman.model.common.actors.Ghost.RED_GHOST;
import static de.amr.games.pacman.model.common.world.World.t;

import java.util.Arrays;
import java.util.stream.Stream;

import de.amr.games.pacman.controller.common.GameController;
import de.amr.games.pacman.controller.common.GameState;
import de.amr.games.pacman.controller.pacman.IntroController.Context;
import de.amr.games.pacman.controller.pacman.IntroController.State;
import de.amr.games.pacman.lib.Direction;
import de.amr.games.pacman.lib.TickTimer;
import de.amr.games.pacman.lib.animation.SingleSpriteAnimation;
import de.amr.games.pacman.lib.animation.SpriteAnimation;
import de.amr.games.pacman.lib.animation.SpriteAnimations;
import de.amr.games.pacman.lib.fsm.Fsm;
import de.amr.games.pacman.lib.fsm.FsmState;
import de.amr.games.pacman.model.common.actors.AnimKeys;
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

	public final Context ctx;

	public IntroController(GameController gameController) {
		super(State.values());
		for (var state : states) {
			state.controller = this;
		}
		ctx = new Context(gameController);
	}

	@Override
	public Context context() {
		return ctx;
	}

	public static class Context {
		public int left = 4;
		public SingleSpriteAnimation<Boolean> blinking = SingleSpriteAnimation.pulse(10);
		public String[] nicknames = { "Blinky", "Pinky", "Inky", "Clyde" };
		public String[] characters = { "SHADOW", "SPEEDY", "BASHFUL", "POKEY" };
		public boolean[] pictureVisible = { false, false, false, false };
		public boolean[] nicknameVisible = { false, false, false, false };
		public boolean[] characterVisible = { false, false, false, false };
		public boolean creditVisible = false;
		public boolean titleVisible = false;
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
			public void onEnter(Context ctx) {
				ctx.gameController.game().scores.gameScore.showContent = false;
				ctx.gameController.game().scores.highScore.showContent = true;
				ctx.ghostIndex = 0;
				Arrays.fill(ctx.pictureVisible, false);
				Arrays.fill(ctx.nicknameVisible, false);
				Arrays.fill(ctx.characterVisible, false);
				ctx.pacMan = new Pac("Pac-Man");
				ctx.ghosts = new Ghost[] { //
						new Ghost(RED_GHOST, "Blinky"), //
						new Ghost(PINK_GHOST, "Pinky"), //
						new Ghost(CYAN_GHOST, "Inky"), //
						new Ghost(ORANGE_GHOST, "Clyde"), //
				};
			}

			@Override
			public void onUpdate(Context ctx) {
				if (timer.tick() == 1) {
					ctx.gameController.game().scores.gameScore.show();
					ctx.gameController.game().scores.highScore.show();
				} else if (timer.tick() == 2) {
					ctx.creditVisible = true;
				} else if (timer.tick() == 3) {
					ctx.titleVisible = true;
				} else if (timer.atSecond(1)) {
					controller.changeState(State.PRESENTING_GHOSTS);
				}
			}
		},

		PRESENTING_GHOSTS {
			@Override
			public void onUpdate(Context ctx) {
				if (timer.atSecond(0)) {
					ctx.pictureVisible[ctx.ghostIndex] = true;
				} else if (timer.atSecond(1.0)) {
					ctx.characterVisible[ctx.ghostIndex] = true;
				} else if (timer.atSecond(1.5)) {
					ctx.nicknameVisible[ctx.ghostIndex] = true;
				} else if (timer.atSecond(2.0)) {
					if (++ctx.ghostIndex < 4) {
						timer.resetIndefinitely();
					}
				} else if (timer.atSecond(2.5)) {
					controller.changeState(State.SHOWING_POINTS);
				}
			}
		},

		SHOWING_POINTS {
			@Override
			public void onEnter(Context ctx) {
				ctx.blinking.stop();
			}

			@Override
			public void onUpdate(Context ctx) {
				if (timer.atSecond(1)) {
					controller.changeState(State.CHASING_PAC);
				}
			}
		},

		CHASING_PAC {
			@Override
			public void onEnter(Context ctx) {
				timer.resetIndefinitely();
				timer.start();
				ctx.pacMan.setPosition(t(36), t(20));
				ctx.pacMan.setMoveDir(Direction.LEFT);
				ctx.pacMan.setAbsSpeed(1.2);
				ctx.pacMan.show();
				ctx.pacMan.animations().ifPresent(SpriteAnimations::ensureRunning);
				for (Ghost ghost : ctx.ghosts) {
					ghost.enterHuntingState();
					ghost.animations().ifPresent(SpriteAnimations::ensureRunning);
					ghost.setPosition(ctx.pacMan.getPosition().plus(16 * (ghost.id + 1), 0));
					ghost.setBothDirs(Direction.LEFT);
					ghost.setAbsSpeed(1.2);
					ghost.show();
				}
			}

			@Override
			public void onUpdate(Context ctx) {
				// Pac-Man reaches the energizer
				if (ctx.pacMan.getPosition().x <= t(ctx.left)) {
					controller.changeState(State.CHASING_GHOSTS);
				}
				// ghosts already reverse direction before Pac-man eats the energizer and turns right!
				else if (ctx.pacMan.getPosition().x <= t(ctx.left) + 4) {
					for (Ghost ghost : ctx.ghosts) {
						ghost.enterFrightenedState();
						ghost.setBothDirs(Direction.RIGHT);
						ghost.setAbsSpeed(0.6);
						ghost.move();
					}
					ctx.pacMan.move();
				}
				// keep moving
				else {
					// wait 1 sec before blinking
					if (timer.atSecond(1)) {
						ctx.blinking.run();
					}
					ctx.blinking.advance();
					ctx.pacMan.move();
					for (Ghost ghost : ctx.ghosts) {
						ghost.move();
					}
				}
			}
		},

		CHASING_GHOSTS {
			@Override
			public void onEnter(Context ctx) {
				timer.resetIndefinitely();
				timer.start();
				ctx.ghostKilledTime = timer.tick();
				ctx.pacMan.setMoveDir(Direction.RIGHT);
				ctx.pacMan.setAbsSpeed(1.2);
			}

			@Override
			public void onUpdate(Context ctx) {
				if (Stream.of(ctx.ghosts).allMatch(ghost -> ghost.is(GhostState.DEAD))) {
					ctx.pacMan.hide();
					controller.changeState(READY_TO_PLAY);
					return;
				}
				var nextVictim = Stream.of(ctx.ghosts)//
						.filter(ctx.pacMan::sameTile)//
						.filter(ghost -> ghost.is(GhostState.FRIGHTENED))//
						.findFirst();
				nextVictim.ifPresent(victim -> {
					ctx.ghostKilledTime = timer.tick();
					victim.killIndex = victim.id;
					victim.enterDeadState();
					ctx.pacMan.hide();
					ctx.pacMan.setAbsSpeed(0);
					Stream.of(ctx.ghosts).forEach(ghost -> {
						ghost.setAbsSpeed(0);
						ghost.animation(AnimKeys.GHOST_BLUE).ifPresent(SpriteAnimation::stop);
					});
				});

				// After 1 sec, Pac-Man and the surviving ghosts get visible again and move on
				if (timer.tick() - ctx.ghostKilledTime == secToTicks(1)) {
					ctx.pacMan.show();
					ctx.pacMan.setAbsSpeed(1.2);
					for (Ghost ghost : ctx.ghosts) {
						if (!ghost.is(GhostState.DEAD)) {
							ghost.show();
							ghost.setAbsSpeed(0.6);
							ghost.animation(AnimKeys.GHOST_BLUE).ifPresent(SpriteAnimation::run);
						} else {
							ghost.hide();
						}
					}
				}
				ctx.pacMan.move();
				for (Ghost ghost : ctx.ghosts) {
					ghost.move();
				}
				ctx.blinking.advance();
			}
		},

		READY_TO_PLAY {
			@Override
			public void onUpdate(Context ctx) {
				if (timer.atSecond(1)) {
					ctx.ghosts[3].hide();
					if (ctx.gameController.game().credit == 0) {
						ctx.gameController.changeState(GameState.READY);
						return;
					}
				}
				if (timer.atSecond(5)) {
					ctx.gameController.restartIntro();
				}
			}
		};

		protected IntroController controller;
		protected final TickTimer timer = new TickTimer("Timer-" + name());

		@Override
		public TickTimer timer() {
			return timer;
		}
	}
}