package de.amr.games.pacman.creatures;

import de.amr.games.pacman.lib.V2i;

/**
 * A ghost.
 * 
 * @author Armin Reichert
 */
public class Ghost extends Creature {

	/** The unique ID of the ghost (0..3). */
	public final byte id;

	/** The readable name of the ghost. */
	public String name;

	/** If the ghost can be eaten by the Pac. */
	public boolean frightened;

	/** If the ghost is dead. */
	public boolean dead;

	/** If the ghost can leave its home tile. */
	public boolean locked;

	/** If the ghost is entering the house. */
	public boolean enteringHouse;

	/** If the ghost is leaving the house. */
	public boolean leavingHouse;

	/** The bounty for this killing ghost. */
	public short bounty;

	/** The individual counter of the ghost, used by the logic when ghosts can leave the house. */
	public int dotCounter;

	/** The "Cruise Elroy" mode of Blinky, the red ghost. */
	public byte elroyMode;

	/** The tile that the ghost tries to move to. Can be inaccessible and outside of the maze. */
	public V2i targetTile = V2i.NULL;

	public Ghost(int id) {
		this.id = (byte) id;
	}
}