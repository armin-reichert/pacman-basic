package de.amr.games.pacman.ui.animation;

import java.util.stream.Stream;

import de.amr.games.pacman.lib.Direction;
import de.amr.games.pacman.model.common.Ghost;

/**
 * Animations for a ghost.
 * 
 * @author Armin Reichert
 */
public interface GhostAnimations2D {

	default void reset(Ghost ghost) {
		ghostFlashing(ghost).reset();
		ghostFrightened(ghost).forEach(TimedSequence::reset);
		ghostKicking(ghost).forEach(TimedSequence::reset);
		ghostReturningHome(ghost).forEach(TimedSequence::reset);
	}

	TimedSequence<?> ghostKicking(Ghost ghost, Direction dir);

	default Stream<TimedSequence<?>> ghostKicking(Ghost ghost) {
		return Direction.stream().map(dir -> ghostKicking(ghost, dir));
	}

	TimedSequence<?> ghostFrightened(Ghost ghost, Direction dir);

	default Stream<TimedSequence<?>> ghostFrightened(Ghost ghost) {
		return Direction.stream().map(dir -> ghostFrightened(ghost, dir));
	}

	TimedSequence<?> ghostFlashing(Ghost ghost);

	TimedSequence<?> ghostReturningHome(Ghost ghost, Direction dir);

	default Stream<TimedSequence<?>> ghostReturningHome(Ghost ghost) {
		return Direction.stream().map(dir -> ghostReturningHome(ghost, dir));
	}

}