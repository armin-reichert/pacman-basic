/*
MIT License

Copyright (c) 2023 Armin Reichert

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

package de.amr.games.pacman.model;

import static de.amr.games.pacman.model.actors.GhostState.LOCKED;

import java.util.Optional;

import org.tinylog.Logger;

import de.amr.games.pacman.model.actors.Ghost;

/**
 * @author Armin Reichert
 */
public class GhostHouseManagement {

	public record GhostUnlockResult(Ghost ghost, String reason) {
	}

	private final GameLevel level;
	private final long pacStarvingTicksLimit;
	private final byte[] globalGhostDotLimits;
	private final byte[] privateGhostDotLimits;
	private final int[] ghostDotCounters;
	private int globalDotCounter;
	private boolean globalDotCounterEnabled;

	public GhostHouseManagement(GameLevel level) {
		this.level = level;
		pacStarvingTicksLimit = level.number() < 5 ? 4 * GameModel.FPS : 3 * GameModel.FPS;
		globalGhostDotLimits = new byte[] { -1, 7, 17, -1 };
		privateGhostDotLimits = switch (level.number()) {
		case 1 -> new byte[] { 0, 0, 30, 60 };
		case 2 -> new byte[] { 0, 0, 0, 50 };
		default -> new byte[] { 0, 0, 0, 0 };
		};
		ghostDotCounters = new int[] { 0, 0, 0, 0 };
		globalDotCounter = 0;
		globalDotCounterEnabled = false;
	}

	public void update() {
		if (globalDotCounterEnabled) {
			if (level.ghost(GameModel.ORANGE_GHOST).is(LOCKED) && globalDotCounter == 32) {
				Logger.trace("{} inside house when counter reached 32", level.ghost(GameModel.ORANGE_GHOST).name());
				resetGlobalDotCounterAndSetEnabled(false);
			} else {
				globalDotCounter++;
				Logger.trace("Global dot counter = {}", globalDotCounter);
			}
		} else {
			level.ghosts(LOCKED).filter(ghost -> ghost.insideHouse(level)).findFirst()
					.ifPresent(this::increaseGhostDotCounter);
		}
	}

	public void onPacKilled() {
		resetGlobalDotCounterAndSetEnabled(true);
	}

	private void resetGlobalDotCounterAndSetEnabled(boolean enabled) {
		globalDotCounter = 0;
		globalDotCounterEnabled = enabled;
		Logger.trace("Global dot counter reset to 0 and {}", enabled ? "enabled" : "disabled");
	}

	private void increaseGhostDotCounter(Ghost ghost) {
		ghostDotCounters[ghost.id()]++;
		Logger.trace("{} dot counter = {}", ghost.name(), ghostDotCounters[ghost.id()]);
	}

	public Optional<GhostUnlockResult> checkIfNextGhostCanLeaveHouse() {
		var ghost = level.ghosts(LOCKED).findFirst().orElse(null);
		if (ghost == null) {
			return Optional.empty();
		}
		if (!ghost.insideHouse(level)) {
			return unlockResult(ghost, "Already outside house");
		}
		var id = ghost.id();
		// check private dot counter
		if (!globalDotCounterEnabled && ghostDotCounters[id] >= privateGhostDotLimits[id]) {
			return unlockResult(ghost, "Private dot counter at limit (%d)", privateGhostDotLimits[id]);
		}
		// check global dot counter
		var globalDotLimit = globalGhostDotLimits[id] == -1 ? Integer.MAX_VALUE : globalGhostDotLimits[id];
		if (globalDotCounter >= globalDotLimit) {
			return unlockResult(ghost, "Global dot counter at limit (%d)", globalDotLimit);
		}
		// check Pac-Man starving time
		if (level.pac().starvingTicks() >= pacStarvingTicksLimit) {
			level.pac().endStarving(); // TODO change pac state here?
			Logger.trace("Pac-Man starving timer reset to 0");
			return unlockResult(ghost, "%s reached starving limit (%d ticks)", level.pac().name(), pacStarvingTicksLimit);
		}
		return Optional.empty();
	}

	private Optional<GhostUnlockResult> unlockResult(Ghost ghost, String reason, Object... args) {
		return Optional.of(new GhostUnlockResult(ghost, reason.formatted(args)));
	}
}