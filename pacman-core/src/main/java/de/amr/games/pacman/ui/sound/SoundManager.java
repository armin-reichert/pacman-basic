package de.amr.games.pacman.ui.sound;

/**
 * Sound interface used by game controller.
 * 
 * @author Armin Reichert
 */
public interface SoundManager {

	void play(PacManGameSound sound);

	default void loopForever(PacManGameSound sound) {
		loop(sound, Integer.MAX_VALUE);
	}

	void loop(PacManGameSound sound, int repetitions);

	void stop(PacManGameSound sound);

	void stopAll();

	void setMuted(boolean muted);
}