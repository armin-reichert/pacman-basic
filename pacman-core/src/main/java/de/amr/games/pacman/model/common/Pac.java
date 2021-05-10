package de.amr.games.pacman.model.common;

import de.amr.games.pacman.lib.TickTimer;
import de.amr.games.pacman.model.world.PacManGameWorld;

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

	/** Controls the time Pac has power. */
	public TickTimer powerTimer = new TickTimer();

	/** Number of clock ticks Pac is resting and will not move. */
	public long restingTicksLeft = 0;

	/** Number of clock ticks Pac has not eaten any pellet. */
	public long starvingTicks = 0;

	@Override
	public String toString() {
		return String.format("%s: position: %s, speed=%.2f, dir=%s, wishDir=%s", name, position, speed, dir(), wishDir());
	}

	public Pac(String name, PacManGameWorld world) {
		this.name = name;
		this.world = world;
	}
}