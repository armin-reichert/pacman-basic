/*
Copyright (c) 2021-2023 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.games.pacman.model.actors;

/**
 * @author Armin Reichert
 */
public interface Bonus {

	public static byte STATE_INACTIVE = 0;
	public static byte STATE_EDIBLE = 1;
	public static byte STATE_EATEN = 2;

	/**
	 * @return Entity representing this bonus in the world.
	 */
	Entity entity();

	/**
	 * @return the symbol of this bonus.
	 */
	byte symbol();

	/**
	 * @return points earned when eating this bonus
	 */
	int points();

	/**
	 * @return state of the bonus
	 */
	byte state();

	/**
	 * Updates the bonus state.
	 */
	void update();

	/**
	 * Changes the bonus state to inactive.
	 */
	void setInactive();

	/**
	 * Consume the bonus.
	 */
	void eat();

	/**
	 * Changes the bonus state to edible.
	 * 
	 * @param points earned when eating this bonus
	 * @param ticks  time how long the bonus is edible
	 */
	void setEdible(long ticks);
}