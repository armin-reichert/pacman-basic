package de.amr.games.pacman.ui.swing.scene;

import java.awt.Graphics2D;
import java.awt.image.BufferedImage;

import de.amr.games.pacman.lib.V2i;

/**
 * Implemented by all scenes of the Pac-Man and Ms. Pac-Man game.
 * 
 * @author Armin Reichert
 */
public interface PacManGameScene {

	V2i size();

	void draw(Graphics2D g);

	default void drawCenteredText(Graphics2D g, String text, int y) {
		g.drawString(text, (size().x - g.getFontMetrics().stringWidth(text)) / 2, y);
	}

	default void drawCenteredImage(Graphics2D g, BufferedImage image, int y) {
		g.drawImage(image, (size().x - image.getWidth()) / 2, y, null);
	}

	default void start() {

	}

	default void end() {

	}
}