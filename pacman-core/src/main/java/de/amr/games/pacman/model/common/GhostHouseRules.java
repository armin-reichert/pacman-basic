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

import static de.amr.games.pacman.lib.Direction.LEFT;
import static de.amr.games.pacman.model.common.actors.Ghost.ID_ORANGE_GHOST;
import static de.amr.games.pacman.model.common.actors.Ghost.ID_RED_GHOST;
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

	private static final Logger LOGGER = LogManager.getFormatterLogger();

	private final int[] ghostDotCounters = new int[4];

	private int globalDotCounter;

	/** Enabled state of the counter used by ghost house logic. */
	private boolean globalDotCounterEnabled;

	/** Level-specific: Max number of clock ticks Pac can be starving until ghost gets unlocked. */
	public int pacStarvingTimeLimit;

	/** Level-specific: Limits for global dot counter, by ghost. */
	public byte[] globalGhostDotLimits;

	/** Level-specific: Limits for private dot counter, by ghost. */
	public byte[] privateGhostDotLimits;

	public void resetAllDotCounters() {
		resetGlobalDotCounter();
		Arrays.fill(ghostDotCounters, 0);
	}

	public void resetGlobalDotCounter() {
		globalDotCounter = 0;
		globalDotCounterEnabled = false;
		LOGGER.info("Global dot counter reset to 0 and disabled");
	}

	public boolean checkIfGhostCanBeUnlocked(GameModel game) {
		var ghost = game.ghosts(LOCKED).findFirst().orElse(null);
		if (ghost == null) {
			return false;
		}
		var inside = game.level().world().ghostHouse().contains(ghost.tile());
		if (!inside) {
			return unlockGhost(game, ghost, "Outside house");
		}
		// check private dot counter
		if (!globalDotCounterEnabled && ghostDotCounters[ghost.id] >= privateGhostDotLimits[ghost.id]) {
			return unlockGhost(game, ghost, "Private dot counter at limit (%d)", privateGhostDotLimits[ghost.id]);
		}
		// check global dot counter
		var globalDotLimit = globalGhostDotLimits[ghost.id] == -1 ? Integer.MAX_VALUE : globalGhostDotLimits[ghost.id];
		if (globalDotCounter >= globalDotLimit) {
			return unlockGhost(game, ghost, "Global dot counter at limit (%d)", globalDotLimit);
		}
		if (game.pac.starvingTicks() >= pacStarvingTimeLimit) {
			game.pac.endStarving();
			return unlockGhost(game, ghost, "%s reached starving limit (%d ticks)", game.pac.name(), pacStarvingTimeLimit);
		}
		return false;
	}

	private boolean unlockGhost(GameModel game, Ghost ghost, String reason, Object... args) {
		if (ghost.id == ID_RED_GHOST) {
			// Red ghost is outside house when locked, enters "hunting" state and moves left
			ghost.setMoveAndWishDir(LEFT);
			ghost.enterStateHuntingPac(game);
		} else {
			// all other ghosts have to leave house first
			ghost.enterStateLeavingHouse(game);
		}
		if (ghost.id == ID_ORANGE_GHOST && game.cruiseElroyState < 0) {
			// Disabled "Cruise Elroy" state is resumed when orange ghost is unlocked
			game.cruiseElroyState = (byte) -game.cruiseElroyState;
			LOGGER.info("Cruise Elroy mode %d resumed", game.cruiseElroyState);
		}
		game.memo.unlockedGhost = Optional.of(ghost);
		game.memo.unlockReason = reason.formatted(args);
		LOGGER.info("Unlocked %s: %s", ghost.name(), game.memo.unlockReason);
		return true;
	}

	public void updateGhostDotCounters(GameModel game) {
		if (globalDotCounterEnabled) {
			if (game.theGhosts[ID_ORANGE_GHOST].is(LOCKED) && globalDotCounter == 32) {
				globalDotCounterEnabled = false;
				globalDotCounter = 0;
				LOGGER.info("Global dot counter disabled and reset to 0, %s was in house when counter reached 32",
						game.theGhosts[ID_ORANGE_GHOST].name());
			} else {
				globalDotCounter++;
			}
		} else {
			game.ghosts(LOCKED).filter(ghost -> ghost.id != ID_RED_GHOST).findFirst()
					.ifPresent(ghost -> ++ghostDotCounters[ghost.id]);
		}
	}

}