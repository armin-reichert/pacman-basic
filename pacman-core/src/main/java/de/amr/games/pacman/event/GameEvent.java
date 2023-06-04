/*
Copyright (c) 2021-2023 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.games.pacman.event;

import static de.amr.games.pacman.lib.Globals.checkGameNotNull;
import static de.amr.games.pacman.lib.Globals.checkNotNull;

import java.util.Optional;

import de.amr.games.pacman.lib.Vector2i;
import de.amr.games.pacman.model.GameModel;

/**
 * Base class for events fired during game play. This class is a kind of compromise between separate subclasses for each
 * event type and a fat base class.
 * 
 * @author Armin Reichert
 */
public class GameEvent {

	public static final byte BONUS_GETS_ACTIVE = 0;
	public static final byte BONUS_GETS_EATEN = 1;
	public static final byte BONUS_EXPIRES = 2;
	public static final byte GAME_STATE_CHANGED = 3;
	public static final byte GHOST_ENTERS_HOUSE = 4;
	public static final byte GHOST_STARTS_RETURNING_HOME = 5;
	public static final byte LEVEL_BEFORE_START = 6;
	public static final byte LEVEL_STARTED = 7;
	public static final byte PAC_FINDS_FOOD = 8;
	public static final byte PLAYER_GETS_EXTRA_LIFE = 9;
	public static final byte PAC_GETS_POWER = 10;
	public static final byte PAC_STARTS_LOSING_POWER = 11;
	public static final byte PAC_LOSES_POWER = 12;
	public static final byte SOUND_EVENT = 13;
	public static final byte UNSPECIFIED_CHANGE = 14;

	public final GameModel game;
	public final byte type;
	public final Optional<Vector2i> tile;

	/**
	 * @param game game model
	 * @param type event type
	 * @param tile tile related to event, may be {@code null}
	 */
	public GameEvent(GameModel game, byte type, Vector2i tile) {
		checkGameNotNull(game);
		checkNotNull(type);
		this.game = game;
		this.type = type;
		this.tile = Optional.ofNullable(tile);
	}

	@Override
	public String toString() {
		var tileStr = tile.isPresent() ? String.format(" tile: %s", tile.get()) : "";
		return String.format("%s%s", type, tileStr);
	}
}