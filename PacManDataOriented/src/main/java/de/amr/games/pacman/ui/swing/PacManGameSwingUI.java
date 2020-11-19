package de.amr.games.pacman.ui.swing;

import static de.amr.games.pacman.PacManGame.levelData;
import static de.amr.games.pacman.PacManGame.log;
import static de.amr.games.pacman.PacManGame.sec;
import static de.amr.games.pacman.World.HTS;
import static de.amr.games.pacman.World.TS;
import static de.amr.games.pacman.World.WORLD_HEIGHT_TILES;
import static de.amr.games.pacman.World.WORLD_WIDTH_TILES;

import java.awt.BasicStroke;
import java.awt.Canvas;
import java.awt.Color;
import java.awt.EventQueue;
import java.awt.Font;
import java.awt.FontFormatException;
import java.awt.Graphics2D;
import java.awt.image.BufferStrategy;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.util.Map;

import javax.imageio.ImageIO;
import javax.swing.JFrame;

import de.amr.games.pacman.Creature;
import de.amr.games.pacman.GameState;
import de.amr.games.pacman.Ghost;
import de.amr.games.pacman.PacManGame;
import de.amr.games.pacman.PacManGameUI;
import de.amr.games.pacman.common.Direction;

public class PacManGameSwingUI extends JFrame implements PacManGameUI {

	public static void main(String[] args) {
		PacManGame game = new PacManGame();
		EventQueue.invokeLater(() -> {
			game.ui = new PacManGameSwingUI(game, 2);
			new Thread(game, "GameLoop").start();
		});
	}

	private boolean debugMode;

	private final PacManGame game;
	private final float scaling;
	private final Canvas canvas;
	private final Keyboard keyboard;

	private BufferedImage imageMazeFull;
	private BufferedImage imageMazeEmpty;
	private BufferedImage imageMazeEmptyWhite;
	private BufferedImage spriteSheet;
	private Map<String, BufferedImage> symbols;
	private Map<Integer, BufferedImage> numbers;
	private Map<Integer, BufferedImage> bountyNumbers;
	private Font scoreFont;
	private String messageText;
	private Color messageColor;

	public PacManGameSwingUI(PacManGame game, float scaling) {
		this.game = game;
		this.scaling = scaling;
		messageText = null;
		messageColor = Color.YELLOW;

		try {
			loadResources();
		} catch (Exception x) {
			x.printStackTrace();
			throw new RuntimeException("Resource loading failed");
		}

		keyboard = new Keyboard(this);

		canvas = new Canvas();
		canvas.setSize((int) (WORLD_WIDTH_TILES * TS * scaling), (int) (WORLD_HEIGHT_TILES * TS * scaling));
		canvas.setFocusable(false);

		setTitle("Pac-Man");
		setResizable(false);
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

		add(canvas);
		pack();
		setLocationRelativeTo(null);
		setVisible(true);
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
		return keyboard.keyPressed(keySpec);
	}

	private void loadResources() {
		spriteSheet = image("/sprites.png");
		imageMazeFull = image("/maze_full.png");
		imageMazeEmpty = image("/maze_empty.png");
		imageMazeEmptyWhite = image("/maze_empty_white.png");

		String fontPath = "/PressStart2P-Regular.ttf";
		try (InputStream fontData = getClass().getResourceAsStream(fontPath)) {
			scoreFont = Font.createFont(Font.TRUETYPE_FONT, fontData).deriveFont((float) TS);
		} catch (IOException x) {
			throw new RuntimeException(String.format("Could not access font, path='%s'", fontPath));
		} catch (FontFormatException x) {
			throw new RuntimeException(String.format("Could not create font, path='%s'", fontPath));
		}
		//@formatter:off
		symbols = Map.of(
			"Cherries",   section(2, 3),
			"Strawberry", section(3, 3),
			"Peach",      section(4, 3),
			"Apple",      section(5, 3),
			"Grapes",     section(6, 3),
			"Galaxian",   section(7, 3),
			"Bell",       section(8, 3),
			"Key",        section(9, 3)
		);
		numbers = Map.of(
			100,  section(0, 9),
			300,  section(1, 9),
			500,  section(2, 9),
			700,  section(3, 9),
			1000, section(4, 9, 2, 1),
			2000, section(4, 10, 2, 1),
			3000, section(4, 11, 2, 1),
			5000, section(4, 12, 2, 1)
		);
		bountyNumbers = Map.of(
			200,  section(0, 8),
			400,  section(1, 8),
			800,  section(2, 8),
			1600, section(3, 8)
		);
		//@formatter:on
	}

	private BufferedImage section(int x, int y, int w, int h) {
		return spriteSheet.getSubimage(x * 16, y * 16, w * 16, h * 16);
	}

	private BufferedImage section(int x, int y) {
		return section(x, y, 1, 1);
	}

	private BufferedImage image(String path) {
		InputStream is = getClass().getResourceAsStream(path);
		if (is == null) {
			throw new RuntimeException(String.format("Could not access resource, path='%s'", path));
		}
		try {
			return ImageIO.read(is);
		} catch (IOException x) {
			throw new RuntimeException(String.format("Could not load image, path='%s'", path));
		}
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
	}

	private void drawDebugInfo(Graphics2D g) {
		if (debugMode) {
			g.setColor(Color.WHITE);
			g.setFont(new Font("Arial", Font.PLAIN, 6));
			g.drawString(String.format("%d frames/sec", game.fps), 1 * TS, 3 * TS);

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
		g.setFont(scoreFont);
		g.setColor(Color.WHITE);
		g.drawString(String.format("SCORE %d", game.points), 16, 16);
	}

	private void drawLivesCounter(Graphics2D g) {
		BufferedImage sprite = section(8, 1);
		for (int i = 0; i < game.lives; ++i) {
			g.drawImage(sprite, 2 * (i + 1) * TS, (WORLD_HEIGHT_TILES - 2) * TS, null);
		}
	}

	private void drawLevelCounter(Graphics2D g) {
		int x = (WORLD_WIDTH_TILES - 4) * TS;
		int first = Math.max(1, game.level - 6);
		for (int level = first; level <= game.level; ++level) {
			BufferedImage symbol = symbols.get(levelData(level).bonusSymbol());
			g.drawImage(symbol, x, (WORLD_HEIGHT_TILES - 2) * TS, null);
			x -= 2 * TS;
		}
	}

	private void hideTile(Graphics2D g, int x, int y) {
		g.setColor(Color.BLACK);
		g.fillRect(x * TS, y * TS, TS, TS);
	}

	private void drawMazeFlashing(Graphics2D g) {
		if (game.mazeFlashes > 0 && game.framesTotal % 30 < 15) {
			g.drawImage(imageMazeEmptyWhite, 0, 3 * TS, null);
			if (game.framesTotal % 30 == 14) {
				--game.mazeFlashes;
				log("Maze flashes: %d", game.mazeFlashes);
			}
		} else {
			g.drawImage(imageMazeEmpty, 0, 3 * TS, null);
		}
	}

	private void drawMaze(Graphics2D g) {
		if (game.changingLevelStateTimer > 0 && game.changingLevelStateTimer <= sec(4f)) {
			drawMazeFlashing(g);
			return;
		}
		g.drawImage(imageMazeFull, 0, 3 * TS, null);
		for (int x = 0; x < WORLD_WIDTH_TILES; ++x) {
			for (int y = 0; y < WORLD_HEIGHT_TILES; ++y) {
				if (game.world.hasEatenFood(x, y)) {
					hideTile(g, x, y);
					continue;
				}
				// energizer blinking
				if (game.world.isEnergizerTile(x, y) && game.framesTotal % 20 < 10
						&& (game.state == GameState.CHASING || game.state == GameState.SCATTERING)) {
					hideTile(g, x, y);
				}
			}
		}
		if (game.bonusAvailableTimer > 0) {
			String symbolName = game.levelData().bonusSymbol();
			g.drawImage(symbols.get(symbolName), 13 * TS, 20 * TS - HTS, null);
		}
		if (game.bonusConsumedTimer > 0) {
			int bonusPoints = game.levelData().bonusPoints();
			g.drawImage(numbers.get(bonusPoints), 13 * TS, 20 * TS - HTS, null);
		}
		if (messageText != null) {
			g.setFont(scoreFont);
			g.setColor(messageColor);
			int textLength = g.getFontMetrics().stringWidth(messageText);
			g.drawString(messageText, WORLD_WIDTH_TILES * TS / 2 - textLength / 2, 21 * TS);
		}

		if (debugMode) {
			for (int x = 0; x < WORLD_WIDTH_TILES; ++x) {
				for (int y = 0; y < WORLD_HEIGHT_TILES; ++y) {
					if (game.world.isIntersectionTile(x, y)) {
						g.setColor(Color.RED);
						g.setStroke(new BasicStroke(0.1f));
						g.drawRect(x * TS, y * TS, TS, TS);
					}
				}
			}
		}
	}

	private void drawPacMan(Graphics2D g) {
		Creature pacMan = game.pacMan;
		BufferedImage sprite;
		int mouthFrame = (int) game.framesTotal % 15 / 5;
		if (pacMan.dead) {
			// 2 seconds full sprite before collapsing animation starts
			if (game.pacManDyingStateTimer >= sec(2) + 11 * 8) {
				sprite = section(2, 0);
			} else if (game.pacManDyingStateTimer >= sec(2)) {
				// collapsing animation
				int frame = (int) (game.pacManDyingStateTimer - sec(2)) / 8;
				sprite = section(13 - frame, 0);
			} else {
				// collapsed sprite after collapsing
				sprite = section(13, 0);
			}
		} else if (game.state == GameState.READY || game.state == GameState.CHANGING_LEVEL) {
			// full sprite
			sprite = section(2, 0);
		} else if (!pacMan.couldMove) {
			// wide open mouth
			sprite = section(0, directionFrame(pacMan.dir));
		} else {
			// closed mouth or open mouth pointing to move direction
			sprite = mouthFrame == 2 ? section(mouthFrame, 0) : section(mouthFrame, directionFrame(pacMan.dir));
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
			// number (bounty) or eyes looking into move direction
			sprite = ghost.bountyTimer > 0 ? bountyNumbers.get(ghost.bounty) : section(8 + directionFrame(ghost.dir), 5);
		} else if (ghost.frightened) {
			int walkingFrame = game.framesTotal % 60 < 30 ? 0 : 1;
			if (game.pacManPowerTimer < sec(2)) {
				// flashing blue/white, walking
				int flashingFrame = game.framesTotal % 20 < 10 ? 8 : 10;
				sprite = section(flashingFrame + walkingFrame, 4);
			} else {
				// blue, walking
				sprite = section(8 + walkingFrame, 4);
			}
		} else {
			int walkingFrame = game.framesTotal % 60 < 30 ? 0 : 1;
			sprite = section(2 * directionFrame(ghost.dir) + walkingFrame, 4 + ghostIndex);
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