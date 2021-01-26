package de.amr.games.pacman.game.core;

import static de.amr.games.pacman.lib.Logging.log;

import java.util.BitSet;

import de.amr.games.pacman.game.worlds.PacManGameWorld;
import de.amr.games.pacman.lib.V2i;

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

	public final PacManGameWorld world;

	protected final BitSet eaten = new BitSet();
	public int totalFoodCount;
	public int foodRemaining;

	public int numGhostsKilled;
	public int mazeNumber; // Ms. Pac-Man, values 1..6

	public PacManGameLevel(PacManGameWorld world, int... values) {
		this.world = world;
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

		// find food
		V2i worldSize = world.sizeInTiles();
		totalFoodCount = 0;
		int energizerCount = 0;
		for (int x = 0; x < worldSize.x; ++x) {
			for (int y = 0; y < worldSize.y; ++y) {
				byte data = world.mapData(x, y);
				if (data == PacManGameWorld.PILL) {
					++totalFoodCount;
				} else if (data == PacManGameWorld.ENERGIZER) {
					energizerCount++;
					++totalFoodCount;
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
		return isFoodRemoved(tile.x, tile.y);
	}

	public boolean isFoodRemoved(int x, int y) {
		return eaten.get(world.tileIndex(x, y));
	}

	public boolean containsFood(int x, int y) {
		return world.isFoodTile(x, y) && !isFoodRemoved(x, y);
	}

	public boolean containsFood(V2i tile) {
		return containsFood(tile.x, tile.y);
	}

	public void removeFood(int x, int y) {
		if (!isFoodRemoved(x, y)) {
			eaten.set(world.tileIndex(x, y));
			--foodRemaining;
		}
	}

	public void removeFood(V2i tile) {
		removeFood(tile.x, tile.y);
	}

	public void restoreFood() {
		eaten.clear();
		foodRemaining = totalFoodCount;
	}

}