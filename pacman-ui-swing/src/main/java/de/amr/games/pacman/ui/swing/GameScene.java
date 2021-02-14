package de.amr.games.pacman.ui.swing;

import java.awt.Graphics2D;
import java.awt.image.BufferedImage;

import de.amr.games.pacman.lib.V2i;

/**
 * Implemented by all scenes of the Pac-Man and Ms. Pac-Man game.
 * 
 * @author Armin Reichert
 */
public interface GameScene {

	default void start() {
	}

	default void end() {
	}

	void update();

	void render(Graphics2D g);

	V2i sizeInPixel();

	// Convenience methods

	default void drawHCenteredText(Graphics2D g, String text, int y) {
		g.drawString(text, (sizeInPixel().x - g.getFontMetrics().stringWidth(text)) / 2, y);
	}

	default void drawHCenteredImage(Graphics2D g, BufferedImage image, int y) {
		g.drawImage(image, (sizeInPixel().x - image.getWidth()) / 2, y, null);
	}
}