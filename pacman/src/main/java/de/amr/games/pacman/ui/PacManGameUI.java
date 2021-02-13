package de.amr.games.pacman.ui;

import java.util.Optional;

import de.amr.games.pacman.model.PacManGameModel;
import de.amr.games.pacman.sound.SoundManager;

/**
 * Interface through which the game controller can access the views.
 * 
 * @author Armin Reichert
 */
public interface PacManGameUI {

	void setGame(PacManGameModel game);

	void reset();

	void show();

	void render();

	void showFlashMessage(String message);

	boolean keyPressed(String keySpec);

	void mute(boolean muted);

	Optional<SoundManager> sounds();

	Optional<PacManGameAnimations> animations();
}