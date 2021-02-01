package de.amr.games.pacman.ui.api;

import java.util.Optional;

import de.amr.games.pacman.model.PacManGame;
import de.amr.games.pacman.ui.sound.SoundManager;

/**
 * Interface through which the game class sees the user interface.
 * 
 * @author Armin Reichert
 */
public interface PacManGameUI {

	void setGame(PacManGame game);

	void setCloseHandler(Runnable handler);

	void updateScene();

	void show();

	void redraw();

	String translation(String key);

	void showMessage(String message, boolean important);

	void showFlashMessage(String message);

	void clearMessages();

	boolean keyPressed(String keySpec);

	Optional<SoundManager> sounds();

	Optional<PacManGameAnimations> animations();
}