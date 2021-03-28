package de.amr.games.pacman.controller.event;

import de.amr.games.pacman.model.common.AbstractGameModel;
import de.amr.games.pacman.model.common.GameVariant;

public abstract class PacManGameEvent {

	public final GameVariant gameVariant;
	public final AbstractGameModel gameModel;

	public PacManGameEvent(GameVariant gameVariant, AbstractGameModel gameModel) {
		this.gameVariant = gameVariant;
		this.gameModel = gameModel;
	}
}