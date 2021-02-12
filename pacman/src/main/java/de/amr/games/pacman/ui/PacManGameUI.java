package de.amr.games.pacman.ui;

import java.util.Optional;

import de.amr.games.pacman.model.PacManGameModel;
import de.amr.games.pacman.sound.SoundManager;

/**
 * Interface through which the game class sees the user interface.
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

	Optional<SoundManager> sounds();

	void mute(boolean muted);

	Optional<PacManGameAnimations> animations();

	public static final PacManGameUI NO_UI = new PacManGameUI() {

		@Override
		public void setGame(PacManGameModel game) {
		}

		@Override
		public void reset() {
		}

		@Override
		public void show() {
		}

		@Override
		public void render() {
		}

		@Override
		public void showFlashMessage(String message) {
		}

		@Override
		public boolean keyPressed(String keySpec) {
			return false;
		}

		@Override
		public Optional<SoundManager> sounds() {
			return Optional.empty();
		}

		@Override
		public void mute(boolean muted) {
		}

		@Override
		public Optional<PacManGameAnimations> animations() {
			return Optional.empty();
		}
	};
}