package de.amr.games.pacman.ui;

public interface PacManGameUI {

	void setDebugMode(boolean debug);

	boolean isDebugMode();

	void render();

	void showGameReadyMessage();

	void showGameOverMessage();

	void clearMessage();

	void startIntroAnimation();

	boolean keyPressed(String keySpec);

	void onExit();
}