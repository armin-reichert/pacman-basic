package de.amr.games.pacman.ui;

/**
 * Interface through which the game controller accesses the UI.
 * 
 * @author Armin Reichert
 */
public interface PacManGameUI {

	void reset();

	void update();

	void showFlashMessage(String message, double seconds);

	default void showFlashMessage(String message) {
		showFlashMessage(message, 1);
	}

	boolean keyPressed(String keySpec);
}