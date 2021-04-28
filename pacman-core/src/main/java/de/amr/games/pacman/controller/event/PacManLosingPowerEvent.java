package de.amr.games.pacman.controller.event;

import de.amr.games.pacman.model.common.GameModel;
import de.amr.games.pacman.model.common.GameVariant;

public class PacManLosingPowerEvent extends PacManGameEvent {

	public PacManLosingPowerEvent(GameVariant gameVariant, GameModel gameModel) {
		super(gameVariant, gameModel);
	}
}
