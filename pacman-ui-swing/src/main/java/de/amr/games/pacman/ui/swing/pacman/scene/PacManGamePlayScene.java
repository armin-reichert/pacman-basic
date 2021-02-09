package de.amr.games.pacman.ui.swing.pacman.scene;

import static de.amr.games.pacman.world.PacManGameWorld.t;

import java.awt.Graphics2D;

import de.amr.games.pacman.controller.PacManGameState;
import de.amr.games.pacman.lib.V2i;
import de.amr.games.pacman.model.AbstractPacManGame;
import de.amr.games.pacman.ui.swing.PacManGameScene;
import de.amr.games.pacman.ui.swing.pacman.rendering.PacManGameSpriteBasedRendering;
import de.amr.games.pacman.ui.swing.rendering.DebugRendering;

/**
 * Scene where the game is played.
 * 
 * @author Armin Reichert
 */
public class PacManGamePlayScene implements PacManGameScene {

	private final V2i size;
	private final PacManGameSpriteBasedRendering rendering;
	private final AbstractPacManGame game;

	public PacManGamePlayScene(V2i size, PacManGameSpriteBasedRendering rendering, AbstractPacManGame game) {
		this.size = size;
		this.rendering = rendering;
		this.game = game;
	}

	@Override
	public V2i sizeInPixel() {
		return size;
	}

	@Override
	public void draw(Graphics2D g) {
		rendering.drawScore(g, game);
		if (!game.attractMode) {
			rendering.drawLivesCounter(g, game, t(2), size.y - t(2));
			rendering.drawLevelCounter(g, game, t(game.level.world.xTiles() - 4), size.y - t(2));
		}
		rendering.drawMaze(g, game);
		if (DebugRendering.on) {
			DebugRendering.drawMazeStructure(g, game);
		}
		if (game.attractMode || game.state == PacManGameState.GAME_OVER) {
			rendering.signalGameOverState(g);
		} else if (game.state == PacManGameState.READY) {
			rendering.signalReadyState(g);
		}
		rendering.drawGuy(g, game.pac, game);
		game.ghosts().forEach(ghost -> rendering.drawGuy(g, ghost, game));
		if (DebugRendering.on) {
			DebugRendering.drawPlaySceneDebugInfo(g, game);
		}
	}
}