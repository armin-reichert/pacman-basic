package de.amr.games.pacman.ui.swing.rendering;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;

import de.amr.games.pacman.lib.V2f;
import de.amr.games.pacman.model.common.GameEntity;
import de.amr.games.pacman.model.common.Ghost;
import de.amr.games.pacman.ui.PacManGameAnimations;
import de.amr.games.pacman.ui.Rendering;
import de.amr.games.pacman.ui.swing.mspacman.entities.Flap;
import de.amr.games.pacman.ui.swing.mspacman.entities.Heart;
import de.amr.games.pacman.ui.swing.mspacman.entities.JuniorBag;
import de.amr.games.pacman.ui.swing.mspacman.entities.Stork;

public interface SwingRendering extends Rendering<Graphics2D, Color, Font, BufferedImage>, PacManGameAnimations {

	default Graphics2D smoothGC(Graphics2D g) {
		Graphics2D gc = (Graphics2D) g.create();
		gc.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
		return gc;
	}

	void drawSprite(Graphics2D g, BufferedImage sprite, float x, float y);

	// Pac-Man game only:

	void drawNail(Graphics2D g, GameEntity nail);

	void drawStretchedBlinky(Graphics2D g, Ghost blinky, V2f nailPosition, int stretching);

	void drawPatchedBlinky(Graphics2D g, Ghost blinky);

	void drawNakedBlinky(Graphics2D g, Ghost blinky);

	// Ms. Pac-Man game only:

	void drawFlap(Graphics2D g, Flap flap);

	void drawHeart(Graphics2D g, Heart heart);

	void drawStork(Graphics2D g, Stork stork);

	void drawJuniorBag(Graphics2D g, JuniorBag bag);

}