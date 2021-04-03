package de.amr.games.pacman.ui.swing.assets;

import java.net.URL;
import java.util.EnumMap;
import java.util.Map;
import java.util.function.Function;

import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;

import de.amr.games.pacman.ui.PacManGameSound;

/**
 * Sound manager for Pac-Man game variants.
 * 
 * TODO how to avoid warning about potential resource leak?
 * 
 * @author Armin Reichert
 */
public class SoundManager {

	private final Function<PacManGameSound, URL> fnSoundURL;
	private final Map<PacManGameSound, Clip> clipCache = new EnumMap<>(PacManGameSound.class);
	private final Clip munch0, munch1;
	private int munchIndex;
	private boolean muted;

	public SoundManager(Function<PacManGameSound, URL> fnSoundURL) {
		this.fnSoundURL = fnSoundURL;
		munchIndex = 0;
		munch0 = createAndOpenClip(fnSoundURL.apply(PacManGameSound.PACMAN_MUNCH));
		munch1 = createAndOpenClip(fnSoundURL.apply(PacManGameSound.PACMAN_MUNCH));
	}

	public void setMuted(boolean muted) {
		this.muted = muted;
	}

	private Clip createAndOpenClip(URL url) {
		try (AudioInputStream as = AudioSystem.getAudioInputStream(url)) {
			Clip clip = AudioSystem.getClip();
			clip.open(as);
			return clip;
		} catch (Exception x) {
			throw new RuntimeException("Error opening audio clip", x);
		}
	}

	@SuppressWarnings("resource")
	private Clip getClip(PacManGameSound sound) {
		Clip clip = null;
		if (sound == PacManGameSound.PACMAN_MUNCH) {
			clip = munchIndex == 0 ? munch0 : munch1;
			munchIndex = (munchIndex + 1) % 2;
		} else if (clipCache.containsKey(sound)) {
			clip = clipCache.get(sound);
		} else {
			clip = createAndOpenClip(fnSoundURL.apply(sound));
			clipCache.put(sound, clip);
		}
		clip.setFramePosition(0);
		return clip;
	}

	@SuppressWarnings("resource")
	public void play(PacManGameSound sound) {
		if (!muted) {
			getClip(sound).start();
		}
	}

	@SuppressWarnings("resource")
	public void loop(PacManGameSound sound, int repetitions) {
		if (!muted) {
			Clip clip = getClip(sound);
			clip.setFramePosition(0);
			clip.loop(repetitions == Integer.MAX_VALUE ? Clip.LOOP_CONTINUOUSLY : repetitions - 1);
		}
	}

	@SuppressWarnings("resource")
	public void stop(PacManGameSound sound) {
		getClip(sound).stop();
	}

	public void stopAll() {
		for (Clip clip : clipCache.values()) {
			clip.stop();
		}
		clipCache.clear();
		munch0.stop();
		munch1.stop();
	}
}