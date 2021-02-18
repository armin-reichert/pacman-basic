package de.amr.games.pacman.ui;

import java.util.stream.Stream;

import de.amr.games.pacman.lib.Animation;
import de.amr.games.pacman.lib.Direction;
import de.amr.games.pacman.model.Ghost;
import de.amr.games.pacman.model.Pac;
import de.amr.games.pacman.model.PacManGameModel;

/**
 * Optional visual animation provided by a game view.
 * 
 * @author Armin Reichert
 */
public interface PacManGameAnimation {

	Animation<?> pacMunchingToDir(Pac pac, Direction dir);

	default Stream<Animation<?>> pacMunching(Pac pac) {
		return Stream.of(Direction.values()).map(dir -> pacMunchingToDir(pac, dir));
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

	Stream<Animation<?>> mazeFlashings();

	Animation<Boolean> energizerBlinking();

	default void reset(PacManGameModel game) {
		mazeFlashings().forEach(Animation::reset);
		energizerBlinking().reset();
		ghostFlashing().reset();
		ghostsFrightened(game.ghosts()).forEach(Animation::reset);
		ghostsKicking(game.ghosts()).forEach(Animation::reset);
		ghostsReturningHome(game.ghosts()).forEach(Animation::reset);
		pacMunching(game.pac).forEach(Animation::reset);
		pacDying().reset();
	}
}