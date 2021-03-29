package de.amr.games.pacman.ui.swing.rendering.pacman;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.util.Map;
import java.util.stream.Stream;

import de.amr.games.pacman.lib.Direction;
import de.amr.games.pacman.lib.V2d;
import de.amr.games.pacman.lib.V2i;
import de.amr.games.pacman.model.common.GameEntity;
import de.amr.games.pacman.model.common.Ghost;
import de.amr.games.pacman.model.common.Pac;
import de.amr.games.pacman.model.pacman.PacManBonus;
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
	public Map<Integer, BufferedImage> getNumberSpritesMap() {
		return assets.getNumberSpritesMap();
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

	// Animations

	@Override
	public TimedSequence<Boolean> energizerBlinking() {
		return assets.energizerBlinkingAnim;
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
	public TimedSequence<?> flapFlappingAnimation() {
		return null;
	}

	@Override
	public TimedSequence<?> storkFlyingAnimation() {
		return null;
	}

	// draw functions

	@Override
	public void drawMaze(Graphics2D g, int mazeNumber, int x, int y, boolean flashing) {
		if (flashing) {
			g.drawImage(mazeFlashing(mazeNumber).animate(), x, y, null);
		} else {
			g.drawImage(assets.mazeFullImage, x, y, null);
		}
	}

	@Override
	public void drawEnergizerTiles(Graphics2D g, Stream<V2i> energizerTiles) {
		if (!mazeAnimations().energizerBlinking().animate()) {
			energizerTiles.forEach(tile -> drawTileCovered(g, tile));
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

	public void drawBlinkyStretched(Graphics2D g, Ghost blinky, V2d nailPosition, int stretching) {
		drawSprite(g, assets.blinkyStretched.frame(stretching), nailPosition.x - 4, nailPosition.y - 4);
		if (stretching < 3) {
			// TODO
//			drawGhost(g, blinky, false);
		} else {
			drawEntity(g, blinky, assets.blinkyDamaged.frame(blinky.dir == Direction.UP ? 0 : 1));
		}
	}

	public void drawBlinkyPatched(Graphics2D g, Ghost blinky) {
		drawEntity(g, blinky, assets.blinkyPatched.animate());
	}

	public void drawBlinkyNaked(Graphics2D g, Ghost blinky) {
		drawEntity(g, blinky, assets.blinkyHalfNaked.animate());
	}

	// Sprites

	@Override
	public BufferedImage bonusSprite(PacManBonus bonus) {
		if (bonus.edibleTicksLeft > 0) {
			return assets.symbolSprites[bonus.symbol];
		}
		if (bonus.eatenTicksLeft > 0) {
			return assets.numberSprites.get(bonus.points);
		}
		return null;
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