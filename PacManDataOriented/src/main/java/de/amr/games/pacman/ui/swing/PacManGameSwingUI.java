package de.amr.games.pacman.ui.swing;

import static de.amr.games.pacman.PacManGame.BLINKY;
import static de.amr.games.pacman.PacManGame.CLYDE;
import static de.amr.games.pacman.PacManGame.INKY;
import static de.amr.games.pacman.PacManGame.PINKY;
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

	final Assets assets;
	final PacManGame game;
	final float scaling;
	final Canvas canvas;
	final Keyboard keyboard;
	final Dimension unscaledSize;
	final Intro intro;
	boolean debugMode;
	String messageText;
	Color messageColor;

	class Intro {

		long timer;
		int pacManX;
		boolean chasingPacMan;

		public Intro() {
			reset();
		}

		void reset() {
			timer = 0;
			pacManX = unscaledSize.width;
			chasingPacMan = true;
		}

		void draw(Graphics2D g) {
			g.setFont(assets.scoreFont);

			int time = 1;
			if (timer >= game.clock.sec(time)) {
				int w = assets.imageLogo.getWidth();
				g.drawImage(assets.imageLogo, (unscaledSize.width - w) / 2, 3, null);
			}

			time += 1;
			if (timer >= game.clock.sec(time)) {
				g.setColor(Color.WHITE);
				drawCenteredText(g, "CHARACTER / NICKNAME", 8 * TS);
			}

			time += 1;
			for (int ghost = 0; ghost <= 3; ++ghost) {
				int y = (10 + 3 * ghost) * TS;
				if (timer >= game.clock.sec(time)) {
					BufferedImage ghostLookingRight = assets.sheet(0, 4 + ghost);
					g.drawImage(ghostLookingRight, 2 * TS - 3, y - 2, null);
				}
				if (timer >= game.clock.sec(time + 0.5f)) {
					String character = (String) GHOST_INTRO_TEXTS[ghost][0];
					String nickname = (String) GHOST_INTRO_TEXTS[ghost][1];
					Color color = (Color) GHOST_INTRO_TEXTS[ghost][2];
					String text = "-";
					text += timer > game.clock.sec(time + 1) ? character + "    " + nickname : character;
					g.setColor(color);
					g.drawString(text, 4 * TS, y + 11);
				}
				time += 2;
			}

			if (timer >= game.clock.sec(time)) {
				g.setColor(Color.PINK);
				g.fillRect(9 * TS + 6, 27 * TS + 2, 2, 2);
				drawCenteredText(g, "10 PTS", 28 * TS);
				blinking(20, () -> {
					g.fillOval(9 * TS, 29 * TS - 2, 10, 10);
				});
				drawCenteredText(g, "50 PTS", 30 * TS);
			}

			time += 1;
			if (timer >= game.clock.sec(time)) {
				if (chasingPacMan) {
					drawChasingPacMan(g, time);
				} else {
					drawChasingGhosts(g, time);
				}
			}

			time += 1;
			if (timer >= game.clock.sec(time)) {
				g.setColor(Color.WHITE);
				blinking(20, () -> {
					drawCenteredText(g, "Press SPACE to play!", unscaledSize.height - 20);
				});
			}

			++timer;
			if (timer == game.clock.sec(30)) {
				reset();
			}
		}

		void drawChasingPacMan(Graphics2D g, int time) {
			int x = pacManX;
			int y = 22 * TS;
			blinking(20, () -> {
				g.setColor(Color.PINK);
				g.fillOval(2 * TS, y + 2, 10, 10);
			});
			g.drawImage(pacManWalking(LEFT), x, y, null);
			x += 24;
			g.drawImage(ghostWalking(LEFT, BLINKY), x, y, null);
			x += 16;
			g.drawImage(ghostWalking(LEFT, PINKY), x, y, null);
			x += 16;
			g.drawImage(ghostWalking(LEFT, INKY), x, y, null);
			x += 16;
			g.drawImage(ghostWalking(LEFT, CLYDE), x, y, null);
			if (pacManX > 2 * TS) {
				pacManX -= 1;
			} else {
				chasingPacMan = false;
			}
		}

		void drawChasingGhosts(Graphics2D g, int time) {
			int x = pacManX;
			int y = 22 * TS;
			g.drawImage(pacManWalking(RIGHT), x, y, null);
			x += 24;
			BufferedImage ghost = ghostFrightened();
			for (int i = 0; i < 4; ++i) {
				g.drawImage(ghost, x, y, null);
				x += 16;
			}
			if (pacManX < unscaledSize.width) {
				pacManX += 1;
			} else {
				// TODO what?
			}
		}

		BufferedImage pacManWalking(Direction dir) {
			int mouthFrame = frameIndex(5, 3);
			return mouthFrame == 2 ? assets.sheet(mouthFrame, 0) : assets.sheet(mouthFrame, DIR_INDEX.get(dir));
		}

		BufferedImage ghostWalking(Direction dir, int ghost) {
			return assets.sheet(2 * DIR_INDEX.get(dir) + frameIndex(5, 2), 4 + ghost);
		}

		BufferedImage ghostFrightened() {
			return assets.sheet(8 + frameIndex(5, 2), 4);
		}
	}

	public PacManGameSwingUI(PacManGame game, float scaling) {
		this.game = game;
		this.scaling = scaling;
		unscaledSize = new Dimension(WORLD_WIDTH_TILES * TS, WORLD_HEIGHT_TILES * TS);
		messageText = null;
		messageColor = Color.YELLOW;
		intro = new Intro();
		assets = new Assets();
		keyboard = new Keyboard(this);
		canvas = new Canvas();
		canvas.setBackground(Color.BLACK);
		canvas.setSize((int) (unscaledSize.width * scaling), (int) (unscaledSize.height * scaling));
		canvas.setFocusable(false);
		add(canvas);
		setTitle("Pac-Man");
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
			intro.draw(g);
		} else {
			drawPlayingScene(g);
		}
		if (game.paused) {
			g.setColor(new Color(200, 200, 200, 100));
			g.fillRect(0, 0, unscaledSize.width, unscaledSize.height);
			g.setColor(Color.GREEN);
			g.setFont(new Font(Font.MONOSPACED, Font.BOLD, 28));
			drawCenteredText(g, "PAUSED", 16 * TS);
		}
	}

	@Override
	public void startIntroAnimation() {
		intro.reset();
	}

	private void blinking(int ticks, Runnable code) {
		if (game.clock.framesTotal % (2 * ticks) < ticks) {
			code.run();
		}
	}

	private void drawFPS(Graphics2D g) {
		g.setColor(Color.GRAY);
		g.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 8));
		drawCenteredText(g, String.format("%d fps", game.clock.fps), unscaledSize.height - 3);
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

	private void drawCenteredText(Graphics2D g, String text, int y) {
		int textWidth = g.getFontMetrics().stringWidth(text);
		g.drawString(text, (unscaledSize.width - textWidth) / 2, y);

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
			drawCenteredText(g, messageText, 21 * TS);
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
			sprite = assets.sheet(0, DIR_INDEX.get(pacMan.dir));
		} else {
			// switch between mouth closed and mouth open
			int mouthFrame = frameIndex(5, 3);
			sprite = mouthFrame == 2 ? assets.sheet(mouthFrame, 0) : assets.sheet(mouthFrame, DIR_INDEX.get(pacMan.dir));
		}
		g.drawImage(sprite, (int) pacMan.position.x - HTS, (int) pacMan.position.y - HTS, null);
	}

	private void drawGhost(Graphics2D g, int ghostIndex) {
		Ghost ghost = game.ghosts[ghostIndex];
		if (!ghost.visible) {
			return;
		}

		BufferedImage sprite;
		int dir = DIR_INDEX.get(ghost.dir);
		int walking = ghost.speed == 0 ? 0 : frameIndex(5, 2);
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
				int flashing = frameIndex(10, 2) == 0 ? 8 : 10;
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

	private int frameIndex(int frameDurationTicks, int numFrames) {
		return (int) (game.clock.framesTotal / frameDurationTicks) % numFrames;
	}
}