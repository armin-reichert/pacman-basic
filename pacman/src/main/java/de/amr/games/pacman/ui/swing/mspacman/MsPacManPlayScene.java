package de.amr.games.pacman.ui.swing.mspacman;

import static de.amr.games.pacman.game.core.PacManGameState.HUNTING;
import static de.amr.games.pacman.game.core.PacManGameWorld.HTS;
import static de.amr.games.pacman.game.core.PacManGameWorld.TS;
import static de.amr.games.pacman.game.core.PacManGameWorld.t;
import static de.amr.games.pacman.game.creatures.GhostState.DEAD;
import static de.amr.games.pacman.game.creatures.GhostState.ENTERING_HOUSE;
import static de.amr.games.pacman.game.creatures.GhostState.FRIGHTENED;
import static de.amr.games.pacman.game.creatures.GhostState.LOCKED;
import static de.amr.games.pacman.game.heaven.God.clock;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.util.stream.IntStream;

import de.amr.games.pacman.game.core.PacManGameModel;
import de.amr.games.pacman.game.core.PacManGameState;
import de.amr.games.pacman.game.creatures.Creature;
import de.amr.games.pacman.game.creatures.Ghost;
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
public class MsPacManPlayScene implements PacManGameScene, PacManGameAnimations {

	private final PacManGameSwingUI ui;
	private final V2i size;
	private final MsPacManAssets assets;
	private final PacManGameModel game;
	private Animation<BufferedImage> mazeFlashing;

	public MsPacManPlayScene(PacManGameSwingUI ui, V2i size, MsPacManAssets assets, PacManGameModel game) {
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
	public void startPacManCollapsing() {
		assets.pacCollapsing.restart();
	}

	@Override
	public void endPacManCollapsing() {
		assets.pacCollapsing.stop();
	}

	@Override
	public void startMazeFlashing(int numFlashes) {
		mazeFlashing = Animation.of(assets.mazeEmptyBright[game.level.mazeNumber - 1],
				assets.mazeEmptyDark[game.level.mazeNumber - 1]);
		mazeFlashing.frameDuration(15).repetitions(numFlashes).restart();
	}

	@Override
	public void endMazeFlashing() {
		mazeFlashing.stop();
		mazeFlashing = null;
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

	private Color getScoreColor() {
		switch (game.level.mazeNumber) {
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

	private void drawScore(Graphics2D g) {
		g.setFont(assets.scoreFont);
		g.translate(0, 2);
		g.setColor(Color.WHITE);
		g.drawString(ui.translation("SCORE"), t(1), t(1));
		g.drawString(ui.translation("HI_SCORE"), t(16), t(1));
		g.translate(0, 1);
		g.setColor(getScoreColor());
		g.drawString(String.format("%08d", game.score), t(1), t(2));
		g.setColor(Color.LIGHT_GRAY);
		g.drawString(String.format("L%02d", game.levelNumber), t(9), t(2));
		g.setColor(getScoreColor());
		g.drawString(String.format("%08d", game.highscorePoints), t(16), t(2));
		g.setColor(Color.LIGHT_GRAY);
		g.drawString(String.format("L%02d", game.highscoreLevel), t(24), t(2));
		g.translate(0, -3);
	}

	private void drawLivesCounter(Graphics2D g) {
		int maxLives = 5;
		int displayedLives = game.lives;
		int y = size.y - t(2);
		for (int i = 0; i < Math.min(displayedLives, maxLives); ++i) {
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
		for (int levelNumber = 1; levelNumber <= Math.min(game.levelNumber, 7); ++levelNumber) {
			BufferedImage symbol = assets.symbols[game.levelSymbols.get(levelNumber - 1)];
			g.drawImage(symbol, x, size.y - t(2), null);
			x -= t(2);
		}
	}

	private void drawMaze(Graphics2D g) {
		if (mazeFlashing != null) {
			// TODO fixme
			BufferedImage frame = mazeFlashing.sprite();
			if (frame != null) {
				g.drawImage(mazeFlashing.sprite(), 0, t(3), null);
			}
			return;
		}
		g.drawImage(assets.mazeFull[game.level.mazeNumber - 1], 0, t(3), null);
		IntStream.range(0, game.world.xTiles()).forEach(x -> {
			IntStream.range(4, game.world.yTiles() - 3).forEach(y -> {
				if (game.level.isFoodRemoved(x, y)
						|| game.state == HUNTING && game.world.isEnergizerTile(x, y) && clock.ticksTotal % 20 < 10) {
					g.setColor(Color.BLACK);
					g.fillRect(t(x), t(y), TS, TS);
				}
			});
		});
		drawBonus(g);
		if (PacManGameSwingUI.debugMode) {
			drawMazeStructure(g, game);
		}
	}

	private static final int BONUS_JUMP[] = { -2, 0, 2 };

	private void drawBonus(Graphics2D g) {
		int x = (int) (game.bonus.position.x) - HTS;
		int y = (int) (game.bonus.position.y) - HTS;
		if (game.bonus.edibleTicksLeft > 0) {
			int frame = clock.frame(20, BONUS_JUMP.length);
			if (game.bonus.dir == Direction.LEFT || game.bonus.dir == Direction.RIGHT) {
				y += BONUS_JUMP[frame]; // TODO this is not yet correct
			}
			g.drawImage(assets.symbols[game.bonus.symbol], x, y, null);
		} else if (game.bonus.eatenTicksLeft > 0) {
			g.drawImage(assets.numbers.get(game.bonus.points), x, y, null);
		}
	}

	private BufferedImage sprite(Pac pac) {
		if (pac.dead) {
			if (assets.pacCollapsing.isRunning() || assets.pacCollapsing.isComplete()) {
				return assets.pacCollapsing.sprite();
			}
			return assets.pacMouthOpen.get(pac.dir);
		}
		if (pac.speed == 0 || !pac.couldMove) {
			return assets.pacMouthOpen.get(pac.dir);
		}
		return assets.pacMunching.get(pac.dir).sprite();
	}

	private BufferedImage sprite(Ghost ghost) {
		if (ghost.bounty > 0) {
			return assets.bountyNumbers.get(ghost.bounty);
		}
		if (ghost.is(DEAD) || ghost.is(ENTERING_HOUSE)) {
			return assets.ghostEyes.get(ghost.wishDir);
		}
		if (ghost.is(FRIGHTENED)) {
			if (game.pac.powerTicksLeft <= assets.ghostFlashing.getDuration() * game.level.numFlashes
					&& game.state == PacManGameState.HUNTING) {
				return assets.ghostFlashing.sprite();
			}
			return assets.ghostBlue.sprite();
		}
		if (ghost.is(LOCKED) && game.pac.powerTicksLeft > 0) {
			return assets.ghostBlue.sprite();
		}
		return assets.ghostsWalking.get(ghost.id).get(ghost.wishDir).sprite();
	}
}