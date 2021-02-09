package de.amr.games.pacman.ui.api;

import java.util.stream.Stream;

import de.amr.games.pacman.lib.Animation;
import de.amr.games.pacman.lib.Direction;
import de.amr.games.pacman.model.AbstractPacManGame;
import de.amr.games.pacman.model.creatures.Ghost;

/**
 * Optional animations provided by a game UI.
 * 
 * @author Armin Reichert
 */
public interface PacManGameAnimations {

	Animation<?> pacMunchingToDir(Direction dir);

	default Stream<Animation<?>> pacMunching() {
		return Stream.of(Direction.values()).map(this::pacMunchingToDir);
	}

	Animation<?> pacDying();

	Animation<?> ghostKickingToDir(Ghost ghost, Direction dir);

	default Stream<Animation<?>> ghostKicking(Ghost ghost) {
		return Stream.of(Direction.values()).map(dir -> ghostKickingToDir(ghost, dir));
	}

	default Stream<Animation<?>> ghostsKicking(Stream<Ghost> ghosts) {
		return ghosts.flatMap(this::ghostKicking);
	}

	Animation<?> ghostFrightenedToDir(Ghost ghost, Direction dir);

	default Stream<Animation<?>> ghostFrightened(Ghost ghost) {
		return Stream.of(Direction.values()).map(dir -> ghostFrightenedToDir(ghost, dir));
	}

	default Stream<Animation<?>> ghostsFrightened(Stream<Ghost> ghosts) {
		return ghosts.flatMap(this::ghostFrightened);
	}

	Animation<?> ghostFlashing();

	Animation<?> ghostReturningHomeToDir(Ghost ghost, Direction dir);

	default Stream<Animation<?>> ghostReturningHome(Ghost ghost) {
		return Stream.of(Direction.values()).map(dir -> ghostReturningHomeToDir(ghost, dir));
	}

	default Stream<Animation<?>> ghostsReturningHome(Stream<Ghost> ghosts) {
		return ghosts.flatMap(this::ghostReturningHome);
	}

	Animation<?> mazeFlashing(int mazeNumber);

	Animation<Boolean> energizerBlinking();

	default void resetAllAnimations(AbstractPacManGame game) {
		energizerBlinking().reset();
		ghostFlashing().reset();
		ghostsFrightened(game.ghosts()).forEach(Animation::reset);
		ghostsKicking(game.ghosts()).forEach(Animation::reset);
		ghostsReturningHome(game.ghosts()).forEach(Animation::reset);
		pacMunching().forEach(Animation::reset);
		pacDying().reset();
	}
}