package de.amr.games.pacman.controller;

import de.amr.games.pacman.model.common.GameModel;
import de.amr.games.pacman.model.common.GameVariant;

public class BonusEatenEvent extends PacManGameEvent {

	public BonusEatenEvent(GameVariant gameVariant, GameModel gameModel) {
		super(gameVariant, gameModel);
	}
}