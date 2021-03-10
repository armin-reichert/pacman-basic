package de.amr.games.pacman.ui.animation;

import java.util.stream.Stream;

import de.amr.games.pacman.lib.Direction;
import de.amr.games.pacman.model.common.Ghost;

/**
 * Animations for a ghost.
 * 
 * @author Armin Reichert
 */
public interface GhostAnimations {

	default void reset(Ghost ghost) {
		ghostFlashing(ghost).reset();
		ghostFrightened(ghost).forEach(Animation::reset);
		ghostKicking(ghost).forEach(Animation::reset);
		ghostReturningHome(ghost).forEach(Animation::reset);
	}

	Animation<?> ghostKicking(Ghost ghost, Direction dir);

	default Stream<Animation<?>> ghostKicking(Ghost ghost) {
		return Direction.stream().map(dir -> ghostKicking(ghost, dir));
	}

	Animation<?> ghostFrightened(Ghost ghost, Direction dir);

	default Stream<Animation<?>> ghostFrightened(Ghost ghost) {
		return Direction.stream().map(dir -> ghostFrightened(ghost, dir));
	}

	Animation<?> ghostFlashing(Ghost ghost);

	Animation<?> ghostReturningHome(Ghost ghost, Direction dir);

	default Stream<Animation<?>> ghostReturningHome(Ghost ghost) {
		return Direction.stream().map(dir -> ghostReturningHome(ghost, dir));
	}

}