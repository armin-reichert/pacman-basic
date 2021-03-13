package de.amr.games.pacman.ui.swing.rendering;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;

import de.amr.games.pacman.ui.Rendering;
import de.amr.games.pacman.ui.animation.PacManGameAnimations2D;

public interface PacManGameRendering2D extends Rendering<Graphics2D, Color, Font, BufferedImage>, PacManGameAnimations2D {

	void drawSprite(Graphics2D g, BufferedImage sprite, double x, double y);

}