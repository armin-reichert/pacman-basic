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
	public final List<EnumMap<Direction, Animation>> ghostWalking;
	public final EnumMap<Direction, BufferedImage> ghostEyes;
	public final Animation ghostBlue;
	public final Animation ghostFlashing;
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

		life                = sprite(8, 1);

		symbols[CHERRIES]   = sprite(2, 3);
		symbols[STRAWBERRY] = sprite(3, 3);
		symbols[PEACH]      = sprite(4, 3);
		symbols[APPLE]      = sprite(5, 3);
		symbols[GRAPES]     = sprite(6, 3);
		symbols[GALAXIAN]   = sprite(7, 3);
		symbols[BELL]       = sprite(8, 3);
		symbols[KEY]        = sprite(9, 3);
	
		numbers.put(200,  sprite(0, 8));
		numbers.put(400,  sprite(1, 8));
		numbers.put(800,  sprite(2, 8));
		numbers.put(1600, sprite(3, 8));
		
		numbers.put(100,  sprite(0, 9));
		numbers.put(300,  sprite(1, 9));
		numbers.put(500,  sprite(2, 9));
		numbers.put(700,  sprite(3, 9));
		
		numbers.put(1000, region(4, 9, 2, 1)); // left-aligned 
		numbers.put(2000, region(3, 10, 3, 1));
		numbers.put(3000, region(3, 11, 3, 1));
		numbers.put(5000, region(3, 12, 3, 1));
	
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

		pacMouthClosed = sprite(2, 0);

		pacMouthOpen = new EnumMap<>(Direction.class);
		for (Direction dir : Direction.values()) {
			pacMouthOpen.put(dir, sprite(1, index(dir)));
		}

		pacWalking = new EnumMap<>(Direction.class);
		for (Direction dir : Direction.values()) {
			pacWalking.put(dir,
					Animation.of(pacMouthClosed, pacMouthOpen.get(dir), sprite(0, index(dir)), pacMouthOpen.get(dir)).endless()
							.frameDuration(1).run());
		}

		pacCollapsing = Animation.of().frameDuration(8);
		for (int i = 0; i < 11; ++i) {
			pacCollapsing.add(sprite(3 + i, 0));
		}

		ghostWalking = new ArrayList<>();
		for (int ghostID = 0; ghostID < 4; ++ghostID) {
			EnumMap<Direction, Animation> walkingTo = new EnumMap<>(Direction.class);
			for (Direction dir : Direction.values()) {
				walkingTo.put(dir, Animation.of(sprite(2 * index(dir), 4 + ghostID), sprite(2 * index(dir) + 1, 4 + ghostID))
						.frameDuration(10).endless().run());
			}
			ghostWalking.add(walkingTo);
		}

		ghostEyes = new EnumMap<>(Direction.class);
		for (Direction dir : Direction.values()) {
			ghostEyes.put(dir, sprite(8 + index(dir), 5));
		}

		ghostBlue = Animation.of(sprite(8, 4), sprite(9, 4)).frameDuration(20).endless().run();

		ghostFlashing = Animation.of(sprite(8, 4), sprite(9, 4), sprite(10, 4), sprite(11, 4)).frameDuration(10).endless()
				.run();

		mazeFlashing = Animation.of(mazeEmptyBright, mazeEmptyDark).frameDuration(15);

	}

	/** Sprite sheet order of directions. */
	private int index(Direction direction) {
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

	private BufferedImage region(int x, int y, int w, int h) {
		return spriteSheet.getSubimage(x * 16, y * 16, w * 16, h * 16);
	}

	private BufferedImage sprite(int x, int y) {
		return region(x, y, 1, 1);
	}
}