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
package de.amr.games.pacman.model.pacman.entities;

import de.amr.games.pacman.model.common.Creature;

/**
 * Bonus symbol. In Ms. Pac-Man, the bonus wanders the maze.
 * 
 * @author Armin Reichert
 */
public class Bonus extends Creature {

	public enum BonusState {
		INACTIVE, EDIBLE, EATEN;
	}

	public BonusState state;
	public int symbol;
	public int points;
	public long timer;

	public Bonus() {
		super("Bonus");
		init();
	}

	public void init() {
		state = BonusState.INACTIVE;
		timer = 0;
		hide();
	}

	public void activate(long ticks, int symbol, int points) {
		state = BonusState.EDIBLE;
		timer = ticks;
		this.symbol = symbol;
		this.points = points;
		show();
	}

	public void eatAndShowValue(long ticks) {
		state = BonusState.EATEN;
		timer = ticks;
	}

	/**
	 * Updates the bonus state and returns any usefule info about the new bonus state
	 * 
	 * @return {@code true} if the bonus expired
	 */
	public boolean updateState() {
		switch (state) {

		case INACTIVE -> {
			return false;
		}

		case EDIBLE, EATEN -> {
			if (timer == 0) {
				hide();
				state = BonusState.INACTIVE;
				return true;
			}
			timer--;
			return false;
		}

		default -> throw new IllegalStateException(String.format("Illegal bonus state '%s'", state));
		}
	}
}