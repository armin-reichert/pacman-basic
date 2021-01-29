package de.amr.games.pacman.ui.api;

import de.amr.games.pacman.game.creatures.Ghost;

public interface PacManGameAnimations {

	default void startGhostWalking(Ghost ghost) {
	}

	default void stopGhostWalking(Ghost ghost) {
	}

	default void startPacManCollapsing() {
	}

	default void endPacManCollapsing() {
	}

	default void startMazeFlashing(int repetitions) {
	}

	default void endMazeFlashing() {
	}
}