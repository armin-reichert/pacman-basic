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

import de.amr.games.pacman.event.GameEvent;
import de.amr.games.pacman.event.GameEventType;
import de.amr.games.pacman.event.GameEvents;
import de.amr.games.pacman.lib.TickTimer;
import de.amr.games.pacman.lib.animation.EntityAnimation;
import de.amr.games.pacman.lib.fsm.FsmState;
import de.amr.games.pacman.model.common.GameModel;
import de.amr.games.pacman.model.common.GameSound;
import de.amr.games.pacman.model.common.GameVariant;
import de.amr.games.pacman.model.common.ScoreManager;
import de.amr.games.pacman.model.common.actors.AnimKeys;
import de.amr.games.pacman.model.common.actors.Creature;
import de.amr.games.pacman.model.common.actors.Entity;
import de.amr.games.pacman.model.common.actors.Ghost;
import de.amr.games.pacman.model.common.actors.GhostState;
import de.amr.games.pacman.model.common.world.ArcadeWorld;

/**
 * Rule of thumb: here, specify the "what" and "when", not the "how" (which should be implemented in the model).
 * 
 * @author Armin Reichert
 */
public enum GameState implements FsmState<GameModel>, GameCommands {

	BOOT() {
		@Override
		public void onEnter(GameModel game) {
			timer.resetIndefinitely();
			timer.start();
			game.levelCounter().clear();
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
			timer.resetIndefinitely();
			timer.start();
			game.reset();
			game.setLevel(1);
		}

		@Override
		public void onUpdate(GameModel game) {
			if (timer.hasExpired()) {
				gc.changeState(READY);
			}
		}

		@Override
		public void selectGameVariant(GameVariant variant) {
			gc.selectGame(variant);
		}

		@Override
		public void addCredit(GameModel game) {
			boolean added = game.addCredit();
			if (added) {
				gc.sounds().play(GameSound.CREDIT);
			}
			gc.changeState(CREDIT);
		}

		@Override
		public void requestGame(GameModel game) {
			if (game.hasCredit()) {
				game.reset();
				game.setLevel(1);
				gc.changeState(READY);
			}
		}

		@Override
		public void startCutscenesTest(GameModel game) {
			INTERMISSION_TEST.intermissionTestNumber = 1;
			gc.changeState(INTERMISSION_TEST);
		}
	},

	CREDIT {
		@Override
		public void onEnter(GameModel game) {
			game.gameScore().showContent = false;
			game.highScore().showContent = true;
		}

		@Override
		public void onUpdate(GameModel context) {
			// nothing to do here
		}

		@Override
		public void addCredit(GameModel game) {
			boolean added = game.addCredit();
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
			if (game.hasCredit() && !game.isPlaying()) {
				getReadyForNewGame(game);
			} else if (game.hasCredit() && game.isPlaying()) {
				continueGame(game);
			} else {
				enterAttractMode(game);
			}
		}

		private void getReadyForNewGame(GameModel game) {
			gc.sounds().play(GameSound.GAME_READY);
			game.reset();
			game.setLevel(1);
			game.levelCounter().clear();
			game.levelCounter().addSymbol(game.level().bonusIndex());
			game.enableScores(true);
			game.gameScore().showContent = true;
			game.resetGuys();
			game.guys().forEach(Creature::hide);
		}

		private void continueGame(GameModel game) {
			game.resetGuys();
			game.guys().forEach(Creature::show);
		}

		private void enterAttractMode(GameModel game) {
			game.resetGuys();
			game.guys().forEach(Creature::show);
			game.enableScores(false);
			game.gameScore().showContent = false;
			gc.attractModeSteering.init();
//			game.isPacImmune = true;
		}

		@Override
		public void onUpdate(GameModel game) {
			if (game.hasCredit() && !game.isPlaying()) {
				// about to start game
				if (timer.tick() == 130) {
					game.guys().forEach(Entity::show);
					game.setLivesOneLessShown(true); // TODO this cannot be the last word
				} else if (timer.tick() == 250) {
					// start game
					game.setPlaying(true);
					game.startHuntingPhase(0);
					gc.changeState(GameState.HUNTING);
				}
			} else {
				// game continuing or attract mode
				if (timer.tick() == 92) {
					game.startHuntingPhase(0);
					gc.changeState(GameState.HUNTING);
				}
			}
		}
	},

	HUNTING {
		@Override
		public void onEnter(GameModel game) {
			game.pac().selectAndEnsureRunningAnimation(AnimKeys.PAC_MUNCHING);
			game.energizerPulse.restart();
			gc.sounds().ensureSirenStarted(game.huntingTimer().phase() / 2);
		}

		@Override
		public void onUpdate(GameModel game) {
			game.memo.forgetEverything(); // ich scholze jetzt

			game.whatHappenedWithFood();
			if (game.memo.allFoodEaten) {
				renderSound(game);
				gc.changeState(LEVEL_COMPLETE);
				return;
			}

			game.whatHappenedWithTheGuys();
			if (game.memo.pacMetKiller) {
				renderSound(game);
				gc.changeState(PACMAN_DYING);
				return;
			}

			if (game.memo.ghostsKilled) {
				renderSound(game);
				gc.changeState(GHOST_DYING);
				return;
			}

			gc.getSteering().steer(game, game.pac());
			game.update();
			renderSound(game);
		}

		private void renderSound(GameModel game) {
			var snd = gc.sounds();
			if (game.huntingTimer().tick() == 0) {
				snd.ensureSirenStarted(game.huntingTimer().phase() / 2);
			}
			if (game.memo.pacGotPower) {
				snd.stopSirens();
				snd.ensureLoop(GameSound.PACMAN_POWER, GameSoundController.LOOP_FOREVER);
			}
			if (game.memo.pacPowerLost) {
				snd.stop(GameSound.PACMAN_POWER);
				snd.ensureSirenStarted(game.huntingTimer().phase() / 2);
			}
			if (game.memo.foodFound) {
				snd.ensureLoop(GameSound.PACMAN_MUNCH, GameSoundController.LOOP_FOREVER);
			}
			if (game.pac().starvingTime() >= 12) { // ???
				snd.stop(GameSound.PACMAN_MUNCH);
			}
			if (game.ghosts(GhostState.RETURNING_TO_HOUSE).count() > 0) {
				if (!snd.isPlaying(GameSound.GHOST_RETURNING)) {
					snd.loop(GameSound.GHOST_RETURNING, GameSoundController.LOOP_FOREVER);
				}
			} else {
				snd.stop(GameSound.GHOST_RETURNING);
			}
		}

		@Override
		public void addCredit(GameModel game) {
			if (!game.isPlaying()) {
				boolean added = game.addCredit();
				if (added) {
					gc.sounds().play(GameSound.CREDIT);
				}
				gc.changeState(CREDIT);
			}
		}

		@Override
		public void cheatEatAllPellets(GameModel game) {
			if (game.isPlaying()) {
				var world = game.level().world();
				world.tiles().filter(not(world::isEnergizerTile)).forEach(world::removeFood);
				GameEvents.publish(GameEventType.PAC_FINDS_FOOD, null);
			}
		}

		@Override
		public void cheatKillAllEatableGhosts(GameModel game) {
			if (game.isPlaying()) {
				game.killAllPossibleGhosts();
				gc.changeState(GameState.GHOST_DYING);
			}
		}

		@Override
		public void cheatEnterNextLevel(GameModel game) {
			if (game.isPlaying()) {
				var world = game.level().world();
				world.tiles().forEach(world::removeFood);
				gc.changeState(GameState.LEVEL_COMPLETE);
			}
		}
	},

	LEVEL_COMPLETE {
		@Override
		public void onEnter(GameModel game) {
			timer.resetSeconds(4);
			timer.start();
			gc.sounds().stopAll();
			game.endLevel();
			// force UI update to ensure maze flashing animation is up to date
			GameEvents.publish(GameEventType.UI_FORCE_UPDATE, null);
		}

		@Override
		public void onUpdate(GameModel game) {
			if (timer.hasExpired()) {
				if (!game.hasCredit()) {
					gc.changeState(INTRO);
				} else if (game.intermissionNumber(game.level().number()) != 0) {
					gc.changeState(INTERMISSION);
				} else {
					gc.changeState(LEVEL_STARTING);
				}
				return;
			}
			var world = (ArcadeWorld) game.level().world();
			if (timer.atSecond(1)) {
				world.levelCompleteAnimation().ifPresent(mazeFlashing -> {
					mazeFlashing.setRepetitions(game.level().numFlashes());
					mazeFlashing.restart();
				});
			}
			world.levelCompleteAnimation().ifPresent(EntityAnimation::advance);
		}
	},

	LEVEL_STARTING {
		@Override
		public void onEnter(GameModel game) {
			timer.resetSeconds(1);
			timer.start();
			game.setLevel(game.level().number() + 1);
			game.startLevel();
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
			timer.resetSeconds(1);
			timer.start();
			game.pac().hide();
			game.ghosts().forEach(ghost -> ghost.pauseFlashing(true));
			gc.sounds().play(GameSound.GHOST_EATEN);
		}

		@Override
		public void onUpdate(GameModel game) {
			if (timer.hasExpired()) {
				gc.resumePreviousState();
			} else {
				gc.getSteering().steer(game, game.pac());
				game.ghosts(GhostState.EATEN, GhostState.RETURNING_TO_HOUSE).forEach(ghost -> ghost.update(game));
				game.energizerPulse.advance();
			}
		}

		@Override
		public void onExit(GameModel game) {
			game.pac().show();
			game.ghosts(GhostState.EATEN).forEach(ghost -> ghost.enterStateReturningToHouse(game));
			game.ghosts().forEach(ghost -> ghost.pauseFlashing(false));
		}
	},

	PACMAN_DYING {
		@Override
		public void onEnter(GameModel game) {
			timer.resetSeconds(4);
			timer.start();
			game.pac().selectedAnimation().ifPresent(EntityAnimation::stop);
			game.bonus().setInactive();
			gc.sounds().stopAll();
		}

		@Override
		public void onUpdate(GameModel game) {
			game.energizerPulse.advance();
			game.pac().update(game);
			if (timer.betweenSeconds(0, 1)) {
				game.ghosts().forEach(Ghost::advanceAnimation);
			}
			if (timer.atSecond(0.25)) {
				game.pac().selectAndResetAnimation(AnimKeys.PAC_DYING);
			} else if (timer.atSecond(1)) {
				game.ghosts().forEach(Ghost::hide);
			} else if (timer.atSecond(1.4)) {
				game.pac().selectedAnimation().ifPresent(EntityAnimation::restart);
				gc.sounds().play(GameSound.PACMAN_DEATH);
			} else if (timer.atSecond(3.0)) {
				game.setLives(game.lives() - 1);
				if (game.lives() == 0) {
					game.energizerPulse.stop();
					game.setLivesOneLessShown(false);
				}
				game.pac().hide();
			} else if (timer.hasExpired()) {
				if (!game.hasCredit()) {
					gc.changeState(INTRO);
				} else {
					gc.changeState(game.lives() == 0 ? GAME_OVER : READY);
				}
			}
		}
	},

	GAME_OVER {
		@Override
		public void onEnter(GameModel game) {
			timer.resetSeconds(1.5);
			timer.start();
			gc.sounds().stopAll();
			game.consumeCredit();
			ScoreManager.saveHiscore(game.highScore(), game.highScoreFile(), game.variant());
		}

		@Override
		public void onUpdate(GameModel game) {
			if (timer.hasExpired()) {
				game.setPlaying(false);
				gc.changeState(game.hasCredit() ? CREDIT : INTRO);
			}
		}

		@Override
		public void onExit(GameModel game) {
			game.reset();
			game.setLevel(1);
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
				gc.changeState(!game.hasCredit() || !game.isPlaying() ? INTRO : LEVEL_STARTING);
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
				if (intermissionTestNumber < 3) {
					++intermissionTestNumber;
					timer.resetIndefinitely();
					timer.start();
					GameEvents.publish(new GameEvent(game, GameEventType.UI_FORCE_UPDATE, null, null));
				} else {
					intermissionTestNumber = 1;
					gc.changeState(INTRO);
				}
			}
		}
	};

	GameController gc;
	final TickTimer timer = new TickTimer("Timer-" + name(), GameModel.FPS);

	// only used by state INTERMISSION_TEST
	public int intermissionTestNumber;

	@Override
	public TickTimer timer() {
		return timer;
	}
}