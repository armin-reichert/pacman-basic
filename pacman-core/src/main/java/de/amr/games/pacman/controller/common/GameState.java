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

import static de.amr.games.pacman.lib.Logging.log;
import static de.amr.games.pacman.model.common.actors.GhostAnimationKey.ANIM_FLASHING;
import static de.amr.games.pacman.model.common.actors.GhostAnimationKey.ANIM_VALUE;
import static de.amr.games.pacman.model.common.actors.PacAnimationKey.ANIM_DYING;
import static de.amr.games.pacman.model.common.actors.PacAnimationKey.ANIM_MUNCHING;
import static java.util.function.Predicate.not;

import de.amr.games.pacman.event.GameEventType;
import de.amr.games.pacman.event.GameEventing;
import de.amr.games.pacman.event.GameStateChangeEvent;
import de.amr.games.pacman.lib.TickTimer;
import de.amr.games.pacman.lib.animation.ThingAnimation;
import de.amr.games.pacman.lib.animation.ThingAnimationCollection;
import de.amr.games.pacman.lib.fsm.Fsm;
import de.amr.games.pacman.lib.fsm.FsmState;
import de.amr.games.pacman.model.common.GameModel;
import de.amr.games.pacman.model.common.GameModel.CheckResult;
import de.amr.games.pacman.model.common.GameSound;
import de.amr.games.pacman.model.common.GameVariant;
import de.amr.games.pacman.model.common.actors.Ghost;
import de.amr.games.pacman.model.common.actors.GhostState;

/**
 * Rule of thumb: here, specify the "what" and "when", not the "how" (which should be implemented in the model).
 * 
 * @author Armin Reichert
 */
public enum GameState implements FsmState<GameModel> {

	INTRO {
		@Override
		public void onEnter(GameModel game) {
			timer.setIndefinite();
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
			game.sounds().ifPresent(snd -> snd.play(GameSound.CREDIT));
			game.increaseCredit();
			fsm.changeState(CREDIT);
		}

		@Override
		public void startIntermissionTest(GameModel game) {
			game.intermissionTestNumber = 1;
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
		public void onUpdate(GameModel game) {
			if (timer.hasExpired()) {
				fsm.changeState(READY);
			}
		}

		@Override
		public void addCredit(GameModel game) {
			game.sounds().ifPresent(snd -> snd.play(GameSound.CREDIT));
			game.increaseCredit();
		}
	},

	READY {
		@Override
		public void onEnter(GameModel game) {
			boolean hasCredit = game.credit() > 0;
			if (hasCredit && !game.playing) {
				// game start
				timer.setSeconds(5);
				game.sounds().ifPresent(snd -> {
					snd.stopAll();
					snd.play(GameSound.GAME_READY);
				});
			} else {
				// game already running or attract mode
				timer.setSeconds(2);
			}
			timer.start();
			game.resetGuys();
			game.scores.gameScore.showContent = hasCredit;
			game.scores.highScore.showContent = true;
			game.energizerPulse.reset();
		}

		@Override
		public void onUpdate(GameModel game) {
			if (timer.hasExpired()) {
				game.startHuntingPhase(0);
				if (game.credit() > 0) {
					game.scores.enable(true);
					game.playing = true;
				} else {
					game.scores.enable(false);
					game.playing = false;
				}
				fsm.changeState(GameState.HUNTING);
			}
		}
	},

	HUNTING {
		@Override
		public void onEnter(GameModel game) {
			game.pac.animation(ANIM_MUNCHING).ifPresent(ThingAnimation::ensureRunning);
			game.ghosts().forEach(ghost -> ghost.animations().ifPresent(ThingAnimationCollection::ensureRunning));
			game.energizerPulse.restart();
		}

		@Override
		public void onUpdate(GameModel game) {
			var checkResult = new CheckResult();
			gameController.steer(game.pac);
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
				if (game.pac.starvingTicks >= 10) {
					snd.stop(GameSound.PACMAN_MUNCH);
				}
				if (game.ghosts(GhostState.DEAD).count() == 0) {
					snd.stop(GameSound.GHOST_RETURNING);
				}
			});
		}

		@Override
		public void cheatEatAllPellets(GameModel game) {
			if (game.playing) {
				game.level.world.tiles().filter(not(game.level.world::isEnergizerTile)).forEach(game.level.world::removeFood);
				GameEventing.publish(GameEventType.PLAYER_FINDS_FOOD, null);
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
			timer.setSeconds(4);
			timer.start();
			game.huntingTimer.stop();
			game.bonus().setInactive();
			game.pac.setAbsSpeed(0);
			game.pac.animations().ifPresent(anim -> anim.reset());
			game.ghosts().forEach(Ghost::hide);
			game.energizerPulse.reset();
			game.sounds().ifPresent(snd -> snd.stopAll());
		}

		@Override
		public void onUpdate(GameModel game) {
			if (timer.atSecond(1)) {
				game.mazeFlashingAnimation().ifPresent(mazeFlashing -> {
					mazeFlashing.repeat(game.level.numFlashes);
					mazeFlashing.restart();
				});
			}
			if (timer.hasExpired()) {
				if (game.credit() == 0) {
					fsm.changeState(INTRO);
				} else if (game.intermissionNumber(game.level.number) != 0) {
					fsm.changeState(INTERMISSION);
				} else {
					fsm.changeState(LEVEL_STARTING);
				}
				return;
			}
			game.mazeFlashingAnimation().ifPresent(ThingAnimation::advance);
		}
	},

	LEVEL_STARTING {
		@Override
		public void onEnter(GameModel game) {
			timer.setSeconds(1);
			timer.start();
			game.setLevel(game.level.number + 1);
			game.resetGuys();
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
			timer.setSeconds(1);
			timer.start();
			game.pac.hide();
			game.ghosts().forEach(ghost -> ghost.animation(ANIM_FLASHING).ifPresent(ThingAnimation::stop));
			game.ghosts().filter(ghost -> ghost.killIndex >= 0)
					.forEach(ghost -> ghost.animations().ifPresent(anim -> anim.select(ANIM_VALUE)));
			game.sounds().ifPresent(snd -> snd.play(GameSound.GHOST_EATEN));
		}

		@Override
		public void onUpdate(GameModel game) {
			if (timer.hasExpired()) {
				fsm.resumePreviousState();
				return;
			}
			gameController.steer(game.pac);
			game.updateGhostsReturningHome();
			game.energizerPulse.advance();
		}

		@Override
		public void onExit(GameModel game) {
			game.pac.show();
			game.ghosts().forEach(ghost -> ghost.animation(ANIM_FLASHING).ifPresent(ThingAnimation::run));
			game.letDeadGhostsReturnHome();
		}
	},

	PACMAN_DYING {
		@Override
		public void onEnter(GameModel game) {
			timer.setSeconds(5);
			timer.start();
			game.pac.setAbsSpeed(0);
			game.pac.animations().ifPresent(anim -> {
				anim.select(ANIM_DYING);
				anim.selectedAnimation().reset();
			});
			game.bonus().setInactive();
			game.sounds().ifPresent(snd -> snd.stopAll());
		}

		@Override
		public void onUpdate(GameModel game) {
			game.energizerPulse.advance();
			if (timer.atSecond(1)) {
				game.ghosts().forEach(Ghost::hide);
			} else if (timer.atSecond(2)) {
				game.pac.animation(ANIM_DYING).ifPresent(ThingAnimation::restart);
				game.sounds().ifPresent(snd -> snd.play(GameSound.PACMAN_DEATH));
			} else if (timer.atSecond(4)) {
				if (--game.lives == 0) {
					game.energizerPulse.stop();
				}
				game.pac.hide();
			} else if (timer.hasExpired()) {
				var nextState = game.credit() == 0 ? INTRO : game.lives == 0 ? GAME_OVER : READY;
				fsm.changeState(nextState);
				return;
			}
		}
	},

	GAME_OVER {
		@Override
		public void onEnter(GameModel game) {
			game.scores.saveHiscore();
			timer.setSeconds(3);
			timer.start();
			game.sounds().ifPresent(snd -> snd.stopAll());
		}

		@Override
		public void onUpdate(GameModel game) {
			if (timer.hasExpired()) {
				game.playing = false;
				game.consumeCredit();
				fsm.changeState(game.credit() > 0 ? CREDIT : INTRO);
			}
		}
	},

	INTERMISSION {
		@Override
		public void onEnter(GameModel game) {
			timer.setIndefinite();
			timer.start(); // UI triggers state timeout
		}

		@Override
		public void onUpdate(GameModel game) {
			if (timer.hasExpired()) {
				fsm.changeState(game.credit() == 0 || !game.playing ? INTRO : LEVEL_STARTING);
			}
		}
	},

	INTERMISSION_TEST {
		@Override
		public void onEnter(GameModel game) {
			timer.setIndefinite();
			timer.start();
			log("Test intermission scene #%d", game.intermissionTestNumber);
		}

		@Override
		public void onUpdate(GameModel game) {
			if (timer.hasExpired()) {
				if (game.intermissionTestNumber < 3) {
					++game.intermissionTestNumber;
					timer.setIndefinite();
					timer.start();
					log("Test intermission scene #%d", game.intermissionTestNumber);
					// This is a hack to trigger the UI to update its current scene
					GameEventing.publish(new GameStateChangeEvent(game, this, this));
				} else {
					fsm.changeState(INTRO);
				}
			}
		}
	};

	// -----------------------------------------------------------

	protected Fsm<GameState, GameModel> fsm;
	GameController gameController;

	protected final TickTimer timer = new TickTimer("Timer-" + name());

	@SuppressWarnings("unchecked")
	@Override
	public void setOwner(Fsm<? extends FsmState<GameModel>, GameModel> fsm) {
		this.fsm = (Fsm<GameState, GameModel>) fsm;
	}

	@Override
	public TickTimer timer() {
		return timer;
	}

	// --- Events

	public void selectGameVariant(GameVariant variant) {
	}

	public void addCredit(GameModel game) {
	}

	public void startIntermissionTest(GameModel game) {
	}

	public void cheatEatAllPellets(GameModel game) {
	}

	public void cheatKillAllEatableGhosts(GameModel game) {
	}

	public void cheatEnterNextLevel(GameModel game) {
	}

}