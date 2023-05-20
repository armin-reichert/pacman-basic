/*
MIT License

Copyright (c) 2021-2023 Armin Reichert

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
		case LEVEL_STARTING:
			onLevelStarting(event);
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

	default void onLevelStarting(GameEvent e) {
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