package de.amr.games.pacman.ui.swing;

import static de.amr.games.pacman.core.World.HTS;
import static de.amr.games.pacman.core.World.TS;
import static de.amr.games.pacman.core.World.WORLD_TILES;
import static de.amr.games.pacman.ui.swing.PacManGameSwingUI.dirIndex;
import static de.amr.games.pacman.ui.swing.PacManGameSwingUI.drawCenteredText;
import static java.lang.Math.round;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.Polygon;
import java.awt.Stroke;
import java.awt.image.BufferedImage;

import de.amr.games.pacman.core.Game;
import de.amr.games.pacman.core.GameState;
import de.amr.games.pacman.core.Ghost;
import de.amr.games.pacman.core.PacMan;
import de.amr.games.pacman.lib.Direction;
import de.amr.games.pacman.lib.V2i;

/**
 * Scene where the game is played.
 * 
 * @author Armin Reichert
 */
class PlayScene {

	static int t(int nTiles) {
		return nTiles * TS;
	}

	static final Polygon TRIANGLE = new Polygon(new int[] { -4, 4, 0 }, new int[] { 0, 0, 4 }, 3);

	public boolean debugMode;

	public final Game game;
	public final Assets assets;
	public final V2i size;

	public String messageText;
	public Color messageColor;

	public PlayScene(Game game, Assets assets, V2i size) {
		this.game = game;
		this.assets = assets;
		this.size = size;
	}

	public void draw(Graphics2D g) {
		drawScore(g);
		drawLivesCounter(g);
		drawLevelCounter(g);
		drawMaze(g);
		drawPacMan(g);
		for (int ghostID = 0; ghostID < 4; ++ghostID) {
			drawGhost(g, ghostID);
		}
		drawMessage(g);
		drawDebugInfo(g);
	}

	private void drawMessage(Graphics2D g) {
		if (game.state == GameState.READY) {
			messageText = "Ready!";
			messageColor = Color.YELLOW;
		} else if (game.state == GameState.GAME_OVER) {
			messageText = "Game Over!";
			messageColor = Color.RED;
		} else {
			messageText = null;
		}
		if (messageText != null) {
			g.setFont(assets.scoreFont);
			g.setColor(messageColor);
			drawCenteredText(g, messageText, t(21), size.x);
		}
	}

	private void drawScore(Graphics2D g) {
		g.setFont(assets.scoreFont);
		g.translate(0, 2);
		g.setColor(Color.WHITE);
		g.drawString("SCORE", t(1), t(1));
		g.drawString("HIGH SCORE", t(16), t(1));
		g.translate(0, 1);
		g.setColor(Color.YELLOW);
		g.drawString(String.format("%08d", game.score), t(1), t(2));
		g.setColor(Color.LIGHT_GRAY);
		g.drawString(String.format("L%02d", game.level), t(9), t(2));
		g.setColor(Color.YELLOW);
		g.drawString(String.format("%08d", game.hiscore.points), t(16), t(2));
		g.setColor(Color.LIGHT_GRAY);
		g.drawString(String.format("L%02d", game.hiscore.level), t(24), t(2));
		g.translate(0, -3);
	}

	private void drawLivesCounter(Graphics2D g) {
		int y = size.y - t(2);
		for (int i = 0; i < Math.min(game.lives, 4); ++i) {
			g.drawImage(assets.imageLive, t(2 * (i + 1)), y, null);
		}
		if (game.lives > 4) {
			g.setColor(Color.YELLOW);
			g.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 8));
			g.drawString("+" + (game.lives - 4), t(10), y + t(1));
		}
	}

	private void drawLevelCounter(Graphics2D g) {
		int x = t(WORLD_TILES.x - 4);
		int first = Math.max(1, game.level - 6);
		for (int level = first; level <= game.level; ++level) {
			BufferedImage symbol = assets.symbols[game.level(level).bonusSymbol];
			g.drawImage(symbol, x, size.y - t(2), null);
			x -= t(2);
		}
	}

	private void hideTile(Graphics2D g, int x, int y) {
		g.setColor(Color.BLACK);
		g.fillRect(t(x), t(y), TS, TS);
	}

	private void drawMazeFlashing(Graphics2D g) {
		game.clock.runAlternating(game.clock.sec(0.25f), () -> {
			g.drawImage(assets.imageMazeEmpty, 0, t(3), null);
		}, () -> {
			g.drawImage(assets.imageMazeEmptyWhite, 0, t(3), null);
		}, () -> {
			game.mazeFlashesRemaining--;
		});
	}

	private void drawMaze(Graphics2D g) {
		if (game.mazeFlashesRemaining > 0) {
			drawMazeFlashing(g);
			return;
		}
		g.drawImage(assets.imageMazeFull, 0, t(3), null);
		for (int x = 0; x < WORLD_TILES.x; ++x) {
			for (int y = 0; y < WORLD_TILES.y; ++y) {
				if (game.world.foodEatenAt(x, y)) {
					hideTile(g, x, y);
					continue;
				}
				// energizer blinking?
				if (game.state == GameState.HUNTING && game.world.isEnergizerTile(x, y)) {
					int xx = x, yy = y;
					game.clock.runOrBeIdle(10, () -> hideTile(g, xx, yy));
				}
			}
		}
		if (game.bonusAvailableTimer > 0) {
			g.drawImage(assets.symbols[game.level().bonusSymbol], t(13), t(20) - HTS, null);
		} else if (game.bonusConsumedTimer > 0) {
			BufferedImage image = assets.numbers.get(game.level().bonusPoints);
			g.drawImage(image, (size.x - image.getWidth()) / 2, t(20) - HTS, null);
		}
		if (debugMode) {
			drawMazeStructure(g);
		}
	}

	private void drawPacMan(Graphics2D g) {
		PacMan pacMan = game.pacMan;
		if (!pacMan.visible) {
			return;
		}
		// use full face as default
		BufferedImage sprite = assets.sheet(2, 0);
		if (pacMan.collapsingTicksRemaining > 0) {
			// collapsing animation
			int frame = 13 - (int) pacMan.collapsingTicksRemaining / 8;
			if (frame > 13) {
				frame = 13;
			} else if (frame < 3) {
				frame = 3;
			}
//			Logging.log("Collapsing, ticks = %d, animation frame = %d", pacMan.collapsingTicksRemaining, frame);
			sprite = assets.sheet(frame, 0);
		} else if (!pacMan.couldMove) {
			// show mouth wide open
			sprite = assets.sheet(0, dirIndex(pacMan.dir));
		} else if (pacMan.speed != 0) {
			// switch between mouth closed and mouth open
			int frame = game.clock.frame(5, 3);
			sprite = frame == 2 ? assets.sheet(frame, 0) : assets.sheet(frame, dirIndex(pacMan.dir));
		}
		g.drawImage(sprite, round(pacMan.position.x - HTS), round(pacMan.position.y - HTS), null);
	}

	private void drawGhost(Graphics2D g, int ghostIndex) {
		Ghost ghost = game.ghosts[ghostIndex];
		if (!ghost.visible) {
			return;
		}

		BufferedImage sprite;
		int dir = dirIndex(ghost.dir);
		int walking = ghost.speed == 0 ? 0 : game.clock.frame(5, 2);
		if (ghost.dead) {
			if (ghost.bounty > 0) {
				// show bounty as number
				sprite = assets.numbers.get(ghost.bounty);
			} else {
				// show eyes looking towards move direction
				sprite = assets.sheet(8 + dir, 5);
			}
		} else if (ghost.frightened) {
			// TODO flash exactly as often as specified by level
			if (game.pacMan.powerTicks < game.clock.sec(2) && ghost.speed != 0) {
				// ghost flashing blue/white, animated walking
				int flashing = game.clock.frame(10, 2) == 0 ? 8 : 10;
				sprite = assets.sheet(walking + flashing, 4);
			} else {
				// blue ghost, animated walking
				sprite = assets.sheet(8 + walking, 4);
			}
		} else {
			sprite = assets.sheet(2 * dir + walking, 4 + ghostIndex);
		}
		g.drawImage(sprite, round(ghost.position.x - HTS), round(ghost.position.y - HTS), null);
	}

	private void drawDebugInfo(Graphics2D g) {
		if (debugMode) {
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
					Color c = Assets.GHOST_COLORS[ghost.id];
					g.setColor(c);
					g.fillRect(t(ghost.targetTile.x) + HTS / 2, t(ghost.targetTile.y) + HTS / 2, HTS, HTS);
				}
			}
		}
	}

	private void drawMazeStructure(Graphics2D g) {
		Color dark = new Color(80, 80, 80, 200);
		Stroke thin = new BasicStroke(0.1f);
		g.setColor(dark);
		g.setStroke(thin);
		for (int x = 0; x < WORLD_TILES.x; ++x) {
			for (int y = 0; y < WORLD_TILES.y; ++y) {
				if (game.world.isIntersectionTile(x, y)) {
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