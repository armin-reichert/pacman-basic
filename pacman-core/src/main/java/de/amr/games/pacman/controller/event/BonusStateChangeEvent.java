package de.amr.games.pacman.controller.event;

import de.amr.games.pacman.model.common.GameModel;
import de.amr.games.pacman.model.common.GameVariant;

public class BonusStateChangeEvent extends PacManGameEvent {

	public static final int ACTIVATED = 0;
	public static final int EATEN = 1;
	public static final int EXPIRED = 2;

	public final int state;

	public BonusStateChangeEvent(GameVariant gameVariant, GameModel gameModel, int state) {
		super(gameVariant, gameModel);
		this.state = state;
	}
}