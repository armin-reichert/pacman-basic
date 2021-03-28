package de.amr.games.pacman.controller.event;

import de.amr.games.pacman.model.common.AbstractGameModel;
import de.amr.games.pacman.model.common.GameVariant;

public class DeadGhostCountChangeEvent extends PacManGameEvent {

	public final int oldCount;
	public final int newCount;

	public DeadGhostCountChangeEvent(GameVariant gameVariant, AbstractGameModel gameModel, int oldCount, int newCount) {
		super(gameVariant, gameModel);
		this.oldCount = oldCount;
		this.newCount = newCount;
	}
}