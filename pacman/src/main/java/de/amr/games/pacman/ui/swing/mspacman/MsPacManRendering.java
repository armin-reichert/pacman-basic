package de.amr.games.pacman.ui.swing.mspacman;

import static de.amr.games.pacman.heaven.God.clock;
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
import java.awt.image.BufferedImage;
import java.util.function.Function;

import de.amr.games.pacman.lib.Animation;
import de.amr.games.pacman.lib.Direction;
import de.amr.games.pacman.model.PacManGame;
import de.amr.games.pacman.model.creatures.Bonus;
import de.amr.games.pacman.model.creatures.Creature;
import de.amr.games.pacman.model.creatures.Ghost;
import de.amr.games.pacman.model.creatures.Pac;
import de.amr.games.pacman.ui.api.PacManGameAnimations;
import de.amr.games.pacman.ui.sound.PacManGameSoundManager;

/**
 * Rendering for the Ms. Pac-Man game.
 * 
 * @author Armin Reichert
 */
public class MsPacManRendering implements PacManGameAnimations {

	public final MsPacManAssets assets;
	public final Function<String, String> translator;
	public final PacManGameSoundManager soundManager;

	public MsPacManRendering(MsPacManAssets assets, Function<String, String> translator) {
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
		return assets.pacSpinning;
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
		return assets.mazesFlashing.get(mazeNumber - 1);
	}

	@Override
	public Animation<Boolean> energizerBlinking() {
		return assets.energizerBlinking;
	}

	public void drawScore(Graphics2D g, PacManGame game) {
		g.setFont(assets.scoreFont);
		g.translate(0, 2);
		g.setColor(Color.WHITE);
		g.drawString(translator.apply("SCORE"), t(1), t(1));
		g.drawString(translator.apply("HI_SCORE"), t(16), t(1));
		g.translate(0, 1);
		g.setColor(assets.getMazeWallColor(game.level.mazeNumber - 1));
		g.drawString(String.format("%08d", game.score), t(1), t(2));
		g.setColor(Color.LIGHT_GRAY);
		g.drawString(String.format("L%02d", game.currentLevelNumber), t(9), t(2));
		g.setColor(assets.getMazeWallColor(game.level.mazeNumber - 1));
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
		for (int firstlevelNumber = 1; firstlevelNumber <= Math.min(game.currentLevelNumber, 7); ++firstlevelNumber) {
			byte symbol = game.levelSymbols.get(firstlevelNumber - 1);
			g.drawImage(assets.spriteAt(assets.symbolSpriteLocations[symbol]), x, y, null);
			x -= t(2);
		}
	}

	public void drawMaze(Graphics2D g, PacManGame game) {
		Animation<BufferedImage> mazeFlashing = mazeFlashing(game.level.mazeNumber);
		if (mazeFlashing.isRunning() || mazeFlashing.isComplete()) {
			g.drawImage(mazeFlashing.currentFrameThenAdvance(), 0, t(3), null);
			return;
		}
		g.drawImage(assets.mazeFull[game.level.mazeNumber - 1], 0, t(3), null);
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

	public void drawPac(Graphics2D g, Pac pac) {
		drawGuy(g, pac, sprite(pac));
	}

	public void drawGhost(Graphics2D g, Ghost ghost, PacManGame game) {
		drawGuy(g, ghost, sprite(ghost, game));
	}

	private final int BONUS_JUMP[] = { -2, 2 };

	public void drawBonus(Graphics2D g, Bonus bonus) {
		int x = (int) (bonus.position.x) - HTS;
		int y = (int) (bonus.position.y) - HTS;
		if (bonus.edibleTicksLeft > 0) {
			int frame = clock.frame(20, BONUS_JUMP.length);
			y += BONUS_JUMP[frame]; // TODO not yet perfect
			g.drawImage(assets.spriteAt(assets.symbolSpriteLocations[bonus.symbol]), x, y, null);
		} else if (bonus.eatenTicksLeft > 0) {
			g.drawImage(assets.spriteAt(assets.bonusValueSpriteLocations.get(bonus.points)), x, y, null);
		}
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
			return assets.pacMouthOpen.get(pac.dir);
		}
		if (pac.speed == 0 || !pac.couldMove) {
			return assets.pacMouthOpen.get(pac.dir);
		}
		return pacMunching(pac.dir).currentFrameThenAdvance();
	}

	private BufferedImage sprite(Ghost ghost, PacManGame game) {
		if (ghost.bounty > 0) {
			return assets.spriteAt(assets.bountyNumberSpriteLocations.get(ghost.bounty));
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