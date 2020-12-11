package de.amr.games.pacman.ui;

public interface PacManGameUI {

	void setDebugMode(boolean debug);

	boolean isDebugMode();

	void render();

	void show();

	void startIntroScene();

	boolean keyPressed(String keySpec);

	void onExit();

	void playSound(Sound sound, boolean cached);

	default void playSound(Sound sound) {
		playSound(sound, true);
	}
}