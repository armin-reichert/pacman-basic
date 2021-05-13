package de.amr.games.pacman.controller.event;

import de.amr.games.pacman.controller.PacManGameState;
import de.amr.games.pacman.model.common.PacManGameModel;

public class PacManGameStateChangeEvent extends PacManGameEvent {

	public final PacManGameState oldGameState;
	public final PacManGameState newGameState;

	public PacManGameStateChangeEvent(PacManGameModel gameModel, PacManGameState oldGameState, PacManGameState newGameState) {
		super(gameModel, Info.GAME_STATE_CHANGE, null, null);
		this.oldGameState = oldGameState;
		this.newGameState = newGameState;
	}

	@Override
	public String toString() {
		return String.format("PacManGameStateChangeEvent(%s->%s)", oldGameState, newGameState);
	}
}