/*
MIT License

Copyright (c) 2021-22 Armin Reichert

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

import static de.amr.games.pacman.model.common.world.World.HTS;

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

	public void init() {
		timer = 0;
		hide();
		state = BonusState.INACTIVE;
	}

	public void activate(int symbol, int points) {
		this.symbol = symbol;
		this.points = points;
		placeAt(world.bonusTile(), HTS, 0);
		show();
		state = BonusState.EDIBLE;
	}

	public void eat() {
		state = BonusState.EATEN;
	}

	public void update() {
		switch (state) {
		case EDIBLE, EATEN -> {
			if (timer > 0) {
				--timer;
			}
			if (timer == 0) {
				init();
			}
		}
		default -> {
		}
		}
	}
}