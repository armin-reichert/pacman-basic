package de.amr.games.pacman.ui.animation;

import java.util.stream.Stream;

import de.amr.games.pacman.lib.Direction;
import de.amr.games.pacman.model.common.Pac;

/**
 * Animations for the player (Pac-Man and Ms. Pac-Man).
 * 
 * @author Armin Reichert
 */
public interface PlayerAnimations {

	default void reset(Pac player) {
		playerMunching(player).forEach(Animation::reset);
		playerDying().reset();
	}

	Animation<?> playerMunching(Pac player, Direction dir);

	default Stream<Animation<?>> playerMunching(Pac player) {
		return Direction.stream().map(dir -> playerMunching(player, dir));
	}

	Animation<?> spouseMunching(Pac spouse, Direction dir);

	default Stream<Animation<?>> spouseMunching(Pac spouse) {
		return Direction.stream().map(dir -> spouseMunching(spouse, dir));
	}

	Animation<?> playerDying();
}