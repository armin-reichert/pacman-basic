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
import static de.amr.games.pacman.model.common.GhostState.DEAD;
import static de.amr.games.pacman.model.common.GhostState.ENTERING_HOUSE;
import static de.amr.games.pacman.model.common.GhostState.FRIGHTENED;
import static de.amr.games.pacman.model.common.GhostState.HUNTING_PAC;

import java.util.function.Consumer;

import de.amr.games.pacman.event.GameEvent;
import de.amr.games.pacman.event.GameEventType;
import de.amr.games.pacman.event.GameStateChangeEvent;
import de.amr.games.pacman.lib.Fsm;
import de.amr.games.pacman.lib.FsmState;
import de.amr.games.pacman.lib.Hiscore;
import de.amr.games.pacman.lib.TickTimer;
import de.amr.games.pacman.model.common.GameModel;
import de.amr.games.pacman.model.common.Ghost;
import de.amr.games.pacman.model.common.Pac;

/**
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

	READY {
		@Override
		public void onEnter(GameModel game) {
			timer.setDurationSeconds(controller.isGameRunning() || controller.credit() == 0 ? 2 : 5).start();
			game.resetGuys();
			game.ghosts().forEach(Ghost::show);
			game.player.show();
		}

		@Override
		public void onUpdate(GameModel game) {
			if (timer.hasExpired()) {
				if (controller.credit() > 0) {
					controller.setGameRunning(true);
				}
				controller.getHuntingTimer().startPhase(game, 0);
				controller.changeState(GameState.HUNTING);
				return;
			}
		}
	},

	HUNTING {
		@Override
		public void onEnter(GameModel game) {
			if (!controller.getHuntingTimer().isStopped()) {
				controller.getHuntingTimer().startHuntingPhase(game, 0);
			}
		}

		@Override
		public void onUpdate(GameModel game) {
			controller.getHuntingTimer().advance();
			if (controller.getHuntingTimer().hasExpired()) {
				controller.getHuntingTimer().startNextHuntingPhase(game);
			}
			if (game.world.foodRemaining() == 0) {
				controller.getHuntingTimer().startPhase(game, 0);
				controller.changeState(LEVEL_COMPLETE);
				return;
			}
			if (game.checkKillPlayer(controller.isPlayerImmune())) {
				controller.getHuntingTimer().startPhase(game, 0);
				controller.changeState(PACMAN_DYING);
				return;
			}
			if (game.checkKillGhosts()) {
				controller.changeState(GHOST_DYING);
				return;
			}
			updatePlayer(game);
			game.updateGhosts(controller.gameVariant(), controller.getHuntingTimer().getPhase());
			game.updateBonus();
		}

		private void updatePlayer(GameModel game) {
			currentPlayerControl().accept(game.player);
			boolean lostPower = game.updatePlayer();
			if (lostPower) {
				controller.getHuntingTimer().start();
			}
			checkFood(game);
		}

		private void checkFood(GameModel game) {
			boolean energizerEaten = game.checkFood(game.player.tile());
			if (energizerEaten && game.player.powerTimer.isRunning()) {
				controller.getHuntingTimer().stop();
			}
		}
	},

	LEVEL_COMPLETE {
		@Override
		public void onEnter(GameModel game) {
			game.bonus.init();
			game.player.setSpeed(0);
			game.ghosts().forEach(Ghost::hide);
			timer.setDurationIndefinite().start();
		}

		@Override
		public void onUpdate(GameModel game) {
			if (timer.hasExpired()) {
				if (controller.credit() == 0) {
					controller.changeState(INTRO);
				} else if (game.intermissionNumber(game.levelNumber) != 0) {
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
			game.setLevel(game.levelNumber + 1);
			game.resetGuys();
			timer.setDurationIndefinite().start();
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
			game.player.setSpeed(0);
			game.ghosts(FRIGHTENED).forEach(ghost -> ghost.state = HUNTING_PAC);
			game.bonus.init();
		}

		@Override
		public void onUpdate(GameModel game) {
			if (timer.hasExpired()) {
				game.player.lives--;
				controller.changeState(controller.credit() == 0 ? INTRO : game.player.lives > 0 ? READY : GAME_OVER);
			}
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
					.forEach(ghost -> game.updateGhost(ghost, controller.gameVariant(), controller.getHuntingTimer().getPhase()));
		}

		@Override
		public void onExit(GameModel game) {
			game.player.show();
			// fire event(s) only for dead ghosts not yet returning home (bounty != 0)
			game.ghosts(DEAD).filter(ghost -> ghost.bounty != 0).forEach(ghost -> {
				ghost.bounty = 0;
				game.publishEvent(new GameEvent(game, GameEventType.GHOST_STARTED_RETURNING_HOME, ghost, null));
			});
		}
	},

	GAME_OVER {
		@Override
		public void onEnter(GameModel game) {
			timer.setDurationSeconds(5).start();
			controller.setGameRunning(false);
			game.ghosts().forEach(ghost -> ghost.setSpeed(0));
			game.player.setSpeed(0);
			new Hiscore(game).save();
		}

		@Override
		public void onUpdate(GameModel game) {
			if (timer.hasExpired()) {
				controller.consumeCredit();
				controller.changeState(INTRO);
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
					game.publishEvent(new GameStateChangeEvent(game, this, this));
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
		return controller.isAutoMoving() || controller.credit() == 0 ? controller.autopilot()
				: controller.playerControl();
	}
}