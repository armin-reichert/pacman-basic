package de.amr.games.pacman.core;

import de.amr.games.pacman.lib.V2i;

/**
 * The Pac-Man.
 * 
 * @author Armin Reichert
 */
public class PacMan extends Creature {

	public long powerTicksLeft;
	public long restingTicksLeft;
	public long collapsingTicksLeft;
	public long starvingTicks;

	public PacMan(V2i homeTile) {
		this.homeTile = homeTile;
	}

	@Override
	public String name() {
		return "Pac-Man";
	}
}