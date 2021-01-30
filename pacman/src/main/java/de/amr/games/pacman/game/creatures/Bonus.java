package de.amr.games.pacman.game.creatures;

import de.amr.games.pacman.game.core.PacManGameWorld;
import de.amr.games.pacman.game.worlds.PacManClassicWorld;

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

	public Bonus(PacManGameWorld world) {
		super(world);
	}

	public void activate(byte bonusSymbol, long ticks) {
		visible = true;
		symbol = bonusSymbol;
		points = PacManClassicWorld.BONUS_POINTS[bonusSymbol];
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