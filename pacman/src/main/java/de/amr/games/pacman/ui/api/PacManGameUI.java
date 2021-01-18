package de.amr.games.pacman.ui.api;

import de.amr.games.pacman.core.PacManGame;

/**
 * Interface through which the game class sees the user interface.
 * 
 * @author Armin Reichert
 */
public interface PacManGameUI {

	void show();

	void setGame(PacManGame game);

	void render();

	void showMessage(String message, boolean important);

	void clearMessage();

	boolean keyPressed(String keySpec);

	void onWindowClosing();

	void playSound(PacManGameSound sound);

	void loopSound(PacManGameSound sound);

	void stopSound(PacManGameSound sound);

	void stopAllSounds();

	void setDebugMode(boolean debug);

	boolean isDebugMode();

}