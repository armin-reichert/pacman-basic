package de.amr.games.pacman.ui.swing.rendering;

import static de.amr.games.pacman.world.PacManGameWorld.HTS;
import static de.amr.games.pacman.world.PacManGameWorld.TS;
import static de.amr.games.pacman.world.PacManGameWorld.t;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.util.ResourceBundle;

import de.amr.games.pacman.controller.PacManGameState;
import de.amr.games.pacman.heaven.God;
import de.amr.games.pacman.model.Bonus;
import de.amr.games.pacman.model.Creature;
import de.amr.games.pacman.model.Ghost;
import de.amr.games.pacman.model.Pac;
import de.amr.games.pacman.model.PacManGameModel;
import de.amr.games.pacman.ui.PacManGameAnimation;
import de.amr.games.pacman.ui.swing.assets.AssetLoader;

public abstract class DefaultGameRendering implements SpriteBasedSceneRendering, PacManGameAnimation {

	public static boolean foodAnimationOn = false; // experimental

	public final ResourceBundle translations;
	public final Font font;

	public DefaultGameRendering() {
		translations = ResourceBundle.getBundle("localization.translation");
		font = AssetLoader.font("/emulogic.ttf", 8);
	}

	protected void drawGuy(Graphics2D g, Creature guy, PacManGameModel game) {
		if (guy.visible) {
			BufferedImage sprite = sprite(guy, game);
			if (sprite != null) {
				int dx = sprite.getWidth() / 2 - HTS, dy = sprite.getHeight() / 2 - HTS;
				Graphics2D g2 = smoothGC(g);
				g2.drawImage(sprite, (int) guy.position.x - dx, (int) guy.position.y - dy, null);
				g2.dispose();
			}
		}
	}

	@Override
	public void drawPac(Graphics2D g, Pac pac, PacManGameModel game) {
		drawGuy(g, pac, game);
	}

	@Override
	public void drawGhost(Graphics2D g, Ghost ghost, PacManGameModel game) {
		drawGuy(g, ghost, game);
	}

	@Override
	public void drawBonus(Graphics2D g, Bonus bonus, PacManGameModel game) {
		drawGuy(g, bonus, game);
	}

	@Override
	public void drawLivesCounter(Graphics2D g, PacManGameModel game, int x, int y) {
		int maxLivesDisplayed = 5;
		int livesDisplayed = game.started ? game.lives - 1 : game.lives;
		Graphics2D g2 = smoothGC(g);
		for (int i = 0; i < Math.min(livesDisplayed, maxLivesDisplayed); ++i) {
			g2.drawImage(lifeSprite(), x + t(2 * i), y, null);
		}
		if (game.lives > maxLivesDisplayed) {
			g2.setColor(Color.YELLOW);
			g2.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 6));
			g2.drawString("+" + (game.lives - maxLivesDisplayed), x + t(10) - 4, y + t(2));
		}
		g2.dispose();
	}

	@Override
	public void drawMaze(Graphics2D g, PacManGameModel game, int x, int y) {
		if (mazeFlashing(game.level.mazeNumber).hasStarted()) {
			// TODO avoid this cats
			BufferedImage image = (BufferedImage) mazeFlashing(game.level.mazeNumber).animate();
			g.drawImage(image, x, y, null);
			return;
		}
		if (foodAnimationOn && game.state == PacManGameState.HUNTING) {
			drawMazeWithFoodAnimation(g, game, game.level.mazeNumber, x, y);
		} else {
			drawMazeWithFood(g, game, game.level.mazeNumber, x, y);
		}
		game.level.world.tiles().filter(game.level::isFoodRemoved).forEach(tile -> {
			g.setColor(Color.BLACK);
			if (game.level.world.isEnergizerTile(tile)) {
				g.setColor(Color.BLACK);
				g.fillRect(t(tile.x) - 1, t(tile.y) - 1, TS + 2, TS + 2);
			} else {
				g.fillRect(t(tile.x), t(tile.y), TS, TS);
			}
		});
		if (energizerBlinking().isRunning() && energizerBlinking().animate()) {
			game.level.world.energizerTiles().forEach(tile -> {
				g.setColor(Color.BLACK);
				g.fillRect(t(tile.x) - 1, t(tile.y) - 1, TS + 2, TS + 2);
			});
		}
		drawBonus(g, game.bonus, game);
	}

	private void drawMazeWithFood(Graphics2D g, PacManGameModel game, int mazeNumber, int x, int y) {
		Graphics2D g2 = smoothGC(g);
		drawFullMaze(g2, game, mazeNumber, x, y);
		g2.dispose();
		game.level.world.tiles().filter(game.level::containsEatenFood).forEach(tile -> {
			g.setColor(Color.BLACK);
			g.fillRect(t(tile.x), t(tile.y), TS, TS);
		});
	}

	private void drawMazeWithFoodAnimation(Graphics2D g, PacManGameModel game, int mazeNumber, int x, int y) {
		drawEmptyMaze(g, game, mazeNumber, x, y);
		game.level.world.tiles().filter(game.level::containsFood).forEach(tile -> {
			if (game.level.world.isEnergizerTile(tile)) {
				g.setColor(Color.PINK);
				g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
				g.fillOval(t(tile.x), t(tile.y), TS, TS);
				g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_OFF);
			} else {
				long z = tile.x + tile.y;
				z += God.clock.ticksTotal / 15;
				int r = (int) (z % HTS) - 1;
				r = Math.max(1, r);
				g.setColor(Color.PINK);
				g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
				g.fillOval(t(tile.x) + HTS - r, t(tile.y) + HTS - r, 2 * r, 2 * r);
				g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_OFF);
			}
		});
	}

	@Override
	public void signalReadyState(Graphics2D g) {
		g.setFont(font);
		g.setColor(Color.YELLOW);
		g.drawString(translations.getString("READY"), t(11), t(21));
	}

	@Override
	public void signalGameOverState(Graphics2D g) {
		g.setFont(font);
		g.setColor(Color.RED);
		g.drawString(translations.getString("GAME"), t(9), t(21));
		g.drawString(translations.getString("OVER"), t(15), t(21));
	}
}
