package de.amr.games.pacman.ui.swing;

import static de.amr.games.pacman.PacManGame.levelData;
import static de.amr.games.pacman.Timing.sec;
import static de.amr.games.pacman.World.HTS;
import static de.amr.games.pacman.World.TS;
import static de.amr.games.pacman.World.WORLD_HEIGHT_TILES;
import static de.amr.games.pacman.World.WORLD_WIDTH_TILES;
import static de.amr.games.pacman.common.Logging.log;

import java.awt.BasicStroke;
import java.awt.Canvas;
import java.awt.Color;
import java.awt.EventQueue;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.Polygon;
import java.awt.image.BufferStrategy;
import java.awt.image.BufferedImage;

import javax.swing.JFrame;

import de.amr.games.pacman.GameState;
import de.amr.games.pacman.PacManGame;
import de.amr.games.pacman.Timing;
import de.amr.games.pacman.common.Direction;
import de.amr.games.pacman.entities.Creature;
import de.amr.games.pacman.entities.Ghost;
import de.amr.games.pacman.ui.PacManGameUI;

public class PacManGameSwingUI extends JFrame implements PacManGameUI {

	public static void main(String[] args) {
		PacManGame game = new PacManGame();
		EventQueue.invokeLater(() -> {
			game.ui = new PacManGameSwingUI(game, 2);
			game.ui.setDebugMode(false);
			new Thread(game, "GameLoop").start();
		});
	}

	private static final Polygon TRIANGLE = new Polygon(new int[] { -4, 4, 0 }, new int[] { 0, 0, 4 }, 3);

	private final Assets assets;
	private final PacManGame game;
	private final float scaling;
	private final Canvas canvas;
	private final Keyboard keyboard;

	private boolean debugMode;
	private String messageText;
	private Color messageColor;

	public PacManGameSwingUI(PacManGame game, float scaling) {
		this.game = game;
		this.scaling = scaling;
		messageText = null;
		messageColor = Color.YELLOW;
		assets = new Assets();
		keyboard = new Keyboard(this);
		canvas = new Canvas();
		canvas.setSize((int) (WORLD_WIDTH_TILES * TS * scaling), (int) (WORLD_HEIGHT_TILES * TS * scaling));
		canvas.setFocusable(false);
		add(canvas);
		setTitle("Pac-Man");
		setResizable(false);
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		pack();
		setLocationRelativeTo(null);
		setVisible(true);
		// these must called be *after* setVisible():
		requestFocus();
		canvas.createBufferStrategy(2);
	}

	@Override
	public boolean isDebugMode() {
		return debugMode;
	}

	@Override
	public void setDebugMode(boolean debug) {
		debugMode = debug;
	}

	@Override
	public void render() {
		BufferStrategy buffers = canvas.getBufferStrategy();
		do {
			do {
				Graphics2D g = (Graphics2D) buffers.getDrawGraphics();
				g.setColor(Color.BLACK);
				g.fillRect(0, 0, canvas.getWidth(), canvas.getHeight());
				g.scale(scaling, scaling);
				drawGame(g);
				g.dispose();
			} while (buffers.contentsRestored());
			buffers.show();
		} while (buffers.contentsLost());
	}

	@Override
	public void redMessage(String text) {
		messageText = text;
		messageColor = Color.RED;
	}

	@Override
	public void yellowMessage(String text) {
		messageText = text;
		messageColor = Color.YELLOW;
	}

	@Override
	public void clearMessage() {
		messageText = null;
	}

	@Override
	public boolean keyPressed(String keySpec) {
		boolean pressed = keyboard.keyPressed(keySpec);
		keyboard.clearKey(keySpec);
		return pressed;
	}

	private void drawGame(Graphics2D g) {
		drawScore(g);
		drawLivesCounter(g);
		drawLevelCounter(g);
		drawMaze(g);
		drawPacMan(g);
		for (int i = 0; i < 4; ++i) {
			drawGhost(g, i);
		}
		drawDebugInfo(g);
		g.setColor(Color.WHITE);
		g.setFont(new Font("Arial", Font.PLAIN, 6));
		g.drawString(String.format("%d frames/sec", Timing.fps), 1 * TS, 3 * TS);
	}

	private void drawDebugInfo(Graphics2D g) {
		if (debugMode) {
			g.setColor(Color.WHITE);
			g.setFont(new Font("Arial", Font.PLAIN, 6));
			String text = "";
			if (game.state == GameState.READY) {
				text = String.format("%s %d ticks remaining", game.state, game.readyStateTimer);
			} else if (game.state == GameState.CHANGING_LEVEL) {
				text = String.format("%s %d ticks remaining", game.state, game.changingLevelStateTimer);
			} else if (game.state == GameState.SCATTERING) {
				text = String.format("%d. %s %d ticks remaining", game.attackWave + 1, game.state, game.scatteringStateTimer);
			} else if (game.state == GameState.CHASING) {
				text = String.format("%d. %s %d ticks remaining", game.attackWave + 1, game.state, game.chasingStateTimer);
			} else if (game.state == GameState.PACMAN_DYING) {
				text = String.format("%s %d ticks remaining", game.state, game.pacManDyingStateTimer);
			} else if (game.state == GameState.GAME_OVER) {
				text = String.format("%s", game.state);
			}
			g.drawString(text, 8 * TS, 3 * TS);
			for (Ghost ghost : game.ghosts) {
				if (ghost.targetTile != null) {
					g.setColor(color(ghost));
					g.fillRect(ghost.targetTile.x * TS + HTS / 2, ghost.targetTile.y * TS + HTS / 2, HTS, HTS);
				}
			}
		}
	}

	private Color color(Ghost ghost) {
		switch (ghost.name) {
		case "Blinky":
			return Color.RED;
		case "Pinky":
			return Color.PINK;
		case "Inky":
			return Color.CYAN;
		case "Clyde":
			return Color.ORANGE;
		default:
			throw new IllegalArgumentException();
		}
	}

	private void drawScore(Graphics2D g) {
		g.setFont(assets.scoreFont);
		g.setColor(Color.WHITE);
		g.drawString(String.format("SCORE %d", game.points), 2 * TS, 2 * TS);
		g.drawString(String.format("LEVEL %02d", game.level), 17 * TS, 2 * TS);
	}

	private void drawLivesCounter(Graphics2D g) {
		for (int i = 0; i < game.lives; ++i) {
			g.drawImage(assets.imageLive, 2 * (i + 1) * TS, (WORLD_HEIGHT_TILES - 2) * TS, null);
		}
	}

	private void drawLevelCounter(Graphics2D g) {
		int x = (WORLD_WIDTH_TILES - 4) * TS;
		int first = Math.max(1, game.level - 6);
		for (int level = first; level <= game.level; ++level) {
			BufferedImage symbol = assets.symbols.get(levelData(level).bonusSymbol());
			g.drawImage(symbol, x, (WORLD_HEIGHT_TILES - 2) * TS, null);
			x -= 2 * TS;
		}
	}

	private void hideTile(Graphics2D g, int x, int y) {
		g.setColor(Color.BLACK);
		g.fillRect(x * TS, y * TS, TS, TS);
	}

	private void drawMazeFlashing(Graphics2D g) {
		if (game.mazeFlashes > 0 && Timing.framesTotal % 30 < 15) {
			g.drawImage(assets.imageMazeEmptyWhite, 0, 3 * TS, null);
			if (Timing.framesTotal % 30 == 14) {
				--game.mazeFlashes;
				log("Maze flashes: %d", game.mazeFlashes);
			}
		} else {
			g.drawImage(assets.imageMazeEmpty, 0, 3 * TS, null);
		}
	}

	private void drawMaze(Graphics2D g) {
		if (game.changingLevelStateTimer > 0 && game.changingLevelStateTimer <= sec(4f)) {
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
				// energizer blinking
				if (game.world.isEnergizerTile(x, y) && Timing.framesTotal % 20 < 10
						&& (game.state == GameState.CHASING || game.state == GameState.SCATTERING)) {
					hideTile(g, x, y);
				}
			}
		}
		if (game.bonusAvailableTimer > 0) {
			String symbolName = game.levelData().bonusSymbol();
			g.drawImage(assets.symbols.get(symbolName), 13 * TS, 20 * TS - HTS, null);
		}
		if (game.bonusConsumedTimer > 0) {
			int number = game.levelData().bonusPoints();
			BufferedImage image = assets.numbers.get(number);
			if (number < 2000) {
				g.drawImage(image, 13 * TS, 20 * TS - HTS, null);
			} else {
				g.drawImage(image, (WORLD_WIDTH_TILES * TS - image.getWidth()) / 2, 20 * TS - HTS, null);
			}
		}
		if (messageText != null) {
			g.setFont(assets.scoreFont);
			g.setColor(messageColor);
			int textWidth = g.getFontMetrics().stringWidth(messageText);
			g.drawString(messageText, WORLD_WIDTH_TILES * TS / 2 - textWidth / 2, 21 * TS);
		}

		if (debugMode) {
			for (int x = 0; x < WORLD_WIDTH_TILES; ++x) {
				for (int y = 0; y < WORLD_HEIGHT_TILES; ++y) {
					if (game.world.isIntersectionTile(x, y)) {
						g.setColor(new Color(100, 100, 100));
						g.setStroke(new BasicStroke(0.1f));
						g.drawLine(x * TS, y * TS + HTS, (x + 1) * TS, y * TS + HTS);
						g.drawLine(x * TS + HTS, y * TS, x * TS + HTS, (y + 1) * TS);
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
	}

	private void drawPacMan(Graphics2D g) {
		Creature pacMan = game.pacMan;
		if (!pacMan.visible) {
			return;
		}
		BufferedImage sprite;
		int mouthFrame = (int) Timing.framesTotal % 15 / 5;
		if (pacMan.dead) {
			// 2 seconds full sprite before collapsing animation starts
			if (game.pacManDyingStateTimer >= sec(2) + 11 * 8) {
				sprite = assets.section(2, 0);
			} else if (game.pacManDyingStateTimer >= sec(2)) {
				// collapsing animation
				int frame = (int) (game.pacManDyingStateTimer - sec(2)) / 8;
				sprite = assets.section(13 - frame, 0);
			} else {
				// collapsed sprite after collapsing
				sprite = assets.section(13, 0);
			}
		} else if (game.state == GameState.READY || game.state == GameState.CHANGING_LEVEL) {
			// full sprite
			sprite = assets.section(2, 0);
		} else if (!pacMan.couldMove) {
			// wide open mouth
			sprite = assets.section(0, directionFrame(pacMan.dir));
		} else {
			// closed mouth or open mouth pointing to move direction
			sprite = mouthFrame == 2 ? assets.section(mouthFrame, 0) : assets.section(mouthFrame, directionFrame(pacMan.dir));
		}
		g.drawImage(sprite, (int) pacMan.position.x - HTS, (int) pacMan.position.y - HTS, null);
	}

	private void drawGhost(Graphics2D g, int ghostIndex) {
		BufferedImage sprite;
		Ghost ghost = game.ghosts[ghostIndex];
		if (!ghost.visible) {
			return;
		}

		if (ghost.dead) {
			// show as number (bounty) or as eyes
			sprite = ghost.bounty > 0 ? assets.bountyNumbers.get(ghost.bounty)
					: assets.section(8 + directionFrame(ghost.dir), 5);
		} else if (ghost.frightened) {
			int walkingFrame = Timing.framesTotal % 60 < 30 ? 0 : 1;
			if (game.pacManPowerTimer < sec(2)) { // TODO
				// flashing blue/white, walking
				int flashingFrame = Timing.framesTotal % 20 < 10 ? 8 : 10;
				sprite = assets.section(flashingFrame + walkingFrame, 4);
			} else {
				// blue, walking
				sprite = assets.section(8 + walkingFrame, 4);
			}
		} else {
			int walkingFrame = Timing.framesTotal % 60 < 30 ? 0 : 1;
			sprite = assets.section(2 * directionFrame(ghost.dir) + walkingFrame, 4 + ghostIndex);
		}
		g.drawImage(sprite, (int) ghost.position.x - HTS, (int) ghost.position.y - HTS, null);

		if (debugMode) {
			g.setColor(Color.WHITE);
			g.drawRect((int) ghost.position.x, (int) ghost.position.y, TS, TS);
		}
	}

	private int directionFrame(Direction dir) {
		switch (dir) {
		case RIGHT:
			return 0;
		case LEFT:
			return 1;
		case UP:
			return 2;
		case DOWN:
			return 3;
		default:
			throw new IllegalStateException();
		}
	}
}