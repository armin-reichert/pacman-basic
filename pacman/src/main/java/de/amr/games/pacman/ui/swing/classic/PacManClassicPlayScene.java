package de.amr.games.pacman.ui.swing.classic;

import static de.amr.games.pacman.game.core.PacManGameState.HUNTING;
import static de.amr.games.pacman.game.core.PacManGameWorld.HTS;
import static de.amr.games.pacman.game.core.PacManGameWorld.TS;
import static de.amr.games.pacman.game.core.PacManGameWorld.t;
import static de.amr.games.pacman.game.creatures.GhostState.FRIGHTENED;
import static de.amr.games.pacman.game.creatures.GhostState.LOCKED;
import static de.amr.games.pacman.game.heaven.God.clock;
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
import de.amr.games.pacman.lib.Direction;
import de.amr.games.pacman.lib.V2i;
import de.amr.games.pacman.ui.api.PacManGameAnimations;
import de.amr.games.pacman.ui.api.PacManGameScene;
import de.amr.games.pacman.ui.swing.Animation;
import de.amr.games.pacman.ui.swing.PacManGameSwingUI;

/**
 * Scene where the game is played.
 * 
 * @author Armin Reichert
 */
public class PacManClassicPlayScene implements PacManGameScene, PacManGameAnimations {

	private final PacManGameSwingUI ui;
	private final V2i size;
	private final PacManClassicAssets assets;
	private final PacManGameModel game;

	public PacManClassicPlayScene(PacManGameSwingUI ui, V2i size, PacManClassicAssets assets, PacManGameModel game) {
		this.ui = ui;
		this.size = size;
		this.assets = assets;
		this.game = game;
	}

	@Override
	public V2i size() {
		return size;
	}

	@Override
	public Animation<BufferedImage> pacCollapsing() {
		return assets.pacCollapsing;
	}

	@Override
	public Animation<BufferedImage> ghostWalking(Ghost ghost, Direction dir) {
		return assets.ghostWalking.get(ghost.id).get(dir);
	}

	@Override
	public Animation<BufferedImage> mazeFlashing(int mazeNumber, int numFlashes) {
		return assets.mazeFlashing.repetitions(numFlashes);
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
			V2i symbolTile = assets.symbolTiles[game.levelSymbols.get(levelNumber - 1)];
			g.drawImage(assets.spriteAt(symbolTile), x, size.y - t(2), null);
			x -= t(2);
		}
	}

	private void drawMaze(Graphics2D g) {
		if (assets.mazeFlashing.isRunning()) {
			g.drawImage(assets.mazeFlashing.currentFrameThenAdvance(), 0, t(3), null);
			return;
		}
		g.drawImage(assets.mazeFull, 0, t(3), null);
		range(0, game.world.xTiles()).forEach(x -> {
			range(4, game.world.yTiles() - 3).forEach(y -> {
				V2i tile = new V2i(x, y);
				if (game.level.isFoodRemoved(tile)
						|| game.state == HUNTING && game.world.isEnergizerTile(tile) && clock.ticksTotal % 20 < 10) {
					g.setColor(Color.BLACK);
					g.fillRect(t(x), t(y), TS, TS);
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
			drawGuy(g, bonus, assets.spriteAt(assets.symbolTiles[bonus.symbol]));
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

	private BufferedImage sprite(Ghost ghost) {
		if (ghost.bounty > 0) {
			return assets.numbers.get(ghost.bounty);
		}
		if (ghost.state == GhostState.DEAD || ghost.state == GhostState.ENTERING_HOUSE) {
			return assets.ghostEyes.get(ghost.wishDir);
		}
		if (ghost.is(FRIGHTENED)) {
			if (game.pac.powerTicksLeft <= assets.ghostFlashing.getDuration() * game.level.numFlashes
					&& game.state == PacManGameState.HUNTING) {
				return assets.ghostFlashing.currentFrameThenAdvance();
			}
			return assets.ghostBlue.currentFrameThenAdvance();
		}
		if (ghost.is(LOCKED) && game.pac.powerTicksLeft > 0) {
			return assets.ghostBlue.currentFrameThenAdvance();
		}
		return assets.ghostWalking.get(ghost.id).get(ghost.wishDir).currentFrameThenAdvance();
	}
}