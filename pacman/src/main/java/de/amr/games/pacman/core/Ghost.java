package de.amr.games.pacman.core;

import de.amr.games.pacman.lib.V2i;

/**
 * A ghost.
 * 
 * @author Armin Reichert
 *
 */
public class Ghost extends Creature {

	public static final byte BLINKY = 0, PINKY = 1, INKY = 2, CLYDE = 3;
	public static final String[] NAMES = { "Blinky", "Pinky", "Inky", "Clyde" };

	public final byte id;
	public V2i scatterTile;
	public boolean frightened;
	public boolean locked;
	public boolean enteringHouse;
	public boolean leavingHouse;
	public boolean forcedTurningBack;
	public short bounty;
	public int dotCounter;
	public byte elroyMode;

	public Ghost(byte id, V2i homeTile, V2i scatterTile) {
		this.id = id;
		this.homeTile = homeTile;
		this.scatterTile = scatterTile;
	}

	@Override
	public String name() {
		if (id == BLINKY && elroyMode > 0) {
			return String.format("%s (Elroy %d)", NAMES[BLINKY], elroyMode);
		}
		return NAMES[id];
	}

	@Override
	public void updateSpeed(World world, Level level) {
		if (leavingHouse || (locked && id != BLINKY)) {
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