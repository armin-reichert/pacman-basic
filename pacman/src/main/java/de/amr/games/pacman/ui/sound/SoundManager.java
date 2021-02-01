package de.amr.games.pacman.ui.sound;

public interface SoundManager {

	void playSound(PacManGameSound sound);

	void loopSound(PacManGameSound sound);

	void stopSound(PacManGameSound sound);

	void stopAllSounds();

}
