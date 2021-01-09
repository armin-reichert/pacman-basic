package de.amr.games.pacman.ui.swing;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;

import de.amr.games.pacman.core.PacManGame;

abstract class Scene {

	public static final Color[] GHOST_COLORS = { Color.RED, Color.PINK, Color.CYAN, Color.ORANGE };

	public final PacManGame game;
	public final Dimension size;
	public PacManGameAssets assets;

	public Scene(PacManGame game, Dimension size) {
		this.game = game;
		this.size = size;
	}

	public void drawCenteredText(Graphics2D g, String text, int y) {
		g.drawString(text, (size.width - g.getFontMetrics().stringWidth(text)) / 2, y);
	}

	public void drawCenteredImage(Graphics2D g, BufferedImage image, int y) {
		g.drawImage(image, (size.width - image.getWidth()) / 2, y, null);
	}
}