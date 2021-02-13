package de.amr.games.pacman.ui;

import java.util.Optional;

import de.amr.games.pacman.model.PacManGameModel;
import de.amr.games.pacman.sound.SoundManager;

/**
 * Null object pattern UI.
 * 
 * @author Armin Reichert
 */
public class PacManGameNoUI implements PacManGameUI {

	public static final PacManGameUI IT = new PacManGameNoUI();

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
}
