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

	private static float percent(int value) {
		return value / 100f;
	}

	public PacManGameWorld world;

	/** 1, 2, 3... */
	public int number;

	public byte bonusSymbol;
	public float playerSpeed;
	public float ghostSpeed;
	public float ghostSpeedTunnel;
	public byte elroy1DotsLeft;
	public float elroy1Speed;
	public byte elroy2DotsLeft;
	public float elroy2Speed;
	public float playerSpeedPowered;
	public float ghostSpeedFrightened;
	public byte ghostFrightenedSeconds;
	public byte numFlashes;

	// food
	protected BitSet eaten = new BitSet();
	public int totalFoodCount;
	public int foodRemaining;

	public int numGhostsKilled;

	/** Ms. Pac-Man: maze number (1, 2, ..., 6) */
	public int mazeNumber;

	public GameLevel(int... values) {
		int i = 0;
		bonusSymbol = (byte) values[i++];
		playerSpeed = percent(values[i++]);
		ghostSpeed = percent(values[i++]);
		ghostSpeedTunnel = percent(values[i++]);
		elroy1DotsLeft = (byte) values[i++];
		elroy1Speed = percent(values[i++]);
		elroy2DotsLeft = (byte) values[i++];
		elroy2Speed = percent(values[i++]);
		playerSpeedPowered = percent(values[i++]);
		ghostSpeedFrightened = percent(values[i++]);
		ghostFrightenedSeconds = (byte) values[i++];
		numFlashes = (byte) values[i++];
	}

	public void setWorld(PacManGameWorld world) {
		this.world = world;
		// count food
		totalFoodCount = 0;
		int energizerCount = 0;
		for (int x = 0; x < world.numCols(); ++x) {
			for (int y = 0; y < world.numRows(); ++y) {
				V2i tile = new V2i(x, y);
				if (world.isFoodTile(tile)) {
					++totalFoodCount;
					if (world.isEnergizerTile(tile)) {
						energizerCount++;
					}
				}
			}
		}
		restoreFood();
		log("Total food: %d (%d pellets, %d energizers)", totalFoodCount, totalFoodCount - energizerCount, energizerCount);
	}

	public void restoreFood() {
		eaten.clear();
		foodRemaining = totalFoodCount;
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

	public boolean containsEatenFood(V2i tile) {
		return world.isFoodTile(tile) && isFoodRemoved(tile);
	}

	public void removeFood(V2i tile) {
		if (!isFoodRemoved(tile)) {
			eaten.set(world.index(tile));
			--foodRemaining;
		}
	}
}