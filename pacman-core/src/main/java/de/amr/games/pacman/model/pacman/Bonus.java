package de.amr.games.pacman.model.pacman;

import java.util.Random;

import de.amr.games.pacman.controller.event.PacManGameEvent;
import de.amr.games.pacman.controller.event.PacManGameEvent.Info;
import de.amr.games.pacman.model.common.Creature;
import de.amr.games.pacman.model.world.PacManGameWorld;

/**
 * Bonus symbol. In Ms. Pac-Man, the bonus wanders the maze.
 * 
 * @author Armin Reichert
 */
public class Bonus extends Creature {

	public static final int INACTIVE = 0;
	public static final int EDIBLE = 1;
	public static final int EATEN = 2;

	/** ID of the bonus symbol. */
	public String symbol;

	/** Value of this bonus. */
	public int points;

	/** Number of ticks left in current state. */
	public long timer;

	public int state;

	protected Random random = new Random();

	public Bonus(PacManGameWorld world) {
		this.world = world;
		init();
	}

	public void init() {
		state = INACTIVE;
		timer = 0;
		visible = false;
		speed = 0;
		newTileEntered = true;
		stuck = false;
		forcedOnTrack = true;
	}

	public void activate(long ticks) {
		state = EDIBLE;
		timer = ticks;
		visible = true;
	}

	public void eaten(long ticks) {
		state = EATEN;
		timer = ticks;
	}

	public PacManGameEvent.Info update() {
		switch (state) {
		case INACTIVE:
			return null;

		case EDIBLE:
			if (timer == 0) {
				visible = false;
				state = INACTIVE;
				return Info.BONUS_EXPIRED;
			}
			timer--;
			return null;

		case EATEN:
			if (timer == 0) {
				visible = false;
				state = INACTIVE;
				return Info.BONUS_EXPIRED;
			}
			timer--;
			return null;

		default:
			throw new IllegalStateException();
		}
	}
}