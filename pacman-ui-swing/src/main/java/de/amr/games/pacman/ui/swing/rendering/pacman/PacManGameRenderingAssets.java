package de.amr.games.pacman.ui.swing.rendering.pacman;

import static de.amr.games.pacman.lib.Direction.DOWN;
import static de.amr.games.pacman.lib.Direction.LEFT;
import static de.amr.games.pacman.lib.Direction.RIGHT;
import static de.amr.games.pacman.lib.Direction.UP;
import static de.amr.games.pacman.ui.swing.assets.AssetLoader.font;
import static de.amr.games.pacman.ui.swing.assets.AssetLoader.image;
import static java.util.Map.entry;

import java.awt.Color;
import java.awt.Font;
import java.awt.image.BufferedImage;
import java.util.Arrays;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

import de.amr.games.pacman.lib.Direction;
import de.amr.games.pacman.lib.TimedSequence;
import de.amr.games.pacman.ui.swing.assets.Spritesheet;

/**
 * Assets used in Pac-Man game.
 * 
 * @author Armin Reichert
 */
public class PacManGameRenderingAssets extends Spritesheet {

	/** Sprite sheet order of directions. */
	static final List<Direction> order = Arrays.asList(RIGHT, LEFT, UP, DOWN);

	private static int index(Direction dir) {
		return order.indexOf(dir);
	}

	public static Color ghostColor(int ghostType) {
		return ghostType == 0 ? Color.RED : ghostType == 1 ? Color.pink : ghostType == 2 ? Color.CYAN : Color.ORANGE;
	}

	public final Font scoreFont;

	public final BufferedImage mazeFullImage;
	public final BufferedImage mazeEmptyImage;
	public final TimedSequence<BufferedImage> mazeFlashingAnim;
	public final Map<String, BufferedImage> symbolSprites;
	public final Map<Integer, BufferedImage> numberSprites;
	public final TimedSequence<BufferedImage> bigPacManAnim;
	public final TimedSequence<BufferedImage> blinkyHalfNaked;
	public final TimedSequence<BufferedImage> blinkyPatched;
	public final BufferedImage nailSprite;

	public PacManGameRenderingAssets() {
		super(image("/pacman/graphics/sprites.png"), 16);

		scoreFont = font("/emulogic.ttf", 8);

		// Sprites and images

		mazeFullImage = image("/pacman/graphics/maze_full.png");
		mazeEmptyImage = image("/pacman/graphics/maze_empty.png");

		//@formatter:off
		symbolSprites = Map.of(
				"CHERRIES", 	sprite(2, 3),
				"STRAWBERRY", sprite(3, 3),
				"PEACH",			sprite(4, 3),
				"APPLE",			sprite(5, 3),
				"GRAPES",			sprite(6, 3),
				"GALAXIAN",		sprite(7, 3),
				"BELL",				sprite(8, 3),
				"KEY",				sprite(9, 3)
		);

		numberSprites = Map.ofEntries(
			entry(200,  sprite(0, 8)),
			entry(400,  sprite(1, 8)),
			entry(800,  sprite(2, 8)),
			entry(1600, sprite(3, 8)),
			
			entry(100,  sprite(0, 9)),
			entry(300,  sprite(1, 9)),
			entry(500,  sprite(2, 9)),
			entry(700,  sprite(3, 9)),
			
			entry(1000, spriteRegion(4, 9, 2, 1)), // left-aligned
			entry(2000, spriteRegion(3, 10, 3, 1)),
			entry(3000, spriteRegion(3, 11, 3, 1)),
			entry(5000, spriteRegion(3, 12, 3, 1))
		);
		//@formatter:on

		// Animations

		BufferedImage mazeEmptyDarkImage = image("/pacman/graphics/maze_empty.png");
		BufferedImage mazeEmptyBrightImage = createBrightEffect(mazeEmptyDarkImage, new Color(33, 33, 255), Color.BLACK);
		mazeFlashingAnim = TimedSequence.of(mazeEmptyBrightImage, mazeEmptyDarkImage).frameDuration(15);

		bigPacManAnim = TimedSequence.of(spriteRegion(2, 1, 2, 2), spriteRegion(4, 1, 2, 2), spriteRegion(6, 1, 2, 2))
				.frameDuration(4).endless().run();

		blinkyPatched = TimedSequence.of(sprite(10, 7), sprite(11, 7)).restart().frameDuration(4).endless();
		blinkyHalfNaked = TimedSequence.of(spriteRegion(8, 8, 2, 1), spriteRegion(10, 8, 2, 1)).endless().frameDuration(4)
				.restart();

		nailSprite = sprite(8, 6);
	}

	public Font getScoreFont() {
		return scoreFont;
	}

	public BufferedImage ghostImageByGhostByDir(int ghostID, Direction dir) {
		return sprite(2 * index(dir), 4 + ghostID);
	}

	public TimedSequence<BufferedImage> createPlayerDyingAnimation() {
		return TimedSequence.of(//
				sprite(3, 0), sprite(4, 0), sprite(5, 0), sprite(6, 0), sprite(7, 0), //
				sprite(8, 0), sprite(9, 0), sprite(10, 0), sprite(11, 0), sprite(12, 0), sprite(13, 0))//
				.frameDuration(8);
	}

	public Map<Direction, TimedSequence<BufferedImage>> createPlayerMunchingAnimations() {
		EnumMap<Direction, TimedSequence<BufferedImage>> munching = new EnumMap<>(Direction.class);
		for (Direction dir : Direction.values()) {
			TimedSequence<BufferedImage> animation = TimedSequence.of(sprite(2, 0), sprite(1, index(dir)),
					sprite(0, index(dir)), sprite(1, index(dir)));
			animation.frameDuration(2).endless().run();
			munching.put(dir, animation);
		}
		return munching;
	}

	public Map<Direction, TimedSequence<BufferedImage>> createGhostKickingAnimations(int ghostID) {
		EnumMap<Direction, TimedSequence<BufferedImage>> walkingTo = new EnumMap<>(Direction.class);
		for (Direction dir : Direction.values()) {
			TimedSequence<BufferedImage> anim = TimedSequence.of(sprite(2 * index(dir), 4 + ghostID),
					sprite(2 * index(dir) + 1, 4 + ghostID));
			anim.frameDuration(10).endless();
			walkingTo.put(dir, anim);
		}
		return walkingTo;
	}

	public TimedSequence<BufferedImage> createGhostFrightenedAnimation() {
		TimedSequence<BufferedImage> animation = TimedSequence.of(sprite(8, 4), sprite(9, 4));
		animation.frameDuration(20).endless();
		return animation;
	}

	public TimedSequence<BufferedImage> createGhostFlashingAnimation() {
		return TimedSequence.of(sprite(8, 4), sprite(9, 4), sprite(10, 4), sprite(11, 4)).frameDuration(4);
	}

	public Map<Direction, TimedSequence<BufferedImage>> createGhostsReturningHomeAnimations() {
		Map<Direction, TimedSequence<BufferedImage>> ghostEyesAnimsByDir = new EnumMap<>(Direction.class);
		for (Direction dir : Direction.values()) {
			ghostEyesAnimsByDir.put(dir, TimedSequence.of(sprite(8 + index(dir), 5)));
		}
		return ghostEyesAnimsByDir;
	}

	public TimedSequence<BufferedImage> createBlinkyStretchedAnimation() {
		return TimedSequence.of(sprite(9, 6), sprite(10, 6), sprite(11, 6), sprite(12, 6));
	}

	public TimedSequence<BufferedImage> createBlinkyDamagedAnimation() {
		return TimedSequence.of(sprite(8, 7), sprite(9, 7));
	}
}