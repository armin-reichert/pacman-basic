package de.amr.games.pacman.ui.animation;

import java.util.stream.Stream;

/**
 * Animations for a maze.
 * 
 * @author Armin Reichert
 */
public interface MazeAnimations2D {

	default void reset() {
		mazeFlashings().forEach(TimedSequence::reset);
	}

	TimedSequence<?> mazeFlashing(int mazeNumber);

	Stream<TimedSequence<?>> mazeFlashings();
}