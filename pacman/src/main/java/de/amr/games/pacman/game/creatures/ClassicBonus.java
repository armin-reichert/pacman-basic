package de.amr.games.pacman.game.creatures;

import static de.amr.games.pacman.game.worlds.PacManGameWorld.HTS;

import de.amr.games.pacman.game.worlds.PacManClassicWorld;
import de.amr.games.pacman.game.worlds.PacManGameWorld;

/**
 * Bonus symbol. In Ms. Pac-Man, the bonus wanders the maze.
 * 
 * @author Armin Reichert
 */
public class ClassicBonus extends Creature {

	/** ID of the bonus symbol. */
	public byte symbol;

	/** Value of this bonus. */
	public int points;

	/** Number of clock ticks the bonus is still available for eating. */
	public long edibleTicksLeft;

	/** Number of clock ticks the consumed bonus is still displayed. */
	public long eatenTicksLeft;

	public ClassicBonus(PacManGameWorld world) {
		super(world);
	}

	public void activate(byte bonusSymbol, long ticks) {
		visible = true;
		symbol = bonusSymbol;
		points = PacManClassicWorld.BONUS_POINTS[bonusSymbol];
		edibleTicksLeft = ticks;
		placeAt(PacManClassicWorld.BONUS_TILE, HTS, 0);
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