package de.amr.games.pacman.ui.swing;

import static de.amr.games.pacman.core.World.TS;
import static de.amr.games.pacman.core.World.t;
import static de.amr.games.pacman.lib.Direction.RIGHT;

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

import de.amr.games.pacman.core.Game;
import de.amr.games.pacman.core.GameState;
import de.amr.games.pacman.ui.PacManGameUI;
import de.amr.games.pacman.ui.Sound;

/**
 * Swing UI for Pac-Man game.
 * 
 * @author Armin Reichert
 */
public class PacManGameSwingUI implements PacManGameUI {

	private final Assets assets;
	private final Game game;
	private final Dimension unscaledSize;
	private final float scaling;
	private final JFrame window;
	private final Timer windowTitleUpdate;
	private final Canvas canvas;
	private final Keyboard keyboard;
	private final SoundManager soundManager;

	private final IntroScene introScene;
	private final PlayScene playScene;

	private boolean debugMode;

	public PacManGameSwingUI(Game game, float scaling) {
		this.game = game;
		this.scaling = scaling;
		unscaledSize = new Dimension(game.world.size.x * TS, game.world.size.y * TS);
		assets = new Assets();
		introScene = new IntroScene(game, assets, unscaledSize);
		playScene = new PlayScene(game, assets, unscaledSize);

		window = new JFrame();
		keyboard = new Keyboard(window);
		soundManager = new SoundManager(assets);

		window.setIconImage(assets.sheet(1, Assets.DIR_INDEX.get(RIGHT)));
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
				drawCurrentScene(g);
				g.dispose();
			} while (buffers.contentsRestored());
			buffers.show();
		} while (buffers.contentsLost());
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

	private void drawCurrentScene(Graphics2D g) {
		if (game.gamePaused) {
			drawPausedScreen(g);
		} else if (game.state == GameState.INTRO) {
			introScene.draw(g);
		} else {
			playScene.draw(g);
		}
	}

	private void drawPausedScreen(Graphics2D g) {
		int w = assets.imageLogo.getWidth();
		g.drawImage(assets.imageLogo, (unscaledSize.width - w) / 2, 3, null);
		g.setColor(new Color(200, 200, 200, 100));
		g.fillRect(0, 0, unscaledSize.width, unscaledSize.height);
		g.setColor(Color.GREEN);
		g.setFont(new Font(Font.MONOSPACED, Font.BOLD, 28));
		g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
		g.drawString("PAUSED", (unscaledSize.width - g.getFontMetrics().stringWidth("PAUSED")) / 2, t(16));
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