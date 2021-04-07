package de.amr.games.pacman.controller.event;

import de.amr.games.pacman.model.common.AbstractGameModel;
import de.amr.games.pacman.model.common.GameVariant;

public class BonusExpiredEvent extends PacManGameEvent {

	public BonusExpiredEvent(GameVariant gameVariant, AbstractGameModel gameModel) {
		super(gameVariant, gameModel);
	}
}