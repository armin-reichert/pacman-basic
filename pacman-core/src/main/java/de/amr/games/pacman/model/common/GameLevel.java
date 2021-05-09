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
	private BitSet eaten = new BitSet();
	public int totalFoodCount;
	public int foodRemaining;

	public int numGhostsKilled;

	/** Ms. Pac-Man: maze number (1, 2, ..., 6) */
	public int mazeNumber;

	public GameLevel(int number, PacManGameWorld world) {
		this.number = number;
		this.world = world;
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
		eaten.clear();
		foodRemaining = totalFoodCount;
		log("Total food: %d (%d pellets, %d energizers)", totalFoodCount, totalFoodCount - energizerCount, energizerCount);
	}

	public void setData(Object[] levelData) {
		int i = 0;
		bonusSymbol = (String) levelData[i++];
		playerSpeed = percent(levelData[i++]);
		ghostSpeed = percent(levelData[i++]);
		ghostSpeedTunnel = percent(levelData[i++]);
		elroy1DotsLeft = (Integer) levelData[i++];
		elroy1Speed = percent(levelData[i++]);
		elroy2DotsLeft = (Integer) levelData[i++];
		elroy2Speed = percent(levelData[i++]);
		playerSpeedPowered = percent(levelData[i++]);
		ghostSpeedFrightened = percent(levelData[i++]);
		ghostFrightenedSeconds = (Integer) levelData[i++];
		numFlashes = (Integer) levelData[i++];
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