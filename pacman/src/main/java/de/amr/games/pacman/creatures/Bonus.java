package de.amr.games.pacman.creatures;

import de.amr.games.pacman.lib.Direction;
import de.amr.games.pacman.lib.V2i;

/**
 * Bonus symbol. In Ms. Pac-Man, the bonus wanders the maze.
 * 
 * @author Armin Reichert
 */
public class Bonus extends Creature {

	/** ID of the bonus symbol. */
	public byte symbol;

	/** Number of clock ticks the bonus is still available for eating. */
	public long availableTicks;

	/** Number of clock ticks the consumed bonus is still displayed. */
	public long consumedTicks;

	/* Ms. Pac-Man only: Portal tile where bonus enters the maze. */
	public V2i startTile;

	/* Ms. Pac-Man only: Diretion in which bonus traverses the maze. */
	public Direction targetDirection;
}