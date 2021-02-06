package de.amr.games.pacman.ui.swing.pacman;

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

import de.amr.games.pacman.controller.PacManGameState;
import de.amr.games.pacman.heaven.God;
import de.amr.games.pacman.lib.Animation;
import de.amr.games.pacman.lib.Direction;
import de.amr.games.pacman.lib.V2i;
import de.amr.games.pacman.model.AbstractPacManGame;
import de.amr.games.pacman.model.creatures.Bonus;
import de.amr.games.pacman.model.creatures.Ghost;
import de.amr.games.pacman.model.creatures.Pac;
import de.amr.games.pacman.ui.api.PacManGameAnimations;
import de.amr.games.pacman.ui.api.SpriteBasedRendering;
import de.amr.games.pacman.ui.sound.PacManGameSoundManager;
import de.amr.games.pacman.ui.swing.Spritesheet;

/**
 * Rendering for the classic Pac-Man game.
 * 
 * @author Armin Reichert
 */
public class PacManGameRendering implements SpriteBasedRendering, PacManGameAnimations {

	public static boolean foodAnimationOn = false;

	public final PacManGameAssets assets;
	public final PacManGameSoundManager soundManager;
	public final Function<String, String> translator;

	public PacManGameRendering(Function<String, String> translator) {
		assets = new PacManGameAssets();
		soundManager = new PacManGameSoundManager(assets.soundMap::get);
		this.translator = translator;
	}

	@Override
	public Spritesheet spritesheet() {
		return assets;
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
	public Animation<BufferedImage> ghostFrightened(Ghost ghost, Direction dir) {
		return assets.ghostBlue;
	}

	@Override
	public Animation<BufferedImage> ghostFlashing() {
		return assets.ghostFlashing;
	}

	@Override
	public Animation<BufferedImage> ghostReturningHome(Ghost ghost, Direction dir) {
		return assets.ghostEyes.get(dir);
	}

	@Override
	public Animation<BufferedImage> mazeFlashing(int mazeNumber) {
		return assets.mazeFlashing;
	}

	@Override
	public Animation<Boolean> energizerBlinking() {
		return assets.energizerBlinking;
	}

	@Override
	public void signalReadyState(Graphics2D g) {
		g.setFont(assets.getScoreFont());
		g.setColor(Color.YELLOW);
		g.drawString(translator.apply("READY"), t(11), t(21));
	}

	@Override
	public void signalGameOverState(Graphics2D g) {
		g.setFont(assets.getScoreFont());
		g.setColor(Color.RED);
		g.drawString(translator.apply("GAME"), t(9), t(21));
		g.drawString(translator.apply("OVER"), t(15), t(21));
	}

	@Override
	public void drawMaze(Graphics2D g, AbstractPacManGame game) {
		if (mazeFlashing(1).hasStarted()) {
			g.drawImage(mazeFlashing(1).animate(), 0, t(3), null);
			return;
		}
		if (foodAnimationOn && game.state == PacManGameState.HUNTING) {
			drawFoodAnimation(g, game);
		} else {
			drawFood(g, game);
		}
		game.level.world.tiles().filter(game.level::isFoodRemoved).forEach(tile -> {
			g.setColor(Color.BLACK);
			g.fillRect(t(tile.x) - 1, t(tile.y) - 1, TS + 2, TS + 2);
		});
		if (energizerBlinking().isRunning() && energizerBlinking().animate()) {
			game.level.world.energizerTiles().forEach(tile -> {
				g.setColor(Color.BLACK);
				// when smooth rendering is on, some artifacts remain, so draw a 2 pixel wider square
				g.fillRect(t(tile.x) - 1, t(tile.y) - 1, TS + 2, TS + 2);
			});
		}
		drawGuy(g, game.bonus, game);
	}

	private void drawFood(Graphics2D g, AbstractPacManGame game) {
		Graphics2D g2 = smoothGC(g);
		g2.drawImage(assets.mazeFull, 0, t(3), null);
		g2.dispose();
		game.level.world.tiles().filter(game.level::containsEatenFood).forEach(tile -> {
			g.setColor(Color.BLACK);
			g.fillRect(t(tile.x), t(tile.y), TS, TS);
		});
	}

	private void drawFoodAnimation(Graphics2D g, AbstractPacManGame game) {
		g.drawImage(assets.mazeEmpty, 0, t(3), null);
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
	public void drawScore(Graphics2D g, AbstractPacManGame game) {
		g.setFont(assets.scoreFont);
		g.translate(0, 1);
		g.setColor(Color.WHITE);
		g.drawString(translator.apply("SCORE"), t(1), t(1));
		g.drawString(translator.apply("HI_SCORE"), t(16), t(1));
		g.translate(0, 1);
		if (game.state != PacManGameState.INTRO && !game.attractMode) {
			g.setColor(Color.YELLOW);
			g.drawString(String.format("%08d", game.score), t(1), t(2));
			g.setColor(Color.LIGHT_GRAY);
			g.drawString(String.format("L%02d", game.currentLevelNumber), t(9), t(2));
			g.setColor(Color.YELLOW);
			g.drawString(String.format("%08d", game.highscorePoints), t(16), t(2));
			g.setColor(Color.LIGHT_GRAY);
			g.drawString(String.format("L%02d", game.highscoreLevel), t(24), t(2));
		}
		g.translate(0, -2);
	}

	@Override
	public void drawLivesCounter(Graphics2D g, AbstractPacManGame game, int x, int y) {
		Graphics2D g2 = smoothGC(g);
		int maxLivesDisplayed = 5;
		int livesDisplayed = game.started ? game.lives - 1 : game.lives;
		for (int i = 0; i < Math.min(livesDisplayed, maxLivesDisplayed); ++i) {
			g2.drawImage(assets.spriteAt(8, 1), x + t(2 * i), y, null);
		}
		if (game.lives > maxLivesDisplayed) {
			g2.setColor(Color.YELLOW);
			g2.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 6));
			g2.drawString("+" + (game.lives - maxLivesDisplayed), x + t(10) - 4, y + t(2));
		}
		g2.dispose();
	}

	@Override
	public void drawLevelCounter(Graphics2D g, AbstractPacManGame game, int rightX, int y) {
		Graphics2D g2 = smoothGC(g);
		int x = rightX;
		int firstLevelNumber = Math.max(1, game.currentLevelNumber - 6);
		for (int levelNumber = firstLevelNumber; levelNumber <= game.currentLevelNumber; ++levelNumber) {
			V2i symbolTile = assets.symbolSpriteLocation[game.levelSymbols.get(levelNumber - 1)];
			g2.drawImage(assets.spriteAt(symbolTile), x, y, null);
			x -= t(2);
		}
		g2.dispose();
	}

	@Override
	public BufferedImage bonusSprite(Bonus bonus, AbstractPacManGame game) {
		if (bonus.edibleTicksLeft > 0) {
			return assets.spriteAt(assets.symbolSpriteLocation[bonus.symbol]);
		}
		if (bonus.eatenTicksLeft > 0) {
			if (bonus.points != 1000) {
				return assets.numbers.get(bonus.points);
			} else {
				// this sprite is somewhat nasty
				BufferedImage sprite = assets.numbers.get(1000);
				return sprite; // TODO snip
			}
		}
		return null;
	}

	@Override
	public BufferedImage pacSprite(Pac pac, AbstractPacManGame game) {
		if (pac.dead) {
			return pacDying().hasStarted() ? pacDying().animate() : pacMunching(pac.dir).frame(0);
		}
		if (pac.speed == 0) {
			return pacMunching(pac.dir).frame(0);
		}
		if (!pac.couldMove) {
			return pacMunching(pac.dir).frame(1);
		}
		return pacMunching(pac.dir).animate();
	}

	@Override
	public BufferedImage ghostSprite(Ghost ghost, AbstractPacManGame game) {
		if (ghost.bounty > 0) {
			return assets.numbers.get(ghost.bounty);
		}
		if (ghost.is(DEAD) || ghost.is(ENTERING_HOUSE)) {
			return ghostReturningHome(ghost, ghost.dir).animate();
		}
		if (ghost.is(FRIGHTENED)) {
			return ghostFlashing().isRunning() ? ghostFlashing().frame() : ghostFrightened(ghost, ghost.dir).animate();
		}
		if (ghost.is(LOCKED) && game.pac.powerTicksLeft > 0) {
			return ghostFrightened(ghost, ghost.dir).animate();
		}
		return ghostWalking(ghost, ghost.wishDir).animate(); // Looks towards wish dir!
	}
}