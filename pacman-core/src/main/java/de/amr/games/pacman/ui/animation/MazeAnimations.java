package de.amr.games.pacman.ui.animation;

import java.util.stream.Stream;

/**
 * Animations for a maze.
 * 
 * @author Armin Reichert
 */
public interface MazeAnimations {

	default void reset() {
		mazeFlashings().forEach(Animation::reset);
		energizerBlinking().reset();
	}

	Animation<?> mazeFlashing(int mazeNumber);

	Stream<Animation<?>> mazeFlashings();

	Animation<Boolean> energizerBlinking();
}