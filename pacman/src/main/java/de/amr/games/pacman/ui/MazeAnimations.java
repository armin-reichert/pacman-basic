package de.amr.games.pacman.ui;

import java.util.stream.Stream;

import de.amr.games.pacman.lib.Animation;

public interface MazeAnimations {

	default void reset() {
		mazeFlashings().forEach(Animation::reset);
		energizerBlinking().reset();
	}

	Animation<?> mazeFlashing(int mazeNumber);

	Stream<Animation<?>> mazeFlashings();

	Animation<Boolean> energizerBlinking();
}