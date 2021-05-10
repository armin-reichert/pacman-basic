package de.amr.games.pacman.model.pacman;

import java.util.Random;

import de.amr.games.pacman.model.common.Creature;
import de.amr.games.pacman.model.world.PacManGameWorld;

/**
 * Bonus symbol. In Ms. Pac-Man, the bonus wanders the maze.
 * 
 * @author Armin Reichert
 */
public class Bonus extends Creature {

	protected Random random = new Random();

	/** ID of the bonus symbol. */
	public String symbol;

	/** Value of this bonus. */
	public int points;

	/** Number of clock ticks the bonus is still available for eating. */
	public long edibleTicksLeft;

	/** Number of clock ticks the consumed bonus is still displayed. */
	public long eatenTicksLeft;

	public Bonus(PacManGameWorld world) {
		this.world = world;
	}

	public void activate(long ticks) {
		edibleTicksLeft = ticks;
	}

	public void eaten(long ticks) {
		edibleTicksLeft = 0;
		eatenTicksLeft = ticks;
	}

	public void update() {
		if (edibleTicksLeft > 0) {
			edibleTicksLeft--;
			if (edibleTicksLeft == 0) {
				edibleTicksLeft = 0;
				visible = false;
			}
		}
		if (eatenTicksLeft > 0) {
			eatenTicksLeft--;
			if (eatenTicksLeft == 0) {
				eatenTicksLeft = 0;
				visible = false;
			}
		}
	}
}