package de.amr.games.pacman.ui.swing;

import static de.amr.games.pacman.worlds.PacManGameWorld.HTS;
import static de.amr.games.pacman.worlds.PacManGameWorld.TS;
import static de.amr.games.pacman.worlds.PacManGameWorld.t;
import static java.lang.Math.round;
import static java.util.stream.IntStream.range;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.Polygon;
import java.awt.Stroke;
import java.awt.image.BufferedImage;
import java.util.ResourceBundle;

import de.amr.games.pacman.core.PacManGame;
import de.amr.games.pacman.core.PacManGameState;
import de.amr.games.pacman.creatures.Ghost;
import de.amr.games.pacman.creatures.PacMan;
import de.amr.games.pacman.lib.Direction;

/**
 * Scene where the game is played.
 * 
 * @author Armin Reichert
 */
class PlayScene extends Scene {

	private final ResourceBundle resources = ResourceBundle.getBundle("localization.PlayScene");

	public PlayScene(PacManGame game, Dimension size) {
		super(game, size);
	}

	public void draw(Graphics2D g) {
		drawScore(g);
		drawLivesCounter(g);
		drawLevelCounter(g);
		drawMaze(g);
		drawPacMan(g, game.pacMan);
		for (Ghost ghost : game.ghosts) {
			drawGhost(g, ghost);
		}
		drawMessage(g);
		drawDebugInfo(g);
	}

	private void drawMessage(Graphics2D g) {
		if (game.state == PacManGameState.READY) {
			g.setFont(assets.scoreFont);
			g.setColor(Color.YELLOW);
			drawCenteredText(g, resources.getString("READY"), t(21));
		} else if (game.state == PacManGameState.GAME_OVER) {
			g.setFont(assets.scoreFont);
			g.setColor(Color.RED);
			drawCenteredText(g, resources.getString("GAME_OVER"), t(21));
		}
	}

	private void drawScore(Graphics2D g) {
		g.setFont(assets.scoreFont);
		g.translate(0, 2);
		g.setColor(Color.WHITE);
		g.drawString(resources.getString("SCORE"), t(1), t(1));
		g.drawString(resources.getString("HI_SCORE"), t(16), t(1));
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
		int y = size.height - t(2);
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
		int x = t(game.world.size().x - 4);
		int first = Math.max(1, game.levelNumber - 6);
		for (int level = first; level <= game.levelNumber; ++level) {
			BufferedImage symbol = assets.symbols[PacManGame.level(level).bonusSymbol];
			g.drawImage(symbol, x, size.height - t(2), null);
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
		range(0, game.world.size().x).forEach(x -> {
			range(4, game.world.size().y - 3).forEach(y -> {
				if (game.world.foodRemoved(x, y)) {
					hideFood(g, x, y);
				} else if (game.state == PacManGameState.HUNTING && game.world.isEnergizerTile(x, y)) {
					game.clock.runOrBeIdle(10, () -> hideFood(g, x, y));
				}
			});
		});
		if (game.bonusAvailableTicks > 0) {
			g.drawImage(assets.symbols[game.level().bonusSymbol], t(13), t(20) - HTS, null);
		} else if (game.bonusConsumedTicks > 0) {
			drawCenteredImage(g, assets.numbers.get(game.level().bonusPoints), t(20) - HTS);
		}
		if (game.ui.isDebugMode()) {
			drawMazeStructure(g);
		}
	}

	private void drawPacMan(Graphics2D g, PacMan pacMan) {
		if (pacMan.visible) {
			g.drawImage(sprite(pacMan), round(pacMan.position.x - HTS), round(pacMan.position.y - HTS), null);
		}
	}

	private BufferedImage sprite(PacMan pacMan) {
		int dir = PacManGameAssets.DIR_INDEX.get(pacMan.dir);
		if (pacMan.collapsingTicksLeft > 0) {
			// collapsing animation
			int frame = 13 - (int) pacMan.collapsingTicksLeft / 8;
			frame = Math.max(frame, 3);
			return assets.section(frame, 0);
		}
		if (pacMan.speed != 0) {
			if (!pacMan.couldMove) {
				// mouth wide open
				return assets.section(0, dir);
			}
			// mouth animation
			int frame = game.clock.frame(5, 3);
			return frame == 2 ? assets.section(frame, 0) : assets.section(frame, dir);
		}
		// full face
		return assets.section(2, 0);
	}

	private void drawGhost(Graphics2D g, Ghost ghost) {
		if (ghost.visible) {
			g.drawImage(sprite(ghost), round(ghost.position.x - HTS), round(ghost.position.y - HTS), null);
		}
	}

	private BufferedImage sprite(Ghost ghost) {
		int dir = PacManGameAssets.DIR_INDEX.get(ghost.wishDir);
		int walking = ghost.speed == 0 ? 0 : game.clock.frame(5, 2);
		if (ghost.bounty > 0) {
			// number
			return assets.numbers.get(ghost.bounty);
		}
		if (ghost.dead) {
			// eyes looking towards intended move direction
			return assets.section(8 + dir, 5);
		}
		if (ghost.frightened) {
			if (game.pacMan.powerTicksLeft <= game.clock.sec(2) && ghost.speed != 0) {
				// TODO flash exactly as often as specified by level
				// flashing blue/white, walking animation
				int flashing = game.clock.frame(10, 2) == 0 ? 8 : 10;
				return assets.section(walking + flashing, 4);
			}
			// blue, walking animation
			return assets.section(8 + walking, 4);
		}
		if (ghost.locked && game.pacMan.powerTicksLeft > 0) {
			// blue, walking animation
			return assets.section(8 + walking, 4);
		}
		// colored, walking animation, looking towards intended move direction
		return assets.section(2 * dir + walking, 4 + ghost.id);
	}

	// debug drawing stuff

	static final Polygon TRIANGLE = new Polygon(new int[] { -4, 4, 0 }, new int[] { 0, 0, 4 }, 3);

	private void drawDebugInfo(Graphics2D g) {
		if (game.ui.isDebugMode()) {
			long remaining = game.state.ticksRemaining();
			String ticksText = remaining == Long.MAX_VALUE ? "forever" : remaining + " ticks remaining";
			String stateText = String.format("%s (%s)", game.stateDescription(), ticksText);
			g.setColor(Color.WHITE);
			g.setFont(new Font("Arial", Font.PLAIN, 6));
			g.drawString(stateText, t(1), t(3));
			for (Ghost ghost : game.ghosts) {
				g.setColor(Color.WHITE);
				g.drawRect(round(ghost.position.x), round(ghost.position.y), TS, TS);
				if (ghost.targetTile != null) {
					Color c = GHOST_COLORS[ghost.id];
					g.setColor(c);
					g.fillRect(t(ghost.targetTile.x) + HTS / 2, t(ghost.targetTile.y) + HTS / 2, HTS, HTS);
				}
			}
			if (game.pacMan.targetTile != null) {
				g.setColor(new Color(255, 255, 0, 200));
				g.fillRect(t(game.pacMan.targetTile.x), t(game.pacMan.targetTile.y), TS, TS);
			}
		}
	}

	private void drawMazeStructure(Graphics2D g) {
		Color dark = new Color(80, 80, 80, 200);
		Stroke thin = new BasicStroke(0.1f);
		g.setColor(dark);
		g.setStroke(thin);
		for (int x = 0; x < game.world.size().x; ++x) {
			for (int y = 0; y < game.world.size().y; ++y) {
				if (game.world.isIntersection(x, y)) {
					for (Direction dir : Direction.values()) {
						int nx = x + dir.vec.x, ny = y + dir.vec.y;
						if (game.world.isWall(nx, ny)) {
							continue;
						}
						g.drawLine(t(x) + HTS, t(y) + HTS, t(nx) + HTS, t(ny) + HTS);
					}
				} else if (game.world.isUpwardsBlocked(x, y)) {
					g.translate(t(x) + HTS, t(y));
					g.fillPolygon(TRIANGLE);
					g.translate(-t(x) - HTS, -t(y));
				}
			}
		}
	}
}