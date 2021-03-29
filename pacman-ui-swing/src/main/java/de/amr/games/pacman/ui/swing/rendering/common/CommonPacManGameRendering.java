package de.amr.games.pacman.ui.swing.rendering.common;

import static de.amr.games.pacman.model.world.PacManGameWorld.HTS;
import static de.amr.games.pacman.model.world.PacManGameWorld.TS;
import static de.amr.games.pacman.model.world.PacManGameWorld.t;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.util.Map;
import java.util.function.Predicate;
import java.util.stream.Stream;

import de.amr.games.pacman.controller.PacManGameState;
import de.amr.games.pacman.lib.Direction;
import de.amr.games.pacman.lib.V2i;
import de.amr.games.pacman.model.common.AbstractGameModel;
import de.amr.games.pacman.model.common.GameEntity;
import de.amr.games.pacman.model.pacman.PacManBonus;
import de.amr.games.pacman.ui.animation.PacManGameAnimations2D;
import de.amr.games.pacman.ui.animation.TimedSequence;

/**
 * Spritesheet-based rendering for both, Pac-Man and Ms. Pac-Man.
 * 
 * @author Armin Reichert
 */
public abstract class CommonPacManGameRendering implements PacManGameAnimations2D {

	public abstract Font getScoreFont();

	public abstract Map<Direction, TimedSequence<BufferedImage>> createPlayerMunchingAnimations();

	public abstract TimedSequence<BufferedImage> createPlayerDyingAnimation();

	public abstract Map<Direction, TimedSequence<BufferedImage>> createSpouseMunchingAnimations();

	public abstract Map<Direction, TimedSequence<BufferedImage>> createGhostKickingAnimations(int ghostID);

	public abstract TimedSequence<BufferedImage> createGhostFrightenedAnimation();

	public abstract TimedSequence<BufferedImage> createGhostFlashingAnimation();

	public abstract Map<Direction, TimedSequence<BufferedImage>> createGhostReturningHomeAnimations();

	public abstract Map<Integer, BufferedImage> getNumberSpritesMap();

	protected Graphics2D smoothGC(Graphics2D g) {
		Graphics2D gc = (Graphics2D) g.create();
		gc.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
		return gc;
	}

	public void drawSprite(Graphics2D g, BufferedImage sprite, double x, double y) {
		Graphics2D gc = smoothGC(g);
		gc.drawImage(sprite, (int) x, (int) y, null);
		gc.dispose();
	}

	protected void drawEntity(Graphics2D g, GameEntity guy, BufferedImage guySprite) {
		if (guy.visible && guySprite != null) {
			int dx = guySprite.getWidth() / 2 - HTS, dy = guySprite.getHeight() / 2 - HTS;
			drawSprite(g, guySprite, guy.position.x - dx, guy.position.y - dy);
		}
	}

	public void drawFoodTiles(Graphics2D g, Stream<V2i> tiles, Predicate<V2i> eaten) {
		tiles.filter(eaten).forEach(tile -> drawTileCovered(g, tile));
	}

	public void drawTileCovered(Graphics2D g, V2i tile) {
		g.setColor(Color.BLACK);
		g.fillRect(tile.x * TS, tile.y * TS, TS, TS);
	}

	public void drawBonus(Graphics2D g, PacManBonus bonus) {
		drawEntity(g, bonus, bonusSprite(bonus));
	}

	public abstract void drawMaze(Graphics2D g, int mazeNumber, int i, int t, boolean running);

	public abstract void drawEnergizerTiles(Graphics2D g, Stream<V2i> energizerTiles);

	public void drawScore(Graphics2D g, AbstractGameModel game, boolean showHiscoreOnly) {
		g.setFont(getScoreFont());
		g.translate(0, 2);
		g.setColor(Color.WHITE);
		g.drawString("SCORE", t(1), t(1));
		g.drawString("HIGH SCORE", t(15), t(1));
		g.translate(0, 1);
		Color pointsColor = getMazeWallColor(game.currentLevel.mazeNumber - 1);
		if (pointsColor == Color.BLACK) {
			pointsColor = Color.YELLOW;
		}
		if (!showHiscoreOnly) {
			g.setColor(pointsColor);
			g.drawString(String.format("%08d", game.score), t(1), t(2));
			g.setColor(Color.LIGHT_GRAY);
			g.drawString(String.format("L%02d", game.currentLevelNumber), t(9), t(2));
		}
		g.setColor(pointsColor);
		g.drawString(String.format("%08d", game.highscorePoints), t(15), t(2));
		g.setColor(Color.LIGHT_GRAY);
		g.drawString(String.format("L%02d", game.highscoreLevel), t(23), t(2));
		g.translate(0, -3);
	}

	public abstract Color getMazeWallColor(int i);

	public void drawLivesCounter(Graphics2D g, AbstractGameModel game, int x, int y) {
		int maxLivesDisplayed = 5;
		for (int i = 0; i < Math.min(game.lives, maxLivesDisplayed); ++i) {
			drawSprite(g, lifeSprite(), x + t(2 * i), y);
		}
		if (game.lives > maxLivesDisplayed) {
			g.setColor(Color.YELLOW);
			g.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 6));
			g.drawString("+" + (game.lives - maxLivesDisplayed), x + t(10), y + t(1) - 2);
		}
	}

	public void drawLevelCounter(Graphics2D g, AbstractGameModel game, int rightX, int y) {
		int x = rightX;
		int firstLevel = Math.max(1, game.currentLevelNumber - 6);
		for (int level = firstLevel; level <= game.currentLevelNumber; ++level) {
			byte symbol = game.levelSymbols.get(level - 1);
			drawSprite(g, symbolSprite(symbol), x, y);
			x -= t(2);
		}
	}

	public void drawGameState(Graphics2D g, AbstractGameModel game, PacManGameState gameState) {
		if (gameState == PacManGameState.READY) {
			g.setFont(getScoreFont());
			g.setColor(Color.YELLOW);
			g.drawString("READY", t(11), t(21));
		} else if (gameState == PacManGameState.GAME_OVER) {
			g.setFont(getScoreFont());
			g.setColor(Color.RED);
			g.drawString("GAME", t(9), t(21));
			g.drawString("OVER", t(15), t(21));
		}
	}

	protected abstract BufferedImage symbolSprite(byte symbol);

	protected abstract BufferedImage bonusSprite(PacManBonus bonus);

	protected abstract BufferedImage lifeSprite();

//	public void drawMaze(Graphics2D g, GameModel game, int x, int y) {
//		if (mazeFlashing(game.level.mazeNumber).hasStarted()) {
//			BufferedImage image = (BufferedImage) mazeFlashing(game.level.mazeNumber).animate();
//			g.drawImage(image, x, y, null);
//			return;
//		}
//		if (foodAnimationOn && game.state == PacManGameState.HUNTING) {
//			drawMazeWithFoodAnimation(g, game, game.level.mazeNumber, x, y);
//		} else {
//			drawMazeWithFood(g, game, game.level.mazeNumber, x, y);
//		}
//		game.level.world.tiles().filter(game.level::isFoodRemoved).forEach(tile -> {
//			g.setColor(Color.BLACK);
//			if (game.level.world.isEnergizerTile(tile)) {
//				g.setColor(Color.BLACK);
//				g.fillRect(t(tile.x) - 1, t(tile.y) - 1, TS + 2, TS + 2);
//			} else {
//				g.fillRect(t(tile.x), t(tile.y), TS, TS);
//			}
//		});
//		if (energizerBlinking().isRunning() && energizerBlinking().animate()) {
//			game.level.world.energizerTiles().forEach(tile -> {
//				g.setColor(Color.BLACK);
//				g.fillRect(t(tile.x) - 1, t(tile.y) - 1, TS + 2, TS + 2);
//			});
//		}
//		drawBonus(g, game.bonus);
//	}
//
//	private void drawMazeWithFood(Graphics2D g, GameModel game, int mazeNumber, int x, int y) {
//		Graphics2D g2 = smoothGC(g);
//		drawFullMaze(g2, game, mazeNumber, x, y);
//		g2.dispose();
//		game.level.world.tiles().filter(game.level::containsEatenFood).forEach(tile -> {
//			g.setColor(Color.BLACK);
//			g.fillRect(t(tile.x), t(tile.y), TS, TS);
//		});
//	}
//
//	private void drawMazeWithFoodAnimation(Graphics2D g, GameModel game, int mazeNumber, int x, int y) {
//		drawEmptyMaze(g, game, mazeNumber, x, y);
//		game.level.world.tiles().filter(game.level::containsFood).forEach(tile -> {
//			if (game.level.world.isEnergizerTile(tile)) {
//				g.setColor(Color.PINK);
//				g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
//				g.fillOval(t(tile.x), t(tile.y), TS, TS);
//				g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_OFF);
//			} else {
//				long z = tile.x + tile.y;
//				z += God.clock.ticksTotal / 15;
//				int r = (int) (z % HTS) - 1;
//				r = Math.max(1, r);
//				g.setColor(Color.PINK);
//				g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
//				g.fillOval(t(tile.x) + HTS - r, t(tile.y) + HTS - r, 2 * r, 2 * r);
//				g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_OFF);
//			}
//		});
//	}

}