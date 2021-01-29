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
		life                = sprite(1, 0);

		symbols[CHERRIES]   = sprite(3, 0);
		symbols[STRAWBERRY] = sprite(4, 0);
		symbols[PEACH]      = sprite(5, 0);
		symbols[PRETZEL]    = sprite(6, 0);
		symbols[APPLE]      = sprite(7, 0);
		symbols[PEAR]       = sprite(8, 0);
		symbols[BANANA]     = sprite(9, 0);
	
		numbers.put(100,  sprite(3, 1));
		numbers.put(200,  sprite(4, 1));
		numbers.put(500,  sprite(5, 1));
		numbers.put(700,  sprite(6, 1));
		numbers.put(1000, sprite(7, 1));
		numbers.put(2000, sprite(8, 1));
		numbers.put(5000, sprite(9, 1));
		
		bountyNumbers.put(200, sprite(0,8));
		bountyNumbers.put(400, sprite(1,8));
		bountyNumbers.put(800, sprite(2,8));
		bountyNumbers.put(1600, sprite(3,8));
	
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
			pacMouthClosed.put(dir, sprite(2, index(dir)));
		}

		pacMouthOpen = new EnumMap<>(Direction.class);
		for (Direction dir : Direction.values()) {
			pacMouthOpen.put(dir, sprite(1, index(dir)));
		}

		pacWalking = new EnumMap<>(Direction.class);
		for (Direction dir : Direction.values()) {
			int dirIndex = index(dir);
			Animation animation = Animation.of(sprite(0, dirIndex), sprite(1, dirIndex), sprite(2, dirIndex),
					sprite(1, dirIndex));
			animation.frameDuration(1).endless().run();
			pacWalking.put(dir, animation);
		}

		pacCollapsing = Animation.of(sprite(0, 2), sprite(0, 3), sprite(0, 0), sprite(0, 1), sprite(0, 2));
		pacCollapsing.frameDuration(10).repetitions(2);

		ghostsWalking = new ArrayList<>();
		for (int ghostID = 0; ghostID < 4; ++ghostID) {
			EnumMap<Direction, Animation> animationForDir = new EnumMap<>(Direction.class);
			for (Direction direction : Direction.values()) {
				int dir = index(direction);
				Animation animation = Animation.of(sprite(2 * dir, 4 + ghostID), sprite(2 * dir + 1, 4 + ghostID));
				animation.frameDuration(4).endless().run();
				animationForDir.put(direction, animation);
			}
			ghostsWalking.add(animationForDir);
		}

		ghostEyes = new EnumMap<>(Direction.class);
		for (Direction direction : Direction.values()) {
			int dir = index(direction);
			ghostEyes.put(direction, sprite(8 + dir, 5));
		}

		ghostBlue = Animation.of(sprite(8, 4), sprite(9, 4)).frameDuration(20).endless().run();

		ghostFlashing = Animation.of(sprite(8, 4), sprite(9, 4), sprite(10, 4), sprite(11, 4)).frameDuration(10)
				.endless().run();
	}

	/** Sprite sheet order of directions. */
	public int index(Direction direction) {
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

	public BufferedImage region(int x, int y, int w, int h) {
		return spriteSheet.getSubimage(456 + x * 16, y * 16, w * 16, h * 16);
	}

	public BufferedImage sprite(int x, int y) {
		return region(x, y, 1, 1);
	}
}