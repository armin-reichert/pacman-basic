package de.amr.games.pacman.ui.swing.mspacman;

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

import de.amr.games.pacman.lib.Animation;
import de.amr.games.pacman.lib.Direction;
import de.amr.games.pacman.ui.swing.assets.Spritesheet;

/**
 * Sprites, animations, images etc. used in Ms. Pac-Man game.
 * 
 * @author Armin Reichert
 */
public class MsPacManGameAssets extends Spritesheet {

	/** Sprite sheet order of directions. */
	static final List<Direction> order = Arrays.asList(RIGHT, LEFT, UP, DOWN);

	static int index(Direction dir) {
		return order.indexOf(dir);
	}

	//@formatter:off
	static final Color[] mazeWallColors = { 
		new Color(255, 183, 174), 
		new Color(71, 183, 255), 
		new Color(222, 151, 81),
		new Color(33, 33, 255), 
		new Color(255, 183, 255), 
		new Color(255, 183, 174)
	};

	static final Color[] mazeWallBorderColors = { 
		new Color(255, 0, 0), 
		new Color(222, 222, 255),
		new Color(222, 222, 255), 
		new Color(255, 183, 81), 
		new Color(255, 255, 0), 
		new Color(255, 0, 0),
	};
	//@formatter:on

	final Font scoreFont;

	final BufferedImage[] symbolSprites;
	final Map<Integer, BufferedImage> bonusValueSprites;
	final Map<Integer, BufferedImage> bountyNumberSprites;

	final BufferedImage lifeSprite;
	final List<BufferedImage> mazeEmptyImages;
	final List<BufferedImage> mazeFullImages;
	final List<Animation<BufferedImage>> mazesFlashingAnims;
	final Animation<Boolean> energizerBlinkingAnim;
	final EnumMap<Direction, Animation<BufferedImage>> pacMunchingAnimByDir;
	final Animation<BufferedImage> pacSpinningAnim;
	final List<EnumMap<Direction, Animation<BufferedImage>>> ghostsKickingAnimsByGhost;
	final EnumMap<Direction, Animation<BufferedImage>> ghostEyesAnimByDir;
	final Animation<BufferedImage> ghostBlueAnim;
	final Animation<BufferedImage> ghostFlashingAnim;
	final Animation<Integer> bonusJumpAnim;
	final Animation<BufferedImage> flapAnim;

	public MsPacManGameAssets() {
		super(image("/mspacman/graphics/sprites.png"), 16);

		scoreFont = font("/emulogic.ttf", 8);

		// Left part of spritesheet contains the 6 mazes, rest is on the right
		mazeEmptyImages = new ArrayList<>(6);
		mazeFullImages = new ArrayList<>(6);
		mazesFlashingAnims = new ArrayList<>(6);
		for (int i = 0; i < 6; ++i) {
			mazeFullImages.add(sheet.getSubimage(0, i * 248, 226, 248));
			mazeEmptyImages.add(sheet.getSubimage(226, i * 248, 226, 248));
			BufferedImage mazeEmpzyBright = createBrightEffect(mazeEmptyImages.get(i), getMazeWallBorderColor(i),
					getMazeWallColor(i));
			mazesFlashingAnims.add(Animation.of(mazeEmpzyBright, mazeEmptyImages.get(i)).frameDuration(15));
		}

		energizerBlinkingAnim = Animation.pulse().frameDuration(10);

		// Switch to right part of spritesheet
		setOrigin(456, 0);

		lifeSprite = spriteAt(1, 0);
		symbolSprites = new BufferedImage[] { spriteAt(3, 0), spriteAt(4, 0), spriteAt(5, 0), spriteAt(6, 0),
				spriteAt(7, 0), spriteAt(8, 0), spriteAt(9, 0) };

		//@formatter:off
		bonusValueSprites = new HashMap<>();
		bonusValueSprites.put(100,  spriteAt(3, 1));
		bonusValueSprites.put(200,  spriteAt(4, 1));
		bonusValueSprites.put(500,  spriteAt(5, 1));
		bonusValueSprites.put(700,  spriteAt(6, 1));
		bonusValueSprites.put(1000, spriteAt(7, 1));
		bonusValueSprites.put(2000, spriteAt(8, 1));
		bonusValueSprites.put(5000, spriteAt(9, 1));
		
		bountyNumberSprites = new HashMap<>();
		bountyNumberSprites.put(200, spriteAt(0,8));
		bountyNumberSprites.put(400, spriteAt(1,8));
		bountyNumberSprites.put(800, spriteAt(2,8));
		bountyNumberSprites.put(1600, spriteAt(3,8));
		//@formatter:on

		pacMunchingAnimByDir = new EnumMap<>(Direction.class);
		for (Direction dir : Direction.values()) {
			int d = index(dir);
			Animation<BufferedImage> munching = Animation.of(spriteAt(0, d), spriteAt(1, d), spriteAt(2, d), spriteAt(1, d));
			munching.frameDuration(2).endless();
			pacMunchingAnimByDir.put(dir, munching);
		}

		pacSpinningAnim = Animation.of(spriteAt(0, 3), spriteAt(0, 0), spriteAt(0, 1), spriteAt(0, 2));
		pacSpinningAnim.frameDuration(10).repetitions(2);

		ghostsKickingAnimsByGhost = new ArrayList<>(4);
		for (int g = 0; g < 4; ++g) {
			EnumMap<Direction, Animation<BufferedImage>> kickingByDir = new EnumMap<>(Direction.class);
			for (Direction dir : Direction.values()) {
				int d = index(dir);
				Animation<BufferedImage> kicking = Animation.of(spriteAt(2 * d, 4 + g), spriteAt(2 * d + 1, 4 + g));
				kicking.frameDuration(4).endless();
				kickingByDir.put(dir, kicking);
			}
			ghostsKickingAnimsByGhost.add(kickingByDir);
		}

		ghostEyesAnimByDir = new EnumMap<>(Direction.class);
		for (Direction dir : Direction.values()) {
			ghostEyesAnimByDir.put(dir, Animation.ofSingle(spriteAt(8 + index(dir), 5)));
		}

		ghostBlueAnim = Animation.of(spriteAt(8, 4), spriteAt(9, 4));
		ghostBlueAnim.frameDuration(20).endless().run();

		ghostFlashingAnim = Animation.of(spriteAt(8, 4), spriteAt(9, 4), spriteAt(10, 4), spriteAt(11, 4));
		ghostFlashingAnim.frameDuration(5).endless();

		bonusJumpAnim = Animation.of(2, -2).frameDuration(15).endless().run();

		flapAnim = Animation.of( //
				sheet.getSubimage(456, 208, 32, 32), //
				sheet.getSubimage(488, 208, 32, 32), //
				sheet.getSubimage(520, 208, 32, 32), //
				sheet.getSubimage(488, 208, 32, 32), //
				sheet.getSubimage(456, 208, 32, 32)//
		);
		flapAnim.repetitions(1).frameDuration(4);
	}

	/**
	 * Note: maze numbers are 1-based, maze index as stored here is 0-based.
	 * 
	 * @param mazeIndex 0-based maze index
	 * @return color of maze walls
	 */
	public Color getMazeWallColor(int mazeIndex) {
		return mazeWallColors[mazeIndex];
	}

	/**
	 * Note: maze numbers are 1-based, maze index as stored here is 0-based.
	 * 
	 * @param mazeIndex 0-based maze index
	 * @return color of maze wall borders
	 */
	public Color getMazeWallBorderColor(int mazeIndex) {
		return mazeWallBorderColors[mazeIndex];
	}

	public Font getScoreFont() {
		return scoreFont;
	}
}