package de.amr.games.pacman.ui.swing.classic;

import static de.amr.games.pacman.model.creatures.GhostState.DEAD;
import static de.amr.games.pacman.model.creatures.GhostState.ENTERING_HOUSE;
import static de.amr.games.pacman.model.creatures.GhostState.FRIGHTENED;
import static de.amr.games.pacman.model.creatures.GhostState.LOCKED;
import static de.amr.games.pacman.world.PacManGameWorld.HTS;
import static de.amr.games.pacman.world.PacManGameWorld.TS;
import static de.amr.games.pacman.world.PacManGameWorld.t;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.util.function.Function;

import de.amr.games.pacman.heaven.God;
import de.amr.games.pacman.lib.Animation;
import de.amr.games.pacman.lib.Direction;
import de.amr.games.pacman.lib.V2i;
import de.amr.games.pacman.model.PacManGame;
import de.amr.games.pacman.model.creatures.Bonus;
import de.amr.games.pacman.model.creatures.Creature;
import de.amr.games.pacman.model.creatures.Ghost;
import de.amr.games.pacman.model.creatures.Pac;
import de.amr.games.pacman.ui.api.PacManGameAnimations;
import de.amr.games.pacman.ui.sound.PacManGameSoundManager;

/**
 * Rendering for the classic Pac-Man game.
 * 
 * @author Armin Reichert
 */
public class PacManClassicRendering implements PacManGameAnimations {

	public static boolean drawFancyFood;

	public final PacManClassicAssets assets;
	public final PacManGameSoundManager soundManager;
	public final Function<String, String> translator;

	public PacManClassicRendering(PacManClassicAssets assets, Function<String, String> translator) {
		this.assets = assets;
		this.translator = translator;
		soundManager = new PacManGameSoundManager(assets.soundURL::get);
	}

	@Override
	public Animation<BufferedImage> pacMunching(Direction dir) {
		return assets.pacMunching.get(dir);
	}

	@Override
	public Animation<BufferedImage> pacDying() {
		return assets.pacCollapsing;
	}

	@Override
	public Animation<BufferedImage> ghostWalking(Ghost ghost, Direction dir) {
		return assets.ghostsWalking.get(ghost.id).get(dir);
	}

	@Override
	public Animation<BufferedImage> ghostFrightened(Direction dir) {
		return assets.ghostBlue;
	}

	@Override
	public Animation<BufferedImage> ghostFlashing(Ghost ghost) {
		return assets.ghostsFlashing.get(ghost.id);
	}

	@Override
	public Animation<BufferedImage> mazeFlashing(int mazeNumber) {
		return assets.mazeFlashing;
	}

	@Override
	public Animation<Boolean> energizerBlinking() {
		return assets.energizerBlinking;
	}

	public void drawMaze(Graphics2D g, PacManGame game) {
		if (assets.mazeFlashing.isRunning() || assets.mazeFlashing.isComplete()) {
			g.drawImage(assets.mazeFlashing.currentFrameThenAdvance(), 0, t(3), null);
			return;
		}
		if (drawFancyFood) {
			drawFancyFood(g, game);
		} else {
			drawFood(g, game);
		}
		game.level.world.tiles().filter(game.level::isFoodRemoved).forEach(tile -> {
			g.setColor(Color.BLACK);
			g.fillRect(t(tile.x), t(tile.y), TS, TS);
		});
		if (energizerBlinking().isRunning() && energizerBlinking().currentFrameThenAdvance()) {
			game.level.world.energizerTiles().forEach(tile -> {
				g.setColor(Color.BLACK);
				g.fillRect(t(tile.x), t(tile.y), TS, TS);
			});
		}
		drawBonus(g, game.bonus);
	}

	private void drawFood(Graphics2D g, PacManGame game) {
		g.drawImage(assets.mazeFull, 0, t(3), null);
		game.level.world.tiles().filter(game.level::containsEatenFood).forEach(tile -> {
			g.setColor(Color.BLACK);
			g.fillRect(t(tile.x), t(tile.y), TS, TS);
		});
	}

	private void drawFancyFood(Graphics2D g, PacManGame game) {
		g.drawImage(assets.mazeEmpty, 0, t(3), null);
		game.level.world.tiles().filter(game.level::containsFood).forEach(tile -> {
			if (game.level.world.isEnergizerTile(tile)) {
				g.setColor(Color.PINK);
				g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
				g.fillOval(t(tile.x), t(tile.y), TS, TS);
				g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_OFF);
			} else {
				int change = (int) (God.clock.ticksTotal / 10);
				int i = (tile.x + tile.y + change) % 14;
				int r = i < 7 ? 1 + i / 2 : 1 + (14 - i) / 2;
				g.setColor(Color.PINK);
				g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
				g.fillOval(t(tile.x) + HTS - r / 2, t(tile.y) + HTS - r / 2, r, r);
				g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_OFF);
			}
		});
	}

	public void drawScore(Graphics2D g, PacManGame game) {
		g.setFont(assets.scoreFont);
		g.translate(0, 2);
		g.setColor(Color.WHITE);
		g.drawString(translator.apply("SCORE"), t(1), t(1));
		g.drawString(translator.apply("HI_SCORE"), t(16), t(1));
		g.translate(0, 1);
		g.setColor(Color.YELLOW);
		g.drawString(String.format("%08d", game.score), t(1), t(2));
		g.setColor(Color.LIGHT_GRAY);
		g.drawString(String.format("L%02d", game.currentLevelNumber), t(9), t(2));
		g.setColor(Color.YELLOW);
		g.drawString(String.format("%08d", game.highscorePoints), t(16), t(2));
		g.setColor(Color.LIGHT_GRAY);
		g.drawString(String.format("L%02d", game.highscoreLevel), t(24), t(2));
		g.translate(0, -3);
	}

	public void drawLivesCounter(Graphics2D g, PacManGame game, int x, int y) {
		int maxLivesDisplayed = 5;
		int livesDisplayed = game.started ? game.lives - 1 : game.lives;
		for (int i = 0; i < Math.min(livesDisplayed, maxLivesDisplayed); ++i) {
			g.drawImage(assets.life, x + t(2 * i), y, null);
		}
		if (game.lives > maxLivesDisplayed) {
			g.setColor(Color.YELLOW);
			g.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 6));
			g.drawString("+" + (game.lives - maxLivesDisplayed), x + t(10) - 4, y + t(2));
		}
	}

	public void drawLevelCounter(Graphics2D g, PacManGame game, int rightX, int y) {
		int x = rightX;
		int firstLevelNumber = Math.max(1, game.currentLevelNumber - 6);
		for (int levelNumber = firstLevelNumber; levelNumber <= game.currentLevelNumber; ++levelNumber) {
			V2i symbolTile = assets.symbolSpriteLocation[game.levelSymbols.get(levelNumber - 1)];
			g.drawImage(assets.spriteAt(symbolTile), x, y, null);
			x -= t(2);
		}
	}

	public void drawPac(Graphics2D g, Pac pac) {
		drawGuy(g, pac, sprite(pac));
	}

	public void drawBonus(Graphics2D g, Bonus bonus) {
		if (bonus.edibleTicksLeft > 0) {
			drawGuy(g, bonus, assets.spriteAt(assets.symbolSpriteLocation[bonus.symbol]));
		}
		if (bonus.eatenTicksLeft > 0) {
			if (bonus.points != 1000) {
				drawGuy(g, bonus, assets.numbers.get(bonus.points));
			} else {
				// this sprite is somewhat nasty
				g.drawImage(assets.numbers.get(1000), (int) (bonus.position.x) - HTS - 2, (int) (bonus.position.y) - HTS, null);
			}
		}
	}

	public void drawGhost(Graphics2D g, Ghost ghost, PacManGame game) {
		drawGuy(g, ghost, sprite(ghost, game));
	}

	private void drawGuy(Graphics2D g, Creature guy, BufferedImage sprite) {
		if (guy.visible) {
			int dx = (sprite.getWidth() - TS) / 2, dy = (sprite.getHeight() - TS) / 2;
			g.drawImage(sprite, (int) (guy.position.x) - dx, (int) (guy.position.y) - dy, null);
		}
	}

	private BufferedImage sprite(Pac pac) {
		if (pac.dead) {
			if (pacDying().isRunning() || pacDying().isComplete()) {
				return pacDying().currentFrameThenAdvance();
			}
			return assets.pacMouthClosed;
		}
		if (pac.speed == 0) {
			return assets.pacMouthClosed;
		}
		if (!pac.couldMove) {
			return assets.pacMouthOpen.get(pac.dir);
		}
		return pacMunching(pac.dir).currentFrameThenAdvance();
	}

	private BufferedImage sprite(Ghost ghost, PacManGame game) {
		if (ghost.bounty > 0) {
			return assets.numbers.get(ghost.bounty);
		}
		if (ghost.is(DEAD) || ghost.is(ENTERING_HOUSE)) {
			return assets.ghostEyes.get(ghost.wishDir);
		}
		if (ghost.is(FRIGHTENED)) {
			return ghostFlashing(ghost).isRunning() ? ghostFlashing(ghost).currentFrameThenAdvance()
					: assets.ghostBlue.currentFrameThenAdvance();
		}
		if (ghost.is(LOCKED) && game.pac.powerTicksLeft > 0) {
			return assets.ghostBlue.currentFrameThenAdvance();
		}
		return ghostWalking(ghost, ghost.wishDir).currentFrameThenAdvance();
	}
}