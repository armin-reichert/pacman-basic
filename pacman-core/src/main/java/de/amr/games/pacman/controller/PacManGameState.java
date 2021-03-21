package de.amr.games.pacman.controller;

/**
 * The states of the game. Each state has a timer.
 * 
 * @author Armin Reichert
 */
public enum PacManGameState {

	INTRO, READY, HUNTING, LEVEL_COMPLETE, LEVEL_STARTING, PACMAN_DYING, GHOST_DYING, GAME_OVER, INTERMISSION;
}