package de.amr.games.pacman.ui.swing.rendering;

import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;

import de.amr.games.pacman.model.Creature;
import de.amr.games.pacman.model.PacManGameModel;

/**
 * Interface used by scenes to get rendered.
 * 
 * @author Armin Reichert
 */
public interface SceneRendering {

	void signalReadyState(Graphics2D g);

	void signalGameOverState(Graphics2D g);

	void drawScore(Graphics2D g, PacManGameModel game, int x, int y);

	void drawHiScore(Graphics2D g, PacManGameModel game, int x, int y);

	void drawLivesCounter(Graphics2D g, PacManGameModel game, int x, int y);

	void drawLevelCounter(Graphics2D g, PacManGameModel game, int rightX, int y);

	void drawMaze(Graphics2D g, PacManGameModel game, int x, int y);

	void drawGuy(Graphics2D g, Creature guy, PacManGameModel game);

	default void drawImage(Graphics2D g, BufferedImage image, float x, float y, boolean smooth) {
		if (smooth) {
			Graphics2D gc = (Graphics2D) g.create();
			gc.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
			gc.drawImage(image, (int) x, (int) y, null);
			gc.dispose();
		} else {
			g.drawImage(image, (int) x, (int) y, null);
		}
	}
}