package de.amr.games.pacman.ui.swing;

import static de.amr.games.pacman.World.HTS;
import static de.amr.games.pacman.World.TS;
import static de.amr.games.pacman.World.WORLD_HEIGHT_TILES;
import static de.amr.games.pacman.World.WORLD_WIDTH_TILES;
import static de.amr.games.pacman.ui.swing.PacManGameSwingUI.dirIndex;
import static de.amr.games.pacman.ui.swing.PacManGameSwingUI.drawCenteredText;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.Polygon;
import java.awt.image.BufferedImage;

import de.amr.games.pacman.GameState;
import de.amr.games.pacman.PacManGame;
import de.amr.games.pacman.common.Direction;
import de.amr.games.pacman.entities.Ghost;
import de.amr.games.pacman.entities.PacMan;

/**
 * Scene where the game is played.
 * 
 * @author Armin Reichert
 */
class PlayScene {

	final Polygon TRIANGLE = new Polygon(new int[] { -4, 4, 0 }, new int[] { 0, 0, 4 }, 3);

	public boolean debugMode;

	public final PacManGame game;
	public final Assets assets;
	public final Dimension size;

	public String messageText;
	public Color messageColor;

	public PlayScene(PacManGame game, Assets assets, Dimension size) {
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
		for (int ghost = 0; ghost < 4; ++ghost) {
			drawGhost(g, ghost);
		}
		drawMessage(g);
		drawDebugInfo(g);
	}

	private void drawMessage(Graphics2D g) {
		if (messageText != null) {
			g.setFont(assets.scoreFont);
			g.setColor(messageColor);
			drawCenteredText(g, messageText, 21 * TS, size.width);
		}
	}

	private void drawDebugInfo(Graphics2D g) {
		if (debugMode) {
			long remaining = game.state.ticksRemaining();
			String ticksText = remaining == Long.MAX_VALUE ? "forever" : remaining + " ticks remaining";
			String stateText = String.format("%s (%s)", game.stateDescription(), ticksText);
			g.setColor(Color.WHITE);
			g.setFont(new Font("Arial", Font.PLAIN, 6));
			g.drawString(stateText, 1 * TS, 3 * TS);
		}
	}

	private void drawScore(Graphics2D g) {
		g.setFont(assets.scoreFont);
		g.translate(0, 2);
		g.setColor(Color.WHITE);
		g.drawString("SCORE", 1 * TS, 1 * TS);
		g.drawString("HIGH SCORE", 16 * TS, 1 * TS);
		g.translate(0, 1);
		g.setColor(Color.YELLOW);
		g.drawString(String.format("%08d", game.score), 1 * TS, 2 * TS);
		g.setColor(Color.LIGHT_GRAY);
		g.drawString(String.format("L%02d", game.level), 9 * TS, 2 * TS);
		g.setColor(Color.YELLOW);
		g.drawString(String.format("%08d", game.hiscore.points), 16 * TS, 2 * TS);
		g.setColor(Color.LIGHT_GRAY);
		g.drawString(String.format("L%02d", game.hiscore.level), 24 * TS, 2 * TS);
		g.translate(0, -3);
	}

	private void drawLivesCounter(Graphics2D g) {
		for (int i = 0; i < game.lives; ++i) {
			g.drawImage(assets.imageLive, 2 * (i + 1) * TS, size.height - 2 * TS, null);
		}
	}

	private void drawLevelCounter(Graphics2D g) {
		int x = (WORLD_WIDTH_TILES - 4) * TS;
		int first = Math.max(1, game.level - 6);
		for (int level = first; level <= game.level; ++level) {
			BufferedImage symbol = assets.symbols.get(game.level(level).bonusSymbol);
			g.drawImage(symbol, x, size.height - 2 * TS, null);
			x -= 2 * TS;
		}
	}

	private void hideTile(Graphics2D g, int x, int y) {
		g.setColor(Color.BLACK);
		g.fillRect(x * TS, y * TS, TS, TS);
	}

	private void drawMazeFlashing(Graphics2D g) {
		game.clock.runAlternating(game.clock.sec(0.25f), () -> {
			g.drawImage(assets.imageMazeEmpty, 0, 3 * TS, null);
		}, () -> {
			g.drawImage(assets.imageMazeEmptyWhite, 0, 3 * TS, null);
		}, () -> {
			--game.mazeFlashesRemaining;
		});
	}

	private void drawMaze(Graphics2D g) {
		if (game.mazeFlashesRemaining > 0 && game.state.ticksRemaining() <= game.clock.sec(4f)) {
			drawMazeFlashing(g);
			return;
		}
		g.drawImage(assets.imageMazeFull, 0, 3 * TS, null);
		for (int x = 0; x < WORLD_WIDTH_TILES; ++x) {
			for (int y = 0; y < WORLD_HEIGHT_TILES; ++y) {
				if (game.world.hasEatenFood(x, y)) {
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
			g.drawImage(assets.symbols.get(game.level().bonusSymbol), 13 * TS, 20 * TS - HTS, null);
		} else if (game.bonusConsumedTimer > 0) {
			BufferedImage image = assets.numbers.get(game.level().bonusPoints);
			g.drawImage(image, (size.width - image.getWidth()) / 2, 20 * TS - HTS, null);
		}
		if (debugMode) {
			drawMazeStructure(g);
		}
	}

	private void drawMazeStructure(Graphics2D g) {
		for (int x = 0; x < WORLD_WIDTH_TILES; ++x) {
			for (int y = 0; y < WORLD_HEIGHT_TILES; ++y) {
				if (game.world.isIntersectionTile(x, y)) {
					g.setColor(new Color(100, 100, 100));
					g.setStroke(new BasicStroke(0.1f));
					for (Direction dir : Direction.values()) {
						int nx = x + dir.vec.x, ny = y + dir.vec.y;
						if (game.world.isWall(nx, ny)) {
							continue;
						}
						g.drawLine(x * TS + HTS, y * TS + HTS, nx * TS + HTS, ny * TS + HTS);
					}
				} else if (game.world.isUpwardsBlocked(x, y)) {
					g.setColor(new Color(100, 100, 100));
					g.setStroke(new BasicStroke(0.1f));
					g.translate(x * TS + HTS, y * TS);
					g.fillPolygon(TRIANGLE);
					g.translate(-(x * TS + HTS), -y * TS);
				}
			}
		}
	}

	private void drawPacMan(Graphics2D g) {
		PacMan pacMan = game.pacMan;
		if (!pacMan.visible) {
			return;
		}
		BufferedImage sprite;
		if (game.state == GameState.PACMAN_DYING) {
			if (game.state.ticksRemaining() >= game.clock.sec(2) + 11 * 8) {
				// for 2 seconds, show full sprite before animation starts
				sprite = assets.sheet(2, 0);
			} else if (game.state.ticksRemaining() >= game.clock.sec(2)) {
				// run collapsing animation
				int frame = (int) (game.state.ticksRemaining() - game.clock.sec(2)) / 8;
				sprite = assets.sheet(13 - frame, 0);
			} else {
				// show collapsed sprite after collapsing
				sprite = assets.sheet(13, 0);
			}
		} else if (pacMan.speed == 0) {
			// show full sprite
			sprite = assets.sheet(2, 0);
		} else if (!pacMan.couldMove) {
			// show mouth wide open
			sprite = assets.sheet(0, dirIndex(pacMan.dir));
		} else {
			// switch between mouth closed and mouth open
			int mouthFrame = game.clock.frame(5, 3);
			sprite = mouthFrame == 2 ? assets.sheet(mouthFrame, 0) : assets.sheet(mouthFrame, dirIndex(pacMan.dir));
		}
		g.drawImage(sprite, (int) pacMan.position.x - HTS, (int) pacMan.position.y - HTS, null);
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
				sprite = assets.bountyNumbers.get(ghost.bounty);
			} else {
				// show eyes looking towards move direction
				sprite = assets.sheet(8 + dir, 5);
			}
		} else if (ghost.frightened) {
			// TODO flash exactly as often as specified by level
			if (game.pacMan.powerTimer < game.clock.sec(2) && ghost.speed != 0) {
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
		g.drawImage(sprite, (int) ghost.position.x - HTS, (int) ghost.position.y - HTS, null);

		if (debugMode) {
			g.setColor(Color.WHITE);
			g.drawRect((int) ghost.position.x, (int) ghost.position.y, TS, TS);
			if (ghost.targetTile != null) {
				Color c = Assets.GHOST_COLORS.get(ghost.name);
				g.setColor(c);
				g.fillRect(ghost.targetTile.x * TS + HTS / 2, ghost.targetTile.y * TS + HTS / 2, HTS, HTS);
			}
		}
	}
}