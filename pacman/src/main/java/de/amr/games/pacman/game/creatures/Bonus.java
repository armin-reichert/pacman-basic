package de.amr.games.pacman.game.creatures;

import java.util.List;
import java.util.stream.Collectors;

import de.amr.games.pacman.game.worlds.PacManGameWorld;
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

	/** Value of this bonus. */
	public int points;

	/** Number of clock ticks the bonus is still available for eating. */
	public long edibleTicksLeft;

	/** Number of clock ticks the consumed bonus is still displayed. */
	public long eatenTicksLeft;

	/* Ms. Pac-Man only: Direction in which bonus traverses the maze. */
	public Direction wanderingDirection;

	public void wander(PacManGameWorld world) {
		V2i location = tile();
		if (!couldMove || world.isIntersection(location)) {
			List<Direction> dirs = accessibleDirections(world, location, dir.opposite()).collect(Collectors.toList());
			if (dirs.size() > 1) {
				// give random movement a bias towards the wandering direction
				dirs.remove(wanderingDirection.opposite());
			}
			wishDir = dirs.get(rnd.nextInt(dirs.size()));
		}
		tryMoving(world);
	}
}