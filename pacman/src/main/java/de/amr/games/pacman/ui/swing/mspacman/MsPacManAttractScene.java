package de.amr.games.pacman.ui.swing.mspacman;

import static de.amr.games.pacman.world.PacManGameWorld.t;

import java.awt.Color;
import java.awt.Graphics2D;
import java.util.Optional;

import de.amr.games.pacman.lib.V2i;
import de.amr.games.pacman.model.MsPacManGame;
import de.amr.games.pacman.ui.api.PacManGameAnimations;
import de.amr.games.pacman.ui.api.PacManGameScene;
import de.amr.games.pacman.ui.api.PacManGameUI;

public class MsPacManAttractScene implements PacManGameScene {

	private final PacManGameUI ui;
	private final V2i size;
	private final MsPacManRendering rendering;
	private final MsPacManGame game;

	public MsPacManAttractScene(PacManGameUI ui, V2i size, MsPacManRendering rendering, MsPacManGame game) {
		this.ui = ui;
		this.size = size;
		this.rendering = rendering;
		this.game = game;
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
	public void start() {
		game.state.resetTimer();
		game.resetGuys();
		game.pac.visible = true;
		game.ghosts().forEach(ghost -> ghost.visible = true);
		ui.showMessage("GAME  OVER", true);
	}

	@Override
	public void update() {
	}

	@Override
	public void end() {
	}

	@Override
	public void draw(Graphics2D g) {
		rendering.drawScore(g, game);
		rendering.drawLivesCounter(g, game, t(2), size.y - t(2));
		rendering.drawLevelCounter(g, game, t(game.level.world.xTiles() - 4), size.y - t(2));
		rendering.drawMaze(g, game);
		rendering.drawPac(g, game.pac);
		game.ghosts().forEach(ghost -> rendering.drawGhost(g, ghost, game));

		g.setFont(rendering.assets.scoreFont);
		g.setColor(Color.RED);
		drawHCenteredText(g, "ATTRACT MODE " + game.state.ticksRun(), t(18));
	}
}