/*
Copyright (c) 2021-2023 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.games.pacman.event;

/**
 * Implemented by classes that listen to game events.
 * 
 * @author Armin Reichert
 */
public interface GameEventListener {

	/**
	 * Called when a game event is received.
	 * 
	 * @param event a game event
	 */
	default void onGameEvent(GameEvent event) {
		switch (event.type) {
		case GAME_STATE_CHANGED:
			onGameStateChange((GameStateChangeEvent) event);
			break;
		case BONUS_GETS_ACTIVE:
			onBonusGetsActive(event);
			break;
		case BONUS_GETS_EATEN:
			onBonusGetsEaten(event);
			break;
		case BONUS_EXPIRES:
			onBonusExpires(event);
			break;
		case GHOST_ENTERS_HOUSE:
			onGhostEntersHouse(event);
			break;
		case GHOST_STARTS_LEAVING_HOUSE:
			onGhostStartsLeavingHouse(event);
			break;
		case GHOST_COMPLETES_LEAVING_HOUSE:
			onGhostCompletesLeavingHouse(event);
			break;
		case GHOST_STARTS_RETURNING_HOME:
			onGhostStartsReturningHome(event);
			break;
		case LEVEL_BEFORE_START:
			onLevelBeforeStart(event);
			break;
		case LEVEL_STARTED:
			onLevelStarted(event);
			break;
		case PAC_FINDS_FOOD:
			onPlayerFindsFood(event);
			break;
		case PLAYER_GETS_EXTRA_LIFE:
			onPlayerGetsExtraLife(event);
			break;
		case PAC_GETS_POWER:
			onPlayerGetsPower(event);
			break;
		case PAC_STARTS_LOSING_POWER:
			onPlayerStartsLosingPower(event);
			break;
		case PAC_LOSES_POWER:
			onPlayerLosesPower(event);
			break;
		case SOUND_EVENT:
			onSoundEvent((SoundEvent) event);
			break;
		case UNSPECIFIED_CHANGE:
			onUnspecifiedChange(event);
			break;
		default:
			throw new IllegalArgumentException("Unknown event type: " + event.type);
		}
	}

	default void onGameStateChange(GameStateChangeEvent e) {
	}

	default void onBonusGetsActive(GameEvent e) {
	}

	default void onBonusGetsEaten(GameEvent e) {
	}

	default void onBonusExpires(GameEvent e) {
	}

	default void onPlayerGetsExtraLife(GameEvent e) {
	}

	default void onGhostEntersHouse(GameEvent e) {
	}

	default void onGhostStartsReturningHome(GameEvent e) {
	}

	default void onGhostStartsLeavingHouse(GameEvent e) {
	}

	default void onGhostCompletesLeavingHouse(GameEvent e) {
	}

	default void onLevelBeforeStart(GameEvent e) {
	}

	default void onLevelStarted(GameEvent e) {
	}

	default void onPlayerFindsFood(GameEvent e) {
	}

	default void onPlayerStartsLosingPower(GameEvent e) {
	}

	default void onPlayerLosesPower(GameEvent e) {
	}

	default void onPlayerGetsPower(GameEvent e) {
	}

	default void onSoundEvent(SoundEvent e) {
	}

	default void onUnspecifiedChange(GameEvent e) {
	}

}