package de.amr.games.pacman.ui.swing;

import java.io.BufferedInputStream;
import java.util.EnumMap;
import java.util.Map;

import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;

import de.amr.games.pacman.ui.Sound;

/**
 * Sound manager for Pac-Man Swing UI.
 * 
 * @author Armin Reichert
 */
class SoundManager {

	private final PacManClassicGameAssets assets;
	private final Map<Sound, Clip> clips = new EnumMap<>(Sound.class);
	private final Clip[] munchClips = new Clip[2];
	private int munchIndex;

	public SoundManager(PacManClassicGameAssets assets) {
		this.assets = assets;
	}

	private Clip openClip(String path) {
		try (BufferedInputStream bs = new BufferedInputStream(getClass().getResourceAsStream(path))) {
			Clip clip = AudioSystem.getClip();
			clip.open(AudioSystem.getAudioInputStream(bs));
			return clip;
		} catch (Exception x) {
			throw new RuntimeException(x);
		}
	}

	public void init() {
		munchIndex = 0;
		for (int i = 0; i < munchClips.length; ++i) {
			munchClips[i] = openClip(assets.soundPaths.get(Sound.MUNCH));
		}
		for (Sound sound : Sound.values()) {
			clips.put(sound, openClip(assets.soundPaths.get(sound)));
		}
	}

	public void playSound(Sound sound) {
		Clip clip;
		if (sound == Sound.MUNCH) {
			clip = munchClips[munchIndex];
			++munchIndex;
			if (munchIndex == munchClips.length) {
				munchIndex = 0;
			}
		} else {
			clip = clips.get(sound);
		}
		clip.setFramePosition(0);
		clip.start();
	}

	public void loopSound(Sound sound) {
		Clip clip = clips.get(sound);
		clip.loop(Clip.LOOP_CONTINUOUSLY);
	}

	public void stopSound(Sound sound) {
		Clip clip = clips.get(sound);
		clip.stop();
	}

	public void stopAllSounds() {
		for (Clip clip : clips.values()) {
			clip.stop();
		}
		for (int i = 0; i < munchClips.length; ++i) {
			munchClips[i].stop();
		}
	}
}