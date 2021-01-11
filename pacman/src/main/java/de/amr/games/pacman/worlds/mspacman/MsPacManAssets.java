package de.amr.games.pacman.worlds.mspacman;

import static de.amr.games.pacman.worlds.mspacman.MsPacManWorld.APPLE;
import static de.amr.games.pacman.worlds.mspacman.MsPacManWorld.BANANA;
import static de.amr.games.pacman.worlds.mspacman.MsPacManWorld.BREZN;
import static de.amr.games.pacman.worlds.mspacman.MsPacManWorld.CHERRIES;
import static de.amr.games.pacman.worlds.mspacman.MsPacManWorld.PEACH;
import static de.amr.games.pacman.worlds.mspacman.MsPacManWorld.STRAWBERRY;

import java.awt.Font;
import java.awt.image.BufferedImage;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.Map;

import de.amr.games.pacman.ui.Sound;
import de.amr.games.pacman.ui.swing.PacManGameAssets;

public class MsPacManAssets extends PacManGameAssets {

	public final BufferedImage spriteSheet;
	public final BufferedImage mazeFull;
	public final BufferedImage mazeEmptyDark;
	public final BufferedImage mazeEmptyBright;
	public final BufferedImage life;
	public final BufferedImage[] symbols = new BufferedImage[8];
	public final Map<Short, BufferedImage> numbers = new HashMap<>();
	public final Map<Sound, String> soundPaths = new EnumMap<>(Sound.class);
	public final Font scoreFont;

	public MsPacManAssets() {
		//@formatter:off
		spriteSheet = image("/worlds/mspacman/sprites.png");
		mazeFull = spriteSheet.getSubimage(0, 0, 226, 248);
		mazeEmptyDark = spriteSheet.getSubimage(226, 0, 226, 248);
		mazeEmptyBright = mazeEmptyDark; // TODO fixme
		scoreFont = font("/PressStart2P-Regular.ttf", 8);
		life                = section(8, 1);

		symbols[CHERRIES]   = section(2, 3);
		symbols[STRAWBERRY] = section(3, 3);
		symbols[PEACH]      = section(4, 3);
		symbols[BREZN]      = section(5, 3);
		symbols[APPLE]     = section(6, 3);
		symbols[BANANA]   = section(7, 3);
	
		numbers.put((short)100,  section(0, 9));
		numbers.put((short)200,  section(0, 8));
		numbers.put((short)300,  section(1, 9));
		numbers.put((short)400,  section(1, 8));
		numbers.put((short)500,  section(2, 9));
		numbers.put((short)700,  section(3, 9));
		numbers.put((short)800,  section(2, 8));
		numbers.put((short)1000, section(4, 9, 2, 1));
		numbers.put((short)1600, section(3, 8));
		numbers.put((short)2000, section(3, 10, 3, 1));
		numbers.put((short)3000, section(3, 11, 3, 1));
		numbers.put((short)5000, section(3, 12, 3, 1));
	
		soundPaths.put(Sound.CREDIT,       "/sound/credit.wav");
		soundPaths.put(Sound.EAT_BONUS,    "/sound/eat_fruit.wav");
		soundPaths.put(Sound.EXTRA_LIFE,   "/sound/extend.wav");
		soundPaths.put(Sound.GAME_READY,   "/sound/game_start.wav");
		soundPaths.put(Sound.GHOST_DEATH,  "/sound/eat_ghost.wav");
		soundPaths.put(Sound.MUNCH,        "/sound/munch_1.wav");
		soundPaths.put(Sound.PACMAN_DEATH, "/sound/death_1.wav");
		soundPaths.put(Sound.PACMAN_POWER, "/sound/power_pellet.wav");
		soundPaths.put(Sound.RETREATING,   "/sound/retreating.wav");
		soundPaths.put(Sound.SIREN_1,      "/sound/siren_1.wav");
		soundPaths.put(Sound.SIREN_2,      "/sound/siren_2.wav");
		soundPaths.put(Sound.SIREN_3,      "/sound/siren_3.wav");
		soundPaths.put(Sound.SIREN_4,      "/sound/siren_4.wav");
		soundPaths.put(Sound.SIREN_5,      "/sound/siren_5.wav");
		//@formatter:on
	}

	public BufferedImage section(int x, int y, int w, int h) {
		return spriteSheet.getSubimage(x * 16, y * 16, w * 16, h * 16);
	}

	public BufferedImage section(int x, int y) {
		return section(x, y, 1, 1);
	}

	@Override
	public String getSoundPath(Sound sound) {
		return soundPaths.get(sound);
	}

}