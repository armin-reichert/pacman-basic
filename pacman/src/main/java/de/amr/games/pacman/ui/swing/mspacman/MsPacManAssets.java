package de.amr.games.pacman.ui.swing.mspacman;

import static de.amr.games.pacman.game.worlds.MsPacManWorld.APPLE;
import static de.amr.games.pacman.game.worlds.MsPacManWorld.BANANA;
import static de.amr.games.pacman.game.worlds.MsPacManWorld.CHERRIES;
import static de.amr.games.pacman.game.worlds.MsPacManWorld.PEACH;
import static de.amr.games.pacman.game.worlds.MsPacManWorld.PEAR;
import static de.amr.games.pacman.game.worlds.MsPacManWorld.PRETZEL;
import static de.amr.games.pacman.game.worlds.MsPacManWorld.STRAWBERRY;
import static de.amr.games.pacman.ui.swing.PacManGameSwingUI.font;
import static de.amr.games.pacman.ui.swing.PacManGameSwingUI.image;
import static de.amr.games.pacman.ui.swing.PacManGameSwingUI.url;

import java.awt.Font;
import java.awt.image.BufferedImage;
import java.net.URL;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import de.amr.games.pacman.lib.Direction;
import de.amr.games.pacman.ui.api.PacManGameSound;
import de.amr.games.pacman.ui.swing.Animation;

public class MsPacManAssets {

	public final BufferedImage gameLogo;
	public final BufferedImage spriteSheet;
	public final BufferedImage[] mazeFull = new BufferedImage[6];
	public final BufferedImage[] mazeEmptyDark = new BufferedImage[6];
	public final BufferedImage[] mazeEmptyBright = new BufferedImage[6];
	public final BufferedImage life;
	public final BufferedImage[] symbols = new BufferedImage[8];
	public final Map<Integer, BufferedImage> numbers = new HashMap<>();
	public final Map<Integer, BufferedImage> bountyNumbers = new HashMap<>();
	public final Map<PacManGameSound, URL> soundURL = new EnumMap<>(PacManGameSound.class);
	public final Font scoreFont;

	public final EnumMap<Direction, BufferedImage> pacMouthClosed;
	public final EnumMap<Direction, BufferedImage> pacMouthOpen;
	public final EnumMap<Direction, Animation> pacMunching;
	public final Animation pacCollapsing;
	public final List<EnumMap<Direction, Animation>> ghostsWalking;
	public final EnumMap<Direction, BufferedImage> ghostEyes;
	public final Animation ghostBlue;
	public final Animation ghostFlashing;

	public MsPacManAssets() {
		//@formatter:off
		gameLogo            = image("/worlds/mspacman/logo.png");
		spriteSheet         = image("/worlds/mspacman/sprites.png");
		
		for (int i = 0; i < 6; ++i) {
			mazeFull[i]         = spriteSheet.getSubimage(0, i*248, 226, 248);
			mazeEmptyDark[i]    = spriteSheet.getSubimage(226, i*248, 226, 248);
			mazeEmptyBright[i]  = null; //TODO fixme
		}
		scoreFont           = font("/PressStart2P-Regular.ttf", 8);
		life                = tile(1, 0);

		symbols[CHERRIES]   = tile(3, 0);
		symbols[STRAWBERRY] = tile(4, 0);
		symbols[PEACH]      = tile(5, 0);
		symbols[PRETZEL]    = tile(6, 0);
		symbols[APPLE]      = tile(7, 0);
		symbols[PEAR]       = tile(8, 0);
		symbols[BANANA]     = tile(9, 0);
	
		numbers.put(100,  tile(3, 1));
		numbers.put(200,  tile(4, 1));
		numbers.put(500,  tile(5, 1));
		numbers.put(700,  tile(6, 1));
		numbers.put(1000, tile(7, 1));
		numbers.put(2000, tile(8, 1));
		numbers.put(5000, tile(9, 1));
		
		bountyNumbers.put(200, tile(0,8));
		bountyNumbers.put(400, tile(1,8));
		bountyNumbers.put(800, tile(2,8));
		bountyNumbers.put(1600, tile(3,8));
	
		soundURL.put(PacManGameSound.CREDIT,           url("/sound/mspacman/Coin Credit.wav"));
		soundURL.put(PacManGameSound.EXTRA_LIFE,       url("/sound/mspacman/Extra Life.wav"));
		soundURL.put(PacManGameSound.GAME_READY,       url("/sound/mspacman/Start.wav"));
		soundURL.put(PacManGameSound.PACMAN_EAT_BONUS, url("/sound/mspacman/Fruit.wav"));
		soundURL.put(PacManGameSound.PACMAN_MUNCH,     url("/sound/mspacman/Ms. Pac Man Pill.wav"));
		soundURL.put(PacManGameSound.PACMAN_DEATH,     url("/sound/mspacman/Died.wav"));
		soundURL.put(PacManGameSound.PACMAN_POWER,     url("/sound/mspacman/Scared Ghost.wav"));
		soundURL.put(PacManGameSound.GHOST_EATEN,      url("/sound/mspacman/Ghost.wav"));
		soundURL.put(PacManGameSound.GHOST_EYES,       url("/sound/mspacman/Ghost Eyes.wav"));
		soundURL.put(PacManGameSound.GHOST_SIREN_1,    url("/sound/mspacman/Ghost Noise.wav"));
		soundURL.put(PacManGameSound.GHOST_SIREN_2,    url("/sound/mspacman/Ghost Noise 1.wav"));
		soundURL.put(PacManGameSound.GHOST_SIREN_3,    url("/sound/mspacman/Ghost Noise 2.wav"));
		soundURL.put(PacManGameSound.GHOST_SIREN_4,    url("/sound/mspacman/Ghost Noise 3.wav"));
		soundURL.put(PacManGameSound.GHOST_SIREN_5,    url("/sound/mspacman/Ghost Noise 4.wav"));
		//@formatter:on

		pacMouthClosed = new EnumMap<>(Direction.class);
		for (Direction dir : Direction.values()) {
			pacMouthClosed.put(dir, tile(2, index(dir)));
		}

		pacMouthOpen = new EnumMap<>(Direction.class);
		for (Direction dir : Direction.values()) {
			pacMouthOpen.put(dir, tile(1, index(dir)));
		}

		pacMunching = new EnumMap<>(Direction.class);
		for (Direction dir : Direction.values()) {
			int d = index(dir);
			Animation animation = Animation.of(tile(0, d), tile(1, d), tile(2, d), tile(1, d));
			animation.frameDuration(2).endless().run();
			pacMunching.put(dir, animation);
		}

		pacCollapsing = Animation.of(tile(0, 2), tile(0, 3), tile(0, 0), tile(0, 1), tile(0, 2));
		pacCollapsing.frameDuration(10).repetitions(2);

		ghostsWalking = new ArrayList<>();
		for (int ghostID = 0; ghostID < 4; ++ghostID) {
			EnumMap<Direction, Animation> animationForDir = new EnumMap<>(Direction.class);
			for (Direction dir : Direction.values()) {
				int d = index(dir);
				Animation animation = Animation.of(tile(2 * d, 4 + ghostID), tile(2 * d + 1, 4 + ghostID));
				animation.frameDuration(4).endless().run();
				animationForDir.put(dir, animation);
			}
			ghostsWalking.add(animationForDir);
		}

		ghostEyes = new EnumMap<>(Direction.class);
		for (Direction dir : Direction.values()) {
			ghostEyes.put(dir, tile(8 + index(dir), 5));
		}

		ghostBlue = Animation.of(tile(8, 4), tile(9, 4)).frameDuration(20).endless().run();

		ghostFlashing = Animation.of(tile(8, 4), tile(9, 4), tile(10, 4), tile(11, 4)).frameDuration(10).endless()
				.run();
	}

	/** Sprite sheet order of directions. */
	public int index(Direction dir) {
		switch (dir) {
		case RIGHT:
			return 0;
		case LEFT:
			return 1;
		case UP:
			return 2;
		case DOWN:
			return 3;
		default:
			return -1;
		}
	}

	public BufferedImage region(int x, int y, int w, int h) {
		return spriteSheet.getSubimage(456 + x * 16, y * 16, w * 16, h * 16);
	}

	public BufferedImage tile(int x, int y) {
		return region(x, y, 1, 1);
	}
}