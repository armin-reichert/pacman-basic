package de.amr.games.pacman.controller;

import de.amr.games.pacman.lib.TickTimer;

class GameState {

	public final PacManGameState id;
	public final TickTimer timer;
	public Runnable onEnter, onUpdate, onExit;

	public GameState(PacManGameState id) {
		this.id = id;
		timer = new TickTimer();
	}
}