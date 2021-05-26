package de.amr.games.pacman.model.common;

import de.amr.games.pacman.lib.TickTimer;
import de.amr.games.pacman.model.world.PacManGameWorld;

/**
 * Pac-Man or Ms. Pac-Man.
 * 
 * @author Armin Reichert
 */
public class Pac extends Creature {

	/** If Pac is dead. */
	public boolean dead = false;

	/** Controls the time Pac has power. */
	public TickTimer powerTimer = new TickTimer("Pac-power-timer");

	/** Number of clock ticks Pac is still resting and will not move. */
	public long restingTicksLeft = 0;

	/** Number of clock ticks Pac has not eaten any pellet. */
	public long starvingTicks = 0;

	public Pac(String name, PacManGameWorld world) {
		super(world, name);
	}

	@Override
	public String toString() {
		return String.format("%s: pos: %s, speed=%.2f, dir=%s, wishDir=%s", name, position, speed, dir(), wishDir());
	}

}