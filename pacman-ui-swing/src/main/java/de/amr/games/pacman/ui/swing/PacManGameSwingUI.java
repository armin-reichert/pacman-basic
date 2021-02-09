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
import de.amr.games.pacman.lib.V2f;
import de.amr.games.pacman.lib.V2i;
import de.amr.games.pacman.model.AbstractPacManGame;
import de.amr.games.pacman.model.MsPacManGame;
import de.amr.games.pacman.model.PacManGame;
import de.amr.games.pacman.ui.api.PacManGameAnimations;
import de.amr.games.pacman.ui.api.PacManGameScene;
import de.amr.games.pacman.ui.api.PacManGameUI;
import de.amr.games.pacman.ui.sound.PacManGameSoundManager;
import de.amr.games.pacman.ui.sound.SoundManager;
import de.amr.games.pacman.ui.swing.mspacman.rendering.MsPacManGameRendering;
import de.amr.games.pacman.ui.swing.mspacman.scene.MsPacManGameIntroScene;
import de.amr.games.pacman.ui.swing.mspacman.scene.MsPacManGamePlayScene;
import de.amr.games.pacman.ui.swing.mspacman.scene.MsPacManIntermission1_TheyMeet;
import de.amr.games.pacman.ui.swing.mspacman.scene.MsPacManIntermission2_TheChase;
import de.amr.games.pacman.ui.swing.mspacman.scene.MsPacManIntermission3_Junior;
import de.amr.games.pacman.ui.swing.pacman.rendering.PacManGameRendering;
import de.amr.games.pacman.ui.swing.pacman.scene.PacManGameIntermission1;
import de.amr.games.pacman.ui.swing.pacman.scene.PacManGameIntermission2;
import de.amr.games.pacman.ui.swing.pacman.scene.PacManGameIntermission3;
import de.amr.games.pacman.ui.swing.pacman.scene.PacManGameIntroScene;
import de.amr.games.pacman.ui.swing.pacman.scene.PacManGamePlayScene;

/**
 * Swing UI for Pac-Man game.
 * 
 * @author Armin Reichert
 */
public class PacManGameSwingUI implements PacManGameUI {

	static final int KEY_SLOWMODE = KeyEvent.VK_S;
	static final int KEY_FASTMODE = KeyEvent.VK_F;
	static final int KEY_DEBUGMODE = KeyEvent.VK_D;
	static final int FLASH_MESSAGE_TICKS = 90;

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

	private final V2i unscaledSize_px;
	private final V2i scaledSize_px;
	private final float scaling;
	private final JFrame window;
	private final Timer titleUpdateTimer;
	private final Canvas canvas;
	private final Keyboard keyboard;

	private final ResourceBundle translations = ResourceBundle.getBundle("localization.translation");

	private final PacManGameRendering pacManRendering;
	private final SoundManager pacManSoundManager;

	private final MsPacManGameRendering msPacManRendering;
	private final SoundManager msPacManSoundManager;

	private final List<String> flashMessages = new ArrayList<>();
	private long flashMessageTicksLeft;

	private boolean muted;

	private Runnable closeHandler = () -> log("Pac-Man Swing UI closed");

	private AbstractPacManGame game;

	private PacManGameScene currentScene;

	private PacManGameIntroScene pacManIntroScene;
	private PacManGamePlayScene pacManClassicPlayScene;
	private PacManGameScene[] pacManIntermissions = new PacManGameScene[3];

	private MsPacManGameIntroScene msPacManIntroScene;
	private MsPacManGamePlayScene msPacManPlayScene;
	private PacManGameScene[] msPacManIntermissions = new PacManGameScene[3];

	public PacManGameSwingUI(AbstractPacManGame game, float scaling) {
		this.scaling = scaling;
		unscaledSize_px = new V2i(game.level.world.xTiles() * TS, game.level.world.yTiles() * TS);
		scaledSize_px = new V2f(unscaledSize_px).scaled(scaling).toV2i();

		window = new JFrame();
		window.setTitle("Pac-Man");
		window.setResizable(false);
		window.setFocusable(true);
		window.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		window.setIconImage(image("/pacman/graphics/pacman.png"));

		window.addKeyListener(new KeyAdapter() {

			@Override
			public void keyPressed(KeyEvent e) {
				handleKeyboardInput(e);
			}
		});

		window.addWindowListener(new WindowAdapter() {

			@Override
			public void windowClosing(WindowEvent e) {
				closeHandler.run();
			}
		});

		keyboard = new Keyboard(window);

		titleUpdateTimer = new Timer(1000,
				e -> window.setTitle(String.format("Pac-Man / Ms. Pac-Man (%d fps)", clock.frequency)));
		titleUpdateTimer.start();

		canvas = new Canvas();
		canvas.setSize(scaledSize_px.x, scaledSize_px.y);
		canvas.setFocusable(false);
		window.getContentPane().add(canvas);

		pacManRendering = new PacManGameRendering(translations);
		pacManSoundManager = new PacManGameSoundManager(pacManRendering.assets::getSoundURL);

		msPacManRendering = new MsPacManGameRendering(translations);
		msPacManSoundManager = new PacManGameSoundManager(msPacManRendering.assets::getSoundURL);

		setGame(game);
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
				g.setColor(Color.BLACK);
				g.fillRect(0, 0, canvas.getWidth(), canvas.getHeight());
				if (currentScene != null) {
					g.scale(scaling, scaling);
					currentScene.draw(g);
					drawFlashMessages(g);
				}
				g.dispose();
			} while (buffers.contentsRestored());
			buffers.show();
		} while (buffers.contentsLost());
	}

	@Override
	public Optional<PacManGameAnimations> animations() {
		if (game instanceof MsPacManGame) {
			return Optional.of(msPacManRendering);
		} else {
			return Optional.of(pacManRendering);
		}
	}

	@Override
	public Optional<SoundManager> sounds() {
		if (muted) {
			// TODO that's just a hack, should have real mute functionality
			return Optional.empty();
		}
		if (game instanceof MsPacManGame) {
			return Optional.ofNullable(msPacManSoundManager);
		} else {
			return Optional.ofNullable(pacManSoundManager);
		}
	}

	@Override
	public void mute(boolean b) {
		this.muted = b;
	}

	@Override
	public void setGame(AbstractPacManGame newGame) {
		this.game = newGame;
		if (game instanceof PacManGame) {
			createPacManScenes();
		} else {
			createMsPacManScenes();
		}
	}

	@Override
	public void setCloseHandler(Runnable handler) {
		closeHandler = handler;
	}

	@Override
	public boolean keyPressed(String keySpec) {
		boolean pressed = keyboard.keyPressed(keySpec);
		keyboard.clearKey(keySpec); // TODO
		return pressed;
	}

	@Override
	public void updateScene() {
		PacManGameScene scene = game instanceof PacManGame ? selectPacManScene() : selectMsPacManScene();
		if (currentScene != scene) {
			if (currentScene != null) {
				currentScene.end();
				log("Current scene changed from %s to %s", currentScene.getClass().getSimpleName(),
						scene.getClass().getSimpleName());
			}
			currentScene = scene;
			currentScene.start();
		}
		currentScene.update();
	}

	private void createPacManScenes() {
		PacManGame pacManGame = (PacManGame) game;
		pacManIntroScene = new PacManGameIntroScene(unscaledSize_px, pacManRendering, pacManGame);
		pacManClassicPlayScene = new PacManGamePlayScene(unscaledSize_px, pacManRendering, pacManGame);
		pacManIntermissions[0] = new PacManGameIntermission1(unscaledSize_px, pacManRendering, pacManSoundManager,
				pacManGame);
		pacManIntermissions[1] = new PacManGameIntermission2(unscaledSize_px, pacManRendering, pacManSoundManager,
				pacManGame);
		pacManIntermissions[2] = new PacManGameIntermission3(unscaledSize_px, pacManRendering, pacManSoundManager,
				pacManGame);
	}

	private PacManGameScene selectPacManScene() {
		if (game.state == PacManGameState.INTRO) {
			return pacManIntroScene;
		}
		if (game.state == PacManGameState.INTERMISSION) {
			return pacManIntermissions[game.intermissionNumber - 1];
		}
		return pacManClassicPlayScene;
	}

	private void createMsPacManScenes() {
		MsPacManGame msPacManGame = (MsPacManGame) game;
		msPacManIntroScene = new MsPacManGameIntroScene(unscaledSize_px, msPacManRendering, msPacManGame);
		msPacManPlayScene = new MsPacManGamePlayScene(unscaledSize_px, msPacManRendering, msPacManGame);
		msPacManIntermissions[0] = new MsPacManIntermission1_TheyMeet(unscaledSize_px, msPacManRendering,
				msPacManSoundManager, msPacManGame);
		msPacManIntermissions[1] = new MsPacManIntermission2_TheChase(unscaledSize_px, msPacManRendering,
				msPacManSoundManager, msPacManGame);
		msPacManIntermissions[2] = new MsPacManIntermission3_Junior(unscaledSize_px, msPacManRendering,
				msPacManSoundManager, msPacManGame);
	}

	private PacManGameScene selectMsPacManScene() {
		MsPacManGame msPacManGame = (MsPacManGame) game;
		if (msPacManGame.state == PacManGameState.INTRO) {
			return msPacManIntroScene;
		}
		if (game.state == PacManGameState.INTERMISSION) {
			return msPacManIntermissions[game.intermissionNumber - 1];
		}
		return msPacManPlayScene;
	}

	private void handleKeyboardInput(KeyEvent e) {
		switch (e.getKeyCode()) {
		case KEY_SLOWMODE:
			clock.targetFrequency = clock.targetFrequency == 60 ? 30 : 60;
			log("Clock frequency changed to %d Hz", clock.targetFrequency);
			showFlashMessage(clock.targetFrequency == 60 ? "Normal speed" : "Slow speed");
			break;
		case KEY_FASTMODE:
			clock.targetFrequency = clock.targetFrequency == 60 ? 120 : 60;
			log("Clock frequency changed to %d Hz", clock.targetFrequency);
			showFlashMessage(clock.targetFrequency == 60 ? "Normal speed" : "Fast speed");
			break;
		case KEY_DEBUGMODE:
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
			robot.mouseMove(window.getX(), window.getY());
		} catch (AWTException x) {
			x.printStackTrace();
		}
	}
}