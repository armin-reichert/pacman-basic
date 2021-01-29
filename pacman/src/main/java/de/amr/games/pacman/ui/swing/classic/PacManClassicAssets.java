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

	public final BufferedImage gameLogo;
	public final BufferedImage mazeFull;
	public final BufferedImage life;
	public final BufferedImage[] symbols;
	public final Map<Integer, BufferedImage> numbers;
	public final BufferedImage pacMouthClosed;
	public final EnumMap<Direction, BufferedImage> pacMouthOpen;
	public final EnumMap<Direction, Animation> pacMunching;
	public final Animation pacCollapsing;
	public final List<EnumMap<Direction, Animation>> ghostWalking;
	public final EnumMap<Direction, BufferedImage> ghostEyes;
	public final Animation ghostBlue;
	public final Animation ghostFlashing;
	public final Animation mazeFlashing;
	public final Map<PacManGameSound, URL> soundURL;
	public final Font scoreFont;

	private final BufferedImage spriteSheet;

	public PacManClassicAssets() {
		//@formatter:off
		gameLogo            = image("/worlds/classic/logo.png");
		spriteSheet         = image("/worlds/classic/sprites.png");
		mazeFull            = image("/worlds/classic/maze_full.png");

		life                = tile(8, 1);

		symbols = new BufferedImage[8];
		symbols[CHERRIES]   = tile(2, 3);
		symbols[STRAWBERRY] = tile(3, 3);
		symbols[PEACH]      = tile(4, 3);
		symbols[APPLE]      = tile(5, 3);
		symbols[GRAPES]     = tile(6, 3);
		symbols[GALAXIAN]   = tile(7, 3);
		symbols[BELL]       = tile(8, 3);
		symbols[KEY]        = tile(9, 3);
	
		numbers = new HashMap<>();
		numbers.put(200,  tile(0, 8));
		numbers.put(400,  tile(1, 8));
		numbers.put(800,  tile(2, 8));
		numbers.put(1600, tile(3, 8));
		
		numbers.put(100,  tile(0, 9));
		numbers.put(300,  tile(1, 9));
		numbers.put(500,  tile(2, 9));
		numbers.put(700,  tile(3, 9));
		
		numbers.put(1000, region(4, 9, 2, 1)); // left-aligned 
		numbers.put(2000, region(3, 10, 3, 1));
		numbers.put(3000, region(3, 11, 3, 1));
		numbers.put(5000, region(3, 12, 3, 1));
	
		soundURL = new EnumMap<>(PacManGameSound.class);
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

		BufferedImage mazeEmptyDark = image("/worlds/classic/maze_empty.png");
		BufferedImage mazeEmptyBright = image("/worlds/classic/maze_empty_white.png");
		mazeFlashing = Animation.of(mazeEmptyBright, mazeEmptyDark).frameDuration(15);

		pacMouthClosed = tile(2, 0);

		pacMouthOpen = new EnumMap<>(Direction.class);
		for (Direction dir : Direction.values()) {
			pacMouthOpen.put(dir, tile(1, index(dir)));
		}

		pacMunching = new EnumMap<>(Direction.class);
		for (Direction dir : Direction.values()) {
			pacMunching.put(dir,
					Animation.of(pacMouthClosed, pacMouthOpen.get(dir), tile(0, index(dir)), pacMouthOpen.get(dir)).endless()
							.frameDuration(1).run());
		}

		pacCollapsing = Animation.of(tile(3, 0), tile(3, 1), tile(3, 2), tile(3, 3), tile(3, 4), tile(3, 5), tile(3, 6),
				tile(3, 7), tile(3, 8), tile(3, 9), tile(3, 10)).frameDuration(8);

		ghostWalking = new ArrayList<>();
		for (int g = 0; g < 4; ++g) {
			EnumMap<Direction, Animation> walkingTo = new EnumMap<>(Direction.class);
			for (Direction dir : Direction.values()) {
				walkingTo.put(dir, Animation.of(tile(2 * index(dir), 4 + g), tile(2 * index(dir) + 1, 4 + g)).frameDuration(10)
						.endless().run());
			}
			ghostWalking.add(walkingTo);
		}

		ghostEyes = new EnumMap<>(Direction.class);
		for (Direction dir : Direction.values()) {
			ghostEyes.put(dir, tile(8 + index(dir), 5));
		}

		ghostBlue = Animation.of(tile(8, 4), tile(9, 4)).frameDuration(20).endless().run();

		ghostFlashing = Animation.of(tile(8, 4), tile(9, 4), tile(10, 4), tile(11, 4)).frameDuration(10).endless().run();
	}

	/** Sprite sheet order of directions. */
	private int index(Direction dir) {
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

	private BufferedImage region(int x, int y, int w, int h) {
		return spriteSheet.getSubimage(x * 16, y * 16, w * 16, h * 16);
	}

	private BufferedImage tile(int x, int y) {
		return region(x, y, 1, 1);
	}
}