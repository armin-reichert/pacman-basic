package de.amr.games.pacman.ui.swing;

import static de.amr.games.pacman.World.TS;
import static de.amr.games.pacman.World.WORLD_HEIGHT_TILES;
import static de.amr.games.pacman.World.WORLD_WIDTH_TILES;
import static de.amr.games.pacman.common.Direction.DOWN;
import static de.amr.games.pacman.common.Direction.LEFT;
import static de.amr.games.pacman.common.Direction.RIGHT;
import static de.amr.games.pacman.common.Direction.UP;

import java.awt.Canvas;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.image.BufferStrategy;
import java.util.Map;

import javax.swing.JFrame;

import de.amr.games.pacman.GameState;
import de.amr.games.pacman.PacManGame;
import de.amr.games.pacman.common.Direction;
import de.amr.games.pacman.ui.PacManGameUI;

/**
 * Swing UI for Pac-Man game.
 * 
 * @author Armin Reichert
 */
public class PacManGameSwingUI extends JFrame implements PacManGameUI {

	static final Map<Direction, Integer> DIR_INDEX = Map.of(RIGHT, 0, LEFT, 1, UP, 2, DOWN, 3);

	public static void drawCenteredText(Graphics2D g, String text, int y, int width) {
		int textWidth = g.getFontMetrics().stringWidth(text);
		g.drawString(text, (width - textWidth) / 2, y);
	}

	public static int dirIndex(Direction dir) {
		return DIR_INDEX.get(dir);
	}

	public final Assets assets;
	public final PacManGame game;
	public final float scaling;
	public final Canvas canvas;
	public final Keyboard keyboard;
	public final Dimension unscaledSize;
	public final IntroScene introScene;
	public final PlayScene playScene;
	public boolean debugMode;

	public PacManGameSwingUI(PacManGame game, float scaling) {
		this.game = game;
		this.scaling = scaling;
		unscaledSize = new Dimension(WORLD_WIDTH_TILES * TS, WORLD_HEIGHT_TILES * TS);
		assets = new Assets();
		keyboard = new Keyboard(this);

		introScene = new IntroScene(game, assets, unscaledSize);
		playScene = new PlayScene(game, assets, unscaledSize);

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
		playScene.debugMode = debug;
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
		playScene.messageText = "Game Over!";
		playScene.messageColor = Color.RED;
	}

	@Override
	public void showGameReadyMessage() {
		playScene.messageText = "Ready!";
		playScene.messageColor = Color.YELLOW;
	}

	@Override
	public void clearMessage() {
		playScene.messageText = null;
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
			playScene.draw(g);
		}
		if (game.paused) {
			drawPauseText(g);
		}
		if (debugMode) {
			drawFPS(g);
		}
	}

	@Override
	public void startIntroScene() {
		introScene.reset();
	}

	private void drawPauseText(Graphics2D g) {
		g.setColor(new Color(200, 200, 200, 100));
		g.fillRect(0, 0, unscaledSize.width, unscaledSize.height);
		g.setColor(Color.GREEN);
		g.setFont(new Font(Font.MONOSPACED, Font.BOLD, 28));
		drawCenteredText(g, "PAUSED", 16 * TS, unscaledSize.width);
	}

	private void drawFPS(Graphics2D g) {
		g.setColor(Color.GRAY);
		g.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 8));
		drawCenteredText(g, String.format("%d fps", game.clock.fps), unscaledSize.height - 3, unscaledSize.width);
	}

}