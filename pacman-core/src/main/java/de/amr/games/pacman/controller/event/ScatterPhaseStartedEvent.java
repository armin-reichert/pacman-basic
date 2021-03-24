package de.amr.games.pacman.controller.event;

import de.amr.games.pacman.model.common.GameModel;
import de.amr.games.pacman.model.common.GameVariant;

public class ScatterPhaseStartedEvent extends PacManGameEvent {

	public final int scatterPhase;

	public ScatterPhaseStartedEvent(GameVariant gameVariant, GameModel gameModel, int scatterPhase) {
		super(gameVariant, gameModel);
		this.scatterPhase = scatterPhase;
	}
}