package de.amr.games.pacman.ui.api;

public interface PacManGameSoundManager {

	void playSound(PacManGameSound sound);

	void loopSound(PacManGameSound sound);

	void stopSound(PacManGameSound sound);

	void stopAllSounds();

}
