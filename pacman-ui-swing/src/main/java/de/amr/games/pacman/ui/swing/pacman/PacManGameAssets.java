package de.amr.games.pacman.ui.swing.pacman;

import static de.amr.games.pacman.lib.Direction.DOWN;
import static de.amr.games.pacman.lib.Direction.LEFT;
import static de.amr.games.pacman.lib.Direction.RIGHT;
import static de.amr.games.pacman.lib.Direction.UP;
import static de.amr.games.pacman.ui.swing.assets.AssetLoader.font;
import static de.amr.games.pacman.ui.swing.assets.AssetLoader.image;

import java.awt.Color;
import java.awt.Font;
import java.awt.image.BufferedImage;
import java.util.Arrays;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import de.amr.games.pacman.lib.Animation;
import de.amr.games.pacman.lib.Direction;
import de.amr.games.pacman.model.Ghost;
import de.amr.games.pacman.model.Pac;
import de.amr.games.pacman.ui.swing.assets.Spritesheet;

/**
 * Assets used in Pac-Man game.
 * 
 * @author Armin Reichert
 */
public class PacManGameAssets extends Spritesheet {

	/** Sprite sheet order of directions. */
	static final List<Direction> order = Arrays.asList(RIGHT, LEFT, UP, DOWN);

	private static int index(Direction dir) {
		return order.indexOf(dir);
	}

	public static Color ghostColor(int ghostType) {
		return ghostType == 0 ? Color.RED : ghostType == 1 ? Color.pink : ghostType == 2 ? Color.CYAN : Color.ORANGE;
	}

	final BufferedImage mazeFullImage;
	final BufferedImage mazeEmptyImage;
	final Animation<BufferedImage> mazeFlashingAnim;

	final BufferedImage[] symbolSprites;
	final Map<Integer, BufferedImage> numberSprites;

	final Map<Pac, EnumMap<Direction, Animation<BufferedImage>>> pacMunchingAnimations = new HashMap<>();
	final Animation<BufferedImage> pacCollapsingAnim;

	final Map<Ghost, EnumMap<Direction, Animation<BufferedImage>>> ghostsWalkingAnimsByGhost = new HashMap<>();
	final EnumMap<Direction, Animation<BufferedImage>> ghostEyesAnimsByDir;
	final Animation<BufferedImage> ghostBlueAnim;
	final Animation<BufferedImage> ghostFlashingAnim;

	final Animation<Boolean> energizerBlinkingAnim;

	final Font scoreFont;

	public PacManGameAssets() {
		super(image("/pacman/graphics/sprites.png"), 16);

		scoreFont = font("/emulogic.ttf", 8);

		// Sprites and images

		mazeFullImage = image("/pacman/graphics/maze_full.png");
		mazeEmptyImage = image("/pacman/graphics/maze_empty.png");

		symbolSprites = new BufferedImage[] { spriteAt(2, 3), spriteAt(3, 3), spriteAt(4, 3), spriteAt(5, 3),
				spriteAt(6, 3), spriteAt(7, 3), spriteAt(8, 3), spriteAt(9, 3) };

		//@formatter:off
		numberSprites = new HashMap<>();
		numberSprites.put(200,  spriteAt(0, 8));
		numberSprites.put(400,  spriteAt(1, 8));
		numberSprites.put(800,  spriteAt(2, 8));
		numberSprites.put(1600, spriteAt(3, 8));
		
		numberSprites.put(100,  spriteAt(0, 9));
		numberSprites.put(300,  spriteAt(1, 9));
		numberSprites.put(500,  spriteAt(2, 9));
		numberSprites.put(700,  spriteAt(3, 9));
		
		numberSprites.put(1000, spritesAt(4, 9, 2, 1)); // left-aligned
		numberSprites.put(2000, spritesAt(3, 10, 3, 1));
		numberSprites.put(3000, spritesAt(3, 11, 3, 1));
		numberSprites.put(5000, spritesAt(3, 12, 3, 1));
		//@formatter:on

		// Animations

		BufferedImage mazeEmptyDarkImage = image("/pacman/graphics/maze_empty.png");
		BufferedImage mazeEmptyBrightImage = createBrightEffect(mazeEmptyDarkImage, new Color(33, 33, 255), Color.BLACK);
		mazeFlashingAnim = Animation.of(mazeEmptyBrightImage, mazeEmptyDarkImage).frameDuration(15);

		energizerBlinkingAnim = Animation.pulse().frameDuration(15);

		pacCollapsingAnim = Animation.of(spriteAt(3, 0), spriteAt(4, 0), spriteAt(5, 0), spriteAt(6, 0), spriteAt(7, 0),
				spriteAt(8, 0), spriteAt(9, 0), spriteAt(10, 0), spriteAt(11, 0), spriteAt(12, 0), spriteAt(13, 0));
		pacCollapsingAnim.frameDuration(8);

		ghostEyesAnimsByDir = new EnumMap<>(Direction.class);
		for (Direction dir : Direction.values()) {
			ghostEyesAnimsByDir.put(dir, Animation.ofSingle(spriteAt(8 + index(dir), 5)));
		}

		ghostBlueAnim = Animation.of(spriteAt(8, 4), spriteAt(9, 4));
		ghostBlueAnim.frameDuration(20).endless();

		ghostFlashingAnim = Animation.of(spriteAt(8, 4), spriteAt(9, 4), spriteAt(10, 4), spriteAt(11, 4));
		ghostFlashingAnim.frameDuration(5).endless();
	}

	public EnumMap<Direction, Animation<BufferedImage>> getOrCreatePacMunchingAnimation(Pac pac) {
		if (!pacMunchingAnimations.containsKey(pac)) {
			pacMunchingAnimations.put(pac, createPacMunchingAnimation());
		}
		return pacMunchingAnimations.get(pac);
	}

	private EnumMap<Direction, Animation<BufferedImage>> createPacMunchingAnimation() {
		EnumMap<Direction, Animation<BufferedImage>> munching = new EnumMap<>(Direction.class);
		for (Direction dir : Direction.values()) {
			Animation<BufferedImage> animation = Animation.of(spriteAt(2, 0), spriteAt(1, index(dir)),
					spriteAt(0, index(dir)), spriteAt(1, index(dir)));
			animation.frameDuration(2).endless().run();
			munching.put(dir, animation);
		}
		return munching;
	}

	private EnumMap<Direction, Animation<BufferedImage>> createGhostWalkingAnimation(int ghostType) {
		EnumMap<Direction, Animation<BufferedImage>> walkingTo = new EnumMap<>(Direction.class);
		for (Direction dir : Direction.values()) {
			Animation<BufferedImage> anim = Animation.of(spriteAt(2 * index(dir), 4 + ghostType),
					spriteAt(2 * index(dir) + 1, 4 + ghostType));
			anim.frameDuration(10).endless();
			walkingTo.put(dir, anim);
		}
		return walkingTo;
	}

	public EnumMap<Direction, Animation<BufferedImage>> getOrCreateGhostsWalkingAnimation(Ghost ghost) {
		if (!ghostsWalkingAnimsByGhost.containsKey(ghost)) {
			ghostsWalkingAnimsByGhost.put(ghost, createGhostWalkingAnimation(ghost.id));
		}
		return ghostsWalkingAnimsByGhost.get(ghost);
	}

	public Font getScoreFont() {
		return scoreFont;
	}

	public BufferedImage ghostImageByGhostByDir(int ghostID, Direction dir) {
		return spriteAt(2 * index(dir), 4 + ghostID);
	}
}