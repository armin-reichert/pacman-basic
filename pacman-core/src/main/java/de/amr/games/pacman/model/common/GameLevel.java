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
package de.amr.games.pacman.model.common;

import static de.amr.games.pacman.lib.Logging.log;

import java.util.BitSet;

import de.amr.games.pacman.lib.V2i;
import de.amr.games.pacman.model.world.PacManGameWorld;

/**
 * Data comprising a game level.
 * 
 * @author Armin Reichert
 */
public class GameLevel {

	private static float percent(Object value) {
		return ((Integer) value) / 100f;
	}

	/** 1, 2, 3... */
	public final int number;
	public final PacManGameWorld world;

	public String bonusSymbol;
	public float playerSpeed;
	public float ghostSpeed;
	public float ghostSpeedTunnel;
	public int elroy1DotsLeft;
	public float elroy1Speed;
	public int elroy2DotsLeft;
	public float elroy2Speed;
	public float playerSpeedPowered;
	public float ghostSpeedFrightened;
	public int ghostFrightenedSeconds;
	public int numFlashes;

	// food in game world
	public int totalFoodCount;
	public int energizerCount;
	public int foodRemaining;
	private BitSet eaten;

	public int numGhostsKilled;

	/** Ms. Pac-Man: maze number (1, 2, ..., 6) */
	public int mazeNumber;

	public GameLevel(int levelNumber, PacManGameWorld world, Object[] levelData) {
		this.number = levelNumber;
		this.world = world;
		bonusSymbol = (String) levelData[0];
		playerSpeed = percent(levelData[1]);
		ghostSpeed = percent(levelData[2]);
		ghostSpeedTunnel = percent(levelData[3]);
		elroy1DotsLeft = (Integer) levelData[4];
		elroy1Speed = percent(levelData[5]);
		elroy2DotsLeft = (Integer) levelData[6];
		elroy2Speed = percent(levelData[7]);
		playerSpeedPowered = percent(levelData[8]);
		ghostSpeedFrightened = percent(levelData[9]);
		ghostFrightenedSeconds = (Integer) levelData[10];
		numFlashes = (Integer) levelData[11];
		totalFoodCount = (int) world.tiles().filter(world::isFoodTile).count();
		energizerCount = (int) world.tiles().filter(world::isEnergizerTile).count();
		foodRemaining = totalFoodCount;
		eaten = new BitSet();

		log("Total food: %d (%d pellets, %d energizers)", totalFoodCount, totalFoodCount - energizerCount, energizerCount);
	}

	public int eatenFoodCount() {
		return totalFoodCount - foodRemaining;
	}

	public boolean isFoodRemoved(V2i tile) {
		return eaten.get(world.index(tile));
	}

	public boolean containsFood(V2i tile) {
		return world.isFoodTile(tile) && !isFoodRemoved(tile);
	}

	public void removeFood(V2i tile) {
		if (containsFood(tile)) {
			eaten.set(world.index(tile));
			--foodRemaining;
		}
	}
}