/*
Copyright (c) 2021-2023 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.games.pacman.model;

import static de.amr.games.pacman.model.GameModel.CYAN_GHOST;
import static de.amr.games.pacman.model.GameModel.ORANGE_GHOST;
import static de.amr.games.pacman.model.GameModel.PINK_GHOST;
import static de.amr.games.pacman.model.GameModel.RED_GHOST;
import static de.amr.games.pacman.model.actors.GhostState.LOCKED;

import java.util.Optional;
import java.util.stream.Stream;

import org.tinylog.Logger;

import de.amr.games.pacman.model.actors.Ghost;

/**
 * @author Armin Reichert
 * 
 * @see PacManDossier
 */
public class GhostHouseManagement {

	public final class GhostUnlockResult {
		private final Ghost ghost;
		private final String reason;

		public GhostUnlockResult(Ghost ghost, String reason) {
			this.ghost = ghost;
			this.reason = reason;
		}

		public Ghost ghost() {
			return ghost;
		}

		public String reason() {
			return reason;
		}
	}

	private final GameLevel level;
	private final long pacStarvingTicksLimit;
	private final byte[] globalGhostDotLimits;
	private byte[] privateGhostDotLimits;
	private final int[] ghostDotCounters;
	private int globalDotCounter;
	private boolean globalDotCounterEnabled;

	public GhostHouseManagement(GameLevel level) {
		this.level = level;
		pacStarvingTicksLimit = level.number() < 5 ? 4 * GameModel.FPS : 3 * GameModel.FPS;
		globalGhostDotLimits = new byte[] { -1, 7, 17, -1 };
		switch (level.number()) {
		case 1:
			privateGhostDotLimits = new byte[] { 0, 0, 30, 60 };
			break;
		case 2:
			privateGhostDotLimits = new byte[] { 0, 0, 0, 50 };
			break;
		default:
			privateGhostDotLimits = new byte[] { 0, 0, 0, 0 };
			break;
		}
		ghostDotCounters = new int[] { 0, 0, 0, 0 };
		globalDotCounter = 0;
		globalDotCounterEnabled = false;
	}

	public void update() {
		if (globalDotCounterEnabled) {
			if (level.ghost(ORANGE_GHOST).is(LOCKED) && globalDotCounter == 32) {
				Logger.trace("{} inside house when counter reached 32", level.ghost(ORANGE_GHOST).name());
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
		// Ensure unlock order of ghosts is RED, PINK, CYAN, ORANGE.
		// The current level implementation guarantees it but...
		var ghost = Stream.of(RED_GHOST, PINK_GHOST, CYAN_GHOST, ORANGE_GHOST).map(level::ghost).filter(g -> g.is(LOCKED))
				.findFirst().orElse(null);

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
		return Optional.of(new GhostUnlockResult(ghost, String.format(reason, args)));
	}
}