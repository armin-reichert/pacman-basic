package de.amr.games.pacman.ui.swing;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import javax.sound.sampled.Clip;
import javax.swing.Timer;

import de.amr.games.pacman.ui.Sound;

/**
 * Sound manager for Pac-Man Swing UI.
 * 
 * @author Armin Reichert
 */
public class SoundManager {

	private final Assets assets;
	private final Map<Sound, Clip> cachedClips = new HashMap<>();
	private final Set<Clip> onetimeClips = new HashSet<>();
	private final Timer clipGC;

	public SoundManager(Assets assets) {
		this.assets = assets;
		clipGC = new Timer(3000, e -> {
			for (Clip onetimeClip : onetimeClips) {
				if (!onetimeClip.isRunning()) {
					onetimeClip.close();
				}
			}
		});
	}

	public void start() {
		clipGC.start();
	}

	public void playSound(Sound sound, boolean useCache) {
		Clip clip = getClip(sound, useCache);
		clip.setFramePosition(0);
		clip.start();
	}

	public void loopSound(Sound sound) {
		Clip clip = getClip(sound, true);
		clip.loop(Clip.LOOP_CONTINUOUSLY);
	}

	public void stopSound(Sound sound) {
		Clip clip = getClip(sound, true);
		clip.stop();
	}

	public void stopAllSounds() {
		for (Clip clip : cachedClips.values()) {
			clip.stop();
		}
	}

	private Clip getClip(Sound sound, boolean useCache) {
		if (useCache) {
			if (cachedClips.containsKey(sound)) {
				return cachedClips.get(sound);
			}
			Clip clip = assets.clip(assets.soundPaths.get(sound));
			cachedClips.put(sound, clip);
			return clip;
		} else {
			Clip clip = assets.clip(assets.soundPaths.get(sound));
			onetimeClips.add(clip);
			return clip;
		}
	}
}