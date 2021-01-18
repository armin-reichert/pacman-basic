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
	public final String name;

	/** The current state of the ghost. */
	public GhostState state;

	/** The bounty earned for killing this ghost. */
	public int bounty;

	/** The individual food counter, used to compute when the ghost can leave the house. */
	public int dotCounter;

	/**
	 * The "Cruise Elroy" mode of Blinky, the red ghost. Value is 1, 2 or -1, -2 (disabled Elroy mode).
	 */
	public byte elroyMode;

	/** The tile that the ghost tries to reach. Can be inaccessible or outside of the maze. */
	public V2i targetTile = V2i.NULL;

	public Ghost(int id, String name) {
		this.id = (byte) id;
		this.name = name;
	}
}