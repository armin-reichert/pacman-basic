package de.amr.games.pacman.model.pacman;

import de.amr.games.pacman.model.common.Creature;

/**
 * Bonus symbol. In Ms. Pac-Man, the bonus wanders the maze.
 * 
 * @author Armin Reichert
 */
public class PacManBonus extends Creature {

	/** ID of the bonus symbol. */
	public byte symbol;

	/** Value of this bonus. */
	public int points;

	/** Number of clock ticks the bonus is still available for eating. */
	public long edibleTicksLeft;

	/** Number of clock ticks the consumed bonus is still displayed. */
	public long eatenTicksLeft;

	public void activate(long ticks) {
		edibleTicksLeft = ticks;
	}

	public void eatAndDisplayValue(long ticks) {
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