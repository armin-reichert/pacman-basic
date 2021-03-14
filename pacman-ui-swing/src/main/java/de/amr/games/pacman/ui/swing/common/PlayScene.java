package de.amr.games.pacman.ui.swing.common;

import static de.amr.games.pacman.model.world.PacManGameWorld.t;

import java.awt.Dimension;
import java.awt.Graphics2D;

import de.amr.games.pacman.controller.PacManGameController;
import de.amr.games.pacman.controller.PacManGameState;
import de.amr.games.pacman.lib.TickTimerEvent;
import de.amr.games.pacman.model.common.GameModel;
import de.amr.games.pacman.model.common.GhostState;
import de.amr.games.pacman.ui.animation.TimedSequence;
import de.amr.games.pacman.ui.sound.SoundManager;
import de.amr.games.pacman.ui.swing.rendering.PacManGameRendering2D;

/**
 * Play scene (Pac-Man and Ms. Pac-Man).
 * 
 * @author Armin Reichert
 */
public class PlayScene extends GameScene {

	private TimedSequence<?> mazeFlashing;

	public PlayScene(PacManGameController controller, Dimension size, PacManGameRendering2D rendering,
			SoundManager sounds) {
		super(controller, size, rendering, sounds);
		controller.fsm.addStateEntryListener(PacManGameState.READY, this::onReadyStateEntry);
		controller.fsm.addStateEntryListener(PacManGameState.HUNTING, this::onHuntingStateEntry);
		controller.fsm.addStateExitListener(PacManGameState.HUNTING, this::onHuntingStateExit);
		controller.fsm.addStateEntryListener(PacManGameState.LEVEL_COMPLETE, this::onLevelCompleteStateEntry);
		controller.fsm.addStateEntryListener(PacManGameState.PACMAN_DYING, this::onPacManDyingStateEntry);
		controller.fsm.addStateEntryListener(PacManGameState.GHOST_DYING, this::onGhostDyingStateEntry);
		controller.fsm.addStateEntryListener(PacManGameState.GAME_OVER, this::onGameOverStateEntry);
	}

	private void onReadyStateEntry(PacManGameState state) {
		rendering.resetAllAnimations(controller.selectedGame());
	}

	private void onHuntingStateEntry(PacManGameState state) {
		rendering.mazeAnimations().energizerBlinking().restart();
		rendering.playerAnimations().playerMunching(controller.selectedGame().player).forEach(TimedSequence::restart);
		controller.selectedGame().ghosts().flatMap(rendering.ghostAnimations()::ghostKicking).forEach(TimedSequence::restart);
	}

	private void onHuntingStateExit(PacManGameState state) {
		rendering.mazeAnimations().energizerBlinking().reset();
	}

	private void onPacManDyingStateEntry(PacManGameState state) {
		controller.selectedGame().ghosts().flatMap(rendering.ghostAnimations()::ghostKicking).forEach(TimedSequence::reset);
	}

	private void onGhostDyingStateEntry(PacManGameState state) {
		rendering.mazeAnimations().energizerBlinking().restart();
	}

	private void onLevelCompleteStateEntry(PacManGameState state) {
		mazeFlashing = rendering.mazeAnimations().mazeFlashing(controller.selectedGame().level.mazeNumber);
	}

	private void runLevelCompleteState(PacManGameState state) {
		GameModel game = controller.selectedGame();
		if (state.timer.isRunningSeconds(2)) {
			game.ghosts().forEach(ghost -> ghost.visible = false);
		}
		if (state.timer.isRunningSeconds(3)) {
			mazeFlashing.restart();
		}
		mazeFlashing.animate();
		if (mazeFlashing.isComplete()) {
			controller.letCurrentGameStateExpire();
		}
	}

	private void onGameOverStateEntry(PacManGameState state) {
		controller.selectedGame().ghosts().flatMap(rendering.ghostAnimations()::ghostKicking).forEach(TimedSequence::reset);
	}

	@Override
	public void start() {
		GameModel game = controller.selectedGame();
		mazeFlashing = rendering.mazeAnimations().mazeFlashing(game.level.mazeNumber).repetitions(game.level.numFlashes);
		mazeFlashing.reset();
		game.player.powerTimer.addEventListener(e -> {
			if (e.type == TickTimerEvent.Type.HALF_EXPIRED) {
				game.ghosts(GhostState.FRIGHTENED).forEach(ghost -> {
					TimedSequence<?> flashing = rendering.ghostAnimations().ghostFlashing(ghost);
					long frameTime = e.ticks / (game.level.numFlashes * flashing.numFrames());
					flashing.frameDuration(frameTime).repetitions(game.level.numFlashes).restart();
				});
			}
		});
	}

	@Override
	public void update() {
		if (controller.fsm.state == PacManGameState.LEVEL_COMPLETE) {
			runLevelCompleteState(controller.fsm.state);
		} else if (controller.fsm.state == PacManGameState.LEVEL_STARTING) {
			controller.letCurrentGameStateExpire();
		}
	}

	@Override
	public void render(Graphics2D g) {
		GameModel game = controller.selectedGame();
		rendering.drawMaze(g, game.level.mazeNumber, 0, t(3), mazeFlashing.isRunning());
		if (!mazeFlashing.isRunning()) {
			rendering.drawFoodTiles(g, game.level.world.tiles().filter(game.level.world::isFoodTile),
					game.level::containsEatenFood);
			rendering.drawEnergizerTiles(g, game.level.world.energizerTiles());
		}
		if (controller.attractMode) {
			rendering.drawGameState(g, game, PacManGameState.GAME_OVER);
		} else {
			rendering.drawGameState(g, game, controller.fsm.state);
		}
		rendering.drawBonus(g, game.bonus);
		rendering.drawPlayer(g, game.player);
		game.ghosts().forEach(ghost -> rendering.drawGhost(g, ghost, game.player.powerTimer.isRunning()));
		rendering.drawScore(g, game, controller.fsm.state == PacManGameState.INTRO || controller.attractMode);
		if (!controller.attractMode) {
			rendering.drawLivesCounter(g, game, t(2), t(34));
		}
		rendering.drawLevelCounter(g, game, t(25), t(34));
	}
}