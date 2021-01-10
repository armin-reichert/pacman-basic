package de.amr.games.pacman.ui.swing;

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

import de.amr.games.pacman.core.PacManGame;
import de.amr.games.pacman.core.PacManGameState;
import de.amr.games.pacman.lib.Direction;
import de.amr.games.pacman.lib.V2i;
import de.amr.games.pacman.ui.PacManGameUI;
import de.amr.games.pacman.ui.Sound;
import de.amr.games.pacman.worlds.classic.PacManClassicAssets;
import de.amr.games.pacman.worlds.classic.PacManClassicIntroScene;
import de.amr.games.pacman.worlds.classic.PacManClassicPlayScene;
import de.amr.games.pacman.worlds.classic.PacManClassicWorld;

/**
 * Swing UI for Pac-Man game.
 * 
 * @author Armin Reichert
 */
public class PacManGameSwingUI implements PacManGameUI {

	private final V2i sizeUnscaled;
	private final float scaling;
	private final JFrame window;
	private final Canvas canvas;
	private final Keyboard keyboard;

	private boolean debugMode;
	private Timer windowTitleUpdate;
	private PacManGame game;
	private Scene currentScene;
	private Scene introScene;
	private Scene playScene;
	private SoundManager soundManager;

	public PacManGameSwingUI(V2i sizeUnscaled, float scaling) {
		this.scaling = scaling;
		this.sizeUnscaled = sizeUnscaled;

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

		canvas = new Canvas();
		canvas.setBackground(Color.BLACK);
		canvas.setSize((int) (sizeUnscaled.x * scaling), (int) (sizeUnscaled.y * scaling));
		canvas.setFocusable(false);
		window.add(canvas);
	}

	@Override
	public void setGame(PacManGame game) {
		this.game = game;
		game.ui = this;
		windowTitleUpdate = new Timer(1000, e -> window.setTitle(String.format("Pac-Man (%d fps)", game.clock.frequency)));
		if (game.world instanceof PacManClassicWorld) {
			initPacManClassic();
		} else {
			throw new IllegalArgumentException("Unknown game world: " + game.world);
		}
	}

	private void initPacManClassic() {
		PacManClassicAssets assets = new PacManClassicAssets("pacman_classic");
		window.setIconImage(assets.section(1, PacManClassicAssets.DIR_INDEX.get(Direction.RIGHT)));
		soundManager = new SoundManager(assets);
		soundManager.init();
		introScene = new PacManClassicIntroScene(game, sizeUnscaled, assets);
		playScene = new PacManClassicPlayScene(game, sizeUnscaled, assets);
	}

	@Override
	public void show() {
		window.pack();
		window.setLocationRelativeTo(null);
		window.setVisible(true);

		// must called be *after* setVisible()
		window.requestFocus();
		canvas.createBufferStrategy(2);

		windowTitleUpdate.start();
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
		g.fillRect(0, 0, sizeUnscaled.x, sizeUnscaled.y);
		g.setColor(Color.GREEN);
		g.setFont(new Font(Font.MONOSPACED, Font.BOLD, 28));
		g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
		g.drawString("PAUSED", (sizeUnscaled.x - g.getFontMetrics().stringWidth("PAUSED")) / 2, t(16));
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