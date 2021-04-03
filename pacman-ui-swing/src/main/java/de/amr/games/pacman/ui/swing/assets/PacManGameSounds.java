package de.amr.games.pacman.ui.swing.assets;

import static de.amr.games.pacman.ui.PacManGameSound.BONUS_EATEN;
import static de.amr.games.pacman.ui.PacManGameSound.CREDIT;
import static de.amr.games.pacman.ui.PacManGameSound.EXTRA_LIFE;
import static de.amr.games.pacman.ui.PacManGameSound.GAME_READY;
import static de.amr.games.pacman.ui.PacManGameSound.GHOST_EATEN;
import static de.amr.games.pacman.ui.PacManGameSound.GHOST_RETURNING_HOME;
import static de.amr.games.pacman.ui.PacManGameSound.GHOST_SIREN_1;
import static de.amr.games.pacman.ui.PacManGameSound.GHOST_SIREN_2;
import static de.amr.games.pacman.ui.PacManGameSound.GHOST_SIREN_3;
import static de.amr.games.pacman.ui.PacManGameSound.GHOST_SIREN_4;
import static de.amr.games.pacman.ui.PacManGameSound.INTERMISSION_1;
import static de.amr.games.pacman.ui.PacManGameSound.INTERMISSION_2;
import static de.amr.games.pacman.ui.PacManGameSound.INTERMISSION_3;
import static de.amr.games.pacman.ui.PacManGameSound.PACMAN_DEATH;
import static de.amr.games.pacman.ui.PacManGameSound.PACMAN_MUNCH;
import static de.amr.games.pacman.ui.PacManGameSound.PACMAN_POWER;

import java.net.URL;
import java.util.EnumMap;
import java.util.Map;

import de.amr.games.pacman.ui.PacManGameSound;

/**
 * Maps the sound constants to the sound resources.
 * 
 * @author Armin Reichert
 */
public class PacManGameSounds {

	private static final Map<PacManGameSound, URL> SOUND_RES_PACMAN = new EnumMap<>(PacManGameSound.class);
	private static final Map<PacManGameSound, URL> SOUND_RES_MSPACMAN = new EnumMap<>(PacManGameSound.class);

	private static URL url(String path) {
		return PacManGameSounds.class.getResource(path);
	}

	public static URL mrPacManSoundURL(PacManGameSound sound) {
		return SOUND_RES_PACMAN.get(sound);
	}

	public static URL msPacManSoundURL(PacManGameSound sound) {
		return SOUND_RES_MSPACMAN.get(sound);
	}

	static {
		//@formatter:off
		SOUND_RES_PACMAN.put(CREDIT,               url("/pacman/sound/credit.wav"));
		SOUND_RES_PACMAN.put(EXTRA_LIFE,           url("/pacman/sound/extend.wav"));
		SOUND_RES_PACMAN.put(GAME_READY,           url("/pacman/sound/game_start.wav"));
		SOUND_RES_PACMAN.put(BONUS_EATEN,          url("/pacman/sound/eat_fruit.wav"));
		SOUND_RES_PACMAN.put(PACMAN_MUNCH,         url("/pacman/sound/munch_1.wav"));
		SOUND_RES_PACMAN.put(PACMAN_DEATH,         url("/pacman/sound/death_1.wav"));
		SOUND_RES_PACMAN.put(PACMAN_POWER,         url("/pacman/sound/power_pellet.wav"));
		SOUND_RES_PACMAN.put(GHOST_EATEN,          url("/pacman/sound/eat_ghost.wav"));
		SOUND_RES_PACMAN.put(GHOST_RETURNING_HOME, url("/pacman/sound/retreating.wav"));
		SOUND_RES_PACMAN.put(GHOST_SIREN_1,        url("/pacman/sound/siren_1.wav"));
		SOUND_RES_PACMAN.put(GHOST_SIREN_2,        url("/pacman/sound/siren_2.wav"));
		SOUND_RES_PACMAN.put(GHOST_SIREN_3,        url("/pacman/sound/siren_3.wav"));
		SOUND_RES_PACMAN.put(GHOST_SIREN_4,        url("/pacman/sound/siren_4.wav"));
		SOUND_RES_PACMAN.put(INTERMISSION_1,       url("/pacman/sound/intermission.wav"));
		SOUND_RES_PACMAN.put(INTERMISSION_2,       url("/pacman/sound/intermission.wav"));
		SOUND_RES_PACMAN.put(INTERMISSION_3,       url("/pacman/sound/intermission.wav"));

		SOUND_RES_MSPACMAN.put(CREDIT,               url("/mspacman/sound/Coin Credit.wav"));
		SOUND_RES_MSPACMAN.put(EXTRA_LIFE,           url("/mspacman/sound/Extra Life.wav"));
		SOUND_RES_MSPACMAN.put(GAME_READY,           url("/mspacman/sound/Start.wav"));
		SOUND_RES_MSPACMAN.put(BONUS_EATEN,          url("/mspacman/sound/Fruit.wav"));
		SOUND_RES_MSPACMAN.put(PACMAN_MUNCH,         url("/mspacman/sound/Ms. Pac Man Pill.wav"));
		SOUND_RES_MSPACMAN.put(PACMAN_DEATH,         url("/mspacman/sound/Died.wav"));
		SOUND_RES_MSPACMAN.put(PACMAN_POWER,         url("/mspacman/sound/Scared Ghost.wav"));
		SOUND_RES_MSPACMAN.put(GHOST_EATEN,          url("/mspacman/sound/Ghost.wav"));
		SOUND_RES_MSPACMAN.put(GHOST_RETURNING_HOME, url("/mspacman/sound/Ghost Eyes.wav"));
		SOUND_RES_MSPACMAN.put(GHOST_SIREN_1,        url("/mspacman/sound/Ghost Noise 1.wav"));
		SOUND_RES_MSPACMAN.put(GHOST_SIREN_2,        url("/mspacman/sound/Ghost Noise 2.wav"));
		SOUND_RES_MSPACMAN.put(GHOST_SIREN_3,        url("/mspacman/sound/Ghost Noise 3.wav"));
		SOUND_RES_MSPACMAN.put(GHOST_SIREN_4,        url("/mspacman/sound/Ghost Noise 5.wav"));
		SOUND_RES_MSPACMAN.put(INTERMISSION_1,       url("/mspacman/sound/They Meet Act 1.wav"));
		SOUND_RES_MSPACMAN.put(INTERMISSION_2,       url("/mspacman/sound/The Chase Act 2.wav"));
		SOUND_RES_MSPACMAN.put(INTERMISSION_3,       url("/mspacman/sound/Junior Act 3.wav"));
		//@formatter:on
	}
}