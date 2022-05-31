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
import static de.amr.games.pacman.model.common.actors.GhostState.DEAD;
import static de.amr.games.pacman.model.common.actors.GhostState.ENTERING_HOUSE;
import static de.amr.games.pacman.model.common.actors.GhostState.FRIGHTENED;
import static de.amr.games.pacman.model.common.actors.GhostState.HUNTING_PAC;

import java.util.function.Consumer;

import de.amr.games.pacman.event.GameEvent;
import de.amr.games.pacman.event.GameEventType;
import de.amr.games.pacman.event.GameStateChangeEvent;
import de.amr.games.pacman.lib.Fsm;
import de.amr.games.pacman.lib.FsmState;
import de.amr.games.pacman.lib.TickTimer;
import de.amr.games.pacman.model.common.GameModel;
import de.amr.games.pacman.model.common.actors.Ghost;
import de.amr.games.pacman.model.common.actors.Pac;

/**
 * Rule of thumb: here, the "what" and "when" should be specified, not the "how" (should be implemented in the model).
 * 
 * @author Armin Reichert
 */
public enum GameState implements FsmState<GameModel> {

	INTRO {
		@Override
		public void onEnter(GameModel game) {
			timer.setDurationIndefinite().start();
			game.reset();
			controller.setGameRunning(false);
		}

		@Override
		public void onUpdate(GameModel game) {
			if (timer.hasExpired()) {
				controller.changeState(READY);
			}
		}
	},

	CREDIT {
		@Override
		public void onUpdate(GameModel game) {
			if (timer.hasExpired()) {
				controller.changeState(READY);
			}
		}
	},

	READY {
		@Override
		public void onEnter(GameModel game) {
			long readySeconds = controller.isGameRunning() || controller.credit() == 0 ? 2 : 5;
			timer.setDurationSeconds(readySeconds).start();
			game.resetGuys();
			game.ghosts().forEach(Ghost::hide);
			game.player.hide();
		}

		@Override
		public void onUpdate(GameModel game) {
			if (timer.atSecond(1.5)) {
				game.ghosts().forEach(Ghost::show);
				game.player.show();
			}
			if (timer.hasExpired()) {
				game.startHuntingPhase(0);
				if (controller.credit() > 0) {
					game.scoreSupport().enabled = true;
					controller.setGameRunning(true);
				} else {
					game.scoreSupport().enabled = false;
					controller.setGameRunning(false);
				}
				controller.changeState(GameState.HUNTING);
			}
		}
	},

	HUNTING {
		@Override
		public void onUpdate(GameModel game) {
			game.advanceHunting();
			game.checkList.clear();

			game.checkLevelComplete();
			if (game.checkList.levelComplete) {
				controller.changeState(LEVEL_COMPLETE);
				return;
			}

			// TODO don't store this info in controller
			if (!controller.isPlayerImmune()) {
				game.checkPlayerKilled();
				if (game.checkList.playerKilled) {
					game.onPlayerKilled();
					controller.changeState(PACMAN_DYING);
					return;
				}
			}

			game.checkGhostsCanBeEaten();
			if (game.checkList.ghostsCanBeEaten) {
				game.eatGhosts(game.checkList.edibleGhosts);
				controller.changeState(GHOST_DYING);
				return;
			}

			currentPlayerControl().accept(game.player);
			game.player.moveThroughLevel(game.level);

			game.checkPlayerPower();
			if (game.checkList.playerPowerFading) {
				game.publish(GameEventType.PLAYER_STARTS_LOSING_POWER, game.player.tile());
			}
			if (game.checkList.playerPowerLost) {
				game.onPlayerLostPower();
				game.publish(GameEventType.PLAYER_LOSES_POWER, game.player.tile());
			}

			game.checkPlayerFindsFood();
			if (game.checkList.foodFound) {
				game.publish(GameEventType.PLAYER_FINDS_FOOD, game.player.tile());
			}
			if (game.checkList.playerGotPower) {
				game.publish(GameEventType.PLAYER_GETS_POWER, game.player.tile());
			}
			if (game.checkList.bonusReached) {
				game.publish(GameEventType.BONUS_GETS_ACTIVE, game.bonus().tile());
			}

			game.checkUnlockGhost();
			if (game.checkList.unlockedGhost != null) {
				game.unlockGhost(game.checkList.unlockedGhost, game.checkList.unlockReason);
			}
			game.ghosts().forEach(ghost -> ghost.update(game));

			game.updateBonus();
		}
	},

	LEVEL_COMPLETE {
		@Override
		public void onEnter(GameModel game) {
			game.huntingTimer.stop();
			if (game.bonus() != null) {
				game.bonus().init();
			}
			game.player.setAbsSpeed(0);
			game.ghosts().forEach(Ghost::hide);
			timer.setDurationIndefinite().start();
		}

		@Override
		public void onUpdate(GameModel game) {
			if (timer.hasExpired()) {
				if (controller.credit() == 0) {
					controller.changeState(INTRO);
				} else if (game.intermissionNumber(game.level.number) != 0) {
					controller.changeState(INTERMISSION);
				} else {
					controller.changeState(LEVEL_STARTING);
				}
			}
		}
	},

	LEVEL_STARTING {
		@Override
		public void onEnter(GameModel game) {
			timer.setDurationIndefinite().start();
			game.setLevel(game.level.number + 1);
			game.resetGuys();
		}

		@Override
		public void onUpdate(GameModel game) {
			if (timer.hasExpired()) {
				controller.changeState(READY);
			}
		}
	},

	PACMAN_DYING {
		@Override
		public void onEnter(GameModel game) {
			timer.setDurationIndefinite().start();
			game.player.setAbsSpeed(0);
			game.ghosts(FRIGHTENED).forEach(ghost -> ghost.state = HUNTING_PAC);
			if (game.bonus() != null) {
				game.bonus().init();
			}
		}

		@Override
		public void onUpdate(GameModel game) {
			if (timer.hasExpired()) {
				game.lives--;
				if (controller.credit() == 0) {
					game.reset();
					controller.changeState(INTRO);
				} else {
					controller.changeState(game.lives > 0 ? READY : GAME_OVER);
				}
			}
		}

		@Override
		public void onExit(GameModel game) {
//			startHuntingPhase(game, 0);
		}
	},

	GHOST_DYING {
		@Override
		public void onEnter(GameModel game) {
			timer.setDurationSeconds(1).start();
			game.player.hide();
		}

		@Override
		public void onUpdate(GameModel game) {
			if (timer.hasExpired()) {
				controller.resumePreviousState();
				return;
			}
			currentPlayerControl().accept(game.player);
			game.ghosts().filter(ghost -> ghost.is(DEAD) && ghost.bounty == 0 || ghost.is(ENTERING_HOUSE))
					.forEach(ghost -> ghost.update(game));
		}

		@Override
		public void onExit(GameModel game) {
			game.player.show();
			// fire event(s) only for dead ghosts not yet returning home (bounty != 0)
			game.ghosts(DEAD).filter(ghost -> ghost.bounty != 0).forEach(ghost -> {
				ghost.bounty = 0;
				game.publish(new GameEvent(game, GameEventType.GHOST_STARTS_RETURNING_HOME, ghost, null));
			});
		}
	},

	GAME_OVER {
		@Override
		public void onEnter(GameModel game) {
			timer.setDurationSeconds(5).start();
			game.ghosts().forEach(ghost -> ghost.setAbsSpeed(0));
			game.ghosts().forEach(Ghost::show);
			game.player.setAbsSpeed(0);
			game.player.show();
			game.scoreSupport().saveHiscore();
		}

		@Override
		public void onUpdate(GameModel game) {
			if (timer.hasExpired()) {
				controller.setGameRunning(false);
				controller.consumeCredit();
				controller.changeState(controller.credit() > 0 ? CREDIT : INTRO);
			}
		}
	},

	INTERMISSION {
		@Override
		public void onEnter(GameModel game) {
			timer.setDurationIndefinite().start(); // UI triggers state timeout
		}

		@Override
		public void onUpdate(GameModel game) {
			if (timer.hasExpired()) {
				controller.changeState(controller.credit() == 0 || !controller.isGameRunning() ? INTRO : LEVEL_STARTING);
			}
		}
	},

	INTERMISSION_TEST {
		@Override
		public void onEnter(GameModel game) {
			timer.setDurationIndefinite().start();
			log("Test intermission scene #%d", game.intermissionTestNumber);
		}

		@Override
		public void onUpdate(GameModel game) {
			if (timer.hasExpired()) {
				if (game.intermissionTestNumber < 3) {
					++game.intermissionTestNumber;
					timer.setDurationIndefinite().start();
					log("Test intermission scene #%d", game.intermissionTestNumber);
					// This is a hack to trigger the UI to update its current scene
					game.publish(new GameStateChangeEvent(game, this, this));
				} else {
					controller.changeState(INTRO);
				}
			}
		}
	};

	// -----------------------------------------------------------

	protected GameController controller;
	protected final TickTimer timer = new TickTimer("Timer-" + name());

	@Override
	public void setFsm(Fsm<? extends FsmState<GameModel>, GameModel> fsm) {
		controller = (GameController) fsm;
	}

	@Override
	public TickTimer timer() {
		return timer;
	}

	protected Consumer<Pac> currentPlayerControl() {
		return controller.isAutoMoving() || controller.credit() == 0 ? controller.autopilot() : controller.playerControl();
	}
}