package de.amr.games.pacman.ui.swing.rendering.mspacman;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.util.Map;

import de.amr.games.pacman.lib.Direction;
import de.amr.games.pacman.lib.TimedSequence;
import de.amr.games.pacman.ui.swing.rendering.common.AbstractPacManGameRendering;

/**
 * Rendering for the Ms. Pac-Man game.
 * 
 * @author Armin Reichert
 */
public class MsPacManGameRendering extends AbstractPacManGameRendering {

	public final MsPacManGameRenderingAssets assets = new MsPacManGameRenderingAssets();

	@Override
	public TimedSequence<BufferedImage> mazeFlashing(int mazeNumber) {
		return assets.mazesFlashingAnims.get(mazeNumber - 1);
	}

	@Override
	public TimedSequence<BufferedImage> createPlayerDyingAnimation() {
		return assets.createPlayerDyingAnimation();
	}

	@Override
	public Map<Direction, TimedSequence<BufferedImage>> createPlayerMunchingAnimations() {
		return assets.createPlayerMunchingAnimations();
	}

	@Override
	public Map<Direction, TimedSequence<BufferedImage>> createSpouseMunchingAnimations() {
		return assets.createSpouseMunchingAnimations();
	}

	@Override
	public Map<Direction, TimedSequence<BufferedImage>> createGhostKickingAnimations(int ghostID) {
		return assets.createGhostKickingAnimations(ghostID);
	}

	@Override
	public TimedSequence<BufferedImage> createGhostFrightenedAnimation() {
		return assets.createGhostFrightenedAnimation();
	}

	@Override
	public TimedSequence<BufferedImage> createGhostFlashingAnimation() {
		return assets.createGhostFlashingAnimation();
	}

	@Override
	public Map<Direction, TimedSequence<BufferedImage>> createGhostReturningHomeAnimations() {
		return assets.createGhostReturningHomeAnimations();
	}

	@Override
	public TimedSequence<Integer> createBonusAnimation() {
		return TimedSequence.of(2, -2).frameDuration(15).endless();
	}

	@Override
	public TimedSequence<BufferedImage> createFlapAnimation() {
		return TimedSequence.of( //
				assets.region(456, 208, 32, 32), //
				assets.region(488, 208, 32, 32), //
				assets.region(520, 208, 32, 32), //
				assets.region(488, 208, 32, 32), //
				assets.region(456, 208, 32, 32)//
		).repetitions(1).frameDuration(4);
	}

	@Override
	public TimedSequence<BufferedImage> createStorkFlyingAnimation() {
		return TimedSequence.of(//
				assets.region(489, 176, 32, 16), //
				assets.region(521, 176, 32, 16)//
		).endless().frameDuration(10);
	}

	@Override
	public BufferedImage getBlueBag() {
		return assets.region(488, 199, 8, 8);
	}

	@Override
	public BufferedImage getJunior() {
		return assets.region(509, 200, 8, 8);
	}

	@Override
	public BufferedImage getHeart() {
		return assets.s(2, 10);
	}

	@Override
	public Map<Integer, BufferedImage> getBountyNumberSpritesMap() {
		return assets.getBountyNumbersSpritesMap();
	}

	@Override
	public Map<Integer, BufferedImage> getBonusNumberSpritesMap() {
		return assets.getBonusNumbersSpritesMap();
	}

	@Override
	public Map<String, BufferedImage> getSymbolSpritesMap() {
		return assets.symbolSprites;
	}

	@Override
	public Font getScoreFont() {
		return assets.scoreFont;
	}

	/**
	 * Note: maze numbers are 1-based, maze index as stored here is 0-based.
	 * 
	 * @param mazeIndex 0-based index
	 * @return
	 */
	@Override
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
	 * @param mazeIndex 0-based index
	 * @return
	 */
	@Override
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

	@Override
	public BufferedImage lifeSprite() {
		return assets.s(1, 0);
	}

	@Override
	public BufferedImage symbolSprite(String symbol) {
		return assets.symbolSprites.get(symbol);
	}

	@Override
	public void drawMaze(Graphics2D g, int mazeNumber, int x, int y, boolean flashing) {
		if (flashing) {
			g.drawImage(mazeFlashing(mazeNumber).animate(), x, y, null);
		} else {
			g.drawImage(assets.mazeFullImages.get(mazeNumber - 1), x, y, null);
		}
	}
}