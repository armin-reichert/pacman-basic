package de.amr.games.pacman.model;

import static de.amr.games.pacman.lib.Logging.log;

import java.util.BitSet;

import de.amr.games.pacman.lib.V2i;
import de.amr.games.pacman.world.PacManGameWorld;

/**
 * Data comprising a game level.
 * 
 * @author Armin Reichert
 */
public class PacManGameLevel {

	private static float percent(int value) {
		return value / 100f;
	}

	public byte bonusSymbol;
	public final float pacSpeed;
	public final float ghostSpeed;
	public final float ghostSpeedTunnel;
	public final byte elroy1DotsLeft;
	public final float elroy1Speed;
	public final byte elroy2DotsLeft;
	public final float elroy2Speed;
	public final float pacSpeedPowered;
	public final float ghostSpeedFrightened;
	public final byte ghostFrightenedSeconds;
	public final byte numFlashes;

	public PacManGameWorld world;

	protected final BitSet eaten = new BitSet();
	public int totalFoodCount;
	public int foodRemaining;

	public int numGhostsKilled;

	/** Ms. Pac-Man maze number (1..6) */
	public int mazeNumber;

	public PacManGameLevel(int... values) {
		int i = 0;
		bonusSymbol = (byte) values[i++];
		pacSpeed = percent(values[i++]);
		ghostSpeed = percent(values[i++]);
		ghostSpeedTunnel = percent(values[i++]);
		elroy1DotsLeft = (byte) values[i++];
		elroy1Speed = percent(values[i++]);
		elroy2DotsLeft = (byte) values[i++];
		elroy2Speed = percent(values[i++]);
		pacSpeedPowered = percent(values[i++]);
		ghostSpeedFrightened = percent(values[i++]);
		ghostFrightenedSeconds = (byte) values[i++];
		numFlashes = (byte) values[i++];
	}

	public void setWorld(PacManGameWorld gameWorld) {
		world = gameWorld;
		// find food
		totalFoodCount = 0;
		int energizerCount = 0;
		for (int x = 0; x < gameWorld.xTiles(); ++x) {
			for (int y = 0; y < gameWorld.yTiles(); ++y) {
				V2i tile = new V2i(x, y);
				if (gameWorld.isFoodTile(tile)) {
					++totalFoodCount;
					if (gameWorld.isEnergizerTile(tile)) {
						energizerCount++;
					}
				}
			}
		}
		eaten.clear();
		foodRemaining = totalFoodCount;
		log("Total food count=%d (%d pellets + %d energizers)", totalFoodCount, totalFoodCount - energizerCount,
				energizerCount);

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

	public void restoreFood() {
		eaten.clear();
		foodRemaining = totalFoodCount;
	}
}