package de.amr.games.pacman.controller.event;

import de.amr.games.pacman.model.common.GameModel;
import de.amr.games.pacman.model.common.GameVariant;

public abstract class PacManGameEvent {

	public final GameVariant gameVariant;
	public final GameModel gameModel;

	public PacManGameEvent(GameVariant gameVariant, GameModel gameModel) {
		this.gameVariant = gameVariant;
		this.gameModel = gameModel;
	}
}