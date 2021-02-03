package de.amr.games.pacman.ui.api;

import java.awt.image.BufferedImage;
import java.util.stream.Stream;

import de.amr.games.pacman.lib.Animation;
import de.amr.games.pacman.lib.Direction;
import de.amr.games.pacman.model.PacManGame;
import de.amr.games.pacman.model.creatures.Ghost;

public interface PacManGameAnimations {

	Animation<BufferedImage> pacMunching(Direction dir);

	Animation<BufferedImage> pacDying();

	Animation<BufferedImage> ghostWalking(Ghost ghost, Direction dir);

	Animation<BufferedImage> ghostFrightened(Ghost ghost, Direction dir);

	Animation<BufferedImage> ghostFlashing(Ghost ghost);

	Animation<BufferedImage> ghostReturningHome(Ghost ghost, Direction dir);

	Animation<BufferedImage> mazeFlashing(int mazeNumber);

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

	default void letPacMunch() {
		Stream.of(Direction.values()).forEach(dir -> pacMunching(dir).restart());
	}

	default void resetAll(PacManGame game) {
		game.ghosts().forEach(ghost -> {
			ghostFlashing(ghost).reset();
			Stream.of(Direction.values()).forEach(dir -> ghostFrightened(ghost, dir).reset());
			Stream.of(Direction.values()).forEach(dir -> ghostWalking(ghost, dir).reset());
		});
		Stream.of(Direction.values()).forEach(dir -> pacMunching(dir).reset());
		pacDying().reset();
		energizerBlinking().reset();
	}
}