package de.amr.games.pacman.ui;

import de.amr.games.pacman.core.PacManGame;

/**
 * Interface through which the game class sees the user interface.
 * 
 * @author Armin Reichert
 */
public interface PacManGameUI {

	void setGame(PacManGame game);

	float scaling();

	void render();

	void show();

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