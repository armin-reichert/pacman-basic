package de.amr.games.pacman.core;

import de.amr.games.pacman.core.Game.Level;
import de.amr.games.pacman.lib.V2i;

public class PacMan extends Creature {

	public int powerTicks;
	public int restingTicks;
	public long starvingTicks;

	public PacMan(String name, V2i homeTile) {
		super(name, homeTile);
	}

	@Override
	public void updateSpeed(World world, Level level) {
		speed = powerTicks > 0 ? level.pacManSpeedPowered : level.pacManSpeed;
	}
}
