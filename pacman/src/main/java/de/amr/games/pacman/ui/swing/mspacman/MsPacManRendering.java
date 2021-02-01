package de.amr.games.pacman.ui.swing.mspacman;

import static de.amr.games.pacman.controller.PacManGameState.HUNTING;
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

import de.amr.games.pacman.lib.Direction;
import de.amr.games.pacman.model.PacManGame;
import de.amr.games.pacman.model.creatures.Bonus;
import de.amr.games.pacman.model.creatures.Creature;
import de.amr.games.pacman.model.creatures.Ghost;
import de.amr.games.pacman.model.creatures.Pac;
import de.amr.games.pacman.ui.api.PacManGameAnimations;
import de.amr.games.pacman.ui.swing.Animation;

/**
 * Rendering for the Ms. Pac-Man game.
 * 
 * @author Armin Reichert
 */
public class MsPacManRendering implements PacManGameAnimations {

	public final MsPacManAssets assets;
	public final Function<String, String> translator;

	public MsPacManRendering(MsPacManAssets assets, Function<String, String> translator) {
		this.assets = assets;
		this.translator = translator;
	}

	@Override
	public Animation<BufferedImage> pacDying() {
		return assets.pacSpinning;
	}

	@Override
	public Animation<BufferedImage> ghostWalking(Ghost ghost, Direction dir) {
		return assets.ghostWalking.get(ghost.id).get(dir);
	}

	@Override
	public Animation<BufferedImage> ghostFlashing(Ghost ghost) {
		return assets.ghostFlashing.get(ghost.id);
	}

	@Override
	public Animation<BufferedImage> mazeFlashing(int mazeNumber) {
		return assets.mazeFlashingAnimations.get(mazeNumber - 1);
	}

	public void drawScore(Graphics2D g, PacManGame game) {
		g.setFont(assets.scoreFont);
		g.translate(0, 2);
		g.setColor(Color.WHITE);
		g.drawString(translator.apply("SCORE"), t(1), t(1));
		g.drawString(translator.apply("HI_SCORE"), t(16), t(1));
		g.translate(0, 1);
		g.setColor(getScoreColor(game.level.mazeNumber));
		g.drawString(String.format("%08d", game.score), t(1), t(2));
		g.setColor(Color.LIGHT_GRAY);
		g.drawString(String.format("L%02d", game.currentLevelNumber), t(9), t(2));
		g.setColor(getScoreColor(game.level.mazeNumber));
		g.drawString(String.format("%08d", game.highscorePoints), t(16), t(2));
		g.setColor(Color.LIGHT_GRAY);
		g.drawString(String.format("L%02d", game.highscoreLevel), t(24), t(2));
		g.translate(0, -3);
	}

	private Color getScoreColor(int mazeNumber) {
		switch (mazeNumber) {
		case 1:
			return new Color(255, 183, 174);
		case 2:
			return new Color(71, 183, 255);
		case 3:
			return new Color(222, 151, 81);
		case 4:
			return new Color(33, 33, 255);
		case 5:
			return new Color(255, 183, 255);
		case 6:
			return new Color(255, 183, 174);
		default:
			return Color.WHITE;
		}
	}

	public void drawLivesCounter(Graphics2D g, PacManGame game, int y) {
		int maxLivesDisplayed = 5;
		int livesDisplayed = game.started ? game.lives - 1 : game.lives;
		for (int i = 0; i < Math.min(livesDisplayed, maxLivesDisplayed); ++i) {
			g.drawImage(assets.life, t(2 * (i + 1)), y, null);
		}
		if (game.lives > maxLivesDisplayed) {
			g.setColor(Color.YELLOW);
			g.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 6));
			g.drawString("+" + (game.lives - maxLivesDisplayed), t(12) - 4, y + t(2));
		}
	}

	public void drawLevelCounter(Graphics2D g, PacManGame game, int y) {
		int x = t(game.level.world.xTiles() - 4);
		for (int levelNumber = 1; levelNumber <= Math.min(game.currentLevelNumber, 7); ++levelNumber) {
			byte symbol = game.levelSymbols.get(levelNumber - 1);
			g.drawImage(assets.spriteAt(assets.symbolTiles[symbol]), x, y, null);
			x -= t(2);
		}
	}

	public void drawMaze(Graphics2D g, PacManGame game) {
		Animation<BufferedImage> mazeFlashing = assets.mazeFlashingAnimations.get(game.level.mazeNumber - 1);
		if (mazeFlashing.isRunning() || mazeFlashing.isComplete()) {
			BufferedImage frame = mazeFlashing.currentFrameThenAdvance();
			if (frame != null) {
				g.drawImage(frame, 0, t(3), null);
			}
			return;
		}
		g.drawImage(assets.mazeFull[game.level.mazeNumber - 1], 0, t(3), null);
		game.level.world.tiles().filter(game.level::isFoodRemoved).forEach(tile -> {
			g.setColor(Color.BLACK);
			g.fillRect(t(tile.x), t(tile.y), TS, TS);
		});
		// TODO use animation instead?
		if (clock.ticksTotal % 20 < 10 && game.state == HUNTING) {
			game.level.world.energizerTiles().forEach(tile -> {
				g.setColor(Color.BLACK);
				g.fillRect(t(tile.x), t(tile.y), TS, TS);
			});
		}
		drawBonus(g, game.bonus);
	}

	private void drawGuy(Graphics2D g, Creature guy, BufferedImage sprite) {
		if (guy.visible) {
			int dx = (sprite.getWidth() - TS) / 2, dy = (sprite.getHeight() - TS) / 2;
			g.drawImage(sprite, (int) (guy.position.x) - dx, (int) (guy.position.y) - dy, null);
		}
	}

	public void drawPac(Graphics2D g, Pac pac) {
		drawGuy(g, pac, sprite(pac));
	}

	public void drawGhost(Graphics2D g, Ghost ghost, PacManGame game) {
		drawGuy(g, ghost, sprite(ghost, game));
	}

	private final int BONUS_JUMP[] = { -2, 0, 2 };

	public void drawBonus(Graphics2D g, Bonus bonus) {
		int x = (int) (bonus.position.x) - HTS;
		int y = (int) (bonus.position.y) - HTS;
		if (bonus.edibleTicksLeft > 0) {
			int frame = clock.frame(20, BONUS_JUMP.length);
			if (bonus.dir == Direction.LEFT || bonus.dir == Direction.RIGHT) {
				y += BONUS_JUMP[frame]; // TODO this is not yet correct
			}
			g.drawImage(assets.spriteAt(assets.symbolTiles[bonus.symbol]), x, y, null);
		} else if (bonus.eatenTicksLeft > 0) {
			g.drawImage(assets.spriteAt(assets.bonusValueTiles.get(bonus.points)), x, y, null);
		}
	}

	private BufferedImage sprite(Pac pac) {
		if (pac.dead) {
			if (assets.pacSpinning.isRunning() || assets.pacSpinning.isComplete()) {
				return assets.pacSpinning.currentFrameThenAdvance();
			}
			return assets.pacMouthOpen.get(pac.dir);
		}
		if (pac.speed == 0 || !pac.couldMove) {
			return assets.pacMouthOpen.get(pac.dir);
		}
		return assets.pacMunching.get(pac.dir).currentFrameThenAdvance();
	}

	private BufferedImage sprite(Ghost ghost, PacManGame game) {
		if (ghost.bounty > 0) {
			return assets.spriteAt(assets.bountyNumberTiles.get(ghost.bounty));
		}
		if (ghost.is(DEAD) || ghost.is(ENTERING_HOUSE)) {
			return assets.ghostEyes.get(ghost.wishDir);
		}
		if (ghost.is(FRIGHTENED)) {
			if (ghostFlashing(ghost).isRunning()) {
				return ghostFlashing(ghost).currentFrameThenAdvance();
			}
			return assets.ghostBlue.currentFrameThenAdvance();
		}
		if (ghost.is(LOCKED) && game.pac.powerTicksLeft > 0) {
			return assets.ghostBlue.currentFrameThenAdvance();
		}
		return assets.ghostWalking.get(ghost.id).get(ghost.wishDir).currentFrameThenAdvance();
	}
}