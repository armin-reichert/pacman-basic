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

import static de.amr.games.pacman.event.GameEvents.publishGameEventOfType;
import static de.amr.games.pacman.event.GameEvents.publishSoundEvent;

import de.amr.games.pacman.event.GameEventType;
import de.amr.games.pacman.event.GameEvents;
import de.amr.games.pacman.lib.anim.AnimKeys;
import de.amr.games.pacman.lib.anim.EntityAnimation;
import de.amr.games.pacman.lib.fsm.FsmState;
import de.amr.games.pacman.lib.timer.TickTimer;
import de.amr.games.pacman.model.common.GameLevel;
import de.amr.games.pacman.model.common.GameModel;
import de.amr.games.pacman.model.common.GameVariant;
import de.amr.games.pacman.model.common.actors.Creature;
import de.amr.games.pacman.model.common.actors.Ghost;
import de.amr.games.pacman.model.common.actors.GhostState;
import de.amr.games.pacman.model.common.world.ArcadeWorld;

/**
 * Rule of thumb: here, specify "what" and "when", not "how" (this should be implemented in the model).
 * 
 * @author Armin Reichert
 */
public enum GameState implements FsmState<GameModel>, GameCommands {

	BOOT() { // "Das muss das Boot abkönnen!"
		@Override
		public void onEnter(GameModel game) {
			timer.restartIndefinitely();
			game.clearLevelCounter();
			game.newScore();
			game.loadHighscore();
			publishSoundEvent("stop_all_sounds");
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
			game.removeLevel();
			publishSoundEvent("stop_all_sounds");
		}

		@Override
		public void onUpdate(GameModel game) {
			if (timer.hasExpired()) {
				gc.changeState(READY);
			}
		}

		@Override
		public void selectGameVariant(GameVariant variant) {
			gc.selectGameVariant(variant);
			gc.boot();
		}

		@Override
		public void addCredit(GameModel game) {
			boolean added = game.changeCredit(1);
			if (added) {
				publishSoundEvent("credit_added");
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
		public void onUpdate(GameModel game) {
			// nothing to do here
		}

		@Override
		public void addCredit(GameModel game) {
			boolean added = game.changeCredit(1);
			if (added) {
				publishSoundEvent("credit_added");
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
			publishSoundEvent("stop_all_sounds");
			if (!game.hasCredit()) {
				game.init();
				game.enterDemoLevel();
				publishGameEventOfType(GameEventType.LEVEL_STARTING);
			} else if (game.isPlaying()) {
				game.level().ifPresent(level -> level.letsGetReadyToRumbleAndShowGuys(true));
			} else {
				game.init();
				game.newScore();
				game.clearLevelCounter();
				game.enterLevel(1);
				publishSoundEvent("ready_to_play");
				publishGameEventOfType(GameEventType.LEVEL_STARTING);
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
					} else if (timer.tick() == showGuysTick + 100) {
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
				publishSoundEvent("hunting_phase_started_%d".formatted(level.huntingPhase()));
				level.world().animation(ArcadeWorld.ENERGIZER_PULSE).ifPresent(EntityAnimation::restart);
			});
		}

		@Override
		public void onUpdate(GameModel game) {
			game.level().ifPresent(level -> {
				// TODO this looks ugly
				var steering = level.pacSteering().orElse(gc.steering());
				steering.steer(level, level.pac());

				level.update();
				if (level.completed()) {
					gc.changeState(LEVEL_COMPLETE);
				} else if (level.pacKilled()) {
					gc.changeState(PACMAN_DYING);
				} else if (level.memo().edibleGhostsExist()) {
					level.killEdibleGhosts();
					gc.changeState(GHOST_DYING);
				}
			});
		}

		@Override
		public void addCredit(GameModel game) {
			if (!game.isPlaying()) {
				boolean added = game.changeCredit(1);
				if (added) {
					publishSoundEvent("credit_added");
				}
				gc.changeState(CREDIT);
			}
		}

		@Override
		public void cheatEatAllPellets(GameModel game) {
			if (game.isPlaying()) {
				game.level().ifPresent(GameLevel::removeAllPellets);
			}
		}

		@Override
		public void cheatKillAllEatableGhosts(GameModel game) {
			if (game.isPlaying()) {
				game.level().ifPresent(level -> {
					level.killAllHuntingAndFrightenedGhosts();
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
			game.level().ifPresent(GameLevel::exit);
			publishSoundEvent("stop_all_sounds");
			publishGameEventOfType(GameEventType.UNSPECIFIED_CHANGE);
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
						gc.changeState(CHANGING_TO_NEXT_LEVEL); // next level
					}
				} else {
					level.world().animation(ArcadeWorld.FLASHING).ifPresent(flashing -> {
						if (timer.atSecond(1)) {
							flashing.setRepetitions(level.params().numFlashes());
							flashing.restart();
						} else {
							flashing.animate();
						}
					});
					level.pac().update(level);
				}
			});
		}
	},

	CHANGING_TO_NEXT_LEVEL {
		@Override
		public void onEnter(GameModel game) {
			timer.restartSeconds(1);
			game.nextLevel();
			publishGameEventOfType(GameEventType.LEVEL_STARTING);
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
				level.ghosts().forEach(ghost -> ghost.stopFlashing(true));
				publishSoundEvent("ghost_eaten");
			});
		}

		@Override
		public void onUpdate(GameModel game) {
			if (timer.hasExpired()) {
				gc.resumePreviousState();
			} else {
				game.level().ifPresent(level -> {
					var steering = level.pacSteering().orElse(gc.steering());
					steering.steer(level, level.pac());
					level.ghosts(GhostState.EATEN, GhostState.RETURNING_TO_HOUSE, GhostState.ENTERING_HOUSE)
							.forEach(ghost -> ghost.update(level));
					level.world().animation(ArcadeWorld.ENERGIZER_PULSE).ifPresent(EntityAnimation::animate);
				});
			}
		}

		@Override
		public void onExit(GameModel game) {
			game.level().ifPresent(level -> {
				level.pac().show();
				level.ghosts(GhostState.EATEN).forEach(ghost -> ghost.enterStateReturningToHouse(level));
				level.ghosts().forEach(ghost -> ghost.stopFlashing(false));
			});
		}
	},

	PACMAN_DYING {
		@Override
		public void onEnter(GameModel game) {
			game.level().ifPresent(level -> {
				timer.restartSeconds(4);
				level.onPacKilled();
				publishSoundEvent("stop_all_sounds");
			});
		}

		@Override
		public void onUpdate(GameModel game) {
			game.level().ifPresent(level -> {
				if (timer.atSecond(1)) {
					level.pac().selectAndResetAnimation(AnimKeys.PAC_DYING);
					level.ghosts().forEach(Ghost::hide);
				} else if (timer.atSecond(1.4)) {
					level.pac().startAnimation();
					publishSoundEvent("pacman_death");
				} else if (timer.atSecond(3.0)) {
					level.pac().hide();
					game.setLives(game.lives() - 1);
					if (game.lives() == 0) {
						level.world().animation(ArcadeWorld.ENERGIZER_PULSE).ifPresent(EntityAnimation::stop);
						game.setOneLessLifeDisplayed(false);
					}
				} else if (timer.hasExpired()) {
					if (!game.hasCredit()) {
						// end of demo level
						GameEvents.setSoundEventsEnabled(true);
						gc.changeState(INTRO);
					} else {
						gc.changeState(game.lives() == 0 ? GAME_OVER : READY);
					}
				} else {
					level.world().animation(ArcadeWorld.ENERGIZER_PULSE).ifPresent(EntityAnimation::animate);
					level.pac().update(level);
					level.ghosts().forEach(Ghost::animate);
				}
			});
		}

		@Override
		public void onExit(GameModel context) {
			context.level().ifPresent(level -> {
				level.bonus().setInactive();
			});
		}
	},

	GAME_OVER {
		@Override
		public void onEnter(GameModel game) {
			timer.restartSeconds(1.2);
			game.changeCredit(-1);
			game.saveNewHighscore();
			publishSoundEvent("stop_all_sounds");
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
			game.removeLevel();
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
				gc.changeState(!game.hasCredit() || !game.isPlaying() ? INTRO : CHANGING_TO_NEXT_LEVEL);
			}
		}
	},

	LEVEL_TEST {

		private int lastTestedLevel;

		@Override
		public void onEnter(GameModel game) {
			lastTestedLevel = switch (game.variant()) {
			case MS_PACMAN -> 8;
			case PACMAN -> 20;
			};
			timer.restartIndefinitely();
			game.init();
			game.enterLevel(1);
			publishGameEventOfType(GameEventType.LEVEL_STARTING);
		}

		@Override
		public void onUpdate(GameModel game) {
			game.level().ifPresent(level -> {
				if (level.number() <= lastTestedLevel) {
					if (timer.atSecond(0.5)) {
						level.guys().forEach(Creature::show);
					} else if (timer.atSecond(1.5)) {
						level.game().onBonusReached();
					} else if (timer.atSecond(2.5)) {
						level.bonus().eat();
						level.guys().forEach(Creature::hide);
					} else if (timer.atSecond(4.5)) {
						level.world().animation(ArcadeWorld.FLASHING).ifPresent(flashing -> {
							flashing.setRepetitions(level.params().numFlashes());
							flashing.restart();
						});
					} else if (timer.atSecond(6.0)) {
						level.exit();
						game.nextLevel();
						timer.restartIndefinitely();
						publishGameEventOfType(GameEventType.LEVEL_STARTING);
					}
					level.world().animation(ArcadeWorld.ENERGIZER_PULSE).ifPresent(EntityAnimation::animate);
					level.world().animation(ArcadeWorld.FLASHING).ifPresent(flashing -> flashing.animate());
					level.ghosts().forEach(ghost -> ghost.update(level));
					level.bonus().update(level);
				} else {
					gc.boot();
				}
			});
		}

		@Override
		public void onExit(GameModel game) {
			game.clearLevelCounter();
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
					publishGameEventOfType(GameEventType.UNSPECIFIED_CHANGE);
				} else {
					gc.intermissionTestNumber = 1;
					gc.changeState(INTRO);
				}
			}
		}
	};

	GameController gc;
	final TickTimer timer = new TickTimer("Timer-" + name());

	@Override
	public TickTimer timer() {
		return timer;
	}
}