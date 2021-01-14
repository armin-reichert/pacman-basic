package de.amr.games.pacman.ui.swing;

import java.io.BufferedInputStream;
import java.util.EnumMap;
import java.util.Map;

import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;

import de.amr.games.pacman.ui.api.PacManGameSound;

/**
 * Sound manager for Pac-Man game.
 * 
 * @author Armin Reichert
 */
class PacManGameSoundManager {

	private final Map<PacManGameSound, Clip> clips = new EnumMap<>(PacManGameSound.class);
	private final Clip[] munching = new Clip[2];
	private int munchIndex;

	public PacManGameSoundManager(SoundAssets assets) {
		munchIndex = 0;
		for (int i = 0; i < munching.length; ++i) {
			munching[i] = openClip(assets.getSoundPath(PacManGameSound.MUNCH));
		}
		for (PacManGameSound sound : PacManGameSound.values()) {
			Clip clip = openClip(assets.getSoundPath(sound));
			clips.put(sound, clip);
		}
	}

	private Clip openClip(String path) {
		try (BufferedInputStream bs = new BufferedInputStream(getClass().getResourceAsStream(path))) {
			try (AudioInputStream as = AudioSystem.getAudioInputStream(bs)) {
				Clip clip = AudioSystem.getClip();
				clip.open(as);
				return clip;
			}
		} catch (Exception x) {
			throw new RuntimeException("Error opening audio clip", x);
		}
	}

	public void playSound(PacManGameSound sound) {
		Clip clip;
		if (sound == PacManGameSound.MUNCH) {
			clip = munching[munchIndex];
			++munchIndex;
			if (munchIndex == munching.length) {
				munchIndex = 0;
			}
		} else {
			clip = clips.get(sound);
		}
		clip.setFramePosition(0);
		clip.start();
	}

	public void loopSound(PacManGameSound sound) {
		Clip clip = clips.get(sound);
		clip.loop(Clip.LOOP_CONTINUOUSLY);
	}

	public void stopSound(PacManGameSound sound) {
		Clip clip = clips.get(sound);
		clip.stop();
	}

	public void stopAllSounds() {
		for (Clip clip : clips.values()) {
			clip.stop();
		}
		for (int i = 0; i < munching.length; ++i) {
			munching[i].stop();
		}
	}
}