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
	public final EnumMap<Direction, Animation> pacWalking;
	public final Animation pacCollapsing;
	public final List<EnumMap<Direction, Animation>> ghostsWalking;
	public final EnumMap<Direction, BufferedImage> ghostEyes;
	public final Animation ghostBlue;
	public final List<Animation> ghostFlashing;

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
		life                = section(1, 0);

		symbols[CHERRIES]   = section(3, 0);
		symbols[STRAWBERRY] = section(4, 0);
		symbols[PEACH]      = section(5, 0);
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
		pacMouthOpen = new EnumMap<>(Direction.class);
		for (Direction direction : Direction.values()) {
			int dir = dirIndex(direction);
			pacMouthClosed.put(direction, section(2, dir));
			pacMouthOpen.put(direction, section(1, dir));
		}

		pacWalking = new EnumMap<>(Direction.class);
		for (Direction direction : Direction.values()) {
			int dir = dirIndex(direction);
			Animation animation = new Animation();
			animation.setFrameDurationTicks(1);
			animation.setRepetitions(Integer.MAX_VALUE);
			animation.start();
			animation.addFrame(section(0, dir));
			animation.addFrame(section(1, dir));
			animation.addFrame(section(2, dir));
			animation.addFrame(section(1, dir));
			pacWalking.put(direction, animation);
		}

		pacCollapsing = new Animation();
		pacCollapsing.setFrameDurationTicks(10);
		pacCollapsing.setRepetitions(2);
		pacCollapsing.addFrame(section(0, 3));
		pacCollapsing.addFrame(section(0, 0));
		pacCollapsing.addFrame(section(0, 1));
		pacCollapsing.addFrame(section(0, 2));

		ghostsWalking = new ArrayList<>();
		for (int ghostID = 0; ghostID < 4; ++ghostID) {
			EnumMap<Direction, Animation> animationForDir = new EnumMap<>(Direction.class);
			for (Direction direction : Direction.values()) {
				int dir = dirIndex(direction);
				Animation animation = new Animation();
				animation.setFrameDurationTicks(4);
				animation.setRepetitions(Integer.MAX_VALUE);
				animation.start();
				animation.addFrame(section(2 * dir, 4 + ghostID));
				animation.addFrame(section(2 * dir + 1, 4 + ghostID));
				animationForDir.put(direction, animation);
			}
			ghostsWalking.add(animationForDir);
		}

		ghostEyes = new EnumMap<>(Direction.class);
		for (Direction direction : Direction.values()) {
			int dir = dirIndex(direction);
			ghostEyes.put(direction, section(8 + dir, 5));
		}

		ghostBlue = new Animation();
		ghostBlue.setFrameDurationTicks(20);
		ghostBlue.setRepetitions(Integer.MAX_VALUE);
		ghostBlue.start();
		ghostBlue.addFrame(section(8, 4));
		ghostBlue.addFrame(section(9, 4));

		ghostFlashing = new ArrayList<>();
		for (int ghostID = 0; ghostID < 4; ++ghostID) {
			Animation animation = new Animation();
			animation.setFrameDurationTicks(10);
			animation.setRepetitions(Integer.MAX_VALUE);
			animation.start();
			animation.addFrame(section(8, 4));
			animation.addFrame(section(9, 4));
			animation.addFrame(section(10, 4));
			animation.addFrame(section(11, 4));
			ghostFlashing.add(animation);
		}
	}

	/** Sprite sheet order of directions. */
	public int dirIndex(Direction direction) {
		switch (direction) {
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

	public BufferedImage section(int x, int y, int w, int h) {
		return spriteSheet.getSubimage(456 + x * 16, y * 16, w * 16, h * 16);
	}

	public BufferedImage section(int x, int y) {
		return section(x, y, 1, 1);
	}
}