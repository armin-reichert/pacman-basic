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
import de.amr.games.pacman.lib.anim.EntityAnimation;
import de.amr.games.pacman.lib.fsm.FsmState;
import de.amr.games.pacman.lib.timer.TickTimer;
import de.amr.games.pacman.model.common.GameLevel;
import de.amr.games.pacman.model.common.GameModel;
import de.amr.games.pacman.model.common.GameSound;
import de.amr.games.pacman.model.common.GameVariant;
import de.amr.games.pacman.model.common.ScoreManager;
import de.amr.games.pacman.model.common.actors.AnimKeys;
import de.amr.games.pacman.model.common.actors.Creature;
import de.amr.games.pacman.model.common.actors.Ghost;
import de.amr.games.pacman.model.common.actors.GhostState;

/**
 * Rule of thumb: here, specify "what" and "when", not "how" (this should be implemented in the model).
 * 
 * @author Armin Reichert
 */
public enum GameState implements FsmState<GameModel>, GameCommands {

	BOOT() { // Boot steigt! Jawoll Herr Kaleu!
		@Override
		public void onEnter(GameModel game) {
			timer.restartIndefinitely();
			game.reset();
			game.clearLevelCounter();
			game.gameScore().reset();
			ScoreManager.loadScore(game.highScore(), game.variant());
		}

		@Override
		public void onUpdate(GameModel game) {
			if (timer.hasExpired()) {
				gc.changeState(INTRO);
			}
		}
	},

	INTRO() {
		@Override
		public void onEnter(GameModel game) {
			timer.restartIndefinitely();
			game.setPlaying(false);
		}

		@Override
		public void onUpdate(GameModel game) {
			if (timer.hasExpired()) {
				gc.changeState(READY);
			}
		}

		@Override
		public void selectGameVariant(GameVariant variant) {
			gc.createGame(variant);
		}

		@Override
		public void addCredit(GameModel game) {
			boolean added = game.changeCredit(1);
			if (added) {
				gc.sounds().play(GameSound.CREDIT);
			}
			gc.changeState(CREDIT);
		}

		@Override
		public void requestGame(GameModel game) {
			if (game.hasCredit()) {
				gc.changeState(READY);
			}
		}

		@Override
		public void startCutscenesTest(GameModel game) {
			gc.intermissionTestNumber = 1;
			gc.changeState(INTERMISSION_TEST);
		}
	},

	CREDIT {
		@Override
		public void onUpdate(GameModel context) {
			// nothing to do here
		}

		@Override
		public void addCredit(GameModel game) {
			boolean added = game.changeCredit(1);
			if (added) {
				gc.sounds().play(GameSound.CREDIT);
			}
		}

		@Override
		public void requestGame(GameModel game) {
			gc.changeState(READY);
		}
	},

	READY {
		@Override
		public void onEnter(GameModel game) {
			gc.sounds().stopAll();
			if (!game.hasCredit()) {
				gc.pacSteeringInAttractMode.init();
				game.enterDemoLevel();
			} else if (game.isPlaying()) {
				game.level().ifPresent(level -> level.letsGetReadyToRumbleAndShowGuys(true));
			} else {
				game.reset();
				game.buildAndEnterLevel(1);
				game.gameScore().reset();
				gc.sounds().play(GameSound.GAME_READY);
			}
		}

		@Override
		public void onUpdate(GameModel game) {
			final int showGuysTick = 130; // not sure
			game.level().ifPresent(level -> {
				if (game.hasCredit() && !game.isPlaying()) {
					// start new game
					if (timer.tick() == showGuysTick) {
						level.guys().forEach(Creature::show);
						game.setOneLessLifeDisplayed(true);
					} else if (timer.tick() == showGuysTick + 118) {
						// start playing
						game.setPlaying(true);
						level.startHuntingPhase(0);
						gc.changeState(GameState.HUNTING);
					}
				} else if (game.isPlaying()) {
					// game already running
					if (timer.tick() == 90) {
						level.guys().forEach(Creature::show);
						level.startHuntingPhase(0);
						gc.changeState(GameState.HUNTING);
					}
				} else {
					// attract mode
					if (timer.tick() == 130) {
						level.guys().forEach(Creature::show);
						level.startHuntingPhase(0);
						gc.changeState(GameState.HUNTING);
					}
				}
			});
		}
	},

	HUNTING {
		@Override
		public void onEnter(GameModel game) {
			game.level().ifPresent(level -> {
				if (!gc.levelTestMode) {
					int sirenIndex = level.huntingPhase() / 2;
					gc.sounds().ensureSirenStarted(sirenIndex);
				}
				level.energizerPulse().restart();
			});
		}

		@Override
		public void onUpdate(GameModel game) {
			game.level().ifPresent(level -> {
				if (gc.levelTestMode) {
					runLevelTestMode(level);
					return;
				}

				gc.steerPac(level);
				level.update();

				level.checkIfPacFoundFood();
				if (level.memo().lastFoodFound) {
					gc.changeState(LEVEL_COMPLETE);
					return;
				}

				level.checkTheGuys();
				if (level.memo().pacKilled) {
					gc.changeState(PACMAN_DYING);
					return;
				}

				if (level.memo().edibleGhostsExist()) {
					level.killEdibleGhosts();
					gc.changeState(GHOST_DYING);
					return;
				}

				level.renderSound(gc.sounds());
			});
		}

		@Override
		public void onExit(GameModel game) {
			game.level().ifPresent(level -> {
				level.renderSound(gc.sounds());
			});
		}

		private void runLevelTestMode(GameLevel level) {
			if (level.number() <= gc.levelTestLastLevelNumber) {
				var timer = gc.state().timer();
				// activate bonus for one second, then eat it / show won points
				if (timer.atSecond(0.0)) {
					level.game().onBonusReached();
				} else if (timer.atSecond(1.0)) {
					level.bonus().eat();
				} else if (timer.atSecond(2.0)) {
					gc.changeState(LEVEL_COMPLETE);
				}
				level.bonus().update(level);
			} else {
				// end level test mode
				gc.levelTestMode = false;
				gc.boot();
			}
		}

		@Override
		public void addCredit(GameModel game) {
			if (!game.isPlaying()) {
				boolean added = game.changeCredit(1);
				if (added) {
					gc.sounds().play(GameSound.CREDIT);
				}
				gc.changeState(CREDIT);
			}
		}

		@Override
		public void cheatEatAllPellets(GameModel game) {
			if (game.isPlaying()) {
				var level = game.level().get();
				var world = level.world();
				world.tiles().filter(not(world::isEnergizerTile)).forEach(world::removeFood);
				GameEvents.publish(GameEventType.PAC_FINDS_FOOD, null);
			}
		}

		@Override
		public void cheatKillAllEatableGhosts(GameModel game) {
			if (game.isPlaying()) {
				game.level().ifPresent(level -> {
					level.killAllPossibleGhosts();
					gc.changeState(GameState.GHOST_DYING);
				});
			}
		}

		@Override
		public void cheatEnterNextLevel(GameModel game) {
			if (game.isPlaying()) {
				game.level().ifPresent(level -> {
					var world = level.world();
					world.tiles().forEach(world::removeFood);
					gc.changeState(GameState.LEVEL_COMPLETE);
				});
			}
		}
	},

	LEVEL_COMPLETE {
		@Override
		public void onEnter(GameModel game) {
			timer.restartSeconds(4);
			gc.sounds().stopAll();
			game.level().ifPresent(GameLevel::exit);
			GameEvents.publish(GameEventType.UI_FORCE_UPDATE, null);
		}

		@Override
		public void onUpdate(GameModel game) {
			game.level().ifPresent(level -> {
				if (timer.hasExpired()) {
					if (!game.hasCredit()) {
						gc.changeState(INTRO);
						// attract mode -> back to intro scene
					} else if (level.params().intermissionNumber() > 0) {
						gc.changeState(INTERMISSION); // play intermission scene
					} else {
						gc.changeState(LEVEL_STARTING); // next level
					}
				} else {
					level.world().levelCompleteAnimation().ifPresent(animation -> {
						if (timer.atSecond(1)) {
							animation.setRepetitions(level.params().numFlashes());
							animation.restart();
						} else {
							animation.animate();
						}
					});
					level.pac().update(level);
				}
			});
		}
	},

	LEVEL_STARTING {
		@Override
		public void onEnter(GameModel game) {
			timer.restartSeconds(1);
			game.level().ifPresent(level -> game.buildAndEnterLevel(level.number() + 1));
			GameEvents.publish(GameEventType.UI_FORCE_UPDATE, null);
		}

		@Override
		public void onUpdate(GameModel game) {
			if (timer.hasExpired()) {
				gc.changeState(READY);
			}
		}
	},

	GHOST_DYING {
		@Override
		public void onEnter(GameModel game) {
			timer.restartSeconds(1);
			game.level().ifPresent(level -> {
				level.pac().hide();
				level.ghosts().forEach(ghost -> ghost.pauseFlashing(true));
				gc.sounds().play(GameSound.GHOST_EATEN);
			});
		}

		@Override
		public void onUpdate(GameModel game) {
			if (timer.hasExpired()) {
				gc.resumePreviousState();
			} else {
				game.level().ifPresent(level -> {
					gc.steering(level).steer(level, level.pac());
					level.ghosts(GhostState.EATEN, GhostState.RETURNING_TO_HOUSE, GhostState.ENTERING_HOUSE)
							.forEach(ghost -> ghost.update(level));
					level.energizerPulse().animate();
				});
			}
		}

		@Override
		public void onExit(GameModel game) {
			game.level().ifPresent(level -> {
				level.pac().show();
				level.ghosts(GhostState.EATEN).forEach(ghost -> ghost.enterStateReturningToHouse(level));
				level.ghosts().forEach(ghost -> ghost.pauseFlashing(false));
			});
		}
	},

	PACMAN_DYING {
		@Override
		public void onEnter(GameModel game) {
			game.level().ifPresent(level -> {
				timer.restartSeconds(4);
				gc.sounds().stopAll();
				level.bonus().setInactive();
				level.onPacKilled();
			});
		}

		@Override
		public void onUpdate(GameModel game) {
			game.level().ifPresent(level -> {
				level.energizerPulse().animate();
				level.pac().update(level);
				if (timer.betweenSeconds(0, 1)) {
					level.ghosts().forEach(Ghost::animate);
				} else if (timer.atSecond(1)) {
					level.pac().selectAndResetAnimation(AnimKeys.PAC_DYING);
					level.ghosts().forEach(Ghost::hide);
				} else if (timer.atSecond(1.4)) {
					level.pac().animation().ifPresent(EntityAnimation::start);
					gc.sounds().play(GameSound.PACMAN_DEATH);
				} else if (timer.atSecond(3.0)) {
					game.setLives(game.lives() - 1);
					if (game.lives() == 0) {
						level.energizerPulse().stop();
						game.setOneLessLifeDisplayed(false);
					}
					level.pac().hide();
				} else if (timer.hasExpired()) {
					if (!game.hasCredit()) {
						gc.changeState(INTRO);
					} else {
						gc.changeState(game.lives() == 0 ? GAME_OVER : READY);
					}
				}
			});
		}
	},

	GAME_OVER {
		@Override
		public void onEnter(GameModel game) {
			timer.restartSeconds(1.2);
			gc.sounds().stopAll();
			game.changeCredit(-1);
			ScoreManager.saveHiscore(game.highScore(), game.variant());
		}

		@Override
		public void onUpdate(GameModel game) {
			if (timer.hasExpired()) {
				gc.changeState(game.hasCredit() ? CREDIT : INTRO);
			}
		}

		@Override
		public void onExit(GameModel game) {
			game.setPlaying(false);
		}
	},

	INTERMISSION {
		@Override
		public void onEnter(GameModel game) {
			timer.restartIndefinitely();
		}

		@Override
		public void onUpdate(GameModel game) {
			if (timer.hasExpired()) {
				gc.changeState(!game.hasCredit() || !game.isPlaying() ? INTRO : LEVEL_STARTING);
			}
		}
	},

	INTERMISSION_TEST {
		@Override
		public void onEnter(GameModel game) {
			timer.restartIndefinitely();
		}

		@Override
		public void onUpdate(GameModel game) {
			if (timer.hasExpired()) {
				if (gc.intermissionTestNumber < 3) {
					++gc.intermissionTestNumber;
					timer.restartIndefinitely();
					GameEvents.publish(GameEventType.UI_FORCE_UPDATE, null);
				} else {
					gc.intermissionTestNumber = 1;
					gc.changeState(INTRO);
				}
			}
		}
	};

	/* package */ GameController gc;
	/* package */ final TickTimer timer = new TickTimer("Timer-" + name());

	@Override
	public TickTimer timer() {
		return timer;
	}
}