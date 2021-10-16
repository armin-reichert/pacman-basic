/*
MIT License

Copyright (c) 2021 Armin Reichert

Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files (the "Software"), to deal
in the Software without restriction, including without limitation the rights
to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
copies of the Software, and to permit persons to whom the Software is
furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all
copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
SOFTWARE.
 */
package de.amr.games.pacman.model.pacman;

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

	public Bonus(PacManGameWorld world) {
		super(world, "Bonus");
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