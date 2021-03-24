package de.amr.games.pacman.controller;

import de.amr.games.pacman.model.common.GameModel;
import de.amr.games.pacman.model.common.GameVariant;

public class DeadGhostCountChangeEvent extends PacManGameEvent {

	public final int oldCount;
	public final int newCount;

	public DeadGhostCountChangeEvent(GameVariant gameVariant, GameModel gameModel, int oldCount, int newCount) {
		super(gameVariant, gameModel);
		this.oldCount = oldCount;
		this.newCount = newCount;
	}
}