package de.amr.games.pacman.ui.api;

import java.util.stream.Stream;

import de.amr.games.pacman.lib.Animation;
import de.amr.games.pacman.lib.Direction;
import de.amr.games.pacman.model.AbstractPacManGame;
import de.amr.games.pacman.model.creatures.Ghost;

public interface PacManGameAnimations {

	Animation<?> pacMunching(Direction dir);

	Animation<?> pacDying();

	Animation<?> ghostWalking(Ghost ghost, Direction dir);

	Animation<?> ghostFrightened(Ghost ghost, Direction dir);

	Animation<?> ghostFlashing();

	Animation<?> ghostReturningHome(Ghost ghost, Direction dir);

	Animation<?> mazeFlashing(int mazeNumber);

	Animation<Boolean> energizerBlinking();

	default void letGhostsFidget(Stream<Ghost> ghosts, boolean on) {
		ghosts.forEach(ghost -> {
			Stream.of(Direction.values()).forEach(dir -> {
				if (on) {
					ghostWalking(ghost, dir).restart();
				} else {
					ghostWalking(ghost, dir).stop();
				}
			});
		});
	}

	default void letGhostBeFrightened(Ghost ghost, boolean on) {
		Stream.of(Direction.values()).forEach(dir -> {
			if (on) {
				ghostFrightened(ghost, dir).restart();
			} else {
				ghostFrightened(ghost, dir).stop();
			}
		});
	}

	default void letPacMunch(boolean on) {
		if (on) {
			Stream.of(Direction.values()).forEach(dir -> pacMunching(dir).restart());
		} else {
			Stream.of(Direction.values()).forEach(dir -> pacMunching(dir).reset());
		}
	}

	default void resetAllAnimations(AbstractPacManGame game) {
		energizerBlinking().reset();
		ghostFlashing().reset();
		game.ghosts().forEach(ghost -> {
			Stream.of(Direction.values()).forEach(dir -> ghostFrightened(ghost, dir).reset());
			Stream.of(Direction.values()).forEach(dir -> ghostWalking(ghost, dir).reset());
		});
		Stream.of(Direction.values()).forEach(dir -> pacMunching(dir).reset());
		pacDying().reset();
	}
}