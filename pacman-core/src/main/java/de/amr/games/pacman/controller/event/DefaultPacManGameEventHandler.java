package de.amr.games.pacman.controller.event;

import de.amr.games.pacman.controller.event.PacManGameEvent.Info;

/**
 * Provides an empty default handler for each game event such that a class implementing this
 * interface just needs to override the needed method(s).
 * 
 * @author Armin Reichert
 */
public interface DefaultPacManGameEventHandler extends PacManGameEventListener {

	@Override
	default void onGameEvent(PacManGameEvent event) {
		if (event.info == Info.BONUS_ACTIVATED) {
			onBonusActivated(event);
		} else if (event.info == Info.BONUS_EATEN) {
			onBonusEaten(event);
		} else if (event.info == Info.BONUS_EXPIRED) {
			onBonusExpired(event);
		} else if (event.info == Info.EXTRA_LIFE) {
			onExtraLife(event);
		} else if (event.info == Info.GHOST_ENTERS_HOUSE) {
			onGhostEntersHouse(event);
		} else if (event.info == Info.GHOST_LEAVES_HOUSE) {
			onGhostLeavesHouse(event);
		} else if (event.info == Info.GHOST_RETURNS_HOME) {
			onGhostReturnsHome(event);
		} else if (event.info == Info.PLAYER_FOUND_FOOD) {
			onPlayerFoundFood(event);
		} else if (event.info == Info.PLAYER_GAINS_POWER) {
			onPlayerGainsPower(event);
		} else if (event.info == Info.PLAYER_LOSING_POWER) {
			onPlayerLosingPower(event);
		} else if (event.info == Info.PLAYER_LOST_POWER) {
			onPlayerLostPower(event);
		} else if (event instanceof ScatterPhaseStartedEvent) {
			onScatterPhaseStarted((ScatterPhaseStartedEvent) event);
		} else if (event instanceof PacManGameStateChangeEvent) {
			onPacManGameStateChange((PacManGameStateChangeEvent) event);
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

	default void onGhostLeavesHouse(PacManGameEvent e) {
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