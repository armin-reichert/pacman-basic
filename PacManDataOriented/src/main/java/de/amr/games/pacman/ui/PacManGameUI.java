package de.amr.games.pacman.ui;

public interface PacManGameUI {

	void setDebugMode(boolean debug);

	boolean isDebugMode();

	void render();

	void redMessage(String text);

	void yellowMessage(String text);

	void clearMessage();

	boolean keyPressed(String keySpec);
}