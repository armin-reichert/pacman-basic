package de.amr.games.pacman.ui;

import java.util.stream.Stream;

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
		mazeFlashings().forEach(Animation::reset);
		energizerBlinking().reset();
		game.ghosts().forEach(ghostAnimations()::reset);
		playerAnimations().reset(game.pac);
	}

	PlayerAnimations playerAnimations();

	GhostAnimations ghostAnimations();

	Animation<?> mazeFlashing(int mazeNumber);

	Stream<Animation<?>> mazeFlashings();

	Animation<Boolean> energizerBlinking();

	Animation<?> storkFlying();

	Animation<?> flapFlapping();
}