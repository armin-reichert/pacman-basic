package de.amr.games.pacman.ui.sound;

import java.util.Arrays;
import java.util.List;

/**
 * Constants for the sounds used in the Pac-Man and Ms. Pac-Man games.
 * 
 * @author Armin Reichert
 */
public enum PacManGameSound {

	//@formatter:off
	BONUS_EATEN, 
	CREDIT, 
	EXTRA_LIFE, 
	GAME_READY, 
	GHOST_EATEN, 
	GHOST_RETURNING_HOME,
	GHOST_SIREN_1, 
	GHOST_SIREN_2, 
	GHOST_SIREN_3, 
	GHOST_SIREN_4, 
	GHOST_SIREN_5, 
	INTERMISSION_1, 
	INTERMISSION_2,
	INTERMISSION_3,
	PACMAN_DEATH, 
	PACMAN_MUNCH, 
	PACMAN_POWER;
	//@formatter:on

	public static final List<PacManGameSound> SIRENS = Arrays.asList(GHOST_SIREN_1, GHOST_SIREN_2, GHOST_SIREN_3,
			GHOST_SIREN_4);
}