package de.amr.games.pacman.ui.sound;

public interface SoundManager {

	void playSound(PacManGameSound sound);

	default void loopSound(PacManGameSound sound) {
		loopSound(sound, Integer.MAX_VALUE);
	}

	void loopSound(PacManGameSound sound, int repetitions);

	void stopSound(PacManGameSound sound);

	void stopAllSounds();

}
