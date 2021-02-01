package de.amr.games.pacman.ui.api;

import java.awt.image.BufferedImage;
import java.util.stream.Stream;

import de.amr.games.pacman.lib.Animation;
import de.amr.games.pacman.lib.Direction;
import de.amr.games.pacman.model.creatures.Ghost;

public interface PacManGameAnimations {

	Animation<BufferedImage> pacMunching(Direction dir);

	Animation<BufferedImage> pacDying();

	Animation<BufferedImage> ghostWalking(Ghost ghost, Direction dir);

	Animation<BufferedImage> ghostFrightened(Direction dir);

	Animation<BufferedImage> ghostFlashing(Ghost ghost);

	Animation<BufferedImage> mazeFlashing(int mazeNumber);

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
}