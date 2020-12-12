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

	void loopSound(Sound sound);

	void playSound(Sound sound, boolean cached);

	void stopSound(Sound sound);

	default void playSound(Sound sound) {
		playSound(sound, true);
	}
}