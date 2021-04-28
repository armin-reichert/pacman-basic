package de.amr.games.pacman.ui.swing.rendering.common;

import static de.amr.games.pacman.model.world.PacManGameWorld.HTS;
import static de.amr.games.pacman.model.world.PacManGameWorld.TS;
import static de.amr.games.pacman.model.world.PacManGameWorld.t;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.util.Map;
import java.util.function.Predicate;
import java.util.stream.Stream;

import de.amr.games.pacman.controller.PacManGameState;
import de.amr.games.pacman.lib.Direction;
import de.amr.games.pacman.lib.TimedSequence;
import de.amr.games.pacman.lib.V2i;
import de.amr.games.pacman.model.common.GameEntity;
import de.amr.games.pacman.model.common.GameModel;

/**
 * Spritesheet-based rendering for Pac-Man and Ms. Pac-Man game.
 * 
 * @author Armin Reichert
 */
public abstract class AbstractPacManGameRendering {

	public abstract Map<Direction, TimedSequence<BufferedImage>> createPlayerMunchingAnimations();

	public abstract TimedSequence<BufferedImage> createPlayerDyingAnimation();

	public abstract Map<Direction, TimedSequence<BufferedImage>> createGhostKickingAnimations(int ghostID);

	public abstract TimedSequence<BufferedImage> createGhostFrightenedAnimation();

	public abstract TimedSequence<BufferedImage> createGhostFlashingAnimation();

	public abstract Map<Direction, TimedSequence<BufferedImage>> createGhostReturningHomeAnimations();

	public abstract Map<String, BufferedImage> getSymbolSpritesMap();

	public abstract Map<Integer, BufferedImage> getBountyNumberSpritesMap();

	public abstract Map<Integer, BufferedImage> getBonusNumberSpritesMap();

	public abstract BufferedImage symbolSprite(String symbol);

	public abstract BufferedImage lifeSprite();

	public abstract TimedSequence<BufferedImage> mazeFlashing(int mazeNumber);

	public abstract Color getMazeWallColor(int mazeIndex);

	public abstract Color getMazeWallBorderColor(int mazeIndex);

	public abstract Font getScoreFont();

	// only use in Pac-Man:

	public TimedSequence<BufferedImage> createBlinkyStretchedAnimation() {
		return null;
	}

	public TimedSequence<BufferedImage> createBlinkyDamagedAnimation() {
		return null;
	}

	// only used in Ms. Pac-Man:

	public TimedSequence<Integer> createBonusAnimation() {
		return null;
	}

	public Map<Direction, TimedSequence<BufferedImage>> createSpouseMunchingAnimations() {
		return null;
	}

	public TimedSequence<BufferedImage> createFlapAnimation() {
		return null;
	}

	public TimedSequence<BufferedImage> createStorkFlyingAnimation() {
		return null;
	}

	public BufferedImage getBlueBag() {
		return null;
	}

	public BufferedImage getJunior() {
		return null;
	}

	public BufferedImage getHeart() {
		return null;
	}

	// drawing

	protected void drawEntitySprite(Graphics2D g, GameEntity entity, BufferedImage sprite) {
		if (entity.visible && sprite != null) {
			int dx = HTS - sprite.getWidth() / 2, dy = HTS - sprite.getHeight() / 2;
			g.drawImage(sprite, (int) (entity.position.x + dx), (int) (entity.position.y + dy), null);
		}
	}

	public void hideEatenFood(Graphics2D g, Stream<V2i> tiles, Predicate<V2i> eaten) {
		g.setColor(Color.BLACK);
		tiles.filter(eaten).forEach(tile -> {
			g.fillRect(tile.x * TS, tile.y * TS, TS, TS);
		});
	}

	public abstract void drawMaze(Graphics2D g, int mazeNumber, int i, int t, boolean running);

	public void drawScore(Graphics2D g, GameModel game, boolean showHiscoreOnly) {
		g.setFont(getScoreFont());
		g.translate(0, 2);
		g.setColor(Color.WHITE);
		g.drawString("SCORE", t(1), t(1));
		g.drawString("HIGH SCORE", t(15), t(1));
		g.translate(0, 1);
		Color pointsColor = getMazeWallColor(game.currentLevel().mazeNumber - 1);
		if (pointsColor == Color.BLACK) {
			pointsColor = Color.YELLOW;
		}
		if (!showHiscoreOnly) {
			g.setColor(pointsColor);
			g.drawString(String.format("%08d", game.score()), t(1), t(2));
			g.setColor(Color.LIGHT_GRAY);
			g.drawString(String.format("L%02d", game.currentLevel().number), t(9), t(2));
		}
		g.setColor(pointsColor);
		g.drawString(String.format("%08d", game.hiscorePoints()), t(15), t(2));
		g.setColor(Color.LIGHT_GRAY);
		g.drawString(String.format("L%02d", game.hiscoreLevel()), t(23), t(2));
		g.translate(0, -3);
	}

	public void drawLivesCounter(Graphics2D g, GameModel game, int x, int y) {
		int maxLivesDisplayed = 5;
		for (int i = 0; i < Math.min(game.lives(), maxLivesDisplayed); ++i) {
			g.drawImage(lifeSprite(), x + t(2 * i), y, null);
		}
		if (game.lives() > maxLivesDisplayed) {
			g.setColor(Color.YELLOW);
			g.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 6));
			g.drawString("+" + (game.lives() - maxLivesDisplayed), x + t(10), y + t(1) - 2);
		}
	}

	public void drawLevelCounter(Graphics2D g, GameModel game, int rightX, int y) {
		int x = rightX;
		int firstLevel = Math.max(1, game.currentLevel().number - 6);
		for (int level = firstLevel; level <= game.currentLevel().number; ++level) {
			String symbol = game.levelSymbol(level);
			g.drawImage(symbolSprite(symbol), x, y, null);
			x -= t(2);
		}
	}

	public void drawGameState(Graphics2D g, GameModel game, PacManGameState gameState) {
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