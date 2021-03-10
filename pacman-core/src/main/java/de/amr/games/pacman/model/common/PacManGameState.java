package de.amr.games.pacman.model.common;

import de.amr.games.pacman.lib.TickTimer;

/**
 * The states of the game. Each state has a timer.
 * 
 * @author Armin Reichert
 */
public enum PacManGameState {

	INTRO, READY, HUNTING, CHANGING_LEVEL, PACMAN_DYING, GHOST_DYING, GAME_OVER, INTERMISSION;

	public final TickTimer timer = new TickTimer();

	public PacManGameState tick() {
		timer.tick();
		return this;
	}
}