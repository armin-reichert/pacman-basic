package de.amr.games.pacman.ui.api;

import java.util.Optional;

import de.amr.games.pacman.model.PacManGame;
import de.amr.games.pacman.ui.sound.SoundManager;

/**
 * Interface through which the game class sees the user interface.
 * 
 * @author Armin Reichert
 */
public interface PacManGameUI {

	void setGame(PacManGame game);

	void setCloseHandler(Runnable handler);

	void updateScene();

	void show();

	void render();

	String translation(String key);

	void showFlashMessage(String message);

	boolean keyPressed(String keySpec);

	Optional<SoundManager> sounds();

	void mute(boolean muted);

	Optional<PacManGameAnimations> animations();

	public static final PacManGameUI NO_UI = new PacManGameUI() {

		@Override
		public void setGame(PacManGame game) {
		}

		@Override
		public void setCloseHandler(Runnable handler) {
		}

		@Override
		public void updateScene() {
		}

		@Override
		public void show() {
		}

		@Override
		public void render() {
		}

		@Override
		public String translation(String key) {
			return key;
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