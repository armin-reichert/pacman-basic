package de.amr.games.pacman.creatures;

import de.amr.games.pacman.lib.Direction;
import de.amr.games.pacman.lib.V2i;

public class Bonus extends Creature {

	public V2i startLocation;
	public Direction targetDirection;
	public byte symbol;
	public long availableTicks;
	public long consumedTicks;
}