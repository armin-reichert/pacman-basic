package de.amr.games.pacman.ui.swing.rendering.mspacman;

import static de.amr.games.pacman.model.world.PacManGameWorld.t;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.util.Map;
import java.util.stream.Stream;

import de.amr.games.pacman.lib.Direction;
import de.amr.games.pacman.model.common.AbstractGameModel;
import de.amr.games.pacman.model.pacman.PacManBonus;
import de.amr.games.pacman.ui.animation.MazeAnimations2D;
import de.amr.games.pacman.ui.animation.PacManGameAnimations2D;
import de.amr.games.pacman.ui.animation.TimedSequence;
import de.amr.games.pacman.ui.swing.rendering.common.AbstractPacManGameRendering;

/**
 * Rendering for the Ms. Pac-Man game.
 * 
 * @author Armin Reichert
 */
public class MsPacManGameRendering extends AbstractPacManGameRendering
		implements PacManGameAnimations2D, MazeAnimations2D {

	public static final MsPacManGameRenderingAssets assets = new MsPacManGameRenderingAssets();

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
		return TimedSequence.of(2, -2).frameDuration(10).endless();
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
		return assets.blueBag;
	}

	@Override
	public BufferedImage getJunior() {
		return assets.junior;
	}

	@Override
	public BufferedImage getHeart() {
		return assets.s(2, 10);
	}

	@Override
	public Map<Integer, BufferedImage> getBountyNumbersSpritesMap() {
		return assets.getBountyNumbersSpritesMap();
	}

	@Override
	public Map<Integer, BufferedImage> getBonusNumbersSpritesMap() {
		return assets.getBonusNumbersSpritesMap();
	}

	@Override
	public BufferedImage[] getSymbolSprites() {
		return assets.symbolSprites;
	}

	@Override
	public MazeAnimations2D mazeAnimations() {
		return this;
	}

	@Override
	public Font getScoreFont() {
		return assets.getScoreFont();
	}

	/**
	 * Note: maze numbers are 1-based, maze index as stored here is 0-based.
	 * 
	 * @param mazeIndex
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

	@Override
	public Stream<TimedSequence<?>> mazeFlashings() {
		// TODO this is silly
		return assets.mazesFlashingAnims.stream().map(TimedSequence.class::cast);
	}

	@Override
	public TimedSequence<BufferedImage> mazeFlashing(int mazeNumber) {
		return assets.mazesFlashingAnims.get(mazeNumber - 1);
	}

	public BufferedImage bonusSprite(PacManBonus bonus, AbstractGameModel game) {
		if (bonus.edibleTicksLeft > 0) {
			return assets.symbolSprites[bonus.symbol];
		}
		if (bonus.eatenTicksLeft > 0) {
			return assets.bonusNumberSprites.get(bonus.points);
		}
		return null;
	}

	@Override
	public BufferedImage lifeSprite() {
		return assets.lifeSprite;
	}

	@Override
	protected BufferedImage symbolSprite(byte symbol) {
		return assets.symbolSprites[symbol];
	}

	@Override
	public void drawMaze(Graphics2D g, int mazeNumber, int x, int y, boolean flashing) {
		if (flashing) {
			g.drawImage(mazeFlashing(mazeNumber).animate(), x, y, null);
		} else {
			g.drawImage(assets.mazeFullImages.get(mazeNumber - 1), x, y, null);
		}
	}

	@Override
	public void drawLevelCounter(Graphics2D g, AbstractGameModel game, int rightX, int y) {
		int x = rightX;
		for (int levelNumber = 1; levelNumber <= Math.min(game.currentLevelNumber, 7); ++levelNumber) {
			byte symbol = game.levelSymbols.get(levelNumber - 1);
			g.drawImage(assets.symbolSprites[symbol], x, y, null);
			x -= t(2);
		}
	}

	public void drawLifeCounterSymbol(Graphics2D g, int x, int y) {
		g.drawImage(assets.lifeSprite, x, y, null);
	}
}