/*
Copyright (c) 2021-2023 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.games.pacman.controller;

import static de.amr.games.pacman.event.GameEvents.publishGameEventOfType;
import static de.amr.games.pacman.event.GameEvents.publishSoundEvent;

import de.amr.games.pacman.event.GameEventType;
import de.amr.games.pacman.event.GameEvents;
import de.amr.games.pacman.lib.fsm.FsmState;
import de.amr.games.pacman.lib.timer.TickTimer;
import de.amr.games.pacman.model.GameLevel;
import de.amr.games.pacman.model.GameModel;
import de.amr.games.pacman.model.actors.Creature;
import de.amr.games.pacman.model.actors.Ghost;
import de.amr.games.pacman.model.actors.GhostState;
import de.amr.games.pacman.model.actors.PacAnimations;

/**
 * Rule of thumb: here, specify "what" and "when", not "how" (this should be implemented in the model).
 * 
 * @author Armin Reichert
 */
public enum GameState implements FsmState<GameModel> {

	BOOT() { // "Das muss das Boot abkönnen!"
		@Override
		public void onEnter(GameModel game) {
			timer.restartIndefinitely();
			game.clearLevelCounter();
			game.score().reset();
			game.loadHighscore();
			publishSoundEvent(GameModel.SE_STOP_ALL_SOUNDS, game);
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
			publishSoundEvent(GameModel.SE_STOP_ALL_SOUNDS, game);
		}

		@Override
		public void onUpdate(GameModel game) {
			if (timer.hasExpired()) {
				gc.changeState(READY);
			}
		}
	},

	CREDIT {
		@Override
		public void onUpdate(GameModel game) {
			// nothing to do here
		}
	},

	READY {
		@Override
		public void onEnter(GameModel game) {
			gc.getManualPacSteering().setEnabled(false);
			publishSoundEvent(GameModel.SE_STOP_ALL_SOUNDS, game);
			if (!game.hasCredit()) {
				game.start();
				game.enterDemoLevel();
				publishGameEventOfType(GameEventType.LEVEL_STARTED, game);
			} else if (game.isPlaying()) {
				game.level().ifPresent(level -> level.letsGetReadyToRumbleAndShowGuys(true));
			} else {
				game.start();
				game.score().reset();
				game.clearLevelCounter();
				game.enterLevel(1);
				publishSoundEvent(GameModel.SE_READY_TO_PLAY, game);
				publishGameEventOfType(GameEventType.LEVEL_STARTED, game);
			}
		}

		@Override
		public void onUpdate(GameModel game) {
			final int showGuysTick = 120; // not sure
			game.level().ifPresent(level -> {
				if (game.hasCredit() && !game.isPlaying()) {
					// start new game
					if (timer.tick() == showGuysTick) {
						level.guys().forEach(Creature::show);
						game.setOneLessLifeDisplayed(true);
					} else if (timer.tick() == showGuysTick + 120) {
						// start playing
						game.setPlaying(true);
						level.startHunting(0);
						gc.changeState(GameState.HUNTING);
					}
				} else if (game.isPlaying()) {
					// game already running
					if (timer.tick() == 90) {
						level.guys().forEach(Creature::show);
						level.startHunting(0);
						gc.changeState(GameState.HUNTING);
					}
				} else {
					// attract mode
					if (timer.tick() == 130) {
						level.guys().forEach(Creature::show);
						level.startHunting(0);
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
				gc.getManualPacSteering().setEnabled(true);
				switch (level.huntingPhase()) {
				case 0:
					publishSoundEvent(GameModel.SE_HUNTING_PHASE_STARTED_0, game);
					break;
				case 2:
					publishSoundEvent(GameModel.SE_HUNTING_PHASE_STARTED_2, game);
					break;
				case 4:
					publishSoundEvent(GameModel.SE_HUNTING_PHASE_STARTED_4, game);
					break;
				case 6:
					publishSoundEvent(GameModel.SE_HUNTING_PHASE_STARTED_6, game);
					break;
				default:
					break;
				}
				level.pac().startAnimation();
				level.ghosts().forEach(Ghost::startAnimation);
				level.world().energizerBlinking().restart();
			});
		}

		@Override
		public void onUpdate(GameModel game) {
			game.level().ifPresent(level -> {
				// TODO this looks ugly
				var steering = level.pacSteering().orElse(gc.steering());
				steering.steer(level, level.pac());
				level.update();
				level.world().energizerBlinking().tick();
				if (level.isCompleted()) {
					gc.changeState(LEVEL_COMPLETE);
				} else if (level.pacKilled()) {
					gc.changeState(PACMAN_DYING);
				} else if (level.memo().edibleGhostsExist()) {
					level.killEdibleGhosts();
					gc.changeState(GHOST_DYING);
				}
			});
		}
	},

	LEVEL_COMPLETE {
		@Override
		public void onEnter(GameModel game) {
			gc.getManualPacSteering().setEnabled(false);
			timer.restartSeconds(4);
			game.level().ifPresent(GameLevel::exit);
			publishSoundEvent(GameModel.SE_STOP_ALL_SOUNDS, game);
		}

		@Override
		public void onUpdate(GameModel game) {
			game.level().ifPresent(level -> {
				if (timer.hasExpired()) {
					if (!game.hasCredit()) {
						gc.changeState(INTRO);
						// attract mode -> back to intro scene
					} else if (level.intermissionNumber > 0) {
						gc.changeState(INTERMISSION); // play intermission scene
					} else {
						gc.changeState(CHANGING_TO_NEXT_LEVEL); // next level
					}
				} else {
					level.pac().stopAnimation();
					level.pac().resetAnimation();
					var flashing = level.world().mazeFlashing();
					if (timer.atSecond(1)) {
						flashing.restart(2 * level.numFlashes);
					} else {
						flashing.tick();
					}
					level.pac().update(level);
				}
			});
		}
	},

	CHANGING_TO_NEXT_LEVEL {
		@Override
		public void onEnter(GameModel game) {
			gc.getManualPacSteering().setEnabled(false);
			timer.restartSeconds(1);
			game.nextLevel();
			publishGameEventOfType(GameEventType.LEVEL_STARTED, game);
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
				level.ghosts().forEach(ghost -> ghost.animations().ifPresent(ani -> ani.stopSelected()));
				publishSoundEvent(GameModel.SE_GHOST_EATEN, game);
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
							.forEach(ghost -> ghost.update());
					level.world().energizerBlinking().tick();
				});
			}
		}

		@Override
		public void onExit(GameModel game) {
			game.level().ifPresent(level -> {
				level.pac().show();
				level.ghosts(GhostState.EATEN).forEach(ghost -> ghost.enterStateReturningToHouse());
				level.ghosts().forEach(ghost -> ghost.animations().ifPresent(ani -> ani.startSelected()));
			});
		}
	},

	PACMAN_DYING {
		@Override
		public void onEnter(GameModel game) {
			game.level().ifPresent(level -> {
				gc.getManualPacSteering().setEnabled(false);
				timer.restartSeconds(4);
				level.onPacKilled();
				publishSoundEvent(GameModel.SE_STOP_ALL_SOUNDS, game);
			});
		}

		@Override
		public void onUpdate(GameModel game) {
			game.level().ifPresent(level -> {
				if (timer.atSecond(1)) {
					level.pac().selectAnimation(PacAnimations.DYING);
					level.pac().resetAnimation();
					level.ghosts().forEach(Ghost::hide);
				} else if (timer.atSecond(1.4)) {
					level.pac().startAnimation();
					publishSoundEvent(GameModel.SE_PACMAN_DEATH, game);
				} else if (timer.atSecond(3.0)) {
					level.pac().hide();
					game.setLives(game.lives() - 1);
					if (game.lives() == 0) {
						level.world().mazeFlashing().stop();
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
					level.world().energizerBlinking().tick();
					level.pac().update(level);
				}
			});
		}

		@Override
		public void onExit(GameModel context) {
			context.level().ifPresent(level -> level.bonusManagement().deactivateBonus());
		}
	},

	GAME_OVER {
		@Override
		public void onEnter(GameModel game) {
			gc.getManualPacSteering().setEnabled(false);
			timer.restartSeconds(1.2);
			game.changeCredit(-1);
			game.saveNewHighscore();
			publishSoundEvent(GameModel.SE_STOP_ALL_SOUNDS, game);
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
			switch (game.variant()) {
			case MS_PACMAN:
				lastTestedLevel = 18;
				break;
			case PACMAN:
				lastTestedLevel = 20;
				break;
			default:
				break;
			}
			timer.restartIndefinitely();
			game.start();
			game.enterLevel(1);
			publishGameEventOfType(GameEventType.LEVEL_STARTED, game);
		}

		@Override
		public void onUpdate(GameModel game) {
			game.level().ifPresent(level -> {
				if (level.number() <= lastTestedLevel) {
					if (timer.atSecond(0.5)) {
						level.guys().forEach(Creature::show);
					} else if (timer.atSecond(1.5)) {
						level.bonusManagement().handleBonusReached(0);
					} else if (timer.atSecond(2.5)) {
						level.bonusManagement().getBonus().get().eat();
						publishSoundEvent(GameModel.SE_BONUS_EATEN, game);
					} else if (timer.atSecond(4.5)) {
						level.bonusManagement().handleBonusReached(1);
					} else if (timer.atSecond(5.5)) {
						level.bonusManagement().getBonus().get().eat();
						level.guys().forEach(Creature::hide);
					} else if (timer.atSecond(6.5)) {
						var flashing = level.world().mazeFlashing();
						flashing.restart(2 * level.numFlashes);
					} else if (timer.atSecond(12.0)) {
						level.exit();
						game.nextLevel();
						timer.restartIndefinitely();
						publishGameEventOfType(GameEventType.LEVEL_STARTED, game);
					}
					level.world().energizerBlinking().tick();
					level.world().mazeFlashing().tick();
					level.ghosts().forEach(ghost -> ghost.update());
					level.bonusManagement().updateBonus();
				} else {
					gc.restart(GameState.BOOT);
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
				if (game.intermissionTestNumber < 3) {
					++game.intermissionTestNumber;
					timer.restartIndefinitely();
					publishGameEventOfType(GameEventType.UNSPECIFIED_CHANGE, game);
				} else {
					game.intermissionTestNumber = 1;
					gc.changeState(INTRO);
				}
			}
		}
	};

	/** The game controller hosting this state. */
	GameController gc;

	/** The timer of this state. */
	final TickTimer timer = new TickTimer("Timer-" + name());

	@Override
	public TickTimer timer() {
		return timer;
	}
}