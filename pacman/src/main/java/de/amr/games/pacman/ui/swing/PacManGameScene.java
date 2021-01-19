package de.amr.games.pacman.ui.swing;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;

import de.amr.games.pacman.game.core.PacManGame;
import de.amr.games.pacman.lib.V2i;

public abstract class PacManGameScene {

	public static final Color[] GHOST_COLORS = { Color.RED, Color.PINK, Color.CYAN, Color.ORANGE };

	public final PacManGame game;
	public final V2i size;
	public Color bgColor;

	public PacManGameScene(PacManGame game, V2i size) {
		this.game = game;
		this.size = size;
		bgColor = Color.BLACK;
	}

	public void start() {

	}

	public void end() {

	}

	public abstract void draw(Graphics2D g, Graphics2D unscaledGC);

	public void drawCenteredText(Graphics2D g, String text, int y) {
		g.drawString(text, (size.x - g.getFontMetrics().stringWidth(text)) / 2, y);
	}

	public void drawCenteredImage(Graphics2D g, BufferedImage image, int y) {
		g.drawImage(image, (size.x - image.getWidth()) / 2, y, null);
	}
}