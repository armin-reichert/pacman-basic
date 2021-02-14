package de.amr.games.pacman.model;

import de.amr.games.pacman.lib.Direction;

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

	@Override
	public String toString() {
		return String.format("%s: position: %s, speed=%.2f, dir=%s, wishDir=%s", name, position, speed, dir, wishDir);
	}

	public Pac(String pacName, Direction pacStartDir) {
		name = pacName;
		dir = wishDir = startDir = pacStartDir;
	}
}