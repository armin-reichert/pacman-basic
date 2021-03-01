package de.amr.games.pacman.ui.swing.common;

import static de.amr.games.pacman.world.PacManGameWorld.t;

import java.awt.Dimension;
import java.awt.Graphics2D;

import de.amr.games.pacman.controller.PacManGameController;
import de.amr.games.pacman.model.common.PacManGameState;
import de.amr.games.pacman.sound.SoundManager;
import de.amr.games.pacman.ui.swing.rendering.SwingRendering;

/**
 * Play scene (Pac-Man and Ms. Pac-Man).
 * 
 * @author Armin Reichert
 */
public class PlayScene extends GameScene {

	public PlayScene(PacManGameController controller, Dimension size, SwingRendering rendering, SoundManager sounds) {
		super(controller, size, rendering, sounds);
	}

	@Override
	public void update() {
		// nothing to do
	}

	@Override
	public void render(Graphics2D g) {
		boolean mazeFlashing = rendering.mazeAnimations().mazeFlashing(game.level.mazeNumber).hasStarted();
		rendering.drawMaze(g, game.level.mazeNumber, 0, t(3), mazeFlashing);
		if (!mazeFlashing) {
			rendering.drawFoodTiles(g, game.level.world.tiles().filter(game.level.world::isFoodTile),
					game.level::containsEatenFood);
			rendering.drawEnergizerTiles(g, game.level.world.energizerTiles());
		}
		rendering.drawGameState(g, game);
		rendering.drawBonus(g, game.bonus);
		rendering.drawPlayer(g, game.pac);
		game.ghosts().forEach(ghost -> rendering.drawGhost(g, ghost, game.pac.powerTicksLeft > 0));
		rendering.drawScore(g, game, game.state == PacManGameState.INTRO || game.attractMode);
		if (!game.attractMode) {
			rendering.drawLivesCounter(g, game, t(2), t(34));
		}
		rendering.drawLevelCounter(g, game, t(25), t(34));
	}
}