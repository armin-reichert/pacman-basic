package de.amr.games.pacman.model.common;

import de.amr.games.pacman.lib.Direction;

/**
 * Pac-Man or Ms. Pac-Man.
 * 
 * @author Armin Reichert
 */
public class Pac extends Creature {

	/** Readable name, "Pac-Man" or "Ms. Pac-Man". */
	public final String name;

	/** If Pac is dead. */
	public boolean dead = false;

	/** Number of clock ticks Pac still has power. */
	public long powerTicksLeft = 0;

	/** Number of clock ticks Pac is resting and will not move. */
	public long restingTicksLeft = 0;

	/** Number of clock ticks Pac has not eaten any pellet. */
	public long starvingTicks = 0;

	/** Used for demo/autopilot mode. */
	public boolean immune = false;

	@Override
	public String toString() {
		return String.format("%s: position: %s, speed=%.2f, dir=%s, wishDir=%s", name, position, speed, dir, wishDir);
	}

	public Pac(String name, Direction initialDir) {
		this.name = name;
		this.dir = this.wishDir = this.startDir = initialDir;
	}
}