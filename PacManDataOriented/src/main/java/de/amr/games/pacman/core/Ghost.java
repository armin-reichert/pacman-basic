package de.amr.games.pacman.core;

import de.amr.games.pacman.lib.V2i;

public class Ghost extends Creature {

	public static final int BLINKY = 0, PINKY = 1, INKY = 2, CLYDE = 3;

	public final V2i scatterTile;
	public V2i targetTile;
	public boolean frightened;
	public boolean locked;
	public boolean enteringHouse;
	public boolean leavingHouse;
	public int bounty;
	public int dotCounter;
	public byte elroyMode;

	public Ghost(String name, V2i homeTile, V2i scatterTile) {
		super(name, homeTile);
		this.scatterTile = scatterTile;
	}

	@Override
	public void updateSpeed(World world, Game.Level level) {
		if (leavingHouse || (locked && !name.equals("Blinky"))) {
			speed = 0.75f * level.ghostSpeed;
		} else if (dead) {
			speed = 2f * level.ghostSpeed;
		} else if (world.isInsideTunnel(tile().x, tile().y)) {
			speed = level.ghostSpeedTunnel;
		} else if (frightened) {
			speed = level.ghostSpeedFrightened;
		} else if (elroyMode == 1) {
			speed = level.elroy1Speed;
		} else if (elroyMode == 2) {
			speed = level.elroy2Speed;
		} else {
			speed = level.ghostSpeed;
		}
	}
}