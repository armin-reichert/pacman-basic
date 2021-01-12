package de.amr.games.pacman.worlds.mspacman;

import static de.amr.games.pacman.worlds.mspacman.MsPacManWorld.APPLE;
import static de.amr.games.pacman.worlds.mspacman.MsPacManWorld.BANANA;
import static de.amr.games.pacman.worlds.mspacman.MsPacManWorld.PRETZEL;
import static de.amr.games.pacman.worlds.mspacman.MsPacManWorld.CHERRIES;
import static de.amr.games.pacman.worlds.mspacman.MsPacManWorld.ORANGE;
import static de.amr.games.pacman.worlds.mspacman.MsPacManWorld.PEAR;
import static de.amr.games.pacman.worlds.mspacman.MsPacManWorld.STRAWBERRY;

import java.awt.Font;
import java.awt.image.BufferedImage;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.Map;

import de.amr.games.pacman.lib.Direction;
import de.amr.games.pacman.ui.Sound;
import de.amr.games.pacman.ui.swing.PacManGameAssets;

public class MsPacManAssets extends PacManGameAssets {

	/** Sprite sheet order of directions. */
	public static final Map<Direction, Integer> DIR_INDEX = new EnumMap<>(Direction.class);

	static {
		DIR_INDEX.put(Direction.RIGHT, 0);
		DIR_INDEX.put(Direction.LEFT, 1);
		DIR_INDEX.put(Direction.UP, 2);
		DIR_INDEX.put(Direction.DOWN, 3);
	}

	public final BufferedImage gameLogo;
	public final BufferedImage spriteSheet;
	public final BufferedImage[] mazeFull = new BufferedImage[6];
	public final BufferedImage[] mazeEmptyDark = new BufferedImage[6];
	public final BufferedImage[] mazeEmptyBright = new BufferedImage[6];
	public final BufferedImage life;
	public final BufferedImage[] symbols = new BufferedImage[8];
	public final Map<Short, BufferedImage> numbers = new HashMap<>();
	public final Map<Short, BufferedImage> bountyNumbers = new HashMap<>();
	public final Map<Sound, String> soundPaths = new EnumMap<>(Sound.class);
	public final Font scoreFont;

	public MsPacManAssets() {
		//@formatter:off
		gameLogo            = image("/worlds/mspacman/logo.png");
		spriteSheet         = image("/worlds/mspacman/sprites.png");
		
		for (int i = 0; i < 6; ++i) {
			mazeFull[i]         = spriteSheet.getSubimage(0, i*248, 226, 248);
			mazeEmptyDark[i]    = spriteSheet.getSubimage(226, i*248, 226, 248);
			mazeEmptyBright[i]  = mazeEmptyDark[i]; // TODO fixme
		}
		
		scoreFont           = font("/PressStart2P-Regular.ttf", 8);
		life                = section(1, 0);

		symbols[CHERRIES]   = section(3, 0);
		symbols[STRAWBERRY] = section(4, 0);
		symbols[ORANGE]      = section(5, 0);
		symbols[PRETZEL]      = section(6, 0);
		symbols[APPLE]      = section(7, 0);
		symbols[PEAR]       = section(8, 0);
		symbols[BANANA]     = section(9, 0);
	
		numbers.put((short)100,  section(3, 1));
		numbers.put((short)200,  section(4, 1));
		numbers.put((short)500,  section(5, 1));
		numbers.put((short)700,  section(6, 1));
		numbers.put((short)1000, section(7, 1));
		numbers.put((short)2000, section(8, 1));
		numbers.put((short)5000, section(9, 1));
		
		bountyNumbers.put((short)200, section(0,8));
		bountyNumbers.put((short)400, section(1,8));
		bountyNumbers.put((short)800, section(2,8));
		bountyNumbers.put((short)1600, section(3,8));
	
		//TODO use Ms. Pac-Man sounds
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
		return spriteSheet.getSubimage(456 + x * 16, y * 16, w * 16, h * 16);
	}

	public BufferedImage section(int x, int y) {
		return section(x, y, 1, 1);
	}

	@Override
	public String getSoundPath(Sound sound) {
		return soundPaths.get(sound);
	}

}