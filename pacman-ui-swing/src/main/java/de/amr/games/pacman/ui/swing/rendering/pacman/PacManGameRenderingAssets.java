package de.amr.games.pacman.ui.swing.rendering.pacman;

import static de.amr.games.pacman.lib.Direction.DOWN;
import static de.amr.games.pacman.lib.Direction.LEFT;
import static de.amr.games.pacman.lib.Direction.RIGHT;
import static de.amr.games.pacman.lib.Direction.UP;
import static de.amr.games.pacman.ui.swing.assets.AssetLoader.font;
import static de.amr.games.pacman.ui.swing.assets.AssetLoader.image;

import java.awt.Color;
import java.awt.Font;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import de.amr.games.pacman.lib.Direction;
import de.amr.games.pacman.model.common.Ghost;
import de.amr.games.pacman.ui.animation.TimedSequence;
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
	public final BufferedImage[] symbolSprites;
	public final Map<Integer, BufferedImage> numberSprites;
	public final TimedSequence<BufferedImage> pacCollapsingAnim;
	public final Map<Ghost, EnumMap<Direction, TimedSequence<BufferedImage>>> ghostsWalkingAnimsByGhost = new HashMap<>();
	public final EnumMap<Direction, TimedSequence<BufferedImage>> ghostEyesAnimsByDir;
	public final TimedSequence<BufferedImage> ghostBlueAnim;
	public final List<TimedSequence<BufferedImage>> ghostFlashingAnim;
	public final TimedSequence<Boolean> energizerBlinkingAnim;
	public final TimedSequence<BufferedImage> bigPacManAnim;
	public final TimedSequence<BufferedImage> blinkyHalfNaked;
	public final TimedSequence<BufferedImage> blinkyDamaged;
	public final TimedSequence<BufferedImage> blinkyStretched;
	public final TimedSequence<BufferedImage> blinkyPatched;
	public final BufferedImage nailSprite;

	public PacManGameRenderingAssets() {
		super(image("/pacman/graphics/sprites.png"), 16);

		scoreFont = font("/emulogic.ttf", 8);

		// Sprites and images

		mazeFullImage = image("/pacman/graphics/maze_full.png");
		mazeEmptyImage = image("/pacman/graphics/maze_empty.png");

		symbolSprites = new BufferedImage[] { sprite(2, 3), sprite(3, 3), sprite(4, 3), sprite(5, 3), sprite(6, 3),
				sprite(7, 3), sprite(8, 3), sprite(9, 3) };

		//@formatter:off
		numberSprites = new HashMap<>();
		numberSprites.put(200,  sprite(0, 8));
		numberSprites.put(400,  sprite(1, 8));
		numberSprites.put(800,  sprite(2, 8));
		numberSprites.put(1600, sprite(3, 8));
		
		numberSprites.put(100,  sprite(0, 9));
		numberSprites.put(300,  sprite(1, 9));
		numberSprites.put(500,  sprite(2, 9));
		numberSprites.put(700,  sprite(3, 9));
		
		numberSprites.put(1000, spriteRegion(4, 9, 2, 1)); // left-aligned
		numberSprites.put(2000, spriteRegion(3, 10, 3, 1));
		numberSprites.put(3000, spriteRegion(3, 11, 3, 1));
		numberSprites.put(5000, spriteRegion(3, 12, 3, 1));
		//@formatter:on

		// Animations

		BufferedImage mazeEmptyDarkImage = image("/pacman/graphics/maze_empty.png");
		BufferedImage mazeEmptyBrightImage = createBrightEffect(mazeEmptyDarkImage, new Color(33, 33, 255), Color.BLACK);
		mazeFlashingAnim = TimedSequence.of(mazeEmptyBrightImage, mazeEmptyDarkImage).frameDuration(15);

		energizerBlinkingAnim = TimedSequence.pulse().frameDuration(15);

		pacCollapsingAnim = TimedSequence.of(sprite(3, 0), sprite(4, 0), sprite(5, 0), sprite(6, 0), sprite(7, 0),
				sprite(8, 0), sprite(9, 0), sprite(10, 0), sprite(11, 0), sprite(12, 0), sprite(13, 0));
		pacCollapsingAnim.frameDuration(8);

		ghostEyesAnimsByDir = new EnumMap<>(Direction.class);
		for (Direction dir : Direction.values()) {
			ghostEyesAnimsByDir.put(dir, TimedSequence.of(sprite(8 + index(dir), 5)));
		}

		ghostBlueAnim = TimedSequence.of(sprite(8, 4), sprite(9, 4));
		ghostBlueAnim.frameDuration(20).endless();

		ghostFlashingAnim = new ArrayList<>();
		for (int i = 0; i < 4; ++i) {
			ghostFlashingAnim
					.add(TimedSequence.of(sprite(8, 4), sprite(9, 4), sprite(10, 4), sprite(11, 4)).frameDuration(4));
		}

		bigPacManAnim = TimedSequence.of(spriteRegion(2, 1, 2, 2), spriteRegion(4, 1, 2, 2), spriteRegion(6, 1, 2, 2))
				.frameDuration(4).endless().run();

		blinkyPatched = TimedSequence.of(sprite(10, 7), sprite(11, 7)).restart().frameDuration(4).endless();
		blinkyDamaged = TimedSequence.of(sprite(8, 7), sprite(9, 7));
		blinkyStretched = TimedSequence.of(sprite(9, 6), sprite(10, 6), sprite(11, 6), sprite(12, 6));
		blinkyHalfNaked = TimedSequence.of(spriteRegion(8, 8, 2, 1), spriteRegion(10, 8, 2, 1)).endless().frameDuration(4)
				.restart();

		nailSprite = sprite(8, 6);
	}

	public TimedSequence<BufferedImage> createPlayerDyingAnimation() {
		TimedSequence<BufferedImage> animation = TimedSequence.of(sprite(3, 0), sprite(4, 0), sprite(5, 0), sprite(6, 0),
				sprite(7, 0), sprite(8, 0), sprite(9, 0), sprite(10, 0), sprite(11, 0), sprite(12, 0), sprite(13, 0));
		animation.frameDuration(8);
		return animation;
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

	private EnumMap<Direction, TimedSequence<BufferedImage>> createGhostWalkingAnimation(int ghostType) {
		EnumMap<Direction, TimedSequence<BufferedImage>> walkingTo = new EnumMap<>(Direction.class);
		for (Direction dir : Direction.values()) {
			TimedSequence<BufferedImage> anim = TimedSequence.of(sprite(2 * index(dir), 4 + ghostType),
					sprite(2 * index(dir) + 1, 4 + ghostType));
			anim.frameDuration(10).endless();
			walkingTo.put(dir, anim);
		}
		return walkingTo;
	}

	public EnumMap<Direction, TimedSequence<BufferedImage>> getOrCreateGhostsWalkingAnimation(Ghost ghost) {
		if (!ghostsWalkingAnimsByGhost.containsKey(ghost)) {
			ghostsWalkingAnimsByGhost.put(ghost, createGhostWalkingAnimation(ghost.id));
		}
		return ghostsWalkingAnimsByGhost.get(ghost);
	}

	public Font getScoreFont() {
		return scoreFont;
	}

	public BufferedImage ghostImageByGhostByDir(int ghostID, Direction dir) {
		return sprite(2 * index(dir), 4 + ghostID);
	}
}