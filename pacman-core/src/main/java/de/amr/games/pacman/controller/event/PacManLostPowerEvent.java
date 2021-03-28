package de.amr.games.pacman.controller.event;

import de.amr.games.pacman.model.common.AbstractGameModel;
import de.amr.games.pacman.model.common.GameVariant;

public class PacManLostPowerEvent extends PacManGameEvent {

	public PacManLostPowerEvent(GameVariant gameVariant, AbstractGameModel gameModel) {
		super(gameVariant, gameModel);
	}
}
