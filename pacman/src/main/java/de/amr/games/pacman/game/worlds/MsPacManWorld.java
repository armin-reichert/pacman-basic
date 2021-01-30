package de.amr.games.pacman.game.worlds;

import de.amr.games.pacman.lib.V2i;

/**
 * Ms. Pac-Man game world. Has 6 maze variants.
 * 
 * @author Armin Reichert
 */
public class MsPacManWorld extends AbstractPacManGameWorld {

	public static int mazeNumber(int levelNumber) {
		if (levelNumber <= 2) {
			return 1; // pink maze, white dots
		}
		if (levelNumber <= 5) {
			return 2; // light blue maze, yellow dots
		}
		if (levelNumber <= 9) {
			return 3; // orange maze, red dots
		}
		if (levelNumber <= 13) {
			return 4; // dark blue maze, white dots
		}
		// from level 14 on, maze switches between 5 and 6 every 4 levels
		if ((levelNumber - 14) % 8 < 4) {
			return 5; // pink maze, cyan dots (same map as maze 3)
		}
		return 6; // orange maze, white dots (same map as maze 4)
	}

	@Override
	public boolean isUpwardsBlocked(V2i tile) {
		return false; // ghosts can travel all paths
	}
}