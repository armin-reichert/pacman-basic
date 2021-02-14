package de.amr.games.pacman.ui.swing;

import static de.amr.games.pacman.heaven.God.clock;
import static de.amr.games.pacman.lib.Logging.log;
import static de.amr.games.pacman.world.PacManGameWorld.TS;
import static java.lang.Math.cos;

import java.awt.AWTException;
import java.awt.Canvas;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.Robot;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.image.BufferStrategy;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import javax.swing.JFrame;
import javax.swing.Timer;

import de.amr.games.pacman.controller.PacManGameController;
import de.amr.games.pacman.lib.V2f;
import de.amr.games.pacman.lib.V2i;
import de.amr.games.pacman.model.MsPacManGame;
import de.amr.games.pacman.model.PacManGame;
import de.amr.games.pacman.model.PacManGameModel;
import de.amr.games.pacman.sound.SoundManager;
import de.amr.games.pacman.ui.PacManGameAnimation;
import de.amr.games.pacman.ui.PacManGameUI;
import de.amr.games.pacman.ui.swing.assets.AssetLoader;
import de.amr.games.pacman.ui.swing.mspacman.MsPacManGameScenes;
import de.amr.games.pacman.ui.swing.pacman.PacManGameScenes;
import de.amr.games.pacman.ui.swing.rendering.DebugRendering;

/**
 * A Swing implementation of the Pac-Man game UI interface.
 * 
 * @author Armin Reichert
 */
public class PacManGameSwingUI implements PacManGameUI {

	static final int KEY_SLOW_MODE = KeyEvent.VK_S;
	static final int KEY_FAST_MODE = KeyEvent.VK_F;
	static final int KEY_DEBUG_MODE = KeyEvent.VK_D;

	static final int FLASH_MESSAGE_TICKS = 90;

	private final V2i unscaledSize_px;
	private final V2i scaledSize_px;
	private final float scaling;
	private final JFrame window;
	private final Timer titleUpdateTimer;
	private final Canvas canvas;
	private final Keyboard keyboard;

	private final PacManGameScenes pacManGameScenes;
	private final MsPacManGameScenes msPacManGameScenes;

	private PacManGameModel game;
	private GameScene currentScene;

	private final List<String> flashMessages = new ArrayList<>();
	private long flashMessageTicksLeft;

	private boolean muted;

	public PacManGameSwingUI(PacManGameController controller, double scalingFactor) {
		scaling = (float) scalingFactor;
		unscaledSize_px = new V2i(28 * TS, 36 * TS);
		scaledSize_px = new V2f(unscaledSize_px).scaled(this.scaling).toV2i();

		canvas = new Canvas();
		canvas.setSize(scaledSize_px.x, scaledSize_px.y);
		canvas.setFocusable(false);

		window = new JFrame();
		window.setTitle("Pac-Man");
		window.setResizable(false);
		window.setFocusable(true);
		window.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		window.setIconImage(AssetLoader.image("/pacman/graphics/pacman.png"));
		window.addKeyListener(new KeyAdapter() {

			@Override
			public void keyPressed(KeyEvent e) {
				handleKeyboardInput(e);
			}
		});
		window.addWindowListener(new WindowAdapter() {

			@Override
			public void windowClosing(WindowEvent e) {
				controller.endGame();
			}
		});
		window.getContentPane().add(canvas);

		keyboard = new Keyboard(window);

		titleUpdateTimer = new Timer(1000,
				e -> window.setTitle(String.format("Pac-Man / Ms. Pac-Man (%d fps)", clock.frequency)));
		titleUpdateTimer.start();

		pacManGameScenes = new PacManGameScenes();
		msPacManGameScenes = new MsPacManGameScenes();
		setGame(controller.getGame());

		log("Pac-Man Swing UI created");
	}

	@Override
	public void setGame(PacManGameModel newGame) {
		if (newGame instanceof PacManGame) {
			pacManGameScenes.createScenes((PacManGame) newGame, unscaledSize_px);
		} else if (newGame instanceof MsPacManGame) {
			msPacManGameScenes.createScenes((MsPacManGame) newGame, unscaledSize_px);
		} else {
			throw new IllegalArgumentException("Cannot set game, game is not supported: " + newGame);
		}
		this.game = newGame;
	}

	@Override
	public void show() {
		window.pack();
		window.setLocationRelativeTo(null);
		window.setVisible(true);
		window.requestFocus();
		canvas.createBufferStrategy(2);
		moveMousePointerOutOfSight();
	}

	@Override
	public void reset() {
		currentScene.end();
	}

	@Override
	public void showFlashMessage(String message) {
		flashMessages.add(message);
		if (flashMessageTicksLeft == 0) {
			flashMessageTicksLeft = FLASH_MESSAGE_TICKS;
		}
	}

	@Override
	public void update() {
		GameScene newScene = null;
		if (game instanceof PacManGame) {
			newScene = pacManGameScenes.selectScene(game);
		} else if (game instanceof MsPacManGame) {
			newScene = msPacManGameScenes.selectScene(game);
		}
		if (newScene == null) {
			throw new IllegalStateException("No scene found for game state " + game.state);
		}
		if (currentScene != newScene) {
			if (currentScene != null) {
				currentScene.end();
			}
			newScene.start();
			log("Current scene changed from %s to %s", currentScene, newScene);
		}
		currentScene = newScene;
		currentScene.update();
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
				currentScene.render(g);
				drawFlashMessages(g);
				g.dispose();
			} while (buffers.contentsRestored());
			buffers.show();
		} while (buffers.contentsLost());
	}

	@Override
	public Optional<PacManGameAnimation> animation() {
		if (game instanceof MsPacManGame) {
			return Optional.of(msPacManGameScenes.rendering);
		} else {
			return Optional.of(pacManGameScenes.rendering);
		}
	}

	@Override
	public Optional<SoundManager> sound() {
		if (muted) {
			// TODO that's just a hack, should have real mute functionality
			return Optional.empty();
		}
		if (game instanceof MsPacManGame) {
			return Optional.ofNullable(msPacManGameScenes.soundManager);
		} else {
			return Optional.ofNullable(pacManGameScenes.soundManager);
		}
	}

	@Override
	public void mute(boolean b) {
		muted = b;
	}

	@Override
	public boolean keyPressed(String keySpec) {
		boolean pressed = keyboard.keyPressed(keySpec);
		keyboard.clearKey(keySpec); // TODO
		return pressed;
	}

	private void handleKeyboardInput(KeyEvent e) {
		switch (e.getKeyCode()) {
		case KEY_SLOW_MODE:
			clock.targetFrequency = clock.targetFrequency == 60 ? 30 : 60;
			log("Clock frequency changed to %d Hz", clock.targetFrequency);
			showFlashMessage(clock.targetFrequency == 60 ? "Normal speed" : "Slow speed");
			break;
		case KEY_FAST_MODE:
			clock.targetFrequency = clock.targetFrequency == 60 ? 120 : 60;
			log("Clock frequency changed to %d Hz", clock.targetFrequency);
			showFlashMessage(clock.targetFrequency == 60 ? "Normal speed" : "Fast speed");
			break;
		case KEY_DEBUG_MODE:
			DebugRendering.on = !DebugRendering.on;
			log("UI debug mode is %s", DebugRendering.on ? "on" : "off");
			break;
		default:
			break;
		}
	}

	private void drawFlashMessages(Graphics2D g) {
		if (flashMessages.size() > 0 && flashMessageTicksLeft > 0) {
			float t = FLASH_MESSAGE_TICKS - flashMessageTicksLeft;
			float alpha = (float) cos(Math.PI * t / (2 * FLASH_MESSAGE_TICKS));
			String text = flashMessages.get(0);
			g.setColor(Color.BLACK);
			g.fillRect(0, unscaledSize_px.y - 16, unscaledSize_px.x, 16);
			g.setColor(new Color(1, 1, 0, alpha));
			g.setFont(new Font(Font.SERIF, Font.BOLD, 10));
			g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
			g.drawString(text, (unscaledSize_px.x - g.getFontMetrics().stringWidth(text)) / 2, unscaledSize_px.y - 3);
			g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_OFF);
			--flashMessageTicksLeft;
			if (flashMessageTicksLeft == 0) {
				flashMessages.remove(0);
				if (flashMessages.size() > 0) {
					flashMessageTicksLeft = FLASH_MESSAGE_TICKS;
				}
			}
		}
	}

	private void moveMousePointerOutOfSight() {
		try {
			Robot robot = new Robot();
			robot.mouseMove(window.getX() + 10, window.getY());
		} catch (AWTException x) {
			x.printStackTrace();
		}
	}
}