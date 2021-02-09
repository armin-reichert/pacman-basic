package de.amr.games.pacman.ui.swing.mspacman.rendering;

import static de.amr.games.pacman.ui.swing.PacManGameSwingUI.font;
import static de.amr.games.pacman.ui.swing.PacManGameSwingUI.image;
import static de.amr.games.pacman.ui.swing.PacManGameSwingUI.url;

import java.awt.Color;
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
 * Sprites, animations, images etc. used in Ms. Pac-Man game.
 * 
 * @author Armin Reichert
 */
public class MsPacManGameAssets extends Spritesheet {

	/** Sprite sheet order of directions. */
	static int index(Direction dir) {
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

	final V2i[] symbolsSSL; // Sprite Sheet Location
	final Map<Integer, V2i> bonusValuesSSL;
	final Map<Integer, V2i> bountyNumbersSSL;

	final List<BufferedImage> mazesFull;
	final List<Animation<BufferedImage>> mazesFlashing;
	final Animation<Boolean> energizerBlinking;
	final EnumMap<Direction, Animation<BufferedImage>> pacMunching;
	final Animation<BufferedImage> pacSpinning;
	final List<EnumMap<Direction, Animation<BufferedImage>>> ghostsWalking;
	final EnumMap<Direction, Animation<BufferedImage>> ghostEyes;
	final Animation<BufferedImage> ghostBlue;
	final Animation<BufferedImage> ghostFlashing;
	final Animation<Integer> bonusJumps;

	final Map<PacManGameSound, URL> soundMap;
	final Font scoreFont;

	public MsPacManGameAssets() {
		super(image("/mspacman/graphics/sprites.png"), 16);

		scoreFont = font("/emulogic.ttf", 8);

		// Left part of spritesheet contains the 6 mazes, rest is on the right
		mazesFull = new ArrayList<>(6);
		mazesFlashing = new ArrayList<>(6);
		for (int i = 0; i < 6; ++i) {
			mazesFull.add(sheet.getSubimage(0, i * 248, 226, 248));
			BufferedImage mazeEmptyNormal = sheet.getSubimage(226, i * 248, 226, 248);
			BufferedImage mazeEmpzyBright = createBrightEffect(mazeEmptyNormal, getMazeWallBorderColor(i),
					getMazeWallColor(i));
			mazesFlashing.add(Animation.of(mazeEmpzyBright, mazeEmptyNormal).frameDuration(15));
		}

		energizerBlinking = Animation.pulse().frameDuration(10);

		// Switch to right part of spritesheet
		setOrigin(456, 0);

		symbolsSSL = new V2i[] { v2(3, 0), v2(4, 0), v2(5, 0), v2(6, 0), v2(7, 0), v2(8, 0), v2(9, 0) };

		//@formatter:off
		bonusValuesSSL = new HashMap<>();
		bonusValuesSSL.put(100,  v2(3, 1));
		bonusValuesSSL.put(200,  v2(4, 1));
		bonusValuesSSL.put(500,  v2(5, 1));
		bonusValuesSSL.put(700,  v2(6, 1));
		bonusValuesSSL.put(1000, v2(7, 1));
		bonusValuesSSL.put(2000, v2(8, 1));
		bonusValuesSSL.put(5000, v2(9, 1));
		
		bountyNumbersSSL = new HashMap<>();
		bountyNumbersSSL.put(200, v2(0,8));
		bountyNumbersSSL.put(400, v2(1,8));
		bountyNumbersSSL.put(800, v2(2,8));
		bountyNumbersSSL.put(1600, v2(3,8));
		//@formatter:on

		pacMunching = new EnumMap<>(Direction.class);
		for (Direction dir : Direction.values()) {
			int d = index(dir);
			Animation<BufferedImage> munching = Animation.of(spriteAt(0, d), spriteAt(1, d), spriteAt(2, d), spriteAt(1, d));
			munching.frameDuration(2).endless();
			pacMunching.put(dir, munching);
		}

		pacSpinning = Animation.of(spriteAt(0, 3), spriteAt(0, 0), spriteAt(0, 1), spriteAt(0, 2));
		pacSpinning.frameDuration(10).repetitions(2);

		ghostsWalking = new ArrayList<>(4);
		for (int g = 0; g < 4; ++g) {
			EnumMap<Direction, Animation<BufferedImage>> walkingTo = new EnumMap<>(Direction.class);
			for (Direction dir : Direction.values()) {
				int d = index(dir);
				Animation<BufferedImage> walking = Animation.of(spriteAt(2 * d, 4 + g), spriteAt(2 * d + 1, 4 + g));
				walking.frameDuration(4).endless();
				walkingTo.put(dir, walking);
			}
			ghostsWalking.add(walkingTo);
		}

		ghostEyes = new EnumMap<>(Direction.class);
		for (Direction dir : Direction.values()) {
			ghostEyes.put(dir, Animation.ofSingle(spriteAt(8 + index(dir), 5)));
		}

		ghostBlue = Animation.of(spriteAt(8, 4), spriteAt(9, 4));
		ghostBlue.frameDuration(20).endless().run();

		ghostFlashing = Animation.of(spriteAt(8, 4), spriteAt(9, 4), spriteAt(10, 4), spriteAt(11, 4));
		ghostFlashing.frameDuration(5).endless();

		bonusJumps = Animation.of(0, 2, 0, -2).frameDuration(20).endless().run();

		//@formatter:off
		soundMap = new EnumMap<>(PacManGameSound.class);
		soundMap.put(PacManGameSound.CREDIT,           url("/mspacman/sound/Coin Credit.wav"));
		soundMap.put(PacManGameSound.EXTRA_LIFE,       url("/mspacman/sound/Extra Life.wav"));
		soundMap.put(PacManGameSound.GAME_READY,       url("/mspacman/sound/Start.wav"));
		soundMap.put(PacManGameSound.PACMAN_EAT_BONUS, url("/mspacman/sound/Fruit.wav"));
		soundMap.put(PacManGameSound.PACMAN_MUNCH,     url("/mspacman/sound/Ms. Pac Man Pill.wav"));
		soundMap.put(PacManGameSound.PACMAN_DEATH,     url("/mspacman/sound/Died.wav"));
		soundMap.put(PacManGameSound.PACMAN_POWER,     url("/mspacman/sound/Scared Ghost.wav"));
		soundMap.put(PacManGameSound.GHOST_EATEN,      url("/mspacman/sound/Ghost.wav"));
		soundMap.put(PacManGameSound.GHOST_EYES,       url("/mspacman/sound/Ghost Eyes.wav"));
		soundMap.put(PacManGameSound.GHOST_SIREN_1,    url("/mspacman/sound/Ghost Noise.wav"));
		soundMap.put(PacManGameSound.GHOST_SIREN_2,    url("/mspacman/sound/Ghost Noise 1.wav"));
		soundMap.put(PacManGameSound.GHOST_SIREN_3,    url("/mspacman/sound/Ghost Noise 2.wav"));
		soundMap.put(PacManGameSound.GHOST_SIREN_4,    url("/mspacman/sound/Ghost Noise 3.wav"));
		soundMap.put(PacManGameSound.GHOST_SIREN_5,    url("/mspacman/sound/Ghost Noise 4.wav"));
		soundMap.put(PacManGameSound.INTERMISSION_1,   url("/mspacman/sound/They Meet Act 1.wav"));
		soundMap.put(PacManGameSound.INTERMISSION_2,   url("/mspacman/sound/The Chase Act 2.wav"));
		soundMap.put(PacManGameSound.INTERMISSION_3,   url("/mspacman/sound/Junior Act 1.wav"));
		//@formatter:on
	}

	/**
	 * Note: maze numbers are 1-based, maze index as stored here is 0-based.
	 * 
	 * @param mazeIndex
	 * @return
	 */
	public Color getMazeWallColor(int mazeIndex) {
		switch (mazeIndex) {
		case 0:
			return new Color(255, 183, 174);
		case 1:
			return new Color(71, 183, 255);
		case 2:
			return new Color(222, 151, 81);
		case 3:
			return new Color(33, 33, 255);
		case 4:
			return new Color(255, 183, 255);
		case 5:
			return new Color(255, 183, 174);
		default:
			return Color.WHITE;
		}
	}

	/**
	 * Note: maze numbers are 1-based, maze index as stored here is 0-based.
	 * 
	 * @param mazeIndex
	 * @return
	 */
	public Color getMazeWallBorderColor(int mazeIndex) {
		switch (mazeIndex) {
		case 0:
			return new Color(255, 0, 0);
		case 1:
			return new Color(222, 222, 255);
		case 2:
			return new Color(222, 222, 255);
		case 3:
			return new Color(255, 183, 81);
		case 4:
			return new Color(255, 255, 0);
		case 5:
			return new Color(255, 0, 0);
		default:
			return Color.WHITE;
		}
	}

	public Font getScoreFont() {
		return scoreFont;
	}

	public URL getSoundURL(PacManGameSound sound) {
		return soundMap.get(sound);
	}
}