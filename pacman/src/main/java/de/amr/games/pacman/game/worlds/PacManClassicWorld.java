package de.amr.games.pacman.game.worlds;

import java.util.Arrays;
import java.util.List;

import de.amr.games.pacman.lib.V2i;

/**
 * The game world used by the classic Pac-Man game.
 * 
 * @author Armin Reichert
 */
public class PacManClassicWorld extends AbstractPacManGameWorld {

	public static final List<V2i> UPWARDS_BLOCKED_TILES = Arrays.asList(new V2i(12, 13), new V2i(15, 13), new V2i(12, 25),
			new V2i(15, 25));

	public PacManClassicWorld() {
		loadMap("/worlds/classic/map.txt");
	}

	@Override
	public boolean isUpwardsBlocked(V2i tile) {
		return UPWARDS_BLOCKED_TILES.contains(tile);
	}
}