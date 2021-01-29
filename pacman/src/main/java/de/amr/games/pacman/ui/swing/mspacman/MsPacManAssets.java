package de.amr.games.pacman.ui.swing.mspacman;

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
import de.amr.games.pacman.ui.swing.Spritesheet;

public class MsPacManAssets extends Spritesheet {

	public final BufferedImage gameLogo;
	public final BufferedImage[] mazeFull;
	public final BufferedImage[] mazeEmptyDark;
	public final BufferedImage[] mazeEmptyBright;
	public final BufferedImage life;
	public final BufferedImage[] symbols;
	public final Map<Integer, BufferedImage> numbers;
	public final Map<Integer, BufferedImage> bountyNumbers;
	public final Map<PacManGameSound, URL> soundURL;
	public final EnumMap<Direction, BufferedImage> pacMouthOpen;
	public final EnumMap<Direction, BufferedImage> pacMouthClosed;
	public final EnumMap<Direction, Animation<BufferedImage>> pacMunching;
	public final Animation<BufferedImage> pacCollapsing;
	public final List<EnumMap<Direction, Animation<BufferedImage>>> ghostsWalking;
	public final EnumMap<Direction, BufferedImage> ghostEyes;
	public final Animation<BufferedImage> ghostBlue;
	public final Animation<BufferedImage> ghostFlashing;
	public final Font scoreFont;

	public MsPacManAssets() {
		super(image("/worlds/mspacman/sprites.png"), 16);

		scoreFont = font("/PressStart2P-Regular.ttf", 8);

		gameLogo = image("/worlds/mspacman/logo.png");

		mazeFull = new BufferedImage[6];
		mazeEmptyDark = new BufferedImage[6];
		mazeEmptyBright = new BufferedImage[6];
		for (int i = 0; i < 6; ++i) {
			mazeFull[i] = section(0, i * 248, 226, 248);
			mazeEmptyDark[i] = section(226, i * 248, 226, 248);
			mazeEmptyBright[i] = null; // TODO fixme
		}

		// Left part of spritesheet contains the 6 mazes, rest is on the right
		setOrigin(456, 0);

		life = tile(1, 0);

		symbols = new BufferedImage[] { tile(3, 0), tile(4, 0), tile(5, 0), tile(6, 0), tile(7, 0), tile(8, 0),
				tile(9, 0) };

		//@formatter:off
		numbers = new HashMap<>();
		numbers.put(100,  tile(3, 1));
		numbers.put(200,  tile(4, 1));
		numbers.put(500,  tile(5, 1));
		numbers.put(700,  tile(6, 1));
		numbers.put(1000, tile(7, 1));
		numbers.put(2000, tile(8, 1));
		numbers.put(5000, tile(9, 1));
		
		bountyNumbers = new HashMap<>();
		bountyNumbers.put(200, tile(0,8));
		bountyNumbers.put(400, tile(1,8));
		bountyNumbers.put(800, tile(2,8));
		bountyNumbers.put(1600, tile(3,8));
	
		soundURL = new EnumMap<>(PacManGameSound.class);
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

		pacMouthOpen = new EnumMap<>(Direction.class);
		for (Direction dir : Direction.values()) {
			pacMouthOpen.put(dir, tile(1, index(dir)));
		}

		pacMouthClosed = new EnumMap<>(Direction.class);
		for (Direction dir : Direction.values()) {
			pacMouthClosed.put(dir, tile(2, index(dir)));
		}

		pacMunching = new EnumMap<>(Direction.class);
		for (Direction dir : Direction.values()) {
			int d = index(dir);
			Animation<BufferedImage> animation = Animation.of(tile(0, d), tile(1, d), tile(2, d), tile(1, d));
			animation.frameDuration(2).endless().run();
			pacMunching.put(dir, animation);
		}

		pacCollapsing = Animation.of(tile(0, 2), tile(0, 3), tile(0, 0), tile(0, 1), tile(0, 2));
		pacCollapsing.frameDuration(10).repetitions(2);

		ghostsWalking = new ArrayList<>();
		for (int g = 0; g < 4; ++g) {
			EnumMap<Direction, Animation<BufferedImage>> animationForDir = new EnumMap<>(Direction.class);
			for (Direction dir : Direction.values()) {
				int d = index(dir);
				Animation<BufferedImage> animation = Animation.of(tile(2 * d, 4 + g), tile(2 * d + 1, 4 + g));
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

		ghostFlashing = Animation.of(tile(8, 4), tile(9, 4), tile(10, 4), tile(11, 4)).frameDuration(10).endless().run();
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
}