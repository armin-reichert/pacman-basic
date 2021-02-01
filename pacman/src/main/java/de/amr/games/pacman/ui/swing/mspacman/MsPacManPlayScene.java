package de.amr.games.pacman.ui.swing.mspacman;

import static de.amr.games.pacman.world.PacManGameWorld.t;

import java.awt.Graphics2D;
import java.util.Optional;

import de.amr.games.pacman.lib.V2i;
import de.amr.games.pacman.model.PacManGame;
import de.amr.games.pacman.ui.api.PacManGameAnimations;
import de.amr.games.pacman.ui.api.PacManGameScene;
import de.amr.games.pacman.ui.swing.PacManGameSwingUI;

/**
 * Scene where the Ms. Pac-Man game is played.
 * 
 * @author Armin Reichert
 */
public class MsPacManPlayScene implements PacManGameScene {

	private final V2i size;
	private final PacManGame game;
	private final MsPacManRendering rendering;

	public MsPacManPlayScene(PacManGameSwingUI ui, V2i size, MsPacManAssets assets, PacManGame game) {
		this.size = size;
		this.game = game;
		rendering = new MsPacManRendering(assets, ui::translation);
	}

	@Override
	public V2i size() {
		return size;
	}

	@Override
	public Optional<PacManGameAnimations> animations() {
		return Optional.of(rendering);
	}

	@Override
	public void draw(Graphics2D g) {
		rendering.drawScore(g, game);
		rendering.drawLivesCounter(g, game, size.y - t(2));
		rendering.drawLevelCounter(g, game, size.y - t(2));
		rendering.drawMaze(g, game);
		if (PacManGameSwingUI.debugMode) {
			drawMazeStructure(g, game);
		}
		rendering.drawPac(g, game.pac);
		game.ghosts().forEach(ghost -> rendering.drawGhost(g, ghost, game));
		drawDebugInfo(g, game);
	}
}