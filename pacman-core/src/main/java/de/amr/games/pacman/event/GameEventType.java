/*
Copyright (c) 2021-2023 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.games.pacman.event;

/**
 * Game event types.
 * 
 * @author Armin Reichert
 */
public enum GameEventType {
	//@formatter:off
	BONUS_GETS_ACTIVE, 
	BONUS_GETS_EATEN, 
	BONUS_EXPIRES, 
	GAME_STATE_CHANGED,
	GHOST_ENTERS_HOUSE, 
	GHOST_STARTS_LEAVING_HOUSE, 
	GHOST_COMPLETES_LEAVING_HOUSE, 
	GHOST_STARTS_RETURNING_HOME,
	LEVEL_BEFORE_START,
	LEVEL_STARTED,
	PAC_FINDS_FOOD, 
	PLAYER_GETS_EXTRA_LIFE, 
	PAC_GETS_POWER, 
	PAC_STARTS_LOSING_POWER,
	PAC_LOSES_POWER,
	SOUND_EVENT,
	UNSPECIFIED_CHANGE;
	//@formatter:on
}