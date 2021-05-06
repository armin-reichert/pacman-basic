package de.amr.games.pacman.controller.event;

import de.amr.games.pacman.model.common.GameModel;

public class ScatterPhaseStartedEvent extends PacManGameEvent {

	public final int scatterPhase;

	public ScatterPhaseStartedEvent(GameModel gameModel, int scatterPhase) {
		super(gameModel, Info.OTHER, null, null);
		this.scatterPhase = scatterPhase;
	}

	@Override
	public String toString() {
		return String.format("ScatterPhaseStartedEvent: scatterPhase=%d", scatterPhase);
	}
}