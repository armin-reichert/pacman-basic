package de.amr.games.pacman.ui.swing.mspacman;

import static de.amr.games.pacman.ui.swing.Assets.font;
import static de.amr.games.pacman.ui.swing.Assets.image;
import static de.amr.games.pacman.world.MsPacManWorld.APPLE;
import static de.amr.games.pacman.world.MsPacManWorld.BANANA;
import static de.amr.games.pacman.world.MsPacManWorld.CHERRIES;
import static de.amr.games.pacman.world.MsPacManWorld.ORANGE;
import static de.amr.games.pacman.world.MsPacManWorld.PEAR;
import static de.amr.games.pacman.world.MsPacManWorld.PRETZEL;
import static de.amr.games.pacman.world.MsPacManWorld.STRAWBERRY;

import java.awt.Font;
import java.awt.image.BufferedImage;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.Map;

import de.amr.games.pacman.lib.Direction;
import de.amr.games.pacman.ui.api.PacManGameSound;
import de.amr.games.pacman.ui.swing.SoundAssets;

public class MsPacManAssets implements SoundAssets {

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
	public final Map<Integer, BufferedImage> numbers = new HashMap<>();
	public final Map<Integer, BufferedImage> bountyNumbers = new HashMap<>();
	public final Map<PacManGameSound, String> soundPaths = new EnumMap<>(PacManGameSound.class);
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
		symbols[ORANGE]     = section(5, 0);
		symbols[PRETZEL]    = section(6, 0);
		symbols[APPLE]      = section(7, 0);
		symbols[PEAR]       = section(8, 0);
		symbols[BANANA]     = section(9, 0);
	
		numbers.put(100,  section(3, 1));
		numbers.put(200,  section(4, 1));
		numbers.put(500,  section(5, 1));
		numbers.put(700,  section(6, 1));
		numbers.put(1000, section(7, 1));
		numbers.put(2000, section(8, 1));
		numbers.put(5000, section(9, 1));
		
		bountyNumbers.put(200, section(0,8));
		bountyNumbers.put(400, section(1,8));
		bountyNumbers.put(800, section(2,8));
		bountyNumbers.put(1600, section(3,8));
	
		//TODO use Ms. Pac-Man sounds
		soundPaths.put(PacManGameSound.CREDIT,       "/sound/credit.wav");
		soundPaths.put(PacManGameSound.EAT_BONUS,    "/sound/eat_fruit.wav");
		soundPaths.put(PacManGameSound.EXTRA_LIFE,   "/sound/extend.wav");
		soundPaths.put(PacManGameSound.GAME_READY,   "/sound/mspacman/game_start.wav");
		soundPaths.put(PacManGameSound.GHOST_DEATH,  "/sound/eat_ghost.wav");
		soundPaths.put(PacManGameSound.MUNCH,        "/sound/munch_1.wav");
		soundPaths.put(PacManGameSound.PACMAN_DEATH, "/sound/death_1.wav");
		soundPaths.put(PacManGameSound.PACMAN_POWER, "/sound/power_pellet.wav");
		soundPaths.put(PacManGameSound.RETREATING,   "/sound/retreating.wav");
		soundPaths.put(PacManGameSound.SIREN_1,      "/sound/siren_1.wav");
		soundPaths.put(PacManGameSound.SIREN_2,      "/sound/siren_2.wav");
		soundPaths.put(PacManGameSound.SIREN_3,      "/sound/siren_3.wav");
		soundPaths.put(PacManGameSound.SIREN_4,      "/sound/siren_4.wav");
		soundPaths.put(PacManGameSound.SIREN_5,      "/sound/siren_5.wav");
		//@formatter:on
	}

	public BufferedImage section(int x, int y, int w, int h) {
		return spriteSheet.getSubimage(456 + x * 16, y * 16, w * 16, h * 16);
	}

	public BufferedImage section(int x, int y) {
		return section(x, y, 1, 1);
	}

	@Override
	public String getSoundPath(PacManGameSound sound) {
		return soundPaths.get(sound);
	}
}