package de.amr.games.pacman.ui.animation;

import de.amr.games.pacman.model.common.GameModel;

/**
 * Animations for a game.
 * 
 * @author Armin Reichert
 */
public interface PacManGameAnimations {

	default void reset(GameModel game) {
		mazeAnimations().reset();
		game.ghosts().forEach(ghostAnimations()::reset);
		playerAnimations().reset(game.pac);
	}

	PlayerAnimations playerAnimations();

	GhostAnimations ghostAnimations();

	MazeAnimations mazeAnimations();

	Animation<?> storkFlying();

	Animation<?> flapFlapping();
}