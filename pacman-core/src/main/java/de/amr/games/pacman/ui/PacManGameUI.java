package de.amr.games.pacman.ui;

import de.amr.games.pacman.controller.PlayerControl;
import de.amr.games.pacman.controller.event.DefaultPacManGameEventHandler;

/**
 * Interface through which the game controller accesses the UI.
 * 
 * @author Armin Reichert
 */
public interface PacManGameUI extends DefaultPacManGameEventHandler, PlayerControl {

	void reset();

	void update();

	void showFlashMessage(String message, double seconds);

	default void showFlashMessage(String message) {
		showFlashMessage(message, 1);
	}
}