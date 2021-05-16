package de.amr.games.pacman.controller.event;

/**
 * Provides an empty default handler for each game event such that a class implementing this
 * interface just needs to override the needed method(s).
 * 
 * @author Armin Reichert
 */
public interface DefaultPacManGameEventHandler extends PacManGameEventListener {

	@Override
	default void onGameEvent(PacManGameEvent event) {
		switch (event.info) {
		case BONUS_ACTIVATED:
			onBonusActivated(event);
			return;
		case BONUS_EATEN:
			onBonusEaten(event);
			return;
		case BONUS_EXPIRED:
			onBonusExpired(event);
			return;
		case EXTRA_LIFE:
			onExtraLife(event);
			return;
		case GHOST_ENTERS_HOUSE:
			onGhostEntersHouse(event);
			return;
		case GHOST_LEFT_HOUSE:
			onGhostLeftHouse(event);
			return;
		case GHOST_RETURNS_HOME:
			onGhostReturnsHome(event);
			return;
		case PLAYER_FOUND_FOOD:
			onPlayerFoundFood(event);
			return;
		case PLAYER_GAINS_POWER:
			onPlayerGainsPower(event);
			return;
		case PLAYER_LOSING_POWER:
			onPlayerLosingPower(event);
			return;
		case PLAYER_LOST_POWER:
			onPlayerLostPower(event);
			return;
		default:
			if (event instanceof ScatterPhaseStartedEvent) {
				onScatterPhaseStarted((ScatterPhaseStartedEvent) event);
				return;
			}
			if (event instanceof PacManGameStateChangeEvent) {
				onPacManGameStateChange((PacManGameStateChangeEvent) event);
				return;
			}
		}
	}

	default void onBonusActivated(PacManGameEvent e) {
	}

	default void onBonusEaten(PacManGameEvent e) {
	}

	default void onBonusExpired(PacManGameEvent e) {
	}

	default void onExtraLife(PacManGameEvent e) {
	}

	default void onGhostEntersHouse(PacManGameEvent e) {
	}

	default void onGhostReturnsHome(PacManGameEvent e) {
	}

	default void onGhostLeavingHouse(PacManGameEvent e) {
	}
	
	default void onGhostLeftHouse(PacManGameEvent e) {
	}

	default void onPlayerFoundFood(PacManGameEvent e) {
	}

	default void onPlayerLosingPower(PacManGameEvent e) {
	}

	default void onPlayerLostPower(PacManGameEvent e) {
	}

	default void onPlayerGainsPower(PacManGameEvent e) {
	}

	default void onPacManGameStateChange(PacManGameStateChangeEvent e) {
	}

	default void onScatterPhaseStarted(ScatterPhaseStartedEvent e) {
	}
}