package de.amr.games.pacman.ui.swing;

import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;

import de.amr.games.pacman.core.Game;

abstract class Scene {

	public final Game game;
	public final Assets assets;
	public final Dimension size;

	public Scene(Game game, Assets assets, Dimension size) {
		this.game = game;
		this.assets = assets;
		this.size = size;
	}

	public void drawCenteredText(Graphics2D g, String text, int y) {
		g.drawString(text, (size.width - g.getFontMetrics().stringWidth(text)) / 2, y);
	}

	public void drawCenteredImage(Graphics2D g, BufferedImage image, int y) {
		g.drawImage(image, (size.width - image.getWidth()) / 2, y, null);
	}
}