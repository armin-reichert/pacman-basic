package de.amr.games.pacman.entities;

import de.amr.games.pacman.common.V2i;

public class PacMan extends Creature {

	public long powerTimer;
	public byte stoppedTicks;
	public long starvingTicks;

	public PacMan(String name, V2i homeTile) {
		super(name, homeTile);
	}
}
