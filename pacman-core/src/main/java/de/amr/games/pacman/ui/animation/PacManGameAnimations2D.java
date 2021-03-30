package de.amr.games.pacman.ui.animation;

import de.amr.games.pacman.model.common.AbstractGameModel;

/**
 * Animations for a game.
 * 
 * @author Armin Reichert
 */
public interface PacManGameAnimations2D {

	default void resetAllAnimations(AbstractGameModel game) {
		mazeAnimations().reset();
	}

	MazeAnimations2D mazeAnimations();

}