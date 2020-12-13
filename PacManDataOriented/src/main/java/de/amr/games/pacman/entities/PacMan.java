package de.amr.games.pacman.entities;

import de.amr.games.pacman.lib.V2i;

public class PacMan extends Creature {

	public int powerTicks;
	public int restingTicks;
	public long starvingTicks;

	public PacMan(String name, V2i homeTile) {
		super(name, homeTile);
	}
}
