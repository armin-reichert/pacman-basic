package de.amr.games.pacman.ui.swing.rendering.mspacman;

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
import java.util.List;
import java.util.Map;

import de.amr.games.pacman.lib.Direction;
import de.amr.games.pacman.lib.TimedSequence;
import de.amr.games.pacman.ui.swing.assets.Spritesheet;

/**
 * Sprites, animations, images etc. used in Ms. Pac-Man game.
 * 
 * @author Armin Reichert
 */
public class MsPacManGameRenderingAssets extends Spritesheet {

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

	final Map<String, BufferedImage> symbolSprites;
	final Map<Integer, BufferedImage> bonusNumberSprites;
	final Map<Integer, BufferedImage> bountyNumberSprites;

	final List<BufferedImage> mazeEmptyImages;
	final List<BufferedImage> mazeFullImages;
	final List<TimedSequence<BufferedImage>> mazesFlashingAnims;

	public MsPacManGameRenderingAssets() {
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
			mazesFlashingAnims.add(TimedSequence.of(mazeEmpzyBright, mazeEmptyImages.get(i)).frameDuration(15));
		}

		//@formatter:off
		symbolSprites = Map.of(
			"CHERRIES", 	s(3,0),
			"STRAWBERRY", s(4,0),
			"PEACH",			s(5,0),
			"PRETZEL",		s(6,0),
			"APPLE",			s(7,0),
			"PEAR",				s(8,0),
			"BANANA",			s(9,0)
		);

		bonusNumberSprites = Map.of(
				100, s(3, 1), 
				200, s(4, 1), 
				500, s(5, 1), 
				700, s(6, 1), 
				1000, s(7, 1), 
				2000, s(8, 1),
				5000, s(9, 1)
		);

		bountyNumberSprites = Map.of(
				200, s(0, 8), 
				400, s(1, 8), 
				800, s(2, 8), 
				1600, s(3, 8)
		);
		//@formatter:on
	}

	/**
	 * Picks sprite from the right part of the sheet, on the left are the maze images
	 */
	public BufferedImage s(int tileX, int tileY) {
		return sprite(456, 0, tileX, tileY);
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

	public Map<Integer, BufferedImage> getBountyNumbersSpritesMap() {
		return bountyNumberSprites;
	}

	public Map<Integer, BufferedImage> getBonusNumbersSpritesMap() {
		return bonusNumberSprites;
	}

	public TimedSequence<BufferedImage> createPlayerDyingAnimation() {
		TimedSequence<BufferedImage> animation = TimedSequence.of(s(0, 3), s(0, 0), s(0, 1), s(0, 2));
		animation.frameDuration(10).repetitions(2);
		return animation;
	}

	public Map<Direction, TimedSequence<BufferedImage>> createPlayerMunchingAnimations() {
		Map<Direction, TimedSequence<BufferedImage>> munchings = new EnumMap<>(Direction.class);
		for (Direction dir : Direction.values()) {
			int d = index(dir);
			TimedSequence<BufferedImage> munching = TimedSequence.of(s(0, d), s(1, d), s(2, d), s(1, d));
			munching.frameDuration(2).endless();
			munchings.put(dir, munching);
		}
		return munchings;
	}

	public Map<Direction, TimedSequence<BufferedImage>> createSpouseMunchingAnimations() {
		Map<Direction, TimedSequence<BufferedImage>> munchings = new EnumMap<>(Direction.class);
		for (Direction dir : Direction.values()) {
			int d = index(dir);
			TimedSequence<BufferedImage> munching = TimedSequence.of(s(0, 9 + d), s(1, 9 + d), s(2, 9));
			munching.frameDuration(2).endless();
			munchings.put(dir, munching);
		}
		return munchings;
	}

	public Map<Direction, TimedSequence<BufferedImage>> createGhostKickingAnimations(int ghostID) {
		EnumMap<Direction, TimedSequence<BufferedImage>> kickingByDir = new EnumMap<>(Direction.class);
		for (Direction dir : Direction.values()) {
			int d = index(dir);
			TimedSequence<BufferedImage> kicking = TimedSequence.of(s(2 * d, 4 + ghostID), s(2 * d + 1, 4 + ghostID));
			kicking.frameDuration(4).endless();
			kickingByDir.put(dir, kicking);
		}
		return kickingByDir;
	}

	public TimedSequence<BufferedImage> createGhostFrightenedAnimation() {
		TimedSequence<BufferedImage> animation = TimedSequence.of(s(8, 4), s(9, 4));
		animation.frameDuration(20).endless().run();
		return animation;
	}

	public TimedSequence<BufferedImage> createGhostFlashingAnimation() {
		return TimedSequence.of(s(8, 4), s(9, 4), s(10, 4), s(11, 4)).frameDuration(4);
	}

	public Map<Direction, TimedSequence<BufferedImage>> createGhostReturningHomeAnimations() {
		Map<Direction, TimedSequence<BufferedImage>> ghostEyesAnimByDir = new EnumMap<>(Direction.class);
		for (Direction dir : Direction.values()) {
			ghostEyesAnimByDir.put(dir, TimedSequence.of(s(8 + index(dir), 5)));
		}
		return ghostEyesAnimByDir;
	}
}