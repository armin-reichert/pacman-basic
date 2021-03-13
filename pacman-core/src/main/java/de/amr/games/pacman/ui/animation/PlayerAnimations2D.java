package de.amr.games.pacman.ui.animation;

import java.util.stream.Stream;

import de.amr.games.pacman.lib.Direction;
import de.amr.games.pacman.model.common.Pac;

/**
 * Animations for the player (Pac-Man and Ms. Pac-Man).
 * 
 * @author Armin Reichert
 */
public interface PlayerAnimations2D {

	default void reset(Pac player) {
		playerMunching(player).forEach(TimedSequence::reset);
		playerDying().reset();
	}

	TimedSequence<?> playerMunching(Pac player, Direction dir);

	default Stream<TimedSequence<?>> playerMunching(Pac player) {
		return Direction.stream().map(dir -> playerMunching(player, dir));
	}

	TimedSequence<?> spouseMunching(Pac spouse, Direction dir);

	default Stream<TimedSequence<?>> spouseMunching(Pac spouse) {
		return Direction.stream().map(dir -> spouseMunching(spouse, dir));
	}

	TimedSequence<?> playerDying();
}