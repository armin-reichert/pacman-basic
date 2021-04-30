package de.amr.games.pacman.controller.event;

import de.amr.games.pacman.controller.PacManGameState;
import de.amr.games.pacman.model.common.GameModel;
import de.amr.games.pacman.model.common.GameVariant;

public class PacManGameStateChangeEvent extends PacManGameEvent {

	public final PacManGameState oldGameState;
	public final PacManGameState newGameState;

	public PacManGameStateChangeEvent(GameVariant gameVariant, GameModel gameModel, PacManGameState oldGameState,
			PacManGameState newGameState) {
		super(gameVariant, gameModel, null);
		this.oldGameState = oldGameState;
		this.newGameState = newGameState;
	}

	@Override
	public String toString() {
		return String.format("PacManGameStateChangeEvent(%s->%s)", oldGameState, newGameState);
	}
}