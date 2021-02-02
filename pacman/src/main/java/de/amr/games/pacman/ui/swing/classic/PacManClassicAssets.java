package de.amr.games.pacman.ui.swing.classic;

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

import de.amr.games.pacman.lib.Animation;
import de.amr.games.pacman.lib.Direction;
import de.amr.games.pacman.lib.V2i;
import de.amr.games.pacman.ui.sound.PacManGameSound;
import de.amr.games.pacman.ui.swing.Spritesheet;

/**
 * Assets used in Pac-Man game.
 * 
 * <p>
 * Just for testing, some animations or maps just store the sprite coordinates and the subimage gets
 * created every time the animation frame or image is rendered. This does not really make sense if
 * the subimage object has to be created anyway but could be useful if there was a way to draw the
 * corresponding section from the spritesheet image without having to create a subimage object.
 * 
 * @author Armin Reichert
 */
public class PacManClassicAssets extends Spritesheet {

	/** Sprite sheet order of directions. */
	private static int index(Direction dir) {
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

	public final BufferedImage gameLogo;
	public final BufferedImage mazeFull;
	public final BufferedImage life;
	public final V2i[] symbolSpriteLocation;
	public final Map<Integer, BufferedImage> numbers;
	public final BufferedImage pacMouthClosed;
	public final EnumMap<Direction, BufferedImage> pacMouthOpen;
	public final EnumMap<Direction, Animation<BufferedImage>> pacMunching;
	public final Animation<BufferedImage> pacCollapsing;
	public final List<EnumMap<Direction, Animation<BufferedImage>>> ghostsWalking;
	public final EnumMap<Direction, BufferedImage> ghostEyes;
	public final Animation<BufferedImage> ghostBlue;
	public final List<Animation<BufferedImage>> ghostsFlashing;
	public final Animation<BufferedImage> mazeFlashing;
	public final Animation<Boolean> energizerBlinking;

	public final Map<PacManGameSound, URL> soundURL;
	public final Font scoreFont;

	public PacManClassicAssets() {
		super(image("/worlds/classic/sprites.png"), 16);

		gameLogo = image("/worlds/classic/logo.png");
		mazeFull = image("/worlds/classic/maze_full.png");

		life = spriteAt(8, 1);

		symbolSpriteLocation = new V2i[] { v2(2, 3), v2(3, 3), v2(4, 3), v2(5, 3), v2(6, 3), v2(7, 3), v2(8, 3), v2(9, 3) };

		//@formatter:off
		numbers = new HashMap<>();
		numbers.put(200,  spriteAt(0, 8));
		numbers.put(400,  spriteAt(1, 8));
		numbers.put(800,  spriteAt(2, 8));
		numbers.put(1600, spriteAt(3, 8));
		
		numbers.put(100,  spriteAt(0, 9));
		numbers.put(300,  spriteAt(1, 9));
		numbers.put(500,  spriteAt(2, 9));
		numbers.put(700,  spriteAt(3, 9));
		
		numbers.put(1000, spritesAt(4, 9, 2, 1)); // left-aligned 
		numbers.put(2000, spritesAt(3, 10, 3, 1));
		numbers.put(3000, spritesAt(3, 11, 3, 1));
		numbers.put(5000, spritesAt(3, 12, 3, 1));
		//@formatter:on

		scoreFont = font("/PressStart2P-Regular.ttf", 8);

		// Animations

		BufferedImage mazeEmptyDark = image("/worlds/classic/maze_empty.png");
		BufferedImage mazeEmptyBright = image("/worlds/classic/maze_empty_white.png");
		mazeFlashing = Animation.of(mazeEmptyBright, mazeEmptyDark).frameDuration(15);

		energizerBlinking = Animation.of(true, false);
		energizerBlinking.frameDuration(10).endless().run();

		pacMouthClosed = spriteAt(2, 0);

		pacMouthOpen = new EnumMap<>(Direction.class);
		for (Direction dir : Direction.values()) {
			pacMouthOpen.put(dir, spriteAt(1, index(dir)));
		}

		pacMunching = new EnumMap<>(Direction.class);
		for (Direction dir : Direction.values()) {
			Animation<BufferedImage> animation = Animation.of(pacMouthClosed, pacMouthOpen.get(dir), spriteAt(0, index(dir)),
					pacMouthOpen.get(dir));
			animation.endless().frameDuration(2).run();
			pacMunching.put(dir, animation);
		}

		pacCollapsing = Animation.of(spriteAt(3, 0), spriteAt(4, 0), spriteAt(5, 0), spriteAt(6, 0), spriteAt(7, 0),
				spriteAt(8, 0), spriteAt(9, 0), spriteAt(10, 0), spriteAt(11, 0), spriteAt(12, 0), spriteAt(13, 0));
		pacCollapsing.frameDuration(8);

		ghostsWalking = new ArrayList<>(4);
		for (int g = 0; g < 4; ++g) {
			EnumMap<Direction, Animation<BufferedImage>> walkingTo = new EnumMap<>(Direction.class);
			for (Direction dir : Direction.values()) {
				Animation<BufferedImage> animation = Animation.of(spriteAt(2 * index(dir), 4 + g),
						spriteAt(2 * index(dir) + 1, 4 + g));
				animation.frameDuration(10).endless().run();
				walkingTo.put(dir, animation);
			}
			ghostsWalking.add(walkingTo);
		}

		ghostEyes = new EnumMap<>(Direction.class);
		for (Direction dir : Direction.values()) {
			ghostEyes.put(dir, spriteAt(8 + index(dir), 5));
		}

		ghostBlue = Animation.of(spriteAt(8, 4), spriteAt(9, 4));
		ghostBlue.frameDuration(20).endless().run();

		ghostsFlashing = new ArrayList<>(4);
		for (int g = 0; g < 4; ++g) {
			ghostsFlashing
					.add(Animation.of(spriteAt(8, 4), spriteAt(9, 4), spriteAt(10, 4), spriteAt(11, 4)).frameDuration(5));
		}

		//@formatter:off
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
	}
}