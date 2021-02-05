package de.amr.games.pacman.ui.api;

import java.awt.Graphics2D;
import java.awt.image.BufferedImage;

import de.amr.games.pacman.lib.V2i;

/**
 * Implemented by all scenes of the Pac-Man and Ms. Pac-Man game.
 * 
 * @author Armin Reichert
 */
public interface PacManGameScene {

	void draw(Graphics2D g);

	V2i sizeInPixel();

	default void start() {
	}

	default void update() {
	}

	default void end() {
	}

	// Convenience methods

	default void drawHCenteredText(Graphics2D g, String text, int y) {
		g.drawString(text, (sizeInPixel().x - g.getFontMetrics().stringWidth(text)) / 2, y);
	}

	default void drawHCenteredImage(Graphics2D g, BufferedImage image, int y) {
		g.drawImage(image, (sizeInPixel().x - image.getWidth()) / 2, y, null);
	}
}