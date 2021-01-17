package de.amr.games.pacman.ui.swing.classic;

import static de.amr.games.pacman.ui.swing.classic.PacManClassicAssets.DIR_INDEX;
import static de.amr.games.pacman.world.PacManGameWorld.HTS;
import static de.amr.games.pacman.world.PacManGameWorld.TS;
import static de.amr.games.pacman.world.PacManGameWorld.t;
import static java.util.stream.IntStream.range;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.util.ResourceBundle;

import de.amr.games.pacman.core.PacManGame;
import de.amr.games.pacman.core.PacManGameState;
import de.amr.games.pacman.creatures.Bonus;
import de.amr.games.pacman.creatures.Creature;
import de.amr.games.pacman.creatures.Ghost;
import de.amr.games.pacman.creatures.Ghost.GhostState;
import de.amr.games.pacman.creatures.Pac;
import de.amr.games.pacman.lib.V2i;
import de.amr.games.pacman.ui.swing.PacManGameScene;

/**
 * Scene where the game is played.
 * 
 * @author Armin Reichert
 */
public class PacManClassicPlayScene extends PacManGameScene {

	private static final ResourceBundle TEXTS = ResourceBundle.getBundle("localization.translation");

	private final PacManClassicAssets assets;

	public PacManClassicPlayScene(PacManGame game, V2i size, PacManClassicAssets assets) {
		super(game, size);
		this.assets = assets;
	}

	@Override
	public void start() {
	}

	@Override
	public void end() {
	}

	@Override
	public void draw(Graphics2D g, Graphics2D unscaledGC) {
		drawScore(g);
		drawLivesCounter(g);
		drawLevelCounter(g);
		drawMaze(g);
		drawPac(g, game.pac);
		for (Ghost ghost : game.ghosts) {
			drawGhost(g, ghost);
		}
		drawDebugInfo(g);
	}

	private void drawScore(Graphics2D g) {
		g.setFont(assets.scoreFont);
		g.translate(0, 2);
		g.setColor(Color.WHITE);
		g.drawString(TEXTS.getString("SCORE"), t(1), t(1));
		g.drawString(TEXTS.getString("HI_SCORE"), t(16), t(1));
		g.translate(0, 1);
		g.setColor(Color.YELLOW);
		g.drawString(String.format("%08d", game.score), t(1), t(2));
		g.setColor(Color.LIGHT_GRAY);
		g.drawString(String.format("L%02d", game.levelNumber), t(9), t(2));
		g.setColor(Color.YELLOW);
		g.drawString(String.format("%08d", game.hiscore.points), t(16), t(2));
		g.setColor(Color.LIGHT_GRAY);
		g.drawString(String.format("L%02d", game.hiscore.level), t(24), t(2));
		g.translate(0, -3);
	}

	private void drawLivesCounter(Graphics2D g) {
		int maxLives = 5;
		int y = size.y - t(2);
		for (int i = 0; i < Math.min(game.lives - 1, maxLives); ++i) {
			g.drawImage(assets.life, t(2 * (i + 1)), y, null);
		}
		if (game.lives > maxLives) {
			g.setColor(Color.YELLOW);
			g.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 6));
			g.drawString("+" + (game.lives - maxLives), t(12) - 4, y + t(2));
		}
	}

	private void drawLevelCounter(Graphics2D g) {
		int x = t(game.world.sizeInTiles().x - 4);
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
		if (game.mazeFlashesRemaining > 0) {
			game.clock.runAlternating(game.clock.sec(0.25), () -> {
				g.drawImage(assets.mazeEmptyDark, 0, t(3), null);
			}, () -> {
				g.drawImage(assets.mazeEmptyBright, 0, t(3), null);
			}, () -> {
				game.mazeFlashesRemaining--;
			});
			return;
		}
		g.drawImage(assets.mazeFull, 0, t(3), null);
		range(0, game.world.sizeInTiles().x).forEach(x -> {
			range(4, game.world.sizeInTiles().y - 3).forEach(y -> {
				if (game.world.isFoodRemoved(x, y)) {
					hideFood(g, x, y);
				} else if (game.state == PacManGameState.HUNTING && game.world.isEnergizerTile(x, y)) {
					game.clock.runOrBeIdle(10, () -> hideFood(g, x, y));
				}
			});
		});
		drawBonus(g, game.bonus);
		if (game.ui.isDebugMode()) {
			drawMazeStructure(g);
		}
	}

	private void drawGuy(Graphics2D g, Creature guy, BufferedImage sprite) {
		if (guy.visible) {
			int dx = (sprite.getWidth() - TS) / 2, dy = (sprite.getHeight() - TS) / 2;
			g.drawImage(sprite, (int) (guy.position.x) - dx, (int) (guy.position.y) - dy, null);
		}
	}

	private void drawBonus(Graphics2D g, Bonus bonus) {
		if (bonus.availableTicks > 0) {
			drawGuy(g, bonus, assets.symbols[bonus.symbol]);
		}
		if (bonus.consumedTicks > 0) {
			if (game.bonus.points != 1000) {
				drawGuy(g, game.bonus, assets.numbers.get(game.bonus.points));
			} else {
				// this sprite is somewhat nasty
				g.drawImage(assets.numbers.get(1000), (int) (bonus.position.x) - HTS - 2, (int) (bonus.position.y) - HTS, null);
			}
		}
	}

	private void drawPac(Graphics2D g, Pac pac) {
		drawGuy(g, pac, sprite(pac));
	}

	private void drawGhost(Graphics2D g, Ghost ghost) {
		drawGuy(g, ghost, sprite(ghost));
	}

	private BufferedImage sprite(Pac pac) {
		int dir = DIR_INDEX.get(pac.dir);
		if (pac.collapsingTicksLeft > 0) {
			// collapsing animation
			int frame = 13 - (int) pac.collapsingTicksLeft / 8;
			return assets.section(Math.max(frame, 3), 0);
		}
		if (pac.speed == 0) {
			// full face
			return assets.section(2, 0);
		}
		if (!pac.couldMove) {
			// mouth wide open towards move dir
			return assets.section(0, dir);
		}
		// mouth animation towards move dir
		int frame = game.clock.frame(5, 3);
		return frame == 2 ? assets.section(frame, 0) : assets.section(frame, dir);
	}

	private BufferedImage sprite(Ghost ghost) {
		if (ghost.bounty > 0) {
			return assets.numbers.get(ghost.bounty);
		}
		int dir = DIR_INDEX.get(ghost.wishDir);
		int walking = ghost.speed == 0 ? 0 : game.clock.frame(5, 2);
		if (ghost.state == GhostState.DEAD || ghost.state == GhostState.ENTERING_HOUSE) {
			// eyes looking towards intended move direction
			return assets.section(8 + dir, 5);
		}
		if (ghost.state == GhostState.FRIGHTENED) {
			if (game.pac.powerTicksLeft <= 20 * game.level.numFlashes) {
				// flashing blue/white, walking animation
				int flashing = game.clock.frame(10, 2) == 0 ? 8 : 10;
				return assets.section(walking + flashing, 4);
			}
			// blue, walking animation
			return assets.section(8 + walking, 4);
		}
		if (ghost.state == GhostState.LOCKED && game.pac.powerTicksLeft > 0) {
			// blue, walking animation
			return assets.section(8 + walking, 4);
		}
		// colored, walking animation, looking towards *intended* move direction
		return assets.section(2 * dir + walking, 4 + ghost.id);
	}
}