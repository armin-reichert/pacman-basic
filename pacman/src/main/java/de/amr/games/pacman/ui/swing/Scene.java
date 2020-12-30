package de.amr.games.pacman.ui.swing;

import java.awt.Graphics2D;
import java.awt.image.BufferedImage;

import de.amr.games.pacman.core.Game;
import de.amr.games.pacman.lib.V2i;

abstract class Scene {

	public final Game game;
	public final Assets assets;
	public final V2i size;

	public Scene(Game game, Assets assets, V2i size) {
		this.game = game;
		this.assets = assets;
		this.size = size;
	}

	public void drawCenteredText(Graphics2D g, String text, int y) {
		g.drawString(text, (size.x - g.getFontMetrics().stringWidth(text)) / 2, y);
	}

	public void drawCenteredImage(Graphics2D g, BufferedImage image, int y) {
		g.drawImage(image, (size.x - image.getWidth()) / 2, y, null);
	}
}