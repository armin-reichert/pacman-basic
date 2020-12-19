package de.amr.games.pacman.core;

import de.amr.games.pacman.lib.V2i;

public class PacMan extends Creature {

	public int powerTicks;
	public int restingTicks;
	public long starvingTicks;
	public long collapsingTicksRemaining;

	public PacMan(V2i homeTile) {
		super(homeTile);
	}

	@Override
	public String name() {
		return "Pac-Man";
	}

	@Override
	public void updateSpeed(World world, Level level) {
		speed = powerTicks > 0 ? level.pacManSpeedPowered : level.pacManSpeed;
	}
}
