package de.amr.games.pacman.ui.swing.classic;

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

import de.amr.games.pacman.lib.V2i;
import de.amr.games.pacman.model.PacManGame;
import de.amr.games.pacman.model.creatures.Bonus;
import de.amr.games.pacman.model.creatures.Creature;
import de.amr.games.pacman.model.creatures.Ghost;
import de.amr.games.pacman.model.creatures.Pac;

/**
 * Rendering for the classic Pac-Man game.
 * 
 * @author Armin Reichert
 */
public class PacManClassicRendering {

	private final PacManClassicAssets assets;
	private final Function<String, String> translator;

	public PacManClassicRendering(PacManClassicAssets assets, Function<String, String> translator) {
		this.assets = assets;
		this.translator = translator;
	}

	public void drawMaze(Graphics2D g, PacManGame game) {
		if (assets.mazeFlashing.isRunning() || assets.mazeFlashing.isComplete()) {
			g.drawImage(assets.mazeFlashing.currentFrameThenAdvance(), 0, t(3), null);
			return;
		}
		g.drawImage(assets.mazeFull, 0, t(3), null);
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
		int first = Math.max(1, game.currentLevelNumber - 6);
		for (int levelNumber = first; levelNumber <= game.currentLevelNumber; ++levelNumber) {
			V2i symbolTile = assets.symbolTiles[game.levelSymbols.get(levelNumber - 1)];
			g.drawImage(assets.spriteAt(symbolTile), x, y, null);
			x -= t(2);
		}
	}

	public void drawPac(Graphics2D g, Pac pac) {
		drawGuy(g, pac, sprite(pac));
	}

	public void drawBonus(Graphics2D g, Bonus bonus) {
		if (bonus.edibleTicksLeft > 0) {
			drawGuy(g, bonus, assets.spriteAt(assets.symbolTiles[bonus.symbol]));
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

	private BufferedImage sprite(Pac pac) {
		if (pac.dead) {
			if (assets.pacCollapsing.isRunning() || assets.pacCollapsing.isComplete()) {
				return assets.pacCollapsing.currentFrameThenAdvance();
			}
			return assets.pacMouthClosed;
		}
		if (pac.speed == 0) {
			return assets.pacMouthClosed;
		}
		if (!pac.couldMove) {
			return assets.pacMouthOpen.get(pac.dir);
		}
		return assets.pacMunching.get(pac.dir).currentFrameThenAdvance();
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

	private BufferedImage sprite(Ghost ghost, PacManGame game) {
		if (ghost.bounty > 0) {
			return assets.numbers.get(ghost.bounty);
		}
		if (ghost.is(DEAD) || ghost.is(ENTERING_HOUSE)) {
			return assets.ghostEyes.get(ghost.wishDir);
		}
		if (ghost.is(FRIGHTENED)) {
			if (assets.ghostFlashing(ghost).isRunning()) {
				return assets.ghostFlashing(ghost).currentFrameThenAdvance();
			}
			return assets.ghostBlue.currentFrameThenAdvance();
		}
		if (ghost.is(LOCKED) && game.pac.powerTicksLeft > 0) {
			return assets.ghostBlue.currentFrameThenAdvance();
		}
		return assets.ghostWalking.get(ghost.id).get(ghost.wishDir).currentFrameThenAdvance();
	}
}