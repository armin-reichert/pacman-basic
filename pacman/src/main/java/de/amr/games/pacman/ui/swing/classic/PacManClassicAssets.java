package de.amr.games.pacman.ui.swing.classic;

import static de.amr.games.pacman.game.worlds.PacManClassicWorld.APPLE;
import static de.amr.games.pacman.game.worlds.PacManClassicWorld.BELL;
import static de.amr.games.pacman.game.worlds.PacManClassicWorld.CHERRIES;
import static de.amr.games.pacman.game.worlds.PacManClassicWorld.GALAXIAN;
import static de.amr.games.pacman.game.worlds.PacManClassicWorld.GRAPES;
import static de.amr.games.pacman.game.worlds.PacManClassicWorld.KEY;
import static de.amr.games.pacman.game.worlds.PacManClassicWorld.PEACH;
import static de.amr.games.pacman.game.worlds.PacManClassicWorld.STRAWBERRY;
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

/**
 * Assets used in Pac-Man game.
 * 
 * @author Armin Reichert
 */
public class PacManClassicAssets {

	public final BufferedImage spriteSheet;
	public final BufferedImage gameLogo;
	public final BufferedImage mazeFull;
	public final BufferedImage mazeEmptyDark;
	public final BufferedImage mazeEmptyBright;
	public final BufferedImage life;
	public final BufferedImage[] symbols = new BufferedImage[8];
	public final Map<Integer, BufferedImage> numbers = new HashMap<>();
	public final BufferedImage pacMouthClosed;
	public final EnumMap<Direction, BufferedImage> pacMouthOpen;
	public final EnumMap<Direction, Animation> pacWalking;
	public final Animation pacCollapsing;
	public final List<EnumMap<Direction, Animation>> ghostsWalking;
	public final EnumMap<Direction, BufferedImage> ghostEyes;
	public final Animation ghostBlue;
	public final List<Animation> ghostFlashing;
	public final Animation mazeFlashing;

	public final Map<PacManGameSound, URL> soundURL = new EnumMap<>(PacManGameSound.class);
	public final Font scoreFont;

	public PacManClassicAssets() {
		//@formatter:off
		gameLogo            = image("/worlds/classic/logo.png");
		spriteSheet         = image("/worlds/classic/sprites.png");
		mazeFull            = image("/worlds/classic/maze_full.png");
		mazeEmptyDark       = image("/worlds/classic/maze_empty.png");
		mazeEmptyBright     = image("/worlds/classic/maze_empty_white.png");

		life                = section(8, 1);

		symbols[CHERRIES]   = section(2, 3);
		symbols[STRAWBERRY] = section(3, 3);
		symbols[PEACH]      = section(4, 3);
		symbols[APPLE]      = section(5, 3);
		symbols[GRAPES]     = section(6, 3);
		symbols[GALAXIAN]   = section(7, 3);
		symbols[BELL]       = section(8, 3);
		symbols[KEY]        = section(9, 3);
	
		numbers.put(200,  section(0, 8));
		numbers.put(400,  section(1, 8));
		numbers.put(800,  section(2, 8));
		numbers.put(1600, section(3, 8));
		
		numbers.put(100,  section(0, 9));
		numbers.put(300,  section(1, 9));
		numbers.put(500,  section(2, 9));
		numbers.put(700,  section(3, 9));
		
		numbers.put(1000, section(4, 9, 2, 1)); // left-aligned 
		
		numbers.put(2000, section(3, 10, 3, 1));
		numbers.put(3000, section(3, 11, 3, 1));
		numbers.put(5000, section(3, 12, 3, 1));
	
		soundURL.put(PacManGameSound.CREDIT,           url("/sound/classic/credit.wav"));
		soundURL.put(PacManGameSound.EXTRA_LIFE,       url("/sound/classic/extend.wav"));
		soundURL.put(PacManGameSound.GAME_READY,       url("/sound/classic/game_start.wav"));
		soundURL.put(PacManGameSound.PACMAN_EAT_BONUS, url("/sound/classic/eat_fruit.wav"));
		soundURL.put(PacManGameSound.PACMAN_MUNCH,     url("/sound/classic/munch_1.wav"));
		soundURL.put(PacManGameSound.PACMAN_DEATH,     url("/sound/classic/death_1.wav"));
		soundURL.put(PacManGameSound.PACMAN_POWER,     url("/sound/classic/power_pellet.wav"));
		soundURL.put(PacManGameSound.GHOST_EATEN,      url("/sound/classic/eat_ghost.wav"));
		soundURL.put(PacManGameSound.GHOST_EYES,       url("/sound/classic/retreating.wav"));
		soundURL.put(PacManGameSound.GHOST_SIREN_1,    url("/sound/classic/siren_1.wav"));
		soundURL.put(PacManGameSound.GHOST_SIREN_2,    url("/sound/classic/siren_2.wav"));
		soundURL.put(PacManGameSound.GHOST_SIREN_3,    url("/sound/classic/siren_3.wav"));
		soundURL.put(PacManGameSound.GHOST_SIREN_4,    url("/sound/classic/siren_4.wav"));
		soundURL.put(PacManGameSound.GHOST_SIREN_5,    url("/sound/classic/siren_5.wav"));
		//@formatter:on

		scoreFont = font("/PressStart2P-Regular.ttf", 8);

		pacMouthClosed = section(2, 0);
		pacMouthOpen = new EnumMap<>(Direction.class);
		for (Direction direction : Direction.values()) {
			int dir = dirIndex(direction);
			pacMouthOpen.put(direction, section(1, dir));
		}

		pacWalking = new EnumMap<>(Direction.class);
		for (Direction direction : Direction.values()) {
			int dir = dirIndex(direction);
			Animation animation = new Animation();
			animation.setFrameDurationTicks(1);
			animation.setRepetitions(Integer.MAX_VALUE);
			animation.start();
			animation.addFrame(pacMouthClosed);
			animation.addFrame(section(1, dir));
			animation.addFrame(section(0, dir));
			animation.addFrame(section(1, dir));
			pacWalking.put(direction, animation);
		}

		pacCollapsing = new Animation();
		pacCollapsing.setFrameDurationTicks(8);
		for (int i = 0; i < 11; ++i) {
			pacCollapsing.addFrame(section(3 + i, 0));
		}

		ghostsWalking = new ArrayList<>();
		for (int ghostID = 0; ghostID < 4; ++ghostID) {
			EnumMap<Direction, Animation> animationForDir = new EnumMap<>(Direction.class);
			for (Direction direction : Direction.values()) {
				int dir = dirIndex(direction);
				Animation animation = new Animation();
				animation.setFrameDurationTicks(10);
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

		mazeFlashing = new Animation();
		mazeFlashing.setFrameDurationTicks(15);
		mazeFlashing.addFrame(mazeEmptyBright);
		mazeFlashing.addFrame(mazeEmptyDark);
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
		return spriteSheet.getSubimage(x * 16, y * 16, w * 16, h * 16);
	}

	public BufferedImage section(int x, int y) {
		return section(x, y, 1, 1);
	}
}