package de.amr.games.pacman.ui;

import java.util.stream.Stream;

import de.amr.games.pacman.lib.Animation;
import de.amr.games.pacman.lib.Direction;
import de.amr.games.pacman.model.GameModel;
import de.amr.games.pacman.model.guys.Ghost;
import de.amr.games.pacman.model.guys.Pac;

/**
 * Visual animations inside the scenes..
 * 
 * @author Armin Reichert
 */
public interface PacManGameAnimations {

	default void reset(GameModel game) {
		mazeFlashings().forEach(Animation::reset);
		energizerBlinking().reset();
		ghostFlashing().reset();
		ghostsFrightened(game.ghosts()).forEach(Animation::reset);
		ghostsKicking(game.ghosts()).forEach(Animation::reset);
		ghostsReturningHome(game.ghosts()).forEach(Animation::reset);
		playerMunching(game.pac).forEach(Animation::reset);
		playerDying().reset();
	}

	Animation<?> playerMunching(Pac pac, Direction dir);

	default Stream<Animation<?>> playerMunching(Pac pac) {
		return Direction.stream().map(dir -> playerMunching(pac, dir));
	}

	Animation<?> playerDying();

	Animation<?> ghostKicking(Ghost ghost, Direction dir);

	default Stream<Animation<?>> ghostKicking(Ghost ghost) {
		return Direction.stream().map(dir -> ghostKicking(ghost, dir));
	}

	default Stream<Animation<?>> ghostsKicking(Stream<Ghost> ghosts) {
		return ghosts.flatMap(this::ghostKicking);
	}

	Animation<?> ghostFrightened(Ghost ghost, Direction dir);

	default Stream<Animation<?>> ghostFrightened(Ghost ghost) {
		return Direction.stream().map(dir -> ghostFrightened(ghost, dir));
	}

	default Stream<Animation<?>> ghostsFrightened(Stream<Ghost> ghosts) {
		return ghosts.flatMap(this::ghostFrightened);
	}

	Animation<?> ghostFlashing();

	Animation<?> ghostReturningHomeToDir(Ghost ghost, Direction dir);

	default Stream<Animation<?>> ghostReturningHome(Ghost ghost) {
		return Direction.stream().map(dir -> ghostReturningHomeToDir(ghost, dir));
	}

	default Stream<Animation<?>> ghostsReturningHome(Stream<Ghost> ghosts) {
		return ghosts.flatMap(this::ghostReturningHome);
	}

	Animation<?> mazeFlashing(int mazeNumber);

	Stream<Animation<?>> mazeFlashings();

	Animation<Boolean> energizerBlinking();

	// Ms. Pac-Man only:

	Animation<?> storkFlying();
}