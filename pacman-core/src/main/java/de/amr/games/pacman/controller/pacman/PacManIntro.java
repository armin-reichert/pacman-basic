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

import java.util.Arrays;
import java.util.stream.Stream;

import de.amr.games.pacman.controller.common.GameController;
import de.amr.games.pacman.controller.common.GameState;
import de.amr.games.pacman.controller.pacman.PacManIntro.IntroData;
import de.amr.games.pacman.controller.pacman.PacManIntro.IntroState;
import de.amr.games.pacman.lib.Direction;
import de.amr.games.pacman.lib.TickTimer;
import de.amr.games.pacman.lib.animation.EntityAnimation;
import de.amr.games.pacman.lib.animation.SingleEntityAnimation;
import de.amr.games.pacman.lib.fsm.Fsm;
import de.amr.games.pacman.lib.fsm.FsmState;
import de.amr.games.pacman.model.common.GameModel;
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
public class PacManIntro extends Fsm<IntroState, IntroData> {

	public static class IntroData {
		public static final double CHASING_SPEED = 1.1;
		public int left = 4;
		public SingleEntityAnimation<Boolean> blinking = SingleEntityAnimation.pulse(10);
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

		public IntroData(GameController gameController) {
			this.gameController = gameController;
		}
	}

	public enum IntroState implements FsmState<IntroData> {

		START {
			@Override
			public void onEnter(IntroData ctx) {
				ctx.gameController.game().gameScore().setShowContent(false);
				ctx.gameController.game().highScore().setShowContent(true);
				ctx.ghostIndex = 0;
				Arrays.fill(ctx.pictureVisible, false);
				Arrays.fill(ctx.nicknameVisible, false);
				Arrays.fill(ctx.characterVisible, false);
				ctx.pacMan = new Pac("Pac-Man");
				ctx.ghosts = new Ghost[] { //
						new Ghost(Ghost.ID_RED_GHOST, "Blinky"), //
						new Ghost(Ghost.ID_PINK_GHOST, "Pinky"), //
						new Ghost(Ghost.ID_CYAN_GHOST, "Inky"), //
						new Ghost(Ghost.ID_ORANGE_GHOST, "Clyde"), //
				};
			}

			@Override
			public void onUpdate(IntroData ctx) {
				if (timer.tick() == 1) {
					ctx.gameController.game().gameScore().setVisible(true);
					ctx.gameController.game().highScore().setVisible(true);
				} else if (timer.tick() == 2) {
					ctx.creditVisible = true;
				} else if (timer.tick() == 3) {
					ctx.titleVisible = true;
				} else if (timer.atSecond(1)) {
					controller.changeState(IntroState.PRESENTING_GHOSTS);
				}
			}
		},

		PRESENTING_GHOSTS {
			@Override
			public void onUpdate(IntroData ctx) {
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
					controller.changeState(IntroState.SHOWING_POINTS);
				}
			}
		},

		SHOWING_POINTS {
			@Override
			public void onEnter(IntroData ctx) {
				ctx.blinking.stop();
			}

			@Override
			public void onUpdate(IntroData ctx) {
				if (timer.atSecond(1)) {
					controller.changeState(IntroState.CHASING_PAC);
				}
			}
		},

		CHASING_PAC {
			@Override
			public void onEnter(IntroData ctx) {
				timer.resetIndefinitely();
				timer.start();
				ctx.pacMan.setPosition(t(36), t(20));
				ctx.pacMan.setMoveDir(Direction.LEFT);
				ctx.pacMan.setAbsSpeed(IntroData.CHASING_SPEED);
				ctx.pacMan.show();
				ctx.pacMan.selectAndEnsureRunningAnimation(AnimKeys.PAC_MUNCHING);
				for (Ghost ghost : ctx.ghosts) {
					ghost.enterStateHuntingPac(ctx.gameController.game());
					ghost.setPosition(ctx.pacMan.position().plus(16 * (ghost.id + 1), 0));
					ghost.setMoveAndWishDir(Direction.LEFT);
					ghost.setAbsSpeed(IntroData.CHASING_SPEED);
					ghost.show();
					ghost.selectAndEnsureRunningAnimation(AnimKeys.GHOST_COLOR);
				}
			}

			@Override
			public void onUpdate(IntroData ctx) {
				// Pac-Man reaches the energizer
				if (ctx.pacMan.position().x() <= t(ctx.left)) {
					controller.changeState(IntroState.CHASING_GHOSTS);
				}
				// ghosts already reverse direction before Pac-man eats the energizer and turns right!
				else if (ctx.pacMan.position().x() <= t(ctx.left) + 4) {
					for (Ghost ghost : ctx.ghosts) {
						ghost.enterStateFrightened(ctx.gameController.game());
						ghost.selectAndEnsureRunningAnimation(AnimKeys.GHOST_BLUE);
						ghost.setMoveAndWishDir(Direction.RIGHT);
						ghost.setAbsSpeed(0.6);
						ghost.move();
						ghost.advanceAnimation();
					}
					ctx.pacMan.move();
					ctx.pacMan.advanceAnimation();
				}
				// keep moving
				else {
					// wait 1 sec before blinking
					if (timer.atSecond(1)) {
						ctx.blinking.run();
					}
					ctx.blinking.advance();
					ctx.pacMan.move();
					ctx.pacMan.advanceAnimation();
					for (Ghost ghost : ctx.ghosts) {
						ghost.move();
						ghost.advanceAnimation();
					}
				}
			}
		},

		CHASING_GHOSTS {
			@Override
			public void onEnter(IntroData ctx) {
				timer.resetIndefinitely();
				timer.start();
				ctx.ghostKilledTime = timer.tick();
				ctx.pacMan.setMoveDir(Direction.RIGHT);
				ctx.pacMan.setAbsSpeed(IntroData.CHASING_SPEED);
			}

			@Override
			public void onUpdate(IntroData ctx) {
				if (Stream.of(ctx.ghosts).allMatch(ghost -> ghost.is(GhostState.EATEN))) {
					ctx.pacMan.hide();
					controller.changeState(READY_TO_PLAY);
					return;
				}
				var nextVictim = Stream.of(ctx.ghosts)//
						.filter(ctx.pacMan::sameTile)//
						.filter(ghost -> ghost.is(GhostState.FRIGHTENED))//
						.findFirst();
				nextVictim.ifPresent(victim -> {
					victim.killedIndex = victim.id;
					ctx.ghostKilledTime = timer.tick();
					victim.enterStateEaten(ctx.gameController.game());
					ctx.pacMan.hide();
					ctx.pacMan.setAbsSpeed(0);
					Stream.of(ctx.ghosts).forEach(ghost -> {
						ghost.setAbsSpeed(0);
						ghost.animation(AnimKeys.GHOST_BLUE).ifPresent(EntityAnimation::stop);
					});
				});

				// After ??? sec, Pac-Man and the surviving ghosts get visible again and move on
				if (timer.tick() - ctx.ghostKilledTime == timer.secToTicks(0.9)) {
					ctx.pacMan.show();
					ctx.pacMan.setAbsSpeed(IntroData.CHASING_SPEED);
					for (Ghost ghost : ctx.ghosts) {
						if (!ghost.is(GhostState.EATEN)) {
							ghost.show();
							ghost.setAbsSpeed(0.6);
							ghost.animation(AnimKeys.GHOST_BLUE).ifPresent(EntityAnimation::run);
						} else {
							ghost.hide();
						}
					}
				}
				ctx.pacMan.move();
				ctx.pacMan.advanceAnimation();
				for (Ghost ghost : ctx.ghosts) {
					ghost.move();
					ghost.advanceAnimation();
				}
				ctx.blinking.advance();
			}
		},

		READY_TO_PLAY {
			@Override
			public void onUpdate(IntroData ctx) {
				if (timer.atSecond(1)) {
					ctx.ghosts[3].hide();
					if (!ctx.gameController.game().hasCredit()) {
						ctx.gameController.changeState(GameState.READY);
						return;
					}
				}
				if (timer.atSecond(5)) {
					ctx.gameController.restartIntro();
				}
			}
		};

		PacManIntro controller;
		final TickTimer timer = new TickTimer("Timer-" + name(), GameModel.FPS);

		@Override
		public TickTimer timer() {
			return timer;
		}
	}

	private final IntroData ctx;

	public PacManIntro(GameController gameController) {
		states = IntroState.values();
		for (var state : states) {
			state.controller = this;
		}
		ctx = new IntroData(gameController);
	}

	@Override
	public IntroData context() {
		return ctx;
	}
}