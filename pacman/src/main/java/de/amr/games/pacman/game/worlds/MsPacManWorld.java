package de.amr.games.pacman.game.worlds;

import de.amr.games.pacman.lib.V2i;

/**
 * Ms. Pac-Man game world. Has 6 maze variants.
 * 
 * @author Armin Reichert
 */
public class MsPacManWorld extends AbstractPacManGameWorld {

	@Override
	public boolean isUpwardsBlocked(V2i tile) {
		return false; // ghosts can travel all paths
	}
}