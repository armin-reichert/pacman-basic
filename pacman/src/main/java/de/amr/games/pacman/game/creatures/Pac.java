package de.amr.games.pacman.game.creatures;

import de.amr.games.pacman.game.core.PacManGameWorld;

/**
 * The Pac-Man.
 * 
 * @author Armin Reichert
 */
public class Pac extends Creature {

	/** Readable name, "Pac-Man" or "Ms. Pac-Man". */
	public String name;

	/** If Pac is dead. */
	public boolean dead;

	/** Number of clock ticks Pac still has power. */
	public long powerTicksLeft;

	/** Number of clock ticks Pac is resting and will not move. */
	public long restingTicksLeft;

	/** Number of clock ticks Pac has not eaten any pellet. */
	public long starvingTicks;

	/** Used for demo/autopilot mode. */
	public boolean immune;

	public Pac(PacManGameWorld world) {
		super(world);
	}
}