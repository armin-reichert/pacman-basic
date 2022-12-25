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

import de.amr.games.pacman.lib.anim.Pulse;
import de.amr.games.pacman.model.common.actors.Bonus;
import de.amr.games.pacman.model.common.world.World;

/**
 * @author Armin Reichert
 */
public class GameLevel {

	public record Parameters(
	//@formatter:off
		/** Relative player speed in this level. */
		float playerSpeed,
		/** Relative ghost speed in this level. */
		float ghostSpeed,
		/** Relative ghost speed when inside tunnel in this level. */
		float ghostSpeedTunnel,
		/** Number of pellets left before player becomes "Cruise Elroy" with severity 1. */
		int elroy1DotsLeft,
		/** Relative speed of player being "Cruise Elroy" at severity 1. */
		float elroy1Speed,
		/** Number of pellets left before player becomes "Cruise Elroy" with severity 2. */
		int elroy2DotsLeft,
		/** Relative speed of player being "Cruise Elroy" with severity 2. */
		float elroy2Speed,
		/** Relative speed of player in power mode. */
		float playerSpeedPowered,
		/** Relative speed of frightened ghost. */
		float ghostSpeedFrightened,
		/** Number of seconds Pac-Man gets power int this level. */
		int pacPowerSeconds,
		/** Number of maze flashes at end of this level. */
		int numFlashes)
	//@formatter:on
	{
	}

	private final int number;
	private final World world;
	private final Pulse energizerPulse;
	private final Bonus bonus;
	private final GhostHouseRules houseRules;
	private final Parameters params;
	private int numGhostsKilledInLevel;
	private int numGhostsKilledByEnergizer;

	public GameLevel(int levelNumber, World world, Bonus bonus, GhostHouseRules houseRules, byte[] data) {
		this.number = levelNumber;
		this.world = world;
		this.energizerPulse = new Pulse(10, true);
		this.bonus = bonus;
		this.houseRules = houseRules;

		//@formatter:off
		float playerSpeed          = percentage(data[0]);
		float ghostSpeed           = percentage(data[1]);
		float ghostSpeedTunnel     = percentage(data[2]);
		int elroy1DotsLeft         = data[3];
		float elroy1Speed          = percentage(data[4]);
		int elroy2DotsLeft         = data[5];
		float elroy2Speed          = percentage(data[6]);
		float playerSpeedPowered   = percentage(data[7]);
		float ghostSpeedFrightened = percentage(data[8]);
		int pacPowerSeconds        = data[9];
		int numFlashes             = data[10];
		//@formatter:on

		params = new Parameters(playerSpeed, ghostSpeed, ghostSpeedTunnel, elroy1DotsLeft, elroy1Speed, elroy2DotsLeft,
				elroy2Speed, playerSpeedPowered, ghostSpeedFrightened, pacPowerSeconds, numFlashes);
	}

	/** Number of level, starts with 1. */
	public int number() {
		return number;
	}

	/** World used in this level. */
	public World world() {
		return world;
	}

	public Pulse energizerPulse() {
		return energizerPulse;
	}

	/** Bonus used in this level. */
	public Bonus bonus() {
		return bonus;
	}

	/** Ghost house rules in this level */
	public GhostHouseRules houseRules() {
		return houseRules;
	}

	/** Parameters in this level */
	public Parameters params() {
		return params;
	}

	public int numGhostsKilledInLevel() {
		return numGhostsKilledInLevel;
	}

	public void setNumGhostsKilledInLevel(int number) {
		this.numGhostsKilledInLevel = number;
	}

	public int numGhostsKilledByEnergizer() {
		return numGhostsKilledByEnergizer;
	}

	public void setNumGhostsKilledByEnergizer(int number) {
		this.numGhostsKilledByEnergizer = number;
	}

	private static float percentage(int value) {
		return value / 100f;
	}
}