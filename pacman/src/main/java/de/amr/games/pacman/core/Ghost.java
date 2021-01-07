package de.amr.games.pacman.core;

import de.amr.games.pacman.lib.V2i;

/**
 * A ghost.
 * 
 * @author Armin Reichert
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
			return String.format("%s (Cruise Elroy %d)", NAMES[BLINKY], elroyMode);
		}
		return NAMES[id];
	}
}