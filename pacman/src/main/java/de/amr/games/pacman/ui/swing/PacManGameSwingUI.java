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

import de.amr.games.pacman.controller.PacManGameState;
import de.amr.games.pacman.lib.V2i;
import de.amr.games.pacman.model.MsPacManGame;
import de.amr.games.pacman.model.PacManClassicGame;
import de.amr.games.pacman.model.PacManGame;
import de.amr.games.pacman.ui.api.PacManGameAnimations;
import de.amr.games.pacman.ui.api.PacManGameScene;
import de.amr.games.pacman.ui.api.PacManGameUI;
import de.amr.games.pacman.ui.sound.SoundManager;
import de.amr.games.pacman.ui.swing.classic.PacManClassicIntroScene;
import de.amr.games.pacman.ui.swing.classic.PacManClassicPlayScene;
import de.amr.games.pacman.ui.swing.classic.PacManClassicRendering;
import de.amr.games.pacman.ui.swing.mspacman.MsPacManIntroScene;
import de.amr.games.pacman.ui.swing.mspacman.MsPacManPlayScene;
import de.amr.games.pacman.ui.swing.mspacman.MsPacManRendering;

/**
 * Swing UI for Pac-Man game.
 * 
 * @author Armin Reichert
 */
public class PacManGameSwingUI implements PacManGameUI {

	static final ResourceBundle TEXTS = ResourceBundle.getBundle("ui.swing.localization.translation");

	static class Message {

		String text;
		int x, y;
		boolean important;
	}

	static final int KEY_SLOWMODE = KeyEvent.VK_S;
	static final int KEY_FASTMODE = KeyEvent.VK_F;
	static final int KEY_DEBUGMODE = KeyEvent.VK_D;
	static final int FLASH_MESSAGE_TICKS = 60;

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

	private final List<Message> messages = new ArrayList<>();
	private final List<String> flashMessages = new ArrayList<>();
	private long flashMessageTicksLeft;

	private boolean muted;

	private Timer titleUpdateTimer;
	private Runnable closeHandler = () -> log("Pac-Man Swing UI closed");

	private PacManGame game;

	private PacManGameScene displayedScene;

	private PacManClassicIntroScene pacManClassicIntroScene;
	private PacManClassicPlayScene pacManClassicPlayScene;

	private MsPacManIntroScene msPacManIntroScene;
	private MsPacManPlayScene msPacManPlayScene;

	private PacManClassicRendering pacManClassicRendering;
	private MsPacManRendering msPacManRendering;

	public PacManGameSwingUI(PacManGame game, float scaling) {
		this.scaling = scaling;
		unscaledSizePixels = new V2i(game.level.world.xTiles() * TS, game.level.world.yTiles() * TS);
		scaledSizeInPixels = new V2i((int) (unscaledSizePixels.x * scaling), (int) (unscaledSizePixels.y * scaling));

		window = new JFrame();

		window.setTitle("Pac-Man");
		window.setResizable(false);
		window.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		window.setIconImage(image("/pacman.png"));

		window.addKeyListener(new KeyAdapter() {

			@Override
			public void keyPressed(KeyEvent e) {
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
					DebugRendering.on = !DebugRendering.on;
					log("UI debug mode is %s", DebugRendering.on ? "on" : "off");
				}
			}
		});

		window.addWindowListener(new WindowAdapter() {

			@Override
			public void windowClosing(WindowEvent e) {
				closeHandler.run();
			}
		});

		keyboard = new Keyboard(window);

		canvas = new Canvas();
		canvas.setBackground(new Color(0, 0, 0));
		canvas.setSize(scaledSizeInPixels.x, scaledSizeInPixels.y);
		canvas.setFocusable(false);
		window.add(canvas);

		pacManClassicRendering = new PacManClassicRendering(this::translation);
		msPacManRendering = new MsPacManRendering(this::translation);
		setGame(game);
	}

	@Override
	public Optional<PacManGameAnimations> animations() {
		if (game instanceof MsPacManGame) {
			return Optional.of(msPacManRendering);
		} else {
			return Optional.of(pacManClassicRendering);
		}
	}

	@Override
	public Optional<SoundManager> sounds() {
		if (muted) {
			// TODO that's just a hack, should have real mute functionality
			return Optional.empty();
		}
		if (game instanceof MsPacManGame) {
			return Optional.ofNullable(msPacManRendering.soundManager);
		} else {
			return Optional.ofNullable(pacManClassicRendering.soundManager);
		}
	}

	@Override
	public void mute(boolean b) {
		this.muted = b;
	}

	@Override
	public void setCloseHandler(Runnable handler) {
		closeHandler = handler;
	}

	@Override
	public String translation(String key) {
		return TEXTS.getString(key);
	}

	@Override
	public void updateScene() {
		PacManGameScene scene = sceneToBeDisplayed();
		if (displayedScene != scene) {
			if (displayedScene != null) {
				displayedScene.end();
			}
			displayedScene = scene;
			displayedScene.start();
		}
		displayedScene.update();
	}

	private PacManGameScene sceneToBeDisplayed() {
		if (game instanceof PacManClassicGame) {
			if (game.state == PacManGameState.INTRO) {
				return pacManClassicIntroScene;
			} else {
				return pacManClassicPlayScene;
			}
		} else if (game instanceof MsPacManGame) {
			MsPacManGame msPacManGame = (MsPacManGame) game;
			if (msPacManGame.state == PacManGameState.INTRO) {
				return msPacManIntroScene;
			} else {
				return msPacManPlayScene;
			}
		} else {
			throw new IllegalStateException();
		}
	}

	@Override
	public void setGame(PacManGame newGame) {
		this.game = newGame;
		if (game instanceof PacManClassicGame) {
			pacManClassicIntroScene = new PacManClassicIntroScene(unscaledSizePixels, pacManClassicRendering, game);
			pacManClassicPlayScene = new PacManClassicPlayScene(unscaledSizePixels, pacManClassicRendering, game);
		} else if (game instanceof MsPacManGame) {
			MsPacManGame msPacManGame = (MsPacManGame) game;
			msPacManIntroScene = new MsPacManIntroScene(unscaledSizePixels, msPacManRendering, msPacManGame);
			msPacManPlayScene = new MsPacManPlayScene(unscaledSizePixels, msPacManRendering, msPacManGame);
		} else {
			throw new IllegalArgumentException("Illegal game: " + newGame);
		}
		if (titleUpdateTimer != null) {
			titleUpdateTimer.stop();
		}
		titleUpdateTimer = new Timer(1000,
				e -> window.setTitle(String.format("%s (%d fps)", game.pac.name, clock.frequency)));
		titleUpdateTimer.start();
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
	public void showMessage(String messageText, int x, int y, boolean important) {
		Message message = new Message();
		message.text = messageText;
		message.x = x;
		message.y = y;
		message.important = important;
		messages.add(message);
	}

	@Override
	public void clearMessages() {
		messages.clear();
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
	public void render() {
		BufferStrategy buffers = canvas.getBufferStrategy();
		do {
			do {
				Graphics2D g = (Graphics2D) buffers.getDrawGraphics();
				g.setColor(canvas.getBackground());
				g.fillRect(0, 0, canvas.getWidth(), canvas.getHeight());
//				g.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
				drawCurrentScene(g);
				g.dispose();
			} while (buffers.contentsRestored());
			buffers.show();
		} while (buffers.contentsLost());
	}

	private void drawCurrentScene(Graphics2D g) {
		if (displayedScene != null) {
			Graphics2D g2 = (Graphics2D) g.create();
			g2.scale(scaling, scaling);
			drawMessages(g2);
			displayedScene.draw(g2);
			g2.dispose();
		}
	}

	private void drawMessages(Graphics2D g) {
		Font messageFont = game instanceof MsPacManGame ? msPacManRendering.assets.getScoreFont()
				: pacManClassicRendering.assets.getScoreFont();
		g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
		messages.stream().forEach(message -> {
			g.setFont(messageFont);
			g.setColor(message.important ? Color.RED : Color.YELLOW);
			g.drawString(message.text, message.x, message.y);
		});
		if (flashMessages.size() > 0 && flashMessageTicksLeft > 0) {
			String flashMessage = flashMessages.get(0);
			g.setFont(new Font(Font.SERIF, Font.BOLD, 10));
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

	@Override
	public boolean keyPressed(String keySpec) {
		boolean pressed = keyboard.keyPressed(keySpec);
		keyboard.clearKey(keySpec); // TODO
		return pressed;
	}

	private void moveMousePointerOutOfSight() {
		try {
			Robot robot = new Robot();
			robot.mouseMove(window.getX(), window.getY());
		} catch (AWTException x) {
			x.printStackTrace();
		}
	}

}