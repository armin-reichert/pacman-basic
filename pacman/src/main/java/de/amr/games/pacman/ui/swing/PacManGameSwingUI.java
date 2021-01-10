package de.amr.games.pacman.ui.swing;

import static de.amr.games.pacman.worlds.PacManGameWorld.TS;
import static de.amr.games.pacman.worlds.PacManGameWorld.t;

import java.awt.Canvas;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.image.BufferStrategy;

import javax.swing.JFrame;
import javax.swing.Timer;

import de.amr.games.pacman.core.PacManGame;
import de.amr.games.pacman.core.PacManGameState;
import de.amr.games.pacman.lib.Direction;
import de.amr.games.pacman.ui.PacManGameUI;
import de.amr.games.pacman.ui.Sound;
import de.amr.games.pacman.worlds.classic.PacManClassicAssets;
import de.amr.games.pacman.worlds.classic.PacManClassicIntroScene;
import de.amr.games.pacman.worlds.classic.PacManClassicPlayScene;

/**
 * Swing UI for Pac-Man game.
 * 
 * @author Armin Reichert
 */
public class PacManGameSwingUI implements PacManGameUI {

	private final Dimension unscaledSize;
	private final float scaling;
	private final JFrame window;
	private final Timer windowTitleUpdate;
	private final Canvas canvas;
	private final Keyboard keyboard;

	private final PacManGame game;

	private Scene currentScene;
	private Scene introScene;
	private Scene playScene;

	private SoundManager soundManager;

	private boolean debugMode;

	public PacManGameSwingUI(PacManGame game, float scaling) {
		this.game = game;
		this.scaling = scaling;

		unscaledSize = new Dimension(game.world.size().x * TS, game.world.size().y * TS);

		window = new JFrame();
		keyboard = new Keyboard(window);

		window.setResizable(false);
		window.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		window.addWindowListener(new WindowAdapter() {

			@Override
			public void windowClosing(WindowEvent e) {
				onExit();
			}
		});
		window.setTitle("Pac-Man");
		windowTitleUpdate = new Timer(1000, e -> window.setTitle(String.format("Pac-Man (%d fps)", game.clock.frequency)));

		canvas = new Canvas();
		canvas.setBackground(Color.BLACK);
		canvas.setSize((int) (unscaledSize.width * scaling), (int) (unscaledSize.height * scaling));
		canvas.setFocusable(false);
		window.add(canvas);
	}

	public void configurePacManClassic(PacManClassicAssets assets) {
		window.setIconImage(assets.section(1, PacManClassicAssets.DIR_INDEX.get(Direction.RIGHT)));
		soundManager = new SoundManager(assets);
		introScene = new PacManClassicIntroScene(game, unscaledSize, assets);
		playScene = new PacManClassicPlayScene(game, unscaledSize, assets);
	}

	@Override
	public void show() {
		windowTitleUpdate.start();
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
				g.dispose();
			} while (buffers.contentsRestored());
			buffers.show();
		} while (buffers.contentsLost());
	}

	private void drawPausedScreen(Graphics2D g) {
		g.setColor(new Color(200, 200, 200, 100));
		g.fillRect(0, 0, unscaledSize.width, unscaledSize.height);
		g.setColor(Color.GREEN);
		g.setFont(new Font(Font.MONOSPACED, Font.BOLD, 28));
		g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
		g.drawString("PAUSED", (unscaledSize.width - g.getFontMetrics().stringWidth("PAUSED")) / 2, t(16));
		g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_OFF);
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
	public void onExit() {
		game.exit();
	}

	private void updateScene() {
		Scene scene = null;
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