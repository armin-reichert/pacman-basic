package de.amr.games.pacman.ui.api;

import de.amr.games.pacman.game.core.PacManGame;

/**
 * Interface through which the game class sees the user interface.
 * 
 * @author Armin Reichert
 */
public interface PacManGameUI {

	void setGame(PacManGame game);

	void onGameVariantChanged(PacManGame game);

	void openWindow();

	void render();

	void showMessage(String message, boolean important);

	void clearMessage();

	boolean keyPressed(String keySpec);

	void playSound(PacManGameSound sound);

	void loopSound(PacManGameSound sound);

	void stopSound(PacManGameSound sound);

	void stopAllSounds();
}