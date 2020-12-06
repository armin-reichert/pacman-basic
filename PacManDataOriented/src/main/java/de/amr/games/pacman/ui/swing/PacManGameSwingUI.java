package de.amr.games.pacman.ui.swing;

import static de.amr.games.pacman.PacManGame.level;
import static de.amr.games.pacman.World.HTS;
import static de.amr.games.pacman.World.TS;
import static de.amr.games.pacman.World.WORLD_HEIGHT_TILES;
import static de.amr.games.pacman.World.WORLD_WIDTH_TILES;
import static de.amr.games.pacman.common.Direction.DOWN;
import static de.amr.games.pacman.common.Direction.LEFT;
import static de.amr.games.pacman.common.Direction.RIGHT;
import static de.amr.games.pacman.common.Direction.UP;

import java.awt.BasicStroke;
import java.awt.Canvas;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.Polygon;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.image.BufferStrategy;
import java.awt.image.BufferedImage;
import java.util.Map;

import javax.swing.JFrame;

import de.amr.games.pacman.GameClock;
import de.amr.games.pacman.GameState;
import de.amr.games.pacman.PacManGame;
import de.amr.games.pacman.common.Direction;
import de.amr.games.pacman.entities.Creature;
import de.amr.games.pacman.entities.Ghost;
import de.amr.games.pacman.ui.PacManGameUI;

/**
 * Swing UI for Pac-Man game.
 * 
 * @author Armin Reichert
 */
public class PacManGameSwingUI extends JFrame implements PacManGameUI {

	//@formatter:off
	static final Map<String, Color> GHOST_COLORS = Map.of(
		"Blinky", Color.RED, 
		"Pinky ", Color.PINK, 
		"Inky  ", Color.CYAN,	
		"Clyde ", Color.ORANGE);
	
	static final Object[][] GHOST_INTRO_TEXTS = {
		{ "SHADOW ", "\"BLINKY\"", Color.RED }, 
		{ "SPEEDY ", "\"PINKY\"",  Color.PINK },
		{ "BASHFUL", "\"INKY\"",   Color.CYAN }, 
		{ "POKEY  ", "\"CLYDE\"",  Color.ORANGE }
	};

	static final Map<Direction,Integer> DIR_INDEX = Map.of(RIGHT, 0, LEFT, 1, UP, 2,	DOWN, 3);
	
	static final Polygon TRIANGLE = new Polygon(new int[] { -4, 4, 0 }, new int[] { 0, 0, 4 }, 3);
	//@formatter:on

	public static void drawCenteredText(Graphics2D g, String text, int y, int width) {
		int textWidth = g.getFontMetrics().stringWidth(text);
		g.drawString(text, (width - textWidth) / 2, y);

	}

	public static void blinking(GameClock clock, int ticks, Runnable code) {
		if (clock.framesTotal % (2 * ticks) < ticks) {
			code.run();
		}
	}

	public static int dirIndex(Direction dir) {
		return DIR_INDEX.get(dir);
	}

	final Assets assets;
	final PacManGame game;
	final float scaling;
	final Canvas canvas;
	final Keyboard keyboard;
	final Dimension unscaledSize;
	final IntroScene introScene;
	boolean debugMode;
	String messageText;
	Color messageColor;

	public PacManGameSwingUI(PacManGame game, float scaling) {
		this.game = game;
		this.scaling = scaling;
		unscaledSize = new Dimension(WORLD_WIDTH_TILES * TS, WORLD_HEIGHT_TILES * TS);
		messageText = null;
		messageColor = Color.YELLOW;
		assets = new Assets();
		keyboard = new Keyboard(this);

		introScene = new IntroScene(game, assets, unscaledSize);

		canvas = new Canvas();
		canvas.setBackground(Color.BLACK);
		canvas.setSize((int) (unscaledSize.width * scaling), (int) (unscaledSize.height * scaling));
		canvas.setFocusable(false);

		add(canvas);
		setTitle("Pac-Man");
		setIconImage(assets.sheet(1, dirIndex(RIGHT)));
		setResizable(false);
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		addWindowListener(new WindowAdapter() {

			@Override
			public void windowClosing(WindowEvent e) {
				onExit();
			}
		});
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
				g.setColor(canvas.getBackground());
				g.fillRect(0, 0, canvas.getWidth(), canvas.getHeight());
				g.scale(scaling, scaling);
				drawCurrentScene(g);
				g.dispose();
			} while (buffers.contentsRestored());
			buffers.show();
		} while (buffers.contentsLost());
	}

	@Override
	public void showGameOverMessage() {
		messageText = "Game Over!";
		messageColor = Color.RED;
	}

	@Override
	public void showGameReadyMessage() {
		messageText = "Ready!";
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

	@Override
	public void onExit() {
		game.exit();
	}

	private void drawCurrentScene(Graphics2D g) {
		if (game.state == GameState.INTRO) {
			introScene.draw(g);
		} else {
			drawPlayingScene(g);
		}
		if (game.paused) {
			g.setColor(new Color(200, 200, 200, 100));
			g.fillRect(0, 0, unscaledSize.width, unscaledSize.height);
			g.setColor(Color.GREEN);
			g.setFont(new Font(Font.MONOSPACED, Font.BOLD, 28));
			drawCenteredText(g, "PAUSED", 16 * TS, unscaledSize.width);
		}
	}

	@Override
	public void startIntroScene() {
		introScene.reset();
	}

	private void drawFPS(Graphics2D g) {
		g.setColor(Color.GRAY);
		g.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 8));
		drawCenteredText(g, String.format("%d fps", game.clock.fps), unscaledSize.height - 3, unscaledSize.width);
	}

	private void drawPlayingScene(Graphics2D g) {
		drawScore(g);
		drawLivesCounter(g);
		drawLevelCounter(g);
		drawMaze(g);
		drawPacMan(g);
		for (int ghost = 0; ghost < 4; ++ghost) {
			drawGhost(g, ghost);
		}
		drawDebugInfo(g);
		drawFPS(g);
	}

	private void drawDebugInfo(Graphics2D g) {
		if (debugMode) {
			long remaining = game.state.ticksRemaining();
			String ticksText = remaining == Long.MAX_VALUE ? "forever" : remaining + " ticks remaining";
			String stateText = String.format("%s (%s)", game.stateDescription(), ticksText);
			g.setColor(Color.WHITE);
			g.setFont(new Font("Arial", Font.PLAIN, 6));
			g.drawString(stateText, 1 * TS, 3 * TS);
			for (Ghost ghost : game.ghosts) {
				if (ghost.targetTile != null) {
					g.setColor(GHOST_COLORS.get(ghost.name));
					g.fillRect(ghost.targetTile.x * TS + HTS / 2, ghost.targetTile.y * TS + HTS / 2, HTS, HTS);
				}
			}
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
		g.drawString(String.format("%08d", game.points), 1 * TS, 2 * TS);
		g.setColor(Color.LIGHT_GRAY);
		g.drawString(String.format("L%02d", game.level), 9 * TS, 2 * TS);
		g.setColor(Color.YELLOW);
		g.drawString(String.format("%08d", game.hiscorePoints), 16 * TS, 2 * TS);
		g.setColor(Color.LIGHT_GRAY);
		g.drawString(String.format("L%02d", game.hiscoreLevel), 24 * TS, 2 * TS);
		g.translate(0, -3);
	}

	private void drawLivesCounter(Graphics2D g) {
		for (int i = 0; i < game.lives; ++i) {
			g.drawImage(assets.imageLive, 2 * (i + 1) * TS, unscaledSize.height - 2 * TS, null);
		}
	}

	private void drawLevelCounter(Graphics2D g) {
		int x = (WORLD_WIDTH_TILES - 4) * TS;
		int first = Math.max(1, game.level - 6);
		for (int level = first; level <= game.level; ++level) {
			BufferedImage symbol = assets.symbols.get(level(level).bonusSymbol);
			g.drawImage(symbol, x, unscaledSize.height - 2 * TS, null);
			x -= 2 * TS;
		}
	}

	private void hideTile(Graphics2D g, int x, int y) {
		g.setColor(Color.BLACK);
		g.fillRect(x * TS, y * TS, TS, TS);
	}

	private void drawMazeFlashing(Graphics2D g) {
		if (game.mazeFlashesRemaining > 0 && game.clock.framesTotal % 30 < 15) {
			g.drawImage(assets.imageMazeEmptyWhite, 0, 3 * TS, null);
			if (game.clock.framesTotal % 30 == 14) {
				--game.mazeFlashesRemaining;
			}
		} else {
			g.drawImage(assets.imageMazeEmpty, 0, 3 * TS, null);
		}
	}

	private void drawMaze(Graphics2D g) {
		if (game.state == GameState.CHANGING_LEVEL && game.state.ticksRemaining() <= game.clock.sec(4f)) {
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
				if (game.world.isEnergizerTile(x, y) && game.clock.framesTotal % 20 < 10 && (game.state == GameState.HUNTING)) {
					hideTile(g, x, y);
				}
			}
		}
		if (game.bonusAvailableTimer > 0) {
			String symbolName = game.level().bonusSymbol;
			g.drawImage(assets.symbols.get(symbolName), 13 * TS, 20 * TS - HTS, null);
		}
		if (game.bonusConsumedTimer > 0) {
			int number = game.level().bonusPoints;
			BufferedImage image = assets.numbers.get(number);
			if (number < 2000) {
				g.drawImage(image, 13 * TS, 20 * TS - HTS, null);
			} else {
				g.drawImage(image, (unscaledSize.width - image.getWidth()) / 2, 20 * TS - HTS, null);
			}
		}
		if (messageText != null) {
			g.setFont(assets.scoreFont);
			g.setColor(messageColor);
			drawCenteredText(g, messageText, 21 * TS, unscaledSize.width);
		}

		if (debugMode) {
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
	}

	private void drawPacMan(Graphics2D g) {
		Creature pacMan = game.pacMan;
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
		} else if (game.state == GameState.READY || game.state == GameState.CHANGING_LEVEL
				|| game.state == GameState.GAME_OVER) {
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
		}
	}
}