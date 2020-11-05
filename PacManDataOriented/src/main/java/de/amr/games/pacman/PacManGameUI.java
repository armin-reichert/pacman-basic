package de.amr.games.pacman;

public interface PacManGameUI {

	void render();

	void redMessage(String text);

	void yellowMessage(String text);

	void clearMessage();

	boolean keyPressed(String keySpec);
}