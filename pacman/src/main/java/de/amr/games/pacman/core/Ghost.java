package de.amr.games.pacman.core;

import de.amr.games.pacman.lib.V2i;

/**
 * A ghost.
 * 
 * @author Armin Reichert
 */
public class Ghost extends Creature {

	public static final byte BLINKY = 0, PINKY = 1, INKY = 2, CLYDE = 3;

	public final byte id;
	public V2i scatterTile;
	public boolean frightened;
	public boolean locked;
	public boolean enteringHouse;
	public boolean leavingHouse;
	public short bounty;
	public int dotCounter;
	public byte elroyMode;

	public Ghost(byte id) {
		this.id = id;
	}
}