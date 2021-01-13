package de.amr.games.pacman.ui.swing;

import static de.amr.games.pacman.worlds.PacManGameWorld.TS;
import static de.amr.games.pacman.worlds.PacManGameWorld.t;

import java.awt.Canvas;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.image.BufferStrategy;

import javax.swing.JFrame;
import javax.swing.Timer;

import de.amr.games.pacman.core.GameVariant;
import de.amr.games.pacman.core.PacManGame;
import de.amr.games.pacman.core.PacManGameState;
import de.amr.games.pacman.lib.Direction;
import de.amr.games.pacman.lib.V2i;
import de.amr.games.pacman.ui.PacManGameUI;
import de.amr.games.pacman.ui.Sound;
import de.amr.games.pacman.worlds.classic.PacManClassicAssets;
import de.amr.games.pacman.worlds.classic.PacManClassicIntroScene;
import de.amr.games.pacman.worlds.classic.PacManClassicPlayScene;
import de.amr.games.pacman.worlds.mspacman.MsPacManAssets;
import de.amr.games.pacman.worlds.mspacman.MsPacManIntroScene;
import de.amr.games.pacman.worlds.mspacman.MsPacManPlayScene;

/**
 * Swing UI for Pac-Man game.
 * 
 * @author Armin Reichert
 */
public class PacManGameSwingUI implements PacManGameUI {

	private final V2i sizeInPixels; // unscaled size in pixels
	private final float scaling;
	private final JFrame window;
	private final Canvas canvas;
	private final Keyboard keyboard;

	private boolean debugMode;
	private String messageText;
	private Color messageColor;
	private Font messageFont;
	private Timer titleUpdate;
	private PacManGame game;
	private PacManGameScene currentScene;
	private PacManGameScene introScene;
	private PacManGameScene playScene;
	private SoundManager soundManager;

	public PacManGameSwingUI(V2i sizeInTiles, float scaling) {
		this.scaling = scaling;
		sizeInPixels = sizeInTiles.scaled(TS);

		window = new JFrame();
		keyboard = new Keyboard(window);

		window.setResizable(false);
		window.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		// TODO use separate icon
		PacManClassicAssets assets = new PacManClassicAssets();
		window.setIconImage(assets.section(1, PacManClassicAssets.DIR_INDEX.get(Direction.RIGHT)));

		window.addWindowListener(new WindowAdapter() {

			@Override
			public void windowClosing(WindowEvent e) {
				onWindowClosing();
			}
		});
		window.setTitle("Pac-Man");

		canvas = new Canvas();
		canvas.setBackground(Color.BLACK);
		canvas.setSize((int) (sizeInPixels.x * scaling), (int) (sizeInPixels.y * scaling));
		canvas.setFocusable(false);
		window.add(canvas);

		messageFont = PacManGameAssets.font("/PressStart2P-Regular.ttf", 8).deriveFont(Font.ITALIC);
	}

	@Override
	public void setGame(PacManGame game) {
		this.game = game;
		game.ui = this;
		if (soundManager != null) {
			stopAllSounds();
		}
		if (titleUpdate != null) {
			titleUpdate.stop();
		}
		titleUpdate = new Timer(1000,
				e -> window.setTitle(String.format("%s (%d fps)", game.world.pacName(), game.clock.frequency)));
		if (game.variant == GameVariant.CLASSIC) {
			initPacManClassic();
		} else {
			initMsPacManWorld();
		}
	}

	private void initPacManClassic() {
		PacManClassicAssets assets = new PacManClassicAssets();
		soundManager = new SoundManager(assets);
		introScene = new PacManClassicIntroScene(game, sizeInPixels, assets);
		playScene = new PacManClassicPlayScene(game, sizeInPixels, assets);
	}

	private void initMsPacManWorld() {
		MsPacManAssets assets = new MsPacManAssets();
		soundManager = new SoundManager(assets);
		introScene = new MsPacManIntroScene(game, sizeInPixels, assets);
		playScene = new MsPacManPlayScene(game, sizeInPixels, assets);
	}

	@Override
	public float scaling() {
		return scaling;
	}

	@Override
	public void show() {
		window.pack();
		window.setLocationRelativeTo(null);
		window.setVisible(true);

		// must called be *after* setVisible()
		window.requestFocus();
		canvas.createBufferStrategy(2);

		titleUpdate.start();
	}

	@Override
	public void showMessage(String message, boolean important) {
		messageText = message;
		messageColor = important ? Color.RED : Color.yellow;
	}

	@Override
	public void clearMessage() {
		messageText = null;
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
				if (game.gamePaused) {
					drawPausedScreen(g);
				} else {
					updateScene();
					currentScene.draw(g);
				}
				drawMessage(g);
				g.dispose();
			} while (buffers.contentsRestored());
			buffers.show();
		} while (buffers.contentsLost());
	}

	private void drawPausedScreen(Graphics2D g) {
		g.setColor(new Color(200, 200, 200, 100));
		g.fillRect(0, 0, sizeInPixels.x, sizeInPixels.y);
		g.setColor(Color.GREEN);
		g.setFont(new Font(Font.MONOSPACED, Font.BOLD, 28));
		g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
		g.drawString("PAUSED", (sizeInPixels.x - g.getFontMetrics().stringWidth("PAUSED")) / 2, t(16));
		g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_OFF);
	}

	private void drawMessage(Graphics2D g) {
		if (messageText != null) {
			g.setFont(messageFont);
			g.setColor(messageColor);
			int textWidth = g.getFontMetrics().stringWidth(messageText);
			g.drawString(messageText, (sizeInPixels.x - textWidth) / 2, t(21));
		}
	}

	@Override
	public boolean keyPressed(String keySpec) {
		boolean pressed = keyboard.keyPressed(keySpec);
		keyboard.clearKey(keySpec); // TODO
		return pressed;
	}

	@Override
	public boolean anyKeyPressed() {
		return keyboard.anyKeyPressed();
	}

	@Override
	public void onWindowClosing() {
		game.exit();
	}

	private void updateScene() {
		PacManGameScene scene = null;
		if (game.state == PacManGameState.INTRO) {
			scene = introScene;
		} else {
			scene = playScene;
		}
		if (scene != currentScene) {
			if (currentScene != null) {
				currentScene.end();
			}
			currentScene = scene;
			currentScene.start();
		}
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