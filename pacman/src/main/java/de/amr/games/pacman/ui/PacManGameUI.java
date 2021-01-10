package de.amr.games.pacman.ui;

import de.amr.games.pacman.ui.swing.Scene;

/**
 * Interface through which the game class sees the user interface.
 * 
 * @author Armin Reichert
 */
public interface PacManGameUI {

	void setDebugMode(boolean debug);

	boolean isDebugMode();

	void render();

	void show();

	Scene currentScene();

	boolean keyPressed(String keySpec);

	boolean anyKeyPressed();

	void onExit();

	void playSound(Sound sound);

	void loopSound(Sound sound);

	void stopSound(Sound sound);

	void stopAllSounds();
}