/*
MIT License

Copyright (c) 2022 Armin Reichert

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

package de.amr.games.pacman.model.common.actors;

import de.amr.games.pacman.model.common.GameModel;

/**
 * @author Armin Reichert
 */
public interface Bonus {

	/**
	 * @return Entity representing this bonus in the world.
	 */
	Entity entity();

	/**
	 * @return the index representing this bonus
	 */
	int index();

	/**
	 * @return points earned when eating this bonus
	 */
	int points();

	/**
	 * @return state of the bonus
	 */
	BonusState state();

	/**
	 * Updates the bonus state.
	 * 
	 * @param game the game model
	 */
	void update(GameModel game);

	/**
	 * Changes the bonus state to inactive.
	 */
	void setInactive();

	/**
	 * Changes the bonus state to edible.
	 * 
	 * @param index  number representing this bonus
	 * @param points earned when eating this bonus
	 * @param ticks  time how long the bonus is edible
	 */
	void setEdible(int index, int value, long ticks);
}