package de.amr.games.pacman.ui;

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

	boolean keyPressed(String keySpec);

	boolean anyKeyPressed();

	void onExit();

	void playSound(Sound sound);

	void loopSound(Sound sound);

	void stopSound(Sound sound);

	void stopAllSounds();
}