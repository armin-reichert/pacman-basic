package de.amr.games.pacman.ui;

import de.amr.games.pacman.core.GameVariant;
import de.amr.games.pacman.core.PacManGame;

/**
 * Interface through which the game class sees the user interface.
 * 
 * @author Armin Reichert
 */
public interface PacManGameUI {

	void setGame(PacManGame game);

	void setGameVariant(GameVariant variant);

	float scaling();

	void render();

	void show();

	void showMessage(String message, boolean important);

	void clearMessage();

	boolean keyPressed(String keySpec);

	boolean anyKeyPressed();

	void onWindowClosing();

	void playSound(Sound sound);

	void loopSound(Sound sound);

	void stopSound(Sound sound);

	void stopAllSounds();

	void setDebugMode(boolean debug);

	boolean isDebugMode();

}