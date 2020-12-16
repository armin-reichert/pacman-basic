package de.amr.games.pacman.ui;

public interface PacManGameUI {

	void setDebugMode(boolean debug);

	boolean isDebugMode();

	void render();

	void show();

	void startIntroScene();

	void endIntroScene();

	boolean keyPressed(String keySpec);

	void onExit();

	default void playSound(Sound sound) {
		playSound(sound, true);
	}

	void playSound(Sound sound, boolean useCache);

	void loopSound(Sound sound);

	void stopSound(Sound sound);

	void stopAllSounds();

}