package de.amr.games.pacman.ui.swing.classic;

import static de.amr.games.pacman.game.creatures.GhostState.FRIGHTENED;
import static de.amr.games.pacman.game.creatures.GhostState.LOCKED;
import static de.amr.games.pacman.game.worlds.PacManGameWorld.HTS;
import static de.amr.games.pacman.game.worlds.PacManGameWorld.TS;
import static de.amr.games.pacman.game.worlds.PacManGameWorld.t;
import static java.util.stream.IntStream.range;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;

import de.amr.games.pacman.game.core.PacManGameModel;
import de.amr.games.pacman.game.core.PacManGameState;
import de.amr.games.pacman.game.creatures.Bonus;
import de.amr.games.pacman.game.creatures.Creature;
import de.amr.games.pacman.game.creatures.Ghost;
import de.amr.games.pacman.game.creatures.GhostState;
import de.amr.games.pacman.game.creatures.Pac;
import de.amr.games.pacman.game.heaven.God;
import de.amr.games.pacman.lib.V2i;
import de.amr.games.pacman.ui.api.PacManAnimations;
import de.amr.games.pacman.ui.swing.PacManGameSwingUI;
import de.amr.games.pacman.ui.swing.scene.PacManGameScene;

/**
 * Scene where the game is played.
 * 
 * @author Armin Reichert
 */
public class PacManClassicPlayScene implements PacManGameScene, PacManAnimations {

	private final PacManGameSwingUI ui;
	private final PacManGameModel game;
	private final V2i size;
	private final PacManClassicAssets assets;

	public PacManClassicPlayScene(PacManGameSwingUI ui, PacManGameModel game, V2i size, PacManClassicAssets assets) {
		this.ui = ui;
		this.game = game;
		this.size = size;
		this.assets = assets;
	}

	@Override
	public V2i size() {
		return size;
	}

	@Override
	public void startPacManCollapsing() {
		assets.pacCollapsing.reset();
		assets.pacCollapsing.start();
	}

	@Override
	public void endPacManCollapsing() {
		assets.pacCollapsing.stop();
		assets.pacCollapsing.reset();
	}

	@Override
	public void startMazeFlashing(int repetitions) {
		assets.mazeFlashing.setRepetitions(repetitions);
		assets.mazeFlashing.reset();
		assets.mazeFlashing.start();
	}

	@Override
	public void endMazeFlashing() {
		assets.mazeFlashing.stop();
	}

	@Override
	public void draw(Graphics2D g) {
		drawScore(g);
		drawLivesCounter(g);
		drawLevelCounter(g);
		drawMaze(g);
		drawGuy(g, game.pac, sprite(game.pac));
		for (Ghost ghost : game.ghosts) {
			drawGuy(g, ghost, sprite(ghost));
		}
		drawDebugInfo(g, game);
	}

	private void drawGuy(Graphics2D g, Creature guy, BufferedImage sprite) {
		if (guy.visible) {
			int dx = (sprite.getWidth() - TS) / 2, dy = (sprite.getHeight() - TS) / 2;
			g.drawImage(sprite, (int) (guy.position.x) - dx, (int) (guy.position.y) - dy, null);
		}
	}

	private void drawScore(Graphics2D g) {
		g.setFont(assets.scoreFont);
		g.translate(0, 2);
		g.setColor(Color.WHITE);
		g.drawString(ui.translation("SCORE"), t(1), t(1));
		g.drawString(ui.translation("HI_SCORE"), t(16), t(1));
		g.translate(0, 1);
		g.setColor(Color.YELLOW);
		g.drawString(String.format("%08d", game.score), t(1), t(2));
		g.setColor(Color.LIGHT_GRAY);
		g.drawString(String.format("L%02d", game.levelNumber), t(9), t(2));
		g.setColor(Color.YELLOW);
		g.drawString(String.format("%08d", game.highscorePoints), t(16), t(2));
		g.setColor(Color.LIGHT_GRAY);
		g.drawString(String.format("L%02d", game.highscoreLevel), t(24), t(2));
		g.translate(0, -3);
	}

	private void drawLivesCounter(Graphics2D g) {
		int maxLives = 5;
		int y = size.y - t(2);
		for (int i = 0; i < Math.min(game.lives, maxLives); ++i) {
			g.drawImage(assets.life, t(2 * (i + 1)), y, null);
		}
		if (game.lives > maxLives) {
			g.setColor(Color.YELLOW);
			g.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 6));
			g.drawString("+" + (game.lives - maxLives), t(12) - 4, y + t(2));
		}
	}

	private void drawLevelCounter(Graphics2D g) {
		int x = t(game.world.xTiles() - 4);
		int first = Math.max(1, game.levelNumber - 6);
		for (int levelNumber = first; levelNumber <= game.levelNumber; ++levelNumber) {
			BufferedImage symbol = assets.symbols[game.levelSymbols.get(levelNumber - 1)];
			g.drawImage(symbol, x, size.y - t(2), null);
			x -= t(2);
		}
	}

	private void hideFood(Graphics2D g, int x, int y) {
		g.setColor(Color.BLACK);
		g.fillRect(t(x), t(y), TS, TS);
	}

	private void drawMaze(Graphics2D g) {
		if (assets.mazeFlashing.isRunning()) {
			g.drawImage(assets.mazeFlashing.frame(), 0, t(3), null);
			return;
		}
		g.drawImage(assets.mazeFull, 0, t(3), null);
		range(0, game.world.xTiles()).forEach(x -> {
			range(4, game.world.yTiles() - 3).forEach(y -> {
				if (game.level.isFoodRemoved(x, y)) {
					hideFood(g, x, y);
				} else if (game.state == PacManGameState.HUNTING && game.world.isEnergizerTile(x, y)) {
					God.clock.runOrBeIdle(10, () -> hideFood(g, x, y));
				}
			});
		});
		drawBonus(g, game.bonus);
		if (PacManGameSwingUI.debugMode) {
			drawMazeStructure(g, game);
		}
	}

	private void drawBonus(Graphics2D g, Bonus bonus) {
		if (bonus.edibleTicksLeft > 0) {
			drawGuy(g, bonus, assets.symbols[bonus.symbol]);
		}
		if (bonus.eatenTicksLeft > 0) {
			if (game.bonus.points != 1000) {
				drawGuy(g, game.bonus, assets.numbers.get(game.bonus.points));
			} else {
				// this sprite is somewhat nasty
				g.drawImage(assets.numbers.get(1000), (int) (bonus.position.x) - HTS - 2, (int) (bonus.position.y) - HTS, null);
			}
		}
	}

	private BufferedImage sprite(Pac pac) {
		if (pac.dead) {
			return assets.pacCollapsing.isRunning() ? assets.pacCollapsing.frame() : assets.pacMouthClosed;
		}
		if (pac.speed == 0) {
			return assets.pacMouthClosed;
		}
		if (!pac.couldMove) {
			return assets.pacMouthOpen.get(pac.dir);
		}
		return assets.pacWalking.get(pac.dir).frame();
	}

	private BufferedImage sprite(Ghost ghost) {
		if (ghost.bounty > 0) {
			return assets.numbers.get(ghost.bounty);
		}
		if (ghost.state == GhostState.DEAD || ghost.state == GhostState.ENTERING_HOUSE) {
			return assets.ghostEyes.get(ghost.wishDir);
		}
		if (ghost.is(FRIGHTENED)) {
			if (game.pac.powerTicksLeft <= assets.ghostFlashing.get(ghost.id).getDuration() * game.level.numFlashes
					&& game.state == PacManGameState.HUNTING) {
				return assets.ghostFlashing.get(ghost.id).frame();
			}
			return assets.ghostBlue.frame();
		}
		if (ghost.is(LOCKED) && game.pac.powerTicksLeft > 0) {
			return assets.ghostBlue.frame();
		}
		return assets.ghostsWalking.get(ghost.id).get(ghost.wishDir).frame();
	}
}