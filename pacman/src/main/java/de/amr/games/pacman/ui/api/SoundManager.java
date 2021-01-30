package de.amr.games.pacman.ui.api;

public interface SoundManager {

	void playSound(Sound sound);

	void loopSound(Sound sound);

	void stopSound(Sound sound);

	void stopAllSounds();

}
