package de.amr.games.pacman.controller.event;

import de.amr.games.pacman.model.common.GameModel;
import de.amr.games.pacman.model.common.GameVariant;
import de.amr.games.pacman.model.common.Ghost;

public class GhostEntersHouseEvent extends PacManGameEvent {

	public final Ghost ghost;

	public GhostEntersHouseEvent(GameVariant gameVariant, GameModel gameModel, Ghost ghost) {
		super(gameVariant, gameModel);
		this.ghost = ghost;
	}
}