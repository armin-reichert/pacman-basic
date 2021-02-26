package de.amr.games.pacman.ui;

import java.util.Optional;

import de.amr.games.pacman.model.common.GameModel;
import de.amr.games.pacman.sound.SoundManager;
import de.amr.games.pacman.ui.animation.PacManGameAnimations;

/**
 * Interface through which the game controller accesses the views. This interface must be
 * implemented, the animation and sound interfaces are optional.
 * 
 * @author Armin Reichert
 */
public interface PacManGameUI {

	void onGameChanged(GameModel game);

	void show();

	void reset();

	void update();

	void render();

	void showFlashMessage(String message, long ticks);

	boolean keyPressed(String keySpec);

	void mute(boolean muted);

	Optional<SoundManager> sound();

	Optional<PacManGameAnimations> animation();
}