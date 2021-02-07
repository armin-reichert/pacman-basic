package de.amr.games.pacman.ui.swing.mspacman.scene;

import java.awt.Graphics2D;

import de.amr.games.pacman.lib.V2i;
import de.amr.games.pacman.model.MsPacManGame;
import de.amr.games.pacman.ui.api.PacManGameScene;
import de.amr.games.pacman.ui.swing.mspacman.MsPacManGameRendering;

public class MsPacManIntermission3_Junior implements PacManGameScene {

	private final V2i size;
	private final MsPacManGameRendering rendering;
	private final MsPacManGame game;

	public MsPacManIntermission3_Junior(V2i size, MsPacManGameRendering rendering, MsPacManGame game) {
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

	}
}
