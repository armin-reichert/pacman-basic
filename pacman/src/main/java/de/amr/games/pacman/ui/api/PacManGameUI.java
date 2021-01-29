package de.amr.games.pacman.ui.api;

import java.util.Optional;

import de.amr.games.pacman.game.core.PacManGameModel;

/**
 * Interface through which the game class sees the user interface.
 * 
 * @author Armin Reichert
 */
public interface PacManGameUI {

	String translation(String key);

	void updateGame(PacManGameModel game);

	void updateScene();

	void show();

	void redraw();

	void showMessage(String message, boolean important);

	void showFlashMessage(String message);

	void clearMessages();

	boolean keyPressed(String keySpec);

	Optional<PacManGameSoundManager> sounds();

	Optional<PacManGameAnimations> animations();
}