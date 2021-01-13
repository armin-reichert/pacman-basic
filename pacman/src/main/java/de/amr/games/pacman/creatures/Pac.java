package de.amr.games.pacman.creatures;

import de.amr.games.pacman.lib.V2i;

/**
 * The Pac-Man.
 * 
 * @author Armin Reichert
 */
public class Pac extends Creature {

	/** Readable name if the Pac e.g. "Ms. Pac.Man". */
	public String name;

	/** If the Pac is dead. */
	public boolean dead;

	/** Number of clock ticks the Pac still has power. */
	public long powerTicksLeft;

	/** Number of clock ticks the Pac is resting and will not move. */
	public long restingTicksLeft;

	/** Number of clock ticks the Pac still is collapsing. */
	public long collapsingTicksLeft;

	/** Number of clock ticks the Pac has not eaten. */
	public long starvingTicks;

	/** The tile that the Pac is targeting, used by autopilot. */
	public V2i targetTile = V2i.NULL;
}