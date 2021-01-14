package de.amr.games.pacman.creatures;

import static de.amr.games.pacman.worlds.PacManGameWorld.TS;

import de.amr.games.pacman.lib.Direction;
import de.amr.games.pacman.lib.V2f;
import de.amr.games.pacman.lib.V2i;
import de.amr.games.pacman.worlds.PacManGameWorld;

/**
 * Base class for Pac-Man, the ghosts and the bonus. Creatures can move through the maze.
 * 
 * @author Armin Reichert
 */
public class Creature {

	/** Left upper corner of TSxTS collision box. Sprites can be larger. */
	public V2f position = V2f.NULL;

	/** The current move direction. */
	public Direction dir;

	/** The intended move direction that will be taken as soon as possible. */
	public Direction wishDir;

	/** Relative speed (between 0 and 1). */
	public float speed;

	/** If the creature is drawn on the screen. */
	public boolean visible;

	/** If the creature entered a new tile with its last movement or placement. */
	public boolean changedTile;

	/** If the creature could move in the last try. */
	public boolean couldMove;

	/** If the next move will in any case take the intended direction if possible. */
	public boolean forcedDirection;

	/** If movement is constrained to be aligned with the tiles. */
	public boolean forcedOnTrack;

	public void placeAt(V2i tile, float offsetX, float offsetY) {
		position = new V2f(tile.x * TS + offsetX, tile.y * TS + offsetY);
		changedTile = true;
	}

	public V2i tile() {
		return PacManGameWorld.tile(position);
	}

	public V2f offset() {
		return PacManGameWorld.offset(position);
	}

	public void setOffset(float offsetX, float offsetY) {
		placeAt(tile(), offsetX, offsetY);
	}
}