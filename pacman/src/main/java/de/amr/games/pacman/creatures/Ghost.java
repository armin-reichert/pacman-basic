package de.amr.games.pacman.creatures;

/**
 * A ghost.
 * 
 * @author Armin Reichert
 */
public class Ghost extends Creature {

	public final byte id;
	public boolean frightened;
	public boolean locked;
	public boolean enteringHouse;
	public boolean leavingHouse;
	public short bounty;
	public int dotCounter;
	public byte elroyMode;

	public Ghost(int id) {
		this.id = (byte) id;
	}
}