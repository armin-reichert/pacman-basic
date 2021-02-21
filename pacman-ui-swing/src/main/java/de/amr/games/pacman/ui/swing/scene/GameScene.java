package de.amr.games.pacman.ui.swing.scene;

import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;

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

	Dimension sizeInPixel();

	// Convenience methods

	default void drawHCenteredText(Graphics2D g, String text, int y) {
		g.drawString(text, (sizeInPixel().width - g.getFontMetrics().stringWidth(text)) / 2, y);
	}

	default void drawHCenteredImage(Graphics2D g, BufferedImage image, int y) {
		g.drawImage(image, (sizeInPixel().width - image.getWidth()) / 2, y, null);
	}
}