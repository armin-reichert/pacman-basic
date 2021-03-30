package de.amr.games.pacman.ui.swing.rendering.pacman;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.util.Map;
import java.util.stream.Stream;

import de.amr.games.pacman.lib.Direction;
import de.amr.games.pacman.model.common.GameEntity;
import de.amr.games.pacman.model.common.Ghost;
import de.amr.games.pacman.model.common.Pac;
import de.amr.games.pacman.ui.animation.MazeAnimations2D;
import de.amr.games.pacman.ui.animation.PacManGameAnimations2D;
import de.amr.games.pacman.ui.animation.TimedSequence;
import de.amr.games.pacman.ui.swing.rendering.common.CommonPacManGameRendering;

/**
 * Sprite-based rendering for the Pac-Man game.
 * 
 * @author Armin Reichert
 */
public class PacManGameRendering extends CommonPacManGameRendering implements PacManGameAnimations2D, MazeAnimations2D {

	public final PacManGameRenderingAssets assets;

	public PacManGameRendering() {
		assets = new PacManGameRenderingAssets();
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
		throw new UnsupportedOperationException();
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
		return assets.createGhostsReturningHomeAnimations();
	}

	@Override
	public TimedSequence<BufferedImage> createBlinkyStretchedAnimation() {
		return assets.createBlinkyStretchedAnimation();
	}

	@Override
	public TimedSequence<BufferedImage> createBlinkyDamagedAnimation() {
		return assets.createBlinkyDamagedAnimation();
	}

	@Override
	public TimedSequence<Integer> createBonusAnimation() {
		return null;
	}

	@Override
	public Map<Integer, BufferedImage> getBountyNumbersSpritesMap() {
		return assets.numberSprites;
	}

	@Override
	public Map<Integer, BufferedImage> getBonusNumbersSpritesMap() {
		return assets.numberSprites;
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

	public Color getMazeWallBorderColor(int mazeIndex) {
		return new Color(33, 33, 255);
	}

	@Override
	public Color getMazeWallColor(int mazeIndex) {
		return Color.BLACK;
	}

	@Override
	public Stream<TimedSequence<?>> mazeFlashings() {
		return Stream.of(assets.mazeFlashingAnim);
	}

	@Override
	public TimedSequence<BufferedImage> mazeFlashing(int mazeNumber) {
		return assets.mazeFlashingAnim;
	}

	@Override
	public void drawMaze(Graphics2D g, int mazeNumber, int x, int y, boolean flashing) {
		if (flashing) {
			g.drawImage(mazeFlashing(mazeNumber).animate(), x, y, null);
		} else {
			g.drawImage(assets.mazeFullImage, x, y, null);
		}
	}

	public void drawLifeCounterSymbol(Graphics2D g, int x, int y) {
		g.drawImage(lifeSprite(), x, y, null);
	}

	public void drawBigPacMan(Graphics2D g, Pac bigPacMan) {
		drawEntity(g, bigPacMan, assets.bigPacManAnim.animate());
	}

	public void drawNail(Graphics2D g, GameEntity nail) {
		drawEntity(g, nail, assets.nailSprite);
	}

	public void drawBlinkyPatched(Graphics2D g, Ghost blinky) {
		drawEntity(g, blinky, assets.blinkyPatched.animate());
	}

	public void drawBlinkyNaked(Graphics2D g, Ghost blinky) {
		drawEntity(g, blinky, assets.blinkyHalfNaked.animate());
	}

	@Override
	public BufferedImage lifeSprite() {
		return assets.sprite(8, 1);
	}

	@Override
	protected BufferedImage symbolSprite(byte symbol) {
		return assets.symbolSprites[symbol];
	}
}