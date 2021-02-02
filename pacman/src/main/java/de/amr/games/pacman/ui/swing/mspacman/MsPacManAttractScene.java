package de.amr.games.pacman.ui.swing.mspacman;

import static de.amr.games.pacman.world.PacManGameWorld.t;

import java.awt.Color;
import java.awt.Graphics2D;
import java.util.Optional;

import de.amr.games.pacman.lib.V2i;
import de.amr.games.pacman.model.MsPacManGame;
import de.amr.games.pacman.ui.api.PacManGameAnimations;
import de.amr.games.pacman.ui.api.PacManGameScene;

public class MsPacManAttractScene implements PacManGameScene {

	private final V2i size;
	private final MsPacManRendering rendering;
	private final MsPacManGame game;

	public MsPacManAttractScene(V2i size, MsPacManRendering rendering, MsPacManGame game) {
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
	public void update() {
	}

	@Override
	public void draw(Graphics2D g) {
		g.setFont(rendering.assets.scoreFont);
		g.setColor(Color.RED);
		drawHCenteredText(g, "ATTRACT MODE " + game.state.ticksRun(), t(18));
	}
}
