package de.amr.games.pacman.ui;

/**
 * Interface through which the game class can talk to the user interface.
 * 
 * @author Armin Reichert
 *
 */
public interface PacManGameUI {

	void setDebugMode(boolean debug);

	boolean isDebugMode();

	void render();

	void show();

	void startIntroScene();

	void endIntroScene();

	boolean keyPressed(String keySpec);

	boolean anyKeyPressed();

	void onExit();

	default void playSound(Sound sound) {
		playSound(sound, true);
	}

	void playSound(Sound sound, boolean useCache);

	void loopSound(Sound sound);

	void stopSound(Sound sound);

	void stopAllSounds();

}