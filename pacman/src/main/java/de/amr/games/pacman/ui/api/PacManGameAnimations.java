package de.amr.games.pacman.ui.api;

public interface PacManGameAnimations {

	default void startPacManCollapsing() {
	}

	default void endPacManCollapsing() {
	}

	default void startMazeFlashing(int repetitions) {
	}

	default void endMazeFlashing() {
	}
}