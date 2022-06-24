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
package de.amr.games.pacman.controller.common;

import static java.util.function.Predicate.not;

import de.amr.games.pacman.event.GameEventType;
import de.amr.games.pacman.event.GameEvents;
import de.amr.games.pacman.event.GameStateChangeEvent;
import de.amr.games.pacman.lib.TickTimer;
import de.amr.games.pacman.lib.animation.SpriteAnimation;
import de.amr.games.pacman.lib.animation.SpriteAnimations;
import de.amr.games.pacman.lib.fsm.Fsm;
import de.amr.games.pacman.lib.fsm.FsmState;
import de.amr.games.pacman.model.common.GameModel;
import de.amr.games.pacman.model.common.GameModel.CheckResult;
import de.amr.games.pacman.model.common.GameSound;
import de.amr.games.pacman.model.common.GameSounds;
import de.amr.games.pacman.model.common.GameVariant;
import de.amr.games.pacman.model.common.actors.AnimKeys;
import de.amr.games.pacman.model.common.actors.Ghost;
import de.amr.games.pacman.model.common.actors.GhostState;

/**
 * Rule of thumb: here, specify the "what" and "when", not the "how" (which should be implemented in the model).
 * 
 * @author Armin Reichert
 */
public enum GameState implements FsmState<GameModel> {

	INTRO() {
		@Override
		public void onEnter(GameModel game) {
			timer.resetIndefinitely();
			timer.start();
			game.reset();
		}

		@Override
		public void onUpdate(GameModel game) {
			if (timer.hasExpired()) {
				fsm.changeState(READY);
			}
		}

		@Override
		public void selectGameVariant(GameVariant variant) {
			gameController.selectGame(variant);
		}

		@Override
		public void addCredit(GameModel game) {
			game.sounds().ifPresent(snd -> {
				snd.setSilent(false);
				snd.play(GameSound.CREDIT);
			});
			game.credit++;
			fsm.changeState(CREDIT);
		}

		@Override
		public void requestGame(GameModel game) {
			if (game.credit > 0) {
				fsm.changeState(READY);
			}
		}

		@Override
		public void startIntermissionTest(GameModel game) {
			fsm.changeState(INTERMISSION_TEST);
		}
	},

	CREDIT {
		@Override
		public void onEnter(GameModel game) {
			game.scores.gameScore.showContent = false;
			game.scores.highScore.showContent = true;
		}

		@Override
		public void onUpdate(GameModel context) {
			// nothing to do here
		}

		@Override
		public void addCredit(GameModel game) {
			game.sounds().ifPresent(snd -> snd.play(GameSound.CREDIT));
			game.credit++;
		}

		@Override
		public void requestGame(GameModel game) {
			fsm.changeState(READY);
		}
	},

	READY {
		@Override
		public void onEnter(GameModel game) {
			boolean hasCredit = game.credit > 0;
			if (hasCredit && !game.playing) {
				// start new game, play intro music
				timer.resetSeconds(5);
				timer.start();
				game.reset();
				game.sounds().ifPresent(snd -> {
					snd.stopAll();
					snd.setSilent(false);
					snd.play(GameSound.GAME_READY);
				});
			} else {
				// game already running or attract mode
				timer.resetSeconds(2);
				timer.start();
				game.sounds().ifPresent(snd -> snd.setSilent(!game.playing));
			}
			game.scores.gameScore.showContent = hasCredit;
			game.scores.highScore.showContent = true;
			game.getReadyToPlay();
		}

		@Override
		public void onUpdate(GameModel game) {
			if (timer.hasExpired()) {
				if (game.credit > 0) {
					game.scores.enable(true);
					game.playing = true;
				} else {
					// start attract mode
					game.scores.enable(false);
					game.playing = false;
				}
				game.startHuntingPhase(0);
				fsm.changeState(GameState.HUNTING);
			}
		}
	},

	HUNTING {
		@Override
		public void onEnter(GameModel game) {
			game.pac.animation(AnimKeys.PAC_MUNCHING).ifPresent(SpriteAnimation::ensureRunning);
			game.ghosts().forEach(ghost -> ghost.animations().ifPresent(SpriteAnimations::ensureRunning));
			game.energizerPulse.restart();
			game.sounds().ifPresent(snd -> snd.ensureSirenStarted(game.huntingTimer.phase() / 2));
		}

		@Override
		public void onUpdate(GameModel game) {
			var checkResult = new CheckResult();
			gameController.currentSteering().accept(game.pac);
			game.updatePlayer(checkResult);
			if (checkResult.allFoodEaten) {
				fsm.changeState(LEVEL_COMPLETE);
				return;
			}
			if (checkResult.playerKilled) {
				fsm.changeState(PACMAN_DYING);
				return;
			}
			if (checkResult.ghostsKilled) {
				fsm.changeState(GHOST_DYING);
				return;
			}
			game.updateGhosts(checkResult);
			game.updateBonus();
			game.advanceHunting();
			game.energizerPulse.advance();

			game.sounds().ifPresent(snd -> {
				if (game.huntingTimer.tick() == 0) {
					snd.ensureSirenStarted(game.huntingTimer.phase() / 2);
				}
				if (game.pac.starvingTicks >= 12) {
					snd.stop(GameSound.PACMAN_MUNCH);
				}
				if (game.ghosts(GhostState.DEAD).count() == 0) {
					snd.stop(GameSound.GHOST_RETURNING);
				}
			});
		}

		@Override
		public void addCredit(GameModel game) {
			if (!game.playing) {
				game.sounds().ifPresent(snd -> {
					snd.setSilent(false);
					snd.play(GameSound.CREDIT);
				});
				game.credit++;
				fsm.changeState(CREDIT);
			}
		}

		@Override
		public void cheatEatAllPellets(GameModel game) {
			if (game.playing) {
				game.level.world.tiles().filter(not(game.level.world::isEnergizerTile)).forEach(game.level.world::removeFood);
				GameEvents.publish(GameEventType.PLAYER_FINDS_FOOD, null);
			}
		}

		@Override
		public void cheatKillAllEatableGhosts(GameModel game) {
			if (game.playing) {
				Ghost[] prey = game.ghosts()
						.filter(ghost -> ghost.is(GhostState.HUNTING_PAC) || ghost.is(GhostState.FRIGHTENED)).toArray(Ghost[]::new);
				game.ghostKillIndex = -1;
				game.killGhosts(prey);
				fsm.changeState(GameState.GHOST_DYING);
			}
		}

		@Override
		public void cheatEnterNextLevel(GameModel game) {
			if (game.playing) {
				game.level.world.tiles().forEach(game.level.world::removeFood);
				fsm.changeState(GameState.LEVEL_COMPLETE);
			}
		}

	},

	LEVEL_COMPLETE {
		@Override
		public void onEnter(GameModel game) {
			timer.resetSeconds(4);
			timer.start();
			game.huntingTimer.stop();
			game.bonus().setInactive();
			game.pac.setAbsSpeed(0);
			game.pac.animations().ifPresent(SpriteAnimations::reset);
			game.ghosts().forEach(Ghost::hide);
			game.energizerPulse.reset();
			game.sounds().ifPresent(GameSounds::stopAll);
		}

		@Override
		public void onUpdate(GameModel game) {
			if (timer.atSecond(1)) {
				game.mazeFlashingAnimation().ifPresent(mazeFlashing -> {
					mazeFlashing.repetions(game.level.numFlashes);
					mazeFlashing.restart();
				});
			} else if (timer.hasExpired()) {
				if (game.credit == 0) {
					fsm.changeState(INTRO);
				} else if (game.intermissionNumber(game.level.number) != 0) {
					fsm.changeState(INTERMISSION);
				} else {
					fsm.changeState(LEVEL_STARTING);
				}
			} else {
				game.mazeFlashingAnimation().ifPresent(SpriteAnimation::advance);
			}
		}
	},

	LEVEL_STARTING {
		@Override
		public void onEnter(GameModel game) {
			timer.resetSeconds(1);
			timer.start();
			game.setLevel(game.level.number + 1);
			game.getReadyToPlay();
			game.ghosts().forEach(Ghost::hide);
			game.pac.hide();
		}

		@Override
		public void onUpdate(GameModel game) {
			if (timer.hasExpired()) {
				fsm.changeState(READY);
			}
		}
	},

	GHOST_DYING {
		@Override
		public void onEnter(GameModel game) {
			timer.resetSeconds(1);
			timer.start();
			game.pac.hide();
			game.sounds().ifPresent(snd -> snd.play(GameSound.GHOST_EATEN));
		}

		@Override
		public void onUpdate(GameModel game) {
			if (timer.hasExpired()) {
				fsm.resumePreviousState();
				return;
			}
			gameController.currentSteering().accept(game.pac);
			game.updateGhostsReturningHome();
			game.energizerPulse.advance();
		}

		@Override
		public void onExit(GameModel game) {
			game.pac.show();
			game.ghosts().forEach(ghost -> ghost.animation(AnimKeys.GHOST_FLASHING).ifPresent(SpriteAnimation::run));
			game.letDeadGhostsReturnHome();
		}
	},

	PACMAN_DYING {
		@Override
		public void onEnter(GameModel game) {
			timer.resetSeconds(5);
			timer.start();
			game.pac.setAbsSpeed(0);
			game.pac.animations().ifPresent(anim -> {
				anim.select(AnimKeys.PAC_DYING);
				anim.selectedAnimation().reset();
			});
			game.bonus().setInactive();
			game.sounds().ifPresent(GameSounds::stopAll);
		}

		@Override
		public void onUpdate(GameModel game) {
			game.energizerPulse.advance();
			if (timer.atSecond(1)) {
				game.ghosts().forEach(Ghost::hide);
			} else if (timer.atSecond(2)) {
				game.pac.animation(AnimKeys.PAC_DYING).ifPresent(SpriteAnimation::restart);
				game.sounds().ifPresent(snd -> snd.play(GameSound.PACMAN_DEATH));
			} else if (timer.atSecond(4)) {
				if (--game.lives == 0) {
					game.energizerPulse.stop();
				}
				game.pac.hide();
			} else if (timer.hasExpired()) {
				if (game.credit == 0) {
					fsm.changeState(INTRO);
				} else {
					fsm.changeState(game.lives == 0 ? GAME_OVER : READY);
				}
			}
		}
	},

	GAME_OVER {
		@Override
		public void onEnter(GameModel game) {
			game.scores.saveHiscore();
			timer.resetSeconds(3);
			timer.start();
			game.sounds().ifPresent(GameSounds::stopAll);
		}

		@Override
		public void onUpdate(GameModel game) {
			if (timer.hasExpired()) {
				game.playing = false;
				game.consumeCredit();
				fsm.changeState(game.credit > 0 ? CREDIT : INTRO);
			}
		}
	},

	INTERMISSION {
		@Override
		public void onEnter(GameModel game) {
			timer.resetIndefinitely();
			timer.start(); // UI triggers state timeout
		}

		@Override
		public void onUpdate(GameModel game) {
			if (timer.hasExpired()) {
				fsm.changeState(game.credit == 0 || !game.playing ? INTRO : LEVEL_STARTING);
			}
		}
	},

	INTERMISSION_TEST {
		@Override
		public void onEnter(GameModel game) {
			timer.resetIndefinitely();
			timer.start();
		}

		@Override
		public void onUpdate(GameModel game) {
			if (timer.hasExpired()) {
				if (game.intermissionTestNumber < 3) {
					++game.intermissionTestNumber;
					timer.resetIndefinitely();
					timer.start();
					// This is a hack to trigger the UI to update its current scene
					GameEvents.publish(new GameStateChangeEvent(game, this, this));
				} else {
					fsm.changeState(INTRO);
				}
			}
		}
	};

	// -----------------------------------------------------------

	Fsm<GameState, GameModel> fsm;
	GameController gameController;
	TickTimer timer = new TickTimer("Timer-" + name());

	@Override
	public TickTimer timer() {
		return timer;
	}

	// --- Events

	public void selectGameVariant(GameVariant variant) {
		// override if supported for state
	}

	public void requestGame(GameModel game) {
		// override if supported for state
	}

	public void addCredit(GameModel game) {
		// override if supported for state
	}

	public void startIntermissionTest(GameModel game) {
		// override if supported for state
	}

	public void cheatEatAllPellets(GameModel game) {
		// override if supported for state
	}

	public void cheatKillAllEatableGhosts(GameModel game) {
		// override if supported for state
	}

	public void cheatEnterNextLevel(GameModel game) {
		// override if supported for state
	}

}