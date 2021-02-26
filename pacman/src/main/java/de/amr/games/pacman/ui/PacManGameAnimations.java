package de.amr.games.pacman.ui;

import de.amr.games.pacman.lib.Animation;
import de.amr.games.pacman.model.common.GameModel;

/**
 * Visual animations inside the scenes..
 * 
 * @author Armin Reichert
 */
public interface PacManGameAnimations {

	// TODO improve this
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