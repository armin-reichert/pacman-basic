package de.amr.games.pacman;

public enum GameState {

	READY, SCATTERING, CHASING, CHANGING_LEVEL, PACMAN_DYING, GHOST_DYING, GAME_OVER;

	public long timer;
}