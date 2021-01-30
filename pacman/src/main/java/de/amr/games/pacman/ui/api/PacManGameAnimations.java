package de.amr.games.pacman.ui.api;

import java.awt.image.BufferedImage;

import de.amr.games.pacman.game.creatures.Ghost;
import de.amr.games.pacman.lib.Direction;
import de.amr.games.pacman.ui.swing.Animation;

public interface PacManGameAnimations {

	Animation<BufferedImage> pacCollapsing();

	Animation<BufferedImage> ghostWalking(Ghost ghost, Direction dir);

	Animation<BufferedImage> mazeFlashing(int mazeNumber);
}