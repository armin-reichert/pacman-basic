package de.amr.games.pacman.ui;

import java.util.stream.Stream;

import de.amr.games.pacman.lib.Animation;
import de.amr.games.pacman.lib.Direction;
import de.amr.games.pacman.model.common.GameModel;
import de.amr.games.pacman.model.common.Pac;

/**
 * Visual animations inside the scenes..
 * 
 * @author Armin Reichert
 */
public interface PacManGameAnimations {

	// TODO improve this
	default void reset(GameModel game) {
		mazeFlashings().forEach(Animation::reset);
		energizerBlinking().reset();
		game.ghosts().forEach(ghostAnimations()::reset);
		playerMunching(game.pac).forEach(Animation::reset);
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

	GhostAnimations ghostAnimations();

	Animation<?> mazeFlashing(int mazeNumber);

	Stream<Animation<?>> mazeFlashings();

	Animation<Boolean> energizerBlinking();

	Animation<?> storkFlying();

	Animation<?> flapFlapping();
}