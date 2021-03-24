package de.amr.games.pacman.controller;

import de.amr.games.pacman.model.common.GameModel;
import de.amr.games.pacman.model.common.GameVariant;

public class ExtraLifeEvent extends PacManGameEvent {

	public ExtraLifeEvent(GameVariant gameVariant, GameModel gameModel) {
		super(gameVariant, gameModel);
	}

}
