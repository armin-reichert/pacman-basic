package de.amr.games.pacman.ui.swing;

import java.io.BufferedInputStream;
import java.net.URL;
import java.util.EnumMap;
import java.util.Map;
import java.util.function.Function;

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

	private static final int MUNCHES = 2;

	private final Function<PacManGameSound, URL> fnSoundURL;
	private final Map<PacManGameSound, Clip> clipCache = new EnumMap<>(PacManGameSound.class);
	private final Clip[] munchClips = new Clip[MUNCHES];
	private int munchIndex;

	public PacManGameSoundManager(Function<PacManGameSound, URL> fnSoundURL) {
		this.fnSoundURL = fnSoundURL;
		for (int i = 0; i < MUNCHES; ++i) {
			munchClips[i] = createAndOpenClip(fnSoundURL.apply(PacManGameSound.PACMAN_MUNCH));
		}
		munchIndex = 0;
	}

	private Clip createAndOpenClip(URL url) {
		try (BufferedInputStream bs = new BufferedInputStream(url.openStream())) {
			try (AudioInputStream as = AudioSystem.getAudioInputStream(bs)) {
				Clip clip = AudioSystem.getClip();
				clip.open(as);
				return clip;
			}
		} catch (Exception x) {
			throw new RuntimeException("Error opening audio clip", x);
		}
	}

	private Clip getClip(PacManGameSound sound) {
		Clip clip = null;
		if (sound == PacManGameSound.PACMAN_MUNCH) {
			clip = munchClips[munchIndex];
			munchIndex = (munchIndex + 1) % MUNCHES;
		} else if (clipCache.containsKey(sound)) {
			clip = clipCache.get(sound);
		} else {
			clip = createAndOpenClip(fnSoundURL.apply(sound));
			clipCache.put(sound, clip);
		}
		clip.setFramePosition(0);
		return clip;
	}

	public void playSound(PacManGameSound sound) {
		getClip(sound).start();
		;
	}

	public void loopSound(PacManGameSound sound) {
		getClip(sound).loop(Clip.LOOP_CONTINUOUSLY);
	}

	public void stopSound(PacManGameSound sound) {
		getClip(sound).stop();
	}

	public void stopAllSounds() {
		for (PacManGameSound sound : clipCache.keySet()) {
			stopSound(sound);
		}
		for (Clip clip : munchClips) {
			clip.stop();
		}
	}
}