package de.amr.games.pacman.ui.swing;

import static de.amr.games.pacman.core.World.TS;
import static de.amr.games.pacman.core.World.WORLD_TILES;
import static de.amr.games.pacman.lib.Direction.DOWN;
import static de.amr.games.pacman.lib.Direction.LEFT;
import static de.amr.games.pacman.lib.Direction.RIGHT;
import static de.amr.games.pacman.lib.Direction.UP;
import static java.awt.EventQueue.invokeLater;

import java.awt.Canvas;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.image.BufferStrategy;
import java.util.Map;

import javax.swing.JFrame;

import de.amr.games.pacman.core.Game;
import de.amr.games.pacman.core.GameState;
import de.amr.games.pacman.lib.Direction;
import de.amr.games.pacman.lib.V2i;
import de.amr.games.pacman.ui.PacManGameUI;
import de.amr.games.pacman.ui.Sound;

/**
 * Swing UI for Pac-Man game.
 * 
 * @author Armin Reichert
 */
public class PacManGameSwingUI implements PacManGameUI {

	static final Map<Direction, Integer> DIR_INDEX = Map.of(RIGHT, 0, LEFT, 1, UP, 2, DOWN, 3);

	static void drawCenteredText(Graphics2D g, String text, int y, int width) {
		int textWidth = g.getFontMetrics().stringWidth(text);
		g.drawString(text, (width - textWidth) / 2, y);
	}

	static int dirIndex(Direction dir) {
		return DIR_INDEX.get(dir);
	}

	private final Assets assets;
	private final Game game;
	private final V2i unscaledSize;
	private final float scaling;
	private final JFrame window;
	private final Canvas canvas;
	private final Keyboard keyboard;
	private final SoundManager soundManager;

	private final IntroScene introScene;
	private final PlayScene playScene;

	private boolean debugMode;

	public PacManGameSwingUI(Game game, float scaling) {
		this.game = game;
		this.scaling = scaling;
		unscaledSize = new V2i(WORLD_TILES.x * TS, WORLD_TILES.y * TS);
		assets = new Assets();
		introScene = new IntroScene(game, this, assets, unscaledSize);
		playScene = new PlayScene(game, assets, unscaledSize);

		window = new JFrame();
		keyboard = new Keyboard(window);
		soundManager = new SoundManager(assets);

		window.setTitle("Pac-Man");
		window.setIconImage(assets.sheet(1, dirIndex(RIGHT)));
		window.setResizable(false);
		window.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		window.addWindowListener(new WindowAdapter() {

			@Override
			public void windowClosing(WindowEvent e) {
				onExit();
			}
		});

		canvas = new Canvas();
		canvas.setBackground(Color.BLACK);
		canvas.setSize((int) (unscaledSize.x * scaling), (int) (unscaledSize.y * scaling));
		canvas.setFocusable(false);
		window.add(canvas);

	}

	@Override
	public void show() {
		window.pack();
		window.setLocationRelativeTo(null);
		window.setVisible(true);
		// these must called be *after* setVisible():
		window.requestFocus();
		canvas.createBufferStrategy(2);
		soundManager.init();
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
	public boolean keyPressed(String keySpec) {
		boolean pressed = keyboard.keyPressed(keySpec);
		keyboard.clearKey(keySpec);
		return pressed;
	}

	@Override
	public boolean anyKeyPressed() {
		return keyboard.anyKeyPressed();
	}

	@Override
	public void onExit() {
		game.exit();
	}

	private void drawCurrentScene(Graphics2D g) {
		if (game.gamePaused) {
			drawPausedScreen(g);
		} else {
			if (game.state == GameState.INTRO) {
				introScene.draw(g);
			} else {
				playScene.draw(g);
			}
		}
		invokeLater(() -> window.setTitle(String.format("Pac-Man (%d fps)", game.clock.fps)));
	}

	private void drawPausedScreen(Graphics2D g) {
		int w = assets.imageLogo.getWidth();
		g.drawImage(assets.imageLogo, (unscaledSize.x - w) / 2, 3, null);
		g.setColor(new Color(200, 200, 200, 100));
		g.fillRect(0, 0, unscaledSize.x, unscaledSize.y);
		g.setColor(Color.GREEN);
		g.setFont(new Font(Font.MONOSPACED, Font.BOLD, 28));
		g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
		drawCenteredText(g, "PAUSED", 16 * TS, unscaledSize.x);
		g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_OFF);
	}

	@Override
	public void startIntroScene() {
		introScene.reset();
	}

	@Override
	public void endIntroScene() {
		introScene.mute();
	}

	@Override
	public void playSound(Sound sound) {
		soundManager.playSound(sound);
	}

	@Override
	public void loopSound(Sound sound) {
		soundManager.loopSound(sound);
	}

	@Override
	public void stopSound(Sound sound) {
		soundManager.stopSound(sound);
	}

	@Override
	public void stopAllSounds() {
		soundManager.stopAllSounds();
	}
}