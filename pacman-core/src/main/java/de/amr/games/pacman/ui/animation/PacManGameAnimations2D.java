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
		game.ghosts().forEach(ghostAnimations()::reset);
		playerAnimations().reset(game.player);
	}

	PlayerAnimations2D playerAnimations();

	GhostAnimations2D ghostAnimations();

	MazeAnimations2D mazeAnimations();

	// only used in Ms. Pac-Man intermission scenes

	default TimedSequence<?> storkFlying() {
		return null;
	}

	default TimedSequence<?> flapFlapping() {
		return null;
	}
}