package de.amr.games.pacman.ui.swing.classic;

import static de.amr.games.pacman.world.PacManGameWorld.t;

import java.awt.Graphics2D;

import de.amr.games.pacman.lib.V2i;
import de.amr.games.pacman.model.PacManGame;
import de.amr.games.pacman.ui.api.PacManGameScene;
import de.amr.games.pacman.ui.swing.DebugRendering;

/**
 * Scene where the game is played.
 * 
 * @author Armin Reichert
 */
public class PacManClassicPlayScene implements PacManGameScene {

	private final V2i size;
	private final PacManClassicRendering rendering;
	private final PacManGame game;

	public PacManClassicPlayScene(V2i size, PacManClassicRendering rendering, PacManGame game) {
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
		}
		rendering.drawLevelCounter(g, game, t(game.level.world.xTiles() - 4), size.y - t(2));
		rendering.drawMaze(g, game);
		if (DebugRendering.on) {
			DebugRendering.drawMazeStructure(g, game);
		}
		rendering.drawPac(g, game.pac);
		game.ghosts().forEach(ghost -> rendering.drawGhost(g, ghost, game));
		if (DebugRendering.on) {
			DebugRendering.drawPlaySceneDebugInfo(g, game);
		}
	}
}