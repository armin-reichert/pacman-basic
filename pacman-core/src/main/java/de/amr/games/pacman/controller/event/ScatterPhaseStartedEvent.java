package de.amr.games.pacman.controller.event;

import de.amr.games.pacman.model.common.PacManGameModel;

public class ScatterPhaseStartedEvent extends PacManGameEvent {

	public final int scatterPhase;

	public ScatterPhaseStartedEvent(PacManGameModel gameModel, int scatterPhase) {
		super(gameModel, Info.OTHER, null, null);
		this.scatterPhase = scatterPhase;
	}

	@Override
	public String toString() {
		return String.format("ScatterPhaseStartedEvent: scatterPhase=%d", scatterPhase);
	}
}