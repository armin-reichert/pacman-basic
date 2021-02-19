package de.amr.games.pacman.ui;

import java.util.Optional;

import de.amr.games.pacman.model.PacManGameModel;
import de.amr.games.pacman.sound.SoundManager;

/**
 * Interface through which the game controller accesses the views.
 * 
 * @author Armin Reichert
 */
public interface PacManGameUI {

	void onGameChanged(PacManGameModel game);

	void show();

	void reset();

	void update();

	void render();

	void showFlashMessage(String message, long ticks);

	boolean keyPressed(String keySpec);

	void mute(boolean muted);

	Optional<SoundManager> sound();

	Optional<PacManGameAnimation> animation();
}