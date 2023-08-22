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
		case BONUS_ACTIVATED:
			onBonusActivated(event);
			break;
		case BONUS_EATEN:
			onBonusEaten(event);
			break;
		case BONUS_EXPIRED:
			onBonusExpired(event);
			break;
		case CREDIT_ADDED:
			onCreditAdded(event);
			break;
		case EXTRA_LIFE_WON:
				onExtraLifeWon(event);
				break;
		case GAME_STATE_CHANGED:
				onGameStateChange(event);
				break;
		case GHOST_EATEN:
			onGhostEaten(event);
				break;
		case GHOST_ENTERS_HOUSE:
			onGhostEntersHouse(event);
			break;
		case GHOST_STARTS_RETURNING_HOME:
			onGhostStartsReturningHome(event);
			break;
		case HUNTING_PHASE_0_STARTED:
			onHuntingPhase0Started(event);
			break;
		case HUNTING_PHASE_2_STARTED:
			onHuntingPhase2Started(event);
			break;
		case HUNTING_PHASE_4_STARTED:
			onHuntingPhase4Started(event);
			break;
		case HUNTING_PHASE_6_STARTED:
			onHuntingPhase6Started(event);
			break;
		case LEVEL_CREATED:
			onLevelCreated(event);
			break;
		case LEVEL_STARTED:
			onLevelStarted(event);
			break;
		case PAC_DIED:
			onPacDied(event);
			break;
		case PAC_FOUND_FOOD:
			onPacFoundFood(event);
			break;
		case PAC_GETS_POWER:
			onPacGetsPower(event);
			break;
		case PAC_LOST_POWER:
			onPacLostPower(event);
			break;
		case PAC_STARTS_LOSING_POWER:
			onPacStartsLosingPower(event);
			break;
		case READY_TO_PLAY:
			onReadyToPlay(event);
			break;
		case INTERMISSION_1_STARTS:
			onIntermission1Starts(event);
			break;
		case INTERMISSION_2_STARTS:
			onIntermission2Starts(event);
			break;
		case INTERMISSION_3_STARTS:
			onIntermission3Starts(event);
			break;
		case STOP_ALL_SOUNDS:
			onStopAllSounds(event);
			break;
		case UNSPECIFIED_CHANGE:
			onUnspecifiedChange(event);
			break;
		default:
			throw new IllegalArgumentException("Unknown event type: " + event);
		}
	}

	default void onCreditAdded(GameEvent e) {
	}

	default void onExtraLifeWon(GameEvent e) {
	}

	default void onBonusActivated(GameEvent e) {
	}

	default void onBonusEaten(GameEvent e) {
	}

	default void onBonusExpired(GameEvent e) {
	}

	default void onGameStateChange(GameEvent e) {
	}

	default void onGhostEaten(GameEvent e) {
	}
	default void onGhostEntersHouse(GameEvent e) {
	}

	default void onGhostStartsReturningHome(GameEvent e) {
	}

	default void onHuntingPhase0Started(GameEvent e) {
	}

	default void onHuntingPhase2Started(GameEvent e) {
	}

	default void onHuntingPhase4Started(GameEvent e) {
	}

	default void onHuntingPhase6Started(GameEvent e) {
	}

	default void onLevelCreated(GameEvent e) {
	}

	default void onLevelStarted(GameEvent e) {
	}

	default void onPacDied(GameEvent e) {
	}

	default void onPacFoundFood(GameEvent e) {
	}

	default void onPacGetsExtraLife(GameEvent e) {
	}

	default void onPacGetsPower(GameEvent e) {
	}

	default void onPacLostPower(GameEvent e) {
	}

	default void onPacStartsLosingPower(GameEvent e) {
	}

	default void onReadyToPlay(GameEvent event) {
	}

	default void onIntermission1Starts(GameEvent event) {
	}

	default void onIntermission2Starts(GameEvent event) {
	}

	default void onIntermission3Starts(GameEvent event) {
	}

	default void onStopAllSounds(GameEvent event) {
	}

	default void onUnspecifiedChange(GameEvent e) {
	}
}