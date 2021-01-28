package de.amr.games.pacman.ui.swing;

import static de.amr.games.pacman.game.core.PacManGameWorld.TS;
import static de.amr.games.pacman.game.core.PacManGameWorld.t;
import static de.amr.games.pacman.game.heaven.God.clock;
import static de.amr.games.pacman.lib.Logging.log;
import static java.lang.Math.cos;

import java.awt.Canvas;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.image.BufferStrategy;
import java.awt.image.BufferedImage;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.ResourceBundle;

import javax.imageio.ImageIO;
import javax.swing.JFrame;
import javax.swing.Timer;

import de.amr.games.pacman.game.core.PacManGameController;
import de.amr.games.pacman.game.core.PacManGameModel;
import de.amr.games.pacman.game.core.PacManGameState;
import de.amr.games.pacman.game.heaven.God;
import de.amr.games.pacman.lib.V2i;
import de.amr.games.pacman.ui.api.PacManGameAnimations;
import de.amr.games.pacman.ui.api.PacManGameScene;
import de.amr.games.pacman.ui.api.PacManGameUI;
import de.amr.games.pacman.ui.api.PacManGameSoundManager;
import de.amr.games.pacman.ui.swing.classic.PacManClassicAssets;
import de.amr.games.pacman.ui.swing.classic.PacManClassicIntroScene;
import de.amr.games.pacman.ui.swing.classic.PacManClassicPlayScene;
import de.amr.games.pacman.ui.swing.mspacman.MsPacManAssets;
import de.amr.games.pacman.ui.swing.mspacman.MsPacManIntroScene;
import de.amr.games.pacman.ui.swing.mspacman.MsPacManPlayScene;

/**
 * Swing UI for Pac-Man game.
 * 
 * @author Armin Reichert
 */
public class PacManGameSwingUI implements PacManGameUI {

	public static boolean debugMode;

	static final ResourceBundle TEXTS = ResourceBundle.getBundle("ui.swing.localization.translation");

	static final int KEY_PAUSE = KeyEvent.VK_P;
	static final int KEY_SLOWMODE = KeyEvent.VK_S;
	static final int KEY_FASTMODE = KeyEvent.VK_F;
	static final int KEY_DEBUGMODE = KeyEvent.VK_D;

	static final int FLASH_MESSAGE_TICKS = God.clock.sec(1.5);

	public static URL url(String path) {
		return PacManGameSwingUI.class.getResource(path);
	}

	public static BufferedImage image(String path) {
		try (InputStream is = url(path).openStream()) {
			return ImageIO.read(is);
		} catch (Exception x) {
			throw new AssetException("Could not load image with path '%s'", path);
		}
	}

	public static Font font(String fontPath, int size) {
		try (InputStream fontData = url(fontPath).openStream()) {
			return Font.createFont(Font.TRUETYPE_FONT, fontData).deriveFont((float) size);
		} catch (Exception x) {
			throw new AssetException("Could not load font with path '%s'", fontPath);
		}
	}

	private final V2i unscaledSizePixels;
	private final V2i scaledSizeInPixels;
	private final float scaling;
	private final JFrame window;
	private final Canvas canvas;
	private final Keyboard keyboard;

	private String messageText;
	private Color messageColor;
	private Font messageFont;

	private final List<String> flashMessages = new ArrayList<>();
	private long flashMessageTicksLeft;

	private Timer titleUpdateTimer;

	private PacManGameController controller;
	private PacManGameModel game;

	private PacManGameScene currentScene;
	private PacManGameScene introScene;
	private PacManGameScene playScene;
	private DefaultPacManGameSoundManager soundManager;

	public PacManGameSwingUI(PacManGameController controller, int xTiles, int yTiles, float scaling) {
		this.controller = controller;
		controller.setUI(this);

		this.scaling = scaling;
		unscaledSizePixels = new V2i(xTiles * TS, yTiles * TS);
		scaledSizeInPixels = new V2i((int) (unscaledSizePixels.x * scaling), (int) (unscaledSizePixels.y * scaling));

		window = new JFrame();

		window.setTitle("Pac-Man");
		window.setResizable(false);
		window.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		window.setIconImage(image("/pacman.png"));

		window.addWindowListener(new WindowAdapter() {

			@Override
			public void windowClosing(WindowEvent e) {
				controller.saveHighscore();
			}
		});
		window.addKeyListener(new KeyAdapter() {

			@Override
			public void keyPressed(KeyEvent e) {
				if (e.getKeyCode() == KEY_PAUSE) {
					controller.pauseGame(!controller.isGamePaused());
				}
				if (e.getKeyCode() == KEY_SLOWMODE) {
					clock.targetFrequency = clock.targetFrequency == 60 ? 30 : 60;
					log("Clock frequency changed to %d Hz", clock.targetFrequency);
					showFlashMessage(clock.targetFrequency == 60 ? "Normal speed" : "Slow speed");
				}
				if (e.getKeyCode() == KEY_FASTMODE) {
					clock.targetFrequency = clock.targetFrequency == 60 ? 120 : 60;
					log("Clock frequency changed to %d Hz", clock.targetFrequency);
					showFlashMessage(clock.targetFrequency == 60 ? "Normal speed" : "Fast speed");
				}
				if (e.getKeyCode() == KEY_DEBUGMODE) {
					debugMode = !debugMode;
					log("UI debug mode is %s", debugMode ? "on" : "off");
				}
			}

		});

		keyboard = new Keyboard(window);

		canvas = new Canvas();
		canvas.setBackground(new Color(0, 0, 0));
		canvas.setSize(scaledSizeInPixels.x, scaledSizeInPixels.y);
		canvas.setFocusable(false);
		window.add(canvas);

		messageFont = font("/PressStart2P-Regular.ttf", 8).deriveFont(Font.PLAIN);
	}

	@Override
	public String translation(String key) {
		return TEXTS.getString(key);
	}

	@Override
	public void updateGame(PacManGameModel game) {
		this.game = game;
		if (game.variant == PacManGameModel.CLASSIC) {
			PacManClassicAssets assets = new PacManClassicAssets();
			soundManager = new DefaultPacManGameSoundManager(assets.soundURL::get);
			introScene = new PacManClassicIntroScene(this, game, unscaledSizePixels, assets);
			playScene = new PacManClassicPlayScene(this, game, unscaledSizePixels, assets);
		} else {
			MsPacManAssets assets = new MsPacManAssets();
			soundManager = new DefaultPacManGameSoundManager(assets.soundURL::get);
			introScene = new MsPacManIntroScene(this, game, unscaledSizePixels, assets);
			playScene = new MsPacManPlayScene(this, game, unscaledSizePixels, assets);
		}
		if (titleUpdateTimer != null) {
			titleUpdateTimer.stop();
		}
		titleUpdateTimer = new Timer(1000,
				e -> window.setTitle(String.format("%s (%d fps)", game.world.pacName(), clock.frequency)));
		titleUpdateTimer.start();
	}

	@Override
	public void show() {
		window.pack();
		window.setLocationRelativeTo(null);
		window.setVisible(true);
		window.requestFocus();
		canvas.createBufferStrategy(2);
	}

	@Override
	public void showMessage(String message, boolean important) {
		messageText = message;
		messageColor = important ? Color.RED : Color.yellow;
	}

	@Override
	public void clearMessages() {
		messageText = null;
		flashMessages.clear();
		flashMessageTicksLeft = 0;
	}

	@Override
	public void showFlashMessage(String message) {
		flashMessages.add(message);
		if (flashMessageTicksLeft == 0) {
			flashMessageTicksLeft = FLASH_MESSAGE_TICKS;
		}
	}

	@Override
	public void redraw() {
		BufferStrategy buffers = canvas.getBufferStrategy();
		do {
			do {
				Graphics2D g = (Graphics2D) buffers.getDrawGraphics();
				g.setColor(canvas.getBackground());
				g.fillRect(0, 0, canvas.getWidth(), canvas.getHeight());
				drawCurrentScene(g);
				if (controller.isGamePaused()) {
					drawPausedScreen(g);
				}
				g.dispose();
			} while (buffers.contentsRestored());
			buffers.show();
		} while (buffers.contentsLost());
	}

	private void drawCurrentScene(Graphics2D g) {
		updateScene();
		Graphics2D g2 = (Graphics2D) g.create();
		g2.scale(scaling, scaling);
		currentScene.draw(g2);
		drawMessages(g2);
		g2.dispose();
	}

	private void drawMessages(Graphics2D g) {
		g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
		if (messageText != null) {
			g.setFont(messageFont);
			g.setColor(messageColor);
			int textWidth = g.getFontMetrics().stringWidth(messageText);
			g.drawString(messageText, (unscaledSizePixels.x - textWidth) / 2, t(21));
		}
		if (flashMessages.size() > 0 && flashMessageTicksLeft > 0) {
			String flashMessage = flashMessages.get(0);
			g.setFont(new Font(Font.SERIF, Font.BOLD, 12));
			float t = FLASH_MESSAGE_TICKS - flashMessageTicksLeft;
			float alpha = (float) cos(Math.PI * t / (2 * FLASH_MESSAGE_TICKS));
			g.setColor(new Color(1, 1, 0.5f, alpha));
			int flashMessageTextWidth = g.getFontMetrics().stringWidth(flashMessage);
			g.drawString(flashMessage, (unscaledSizePixels.x - flashMessageTextWidth) / 2, unscaledSizePixels.y - 3);
			--flashMessageTicksLeft;
			if (flashMessageTicksLeft == 0) {
				flashMessages.remove(0);
				if (flashMessages.size() > 0) {
					flashMessageTicksLeft = FLASH_MESSAGE_TICKS;
				}
			}
		}
		g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_OFF);
	}

	private void drawPausedScreen(Graphics2D g) {
		Font font = new Font(Font.MONOSPACED, Font.BOLD, 24);
		g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
		g.setColor(new Color(0, 48, 143, 100));
		g.fillRect(0, 0, canvas.getWidth(), canvas.getHeight());
		g.setColor(Color.GREEN);
		g.setFont(font);
		int y = canvas.getHeight() / 2;
		String text = "PAUSED";
		g.drawString(text, (canvas.getWidth() - g.getFontMetrics().stringWidth(text)) / 2, y);
		y += font.getSize() * 150 / 100;
		text = "(Press 'P' key to resume)";
		g.drawString(text, (canvas.getWidth() - g.getFontMetrics().stringWidth(text)) / 2, y);
		g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_OFF);
	}

	@Override
	public boolean keyPressed(String keySpec) {
		boolean pressed = keyboard.keyPressed(keySpec);
		keyboard.clearKey(keySpec); // TODO
		return pressed;
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
	public Optional<PacManGameSoundManager> sounds() {
		return Optional.ofNullable(soundManager);
	}

	@Override
	public Optional<PacManGameAnimations> animations() {
		if (currentScene instanceof PacManGameAnimations) {
			return Optional.ofNullable((PacManGameAnimations) currentScene);
		}
		return Optional.empty();
	}
}