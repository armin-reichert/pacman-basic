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
		if (event instanceof BonusStateChangeEvent) {
			BonusStateChangeEvent bonusChangeEvent = (BonusStateChangeEvent) event;
			if (bonusChangeEvent.state == BonusStateChangeEvent.ACTIVATED) {
				onBonusActivated(bonusChangeEvent);
			} else if (bonusChangeEvent.state == BonusStateChangeEvent.EATEN) {
				onBonusEaten(bonusChangeEvent);
			} else if (bonusChangeEvent.state == BonusStateChangeEvent.EXPIRED) {
				onBonusExpired(bonusChangeEvent);
			}
		} else if (event instanceof ExtraLifeEvent) {
			onExtraLife((ExtraLifeEvent) event);
		} else if (event instanceof GhostReturningHomeEvent) {
			onGhostReturningHome((GhostReturningHomeEvent) event);
		} else if (event instanceof GhostEntersHouseEvent) {
			onGhostEntersHouse((GhostEntersHouseEvent) event);
		} else if (event instanceof GhostLeavesHouseEvent) {
			onGhostLeavesHouse((GhostLeavesHouseEvent) event);
		} else if (event instanceof PacManFoundFoodEvent) {
			onPacManFoundFood((PacManFoundFoodEvent) event);
		} else if (event instanceof PacManGainsPowerEvent) {
			onPacManGainsPower((PacManGainsPowerEvent) event);
		} else if (event instanceof PacManGameStateChangeEvent) {
			onPacManGameStateChange((PacManGameStateChangeEvent) event);
		} else if (event instanceof PacManLosingPowerEvent) {
			onPacManLosingPower((PacManLosingPowerEvent) event);
		} else if (event instanceof PacManLostPowerEvent) {
			onPacManLostPower((PacManLostPowerEvent) event);
		} else if (event instanceof ScatterPhaseStartedEvent) {
			onScatterPhaseStarted((ScatterPhaseStartedEvent) event);
		}
	}

	default void onBonusActivated(BonusStateChangeEvent e) {
	}

	default void onBonusEaten(BonusStateChangeEvent e) {
	}

	default void onBonusExpired(BonusStateChangeEvent e) {
	}

	default void onExtraLife(ExtraLifeEvent e) {
	}

	default void onGhostEntersHouse(GhostEntersHouseEvent e) {
	}

	default void onGhostReturningHome(GhostReturningHomeEvent e) {
	}

	default void onGhostLeavesHouse(GhostLeavesHouseEvent e) {
	}

	default void onPacManFoundFood(PacManFoundFoodEvent e) {
	}

	default void onPacManGainsPower(PacManGainsPowerEvent e) {
	}

	default void onPacManGameStateChange(PacManGameStateChangeEvent e) {
	}

	default void onPacManLosingPower(PacManLosingPowerEvent e) {
	}

	default void onPacManLostPower(PacManLostPowerEvent e) {
	}

	default void onScatterPhaseStarted(ScatterPhaseStartedEvent e) {
	}
}