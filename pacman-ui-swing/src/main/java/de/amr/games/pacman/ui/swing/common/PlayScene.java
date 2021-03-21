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
import de.amr.games.pacman.ui.sound.PacManGameSound;
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
	}

	@Override
	public void onGameStateChange(PacManGameState oldState, PacManGameState newState) {

		// exit state
		if (oldState == PacManGameState.HUNTING) {
			rendering.mazeAnimations().energizerBlinking().reset();
		}

		// enter state
		if (newState == PacManGameState.READY) {
			rendering.resetAllAnimations(controller.selectedGame());
		} else if (newState == PacManGameState.HUNTING) {
			rendering.mazeAnimations().energizerBlinking().restart();
			rendering.playerAnimations().playerMunching(controller.selectedGame().player).forEach(TimedSequence::restart);
			controller.selectedGame().ghosts().flatMap(rendering.ghostAnimations()::ghostKicking)
					.forEach(TimedSequence::restart);
		} else if (newState == PacManGameState.GHOST_DYING) {
			rendering.mazeAnimations().energizerBlinking().restart();
		} else if (newState == PacManGameState.LEVEL_COMPLETE) {
			mazeFlashing = rendering.mazeAnimations().mazeFlashing(controller.selectedGame().level.mazeNumber);
		} else if (newState == PacManGameState.GAME_OVER) {
			controller.selectedGame().ghosts().flatMap(rendering.ghostAnimations()::ghostKicking)
					.forEach(TimedSequence::reset);
		}
	}

	private void startPlayerDyingAnimation(PacManGameState state) {
		GameModel game = controller.selectedGame();
		game.ghosts().flatMap(rendering.ghostAnimations()::ghostKicking).forEach(TimedSequence::reset);
		game.ghosts().forEach(ghost -> ghost.visible = false);
		rendering.playerAnimations().playerDying().restart();
		if (!controller.isAttractMode()) {
			sounds.play(PacManGameSound.PACMAN_DEATH);
		}
	}

	private void runLevelCompleteState(PacManGameState state) {
		GameModel game = controller.selectedGame();
		if (controller.timer().isRunningSeconds(2)) {
			game.ghosts().forEach(ghost -> ghost.visible = false);
		}
		if (controller.timer().isRunningSeconds(3)) {
			mazeFlashing.restart();
		}
		mazeFlashing.animate();
		if (mazeFlashing.isComplete()) {
			controller.letCurrentGameStateExpire();
		}
	}

	private void addListeners() {
		controller.addStateTimeListener(PacManGameState.PACMAN_DYING, this::startPlayerDyingAnimation, 1.0);
	}

	private void removeListeners() {
		controller.removeStateTimeListener(this::startPlayerDyingAnimation);
	}

	@Override
	public void start() {
		addListeners();

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
	public void end() {
		removeListeners();
	}

	@Override
	public void update() {
		if (controller.state == PacManGameState.LEVEL_COMPLETE) {
			runLevelCompleteState(controller.state);
		} else if (controller.state == PacManGameState.LEVEL_STARTING) {
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
		if (controller.isAttractMode()) {
			rendering.drawGameState(g, game, PacManGameState.GAME_OVER);
		} else {
			rendering.drawGameState(g, game, controller.state);
		}
		rendering.drawBonus(g, game.bonus);
		rendering.drawPlayer(g, game.player);
		game.ghosts().forEach(ghost -> rendering.drawGhost(g, ghost, game.player.powerTimer.isRunning()));
		rendering.drawScore(g, game, controller.state == PacManGameState.INTRO || controller.isAttractMode());
		if (!controller.isAttractMode()) {
			rendering.drawLivesCounter(g, game, t(2), t(34));
		}
		rendering.drawLevelCounter(g, game, t(25), t(34));
	}
}