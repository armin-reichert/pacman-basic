package de.amr.games.pacman.controller.event;

import de.amr.games.pacman.controller.PacManGameState;
import de.amr.games.pacman.model.common.AbstractGameModel;
import de.amr.games.pacman.model.common.GameVariant;

public class PacManGameStateChangedEvent extends PacManGameEvent {

	public final PacManGameState oldGameState;
	public final PacManGameState newGameState;

	public PacManGameStateChangedEvent(GameVariant gameVariant, AbstractGameModel gameModel, PacManGameState oldGameState,
			PacManGameState newGameState) {
		super(gameVariant, gameModel);
		this.oldGameState = oldGameState;
		this.newGameState = newGameState;
	}
}