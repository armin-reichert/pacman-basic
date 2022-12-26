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

import static de.amr.games.pacman.lib.steering.Direction.LEFT;
import static de.amr.games.pacman.model.common.actors.Ghost.ID_ORANGE_GHOST;
import static de.amr.games.pacman.model.common.actors.GhostState.LOCKED;

import java.util.Arrays;
import java.util.Optional;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import de.amr.games.pacman.model.common.actors.Ghost;

/**
 * @author Armin Reichert
 */
public class GhostHouseRules {

	public record UnlockResult(Ghost ghost, String reason) {
	}

	private static final Logger LOGGER = LogManager.getFormatterLogger();

	public static final int NO_LIMIT = -1;

	private final int[] ghostDotCounters = new int[4];

	private int globalDotCounter;
	private boolean globalDotCounterEnabled;
	private int pacStarvingTimeLimit;
	private final byte[] globalGhostDotLimits = new byte[4];
	private final byte[] privateGhostDotLimits = new byte[4];

	public GhostHouseRules() {
		// nothing to do
	}

	public void setPacStarvingTimeLimit(int ticks) {
		this.pacStarvingTimeLimit = ticks;
	}

	public void setGlobalGhostDotLimits(int redGhostLimit, int pinkGhostLimit, int cyanGhostLimit, int orangeGhostLimit) {
		globalGhostDotLimits[Ghost.ID_RED_GHOST] = (byte) redGhostLimit;
		globalGhostDotLimits[Ghost.ID_PINK_GHOST] = (byte) pinkGhostLimit;
		globalGhostDotLimits[Ghost.ID_CYAN_GHOST] = (byte) cyanGhostLimit;
		globalGhostDotLimits[Ghost.ID_ORANGE_GHOST] = (byte) orangeGhostLimit;
	}

	public void setPrivateGhostDotLimits(int redGhostLimit, int pinkGhostLimit, int cyanGhostLimit,
			int orangeGhostLimit) {
		privateGhostDotLimits[Ghost.ID_RED_GHOST] = (byte) redGhostLimit;
		privateGhostDotLimits[Ghost.ID_PINK_GHOST] = (byte) pinkGhostLimit;
		privateGhostDotLimits[Ghost.ID_CYAN_GHOST] = (byte) cyanGhostLimit;
		privateGhostDotLimits[Ghost.ID_ORANGE_GHOST] = (byte) orangeGhostLimit;
	}

	public void resetPrivateGhostDotCounters() {
		Arrays.fill(ghostDotCounters, 0);
	}

	public void resetGlobalDotCounterAndSetEnabled(boolean enabled) {
		globalDotCounter = 0;
		globalDotCounterEnabled = enabled;
		LOGGER.trace("Global dot counter reset to 0 and %s", enabled ? "enabled" : "disabled");
	}

	public void updateGhostDotCounters(GameLevel level) {
		if (globalDotCounterEnabled) {
			if (level.ghost(ID_ORANGE_GHOST).is(LOCKED) && globalDotCounter == 32) {
				LOGGER.trace("%s inside house when counter reached 32", level.ghost(ID_ORANGE_GHOST).name());
				resetGlobalDotCounterAndSetEnabled(false);
			} else {
				globalDotCounter++;
			}
		} else {
			var house = level.world().ghostHouse();
			var preferredGhost = level.ghosts(LOCKED).filter(ghost -> house.contains(ghost.tile())).findFirst();
			preferredGhost.ifPresent(this::increaseGhostDotCounter);
		}
	}

	private void increaseGhostDotCounter(Ghost ghost) {
		ghostDotCounters[ghost.id()]++;
		LOGGER.trace("Dot counter for %s increased to %d", ghost.name(), ghostDotCounters[ghost.id()]);
	}

	public Optional<UnlockResult> checkIfGhostUnlocked(GameLevel level) {
		var ghost = level.ghosts(LOCKED).findFirst().orElse(null);
		if (ghost == null) {
			return Optional.empty();
		}
		var outside = !level.world().ghostHouse().contains(ghost.tile());
		if (outside) {
			return unlockGhost(level, ghost, "Outside house");
		}
		// check private dot counter
		if (!globalDotCounterEnabled && ghostDotCounters[ghost.id()] >= privateGhostDotLimits[ghost.id()]) {
			return unlockGhost(level, ghost, "Private dot counter at limit (%d)", privateGhostDotLimits[ghost.id()]);
		}
		// check global dot counter
		var globalDotLimit = globalGhostDotLimits[ghost.id()] == NO_LIMIT ? Integer.MAX_VALUE
				: globalGhostDotLimits[ghost.id()];
		if (globalDotCounter >= globalDotLimit) {
			return unlockGhost(level, ghost, "Global dot counter at limit (%d)", globalDotLimit);
		}
		// check Pac-Man starving reaches limit
		if (level.pac().starvingTicks() >= pacStarvingTimeLimit) {
			level.pac().endStarving();
			LOGGER.trace("Pac-Man starving timer reset to 0");
			return unlockGhost(level, ghost, "%s reached starving limit (%d ticks)", level.pac().name(),
					pacStarvingTimeLimit);
		}
		return Optional.empty();
	}

	private Optional<UnlockResult> unlockGhost(GameLevel level, Ghost ghost, String reason, Object... args) {
		var outside = !level.world().ghostHouse().contains(ghost.tile());
		if (outside) {
			ghost.setMoveAndWishDir(LEFT);
			ghost.enterStateHuntingPac();
		} else {
			// ghost inside house has to leave house first
			ghost.enterStateLeavingHouse(level);
		}
		return Optional.of(new UnlockResult(ghost, reason.formatted(args)));
	}
}