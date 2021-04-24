package de.amr.games.pacman.ui;

import java.util.Optional;

import de.amr.games.pacman.controller.event.PacManGameEventFacade;
import de.amr.games.pacman.lib.Direction;

/**
 * Interface through which the game controller accesses the UI.
 * 
 * @author Armin Reichert
 */
public interface PacManGameUI extends PacManGameEventFacade {

	void reset();

	void update();

	void showFlashMessage(String message, double seconds);

	default void showFlashMessage(String message) {
		showFlashMessage(message, 1);
	}

	Optional<Direction> playerDirectionChangeRequested();

	boolean gameStartRequested();
}