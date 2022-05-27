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

/**
 * @author Armin Reichert
 *
 */
public class GameLevel {

	/** 1-based maze number */
	public int mazeNumber;

	/** 1-based map number (some mazes share the same map) */
	public int mapNumber;

	/** Number of ghosts killed at the current level. */
	public int numGhostsKilled;

	/** Bonus symbol of current level. */
	public int bonusSymbol;

	/** Relative player speed at current level. */
	public final float playerSpeed;

	/** Relative ghost speed at current level. */
	public final float ghostSpeed;

	/** Relative ghost speed when inside tunnel at current level. */
	public final float ghostSpeedTunnel;

	/** Number of pellets left before player becomes "Cruise Elroy" at severity 1. */
	public final int elroy1DotsLeft;

	/** Relative speed of player being "Cruise Elroy" at severity 1. */
	public final float elroy1Speed;

	/** Number of pellets left before player becomes "Cruise Elroy" at severity 2. */
	public final int elroy2DotsLeft;

	/** Relative speed of player being "Cruise Elroy" at severity 2. */
	public final float elroy2Speed;

	/** Relative speed of player in power mode. */
	public final float playerSpeedPowered;

	/** Relative speed of frightened ghost. */
	public final float ghostSpeedFrightened;

	/** Number of seconds ghost are frightened at current level. */
	public final int ghostFrightenedSeconds;

	/** Number of maze flashes at end of current level. */
	public final int numFlashes;

	public GameLevel(Object[] data) {
		bonusSymbol = (int) data[0];
		playerSpeed = percentage(data[1]);
		ghostSpeed = percentage(data[2]);
		ghostSpeedTunnel = percentage(data[3]);
		elroy1DotsLeft = (int) data[4];
		elroy1Speed = percentage(data[5]);
		elroy2DotsLeft = (int) data[6];
		elroy2Speed = percentage(data[7]);
		playerSpeedPowered = percentage(data[8]);
		ghostSpeedFrightened = percentage(data[9]);
		ghostFrightenedSeconds = (int) data[10];
		numFlashes = (int) data[11];
	}

	private float percentage(Object value) {
		return (int) value / 100f;
	}
}