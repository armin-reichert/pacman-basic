package de.amr.games.pacman.ui.swing;

import java.io.BufferedInputStream;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
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

	private final SoundAssets assets;
	private final Map<PacManGameSound, Clip> clipCache = new EnumMap<>(PacManGameSound.class);
	private final List<Clip> munchingClips = new ArrayList<>(2);
	private int munchIndex;

	public PacManGameSoundManager(SoundAssets assets) {
		this.assets = assets;
		munchIndex = 0;
		for (int i = 0; i < 2; ++i) {
			Clip clip = openClip(assets.getSoundPath(PacManGameSound.MUNCH));
			munchingClips.add(clip);
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
		Clip clip = findClip(sound);
		if (!clip.isOpen()) {
			openClip(assets.getSoundPath(sound));
		}
		clip.setFramePosition(0);
		clip.start();
	}

	private Clip findClip(PacManGameSound sound) {
		if (sound == PacManGameSound.MUNCH) {
			Clip clip = munchingClips.get(munchIndex);
			munchIndex = (munchIndex + 1) % munchingClips.size();
			clip.setFramePosition(0);
			return clip;
		} else if (clipCache.containsKey(sound)) {
			Clip clip = clipCache.get(sound);
			return clip;
		} else {
			Clip clip = openClip(assets.getSoundPath(sound));
			clipCache.put(sound, clip);
			return clip;
		}
	}

	public void loopSound(PacManGameSound sound) {
		Clip clip = findClip(sound);
		clip.loop(Clip.LOOP_CONTINUOUSLY);
	}

	public void stopSound(PacManGameSound sound) {
		Clip clip = findClip(sound);
		if (clip.isOpen() && clip.isRunning()) {
			clip.stop();
		}
	}

	public void stopAllSounds() {
		for (PacManGameSound sound : clipCache.keySet()) {
			stopSound(sound);
		}
		for (Clip clip : munchingClips) {
			clip.stop();
		}
	}
}