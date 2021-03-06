package de.amr.games.pacman.controller.event;

import java.util.Objects;
import java.util.Optional;

import de.amr.games.pacman.lib.V2i;
import de.amr.games.pacman.model.common.PacManGameModel;
import de.amr.games.pacman.model.common.Ghost;

/**
 * Base class for events fired during game play. This class is a kind of compromise between separate
 * subclasses for each event type and a fat base class.
 * 
 * @author Armin Reichert
 */
public class PacManGameEvent {

	public enum Info {
		BONUS_ACTIVATED, BONUS_EATEN, BONUS_EXPIRED, EXTRA_LIFE, PLAYER_FOUND_FOOD, PLAYER_GAINS_POWER, PLAYER_LOSING_POWER,
		PLAYER_LOST_POWER, GHOST_ENTERS_HOUSE, GHOST_LEAVING_HOUSE, GHOST_LEFT_HOUSE, GHOST_RETURNS_HOME, GAME_STATE_CHANGE,
		OTHER;
	}

	public final PacManGameModel gameModel;
	public final Info info;
	public final Optional<V2i> tile; // the optional tile where this event occurred
	public final Optional<Ghost> ghost; // the optional ghost this event relates to

	public PacManGameEvent(PacManGameModel gameModel, Info info, Ghost ghost, V2i tile) {
		this.gameModel = Objects.requireNonNull(gameModel);
		this.info = Objects.requireNonNull(info);
		this.tile = Optional.ofNullable(tile);
		this.ghost = Optional.ofNullable(ghost);
	}

	@Override
	public String toString() {
		return ghost.isEmpty() ? //
				String.format("%s: tile %s", info, tile.orElse(null))
				: String.format("%s: tile %s, ghost %s", info, tile.orElse(null), ghost.orElse(null));
	}
}