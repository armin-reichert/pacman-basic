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

package de.amr.games.pacman.model.common;

import de.amr.games.pacman.model.common.world.World;

/**
 * @author Armin Reichert
 */
//@formatter:off
public record GameLevel(
	/** Number of level, starts with 1. */
	int number,
	/** Maze number of this level, starts with 1. */
	int mazeNumber,
	/** World used in this level. */
	World world,
	/** Ghost house rules for this level */
	GhostHouseRules houseRules,
	/** Bonus symbol (index) for current level. */
	int bonusSymbol,
	/** Relative player speed at current level. */
	float playerSpeed,
	/** Relative ghost speed at current level. */
	float ghostSpeed,
	/** Relative ghost speed when inside tunnel at current level. */
	float ghostSpeedTunnel,
	/** Number of pellets left before player becomes "Cruise Elroy" at severity 1. */
	int elroy1DotsLeft,
	/** Relative speed of player being "Cruise Elroy" at severity 1. */
	float elroy1Speed,
	/** Number of pellets left before player becomes "Cruise Elroy" at severity 2. */
	int elroy2DotsLeft,
	/** Relative speed of player being "Cruise Elroy" at severity 2. */
	float elroy2Speed,
	/** Relative speed of player in power mode. */
	float playerSpeedPowered,
	/** Relative speed of frightened ghost. */
	float ghostSpeedFrightened,
	/** Number of seconds ghost are frightened at current level. */
	int ghostFrightenedSeconds,
	/** Number of maze flashes at end of current level. */
	int numFlashes)
{
	//@formatter:on
	/**
	 * @param levelNumber level number (1, 2, ...)
	 * @param mazeNumber  maze number (1, 2, ...)
	 * @param world       world used in this level
	 * @param symbol      if value <code>!= -1</code>, overrides the value in the data array
	 * @param data        array with level data
	 */
	public static GameLevel of(int levelNumber, int mazeNumber, World world, GhostHouseRules houseRules, int symbol,
			byte[] data) {
		int bonusSymbol = symbol == BONUS_SYMBOL_FROM_DATA ? (int) data[0] : symbol;
		float playerSpeed = percentage(data[1]);
		float ghostSpeed = percentage(data[2]);
		float ghostSpeedTunnel = percentage(data[3]);
		int elroy1DotsLeft = data[4];
		float elroy1Speed = percentage(data[5]);
		int elroy2DotsLeft = data[6];
		float elroy2Speed = percentage(data[7]);
		float playerSpeedPowered = percentage(data[8]);
		float ghostSpeedFrightened = percentage(data[9]);
		int ghostFrightenedSeconds = data[10];
		int numFlashes = data[11];
		return new GameLevel(levelNumber, mazeNumber, world, houseRules, bonusSymbol, playerSpeed, ghostSpeed,
				ghostSpeedTunnel, elroy1DotsLeft, elroy1Speed, elroy2DotsLeft, elroy2Speed, playerSpeedPowered,
				ghostSpeedFrightened, ghostFrightenedSeconds, numFlashes);
	}

	public static final int BONUS_SYMBOL_FROM_DATA = -1;

	private static float percentage(int value) {
		return value / 100f;
	}
}