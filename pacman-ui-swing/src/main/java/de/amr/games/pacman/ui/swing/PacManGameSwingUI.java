package de.amr.games.pacman.ui.swing;

import static de.amr.games.pacman.heaven.God.clock;
import static de.amr.games.pacman.lib.Logging.log;
import static de.amr.games.pacman.world.PacManGameWorld.TS;

import java.awt.AWTException;
import java.awt.Canvas;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.Robot;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.image.BufferStrategy;
import java.util.ArrayDeque;
import java.util.Arrays;
import java.util.Deque;
import java.util.Objects;
import java.util.Optional;

import javax.swing.JFrame;
import javax.swing.Timer;

import de.amr.games.pacman.controller.PacManGameController;
import de.amr.games.pacman.lib.V2f;
import de.amr.games.pacman.lib.V2i;
import de.amr.games.pacman.model.GameModel;
import de.amr.games.pacman.model.mspacman.MsPacManGame;
import de.amr.games.pacman.sound.PacManGameSoundManager;
import de.amr.games.pacman.sound.PacManGameSounds;
import de.amr.games.pacman.sound.SoundManager;
import de.amr.games.pacman.ui.FlashMessage;
import de.amr.games.pacman.ui.PacManGameAnimation;
import de.amr.games.pacman.ui.PacManGameUI;
import de.amr.games.pacman.ui.swing.assets.AssetLoader;
import de.amr.games.pacman.ui.swing.common.AbstractGameScene;
import de.amr.games.pacman.ui.swing.common.PlayScene;
import de.amr.games.pacman.ui.swing.mspacman.MsPacMan_GameRendering;
import de.amr.games.pacman.ui.swing.mspacman.MsPacMan_IntermissionScene1;
import de.amr.games.pacman.ui.swing.mspacman.MsPacMan_IntermissionScene2;
import de.amr.games.pacman.ui.swing.mspacman.MsPacMan_IntermissionScene3;
import de.amr.games.pacman.ui.swing.mspacman.MsPacMan_IntroScene;
import de.amr.games.pacman.ui.swing.pacman.PacMan_GameRendering;
import de.amr.games.pacman.ui.swing.pacman.PacMan_IntermissionScene1;
import de.amr.games.pacman.ui.swing.pacman.PacMan_IntermissionScene2;
import de.amr.games.pacman.ui.swing.pacman.PacMan_IntermissionScene3;
import de.amr.games.pacman.ui.swing.pacman.PacMan_IntroScene;
import de.amr.games.pacman.ui.swing.rendering.DebugRendering;

/**
 * A Swing implementation of the Pac-Man game UI interface.
 * 
 * @author Armin Reichert
 */
public class PacManGameSwingUI implements PacManGameUI {

	static final int MS_PACMAN = 0, PACMAN = 1;

	static final int KEY_SLOW_MODE = KeyEvent.VK_S;
	static final int KEY_FAST_MODE = KeyEvent.VK_F;
	static final int KEY_DEBUG_MODE = KeyEvent.VK_D;

	public static final PacMan_GameRendering pacManGameRendering = new PacMan_GameRendering();
	public static final MsPacMan_GameRendering msPacManGameRendering = new MsPacMan_GameRendering();

	public static final SoundManager pacManGameSounds = new PacManGameSoundManager(PacManGameSounds::getPacManSoundURL);
	public static final SoundManager msPacManGameSounds = new PacManGameSoundManager(
			PacManGameSounds::getMsPacManSoundURL);

	private final Dimension unscaledSize;
	private final V2i scaledSize;
	private final float scaling;
	private final JFrame window;
	private final Timer titleUpdateTimer;
	private final Canvas canvas;
	private final Keyboard keyboard;

	private final AbstractGameScene[][] scenes = new AbstractGameScene[2][5];

	private GameModel game;
	private GameScene currentScene;

	private final Deque<FlashMessage> flashMessageQ = new ArrayDeque<>();

	private boolean muted;

	public PacManGameSwingUI(PacManGameController controller, double scalingFactor) {
		scaling = (float) scalingFactor;
		unscaledSize = new Dimension(28 * TS, 36 * TS);
		scaledSize = new V2f(unscaledSize.width, unscaledSize.height).scaled(this.scaling).toV2i();

		canvas = new Canvas();
		canvas.setBackground(Color.BLACK);
		canvas.setSize(scaledSize.x, scaledSize.y);
		canvas.setFocusable(false);

		window = new JFrame();
		window.setTitle("Swing: Pac-Man");
		window.setBackground(Color.BLACK);
		window.setResizable(false);
		window.setFocusable(true);
		window.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		window.setIconImage(AssetLoader.image("/pacman/graphics/pacman.png"));
		window.addKeyListener(new KeyAdapter() {

			@Override
			public void keyPressed(KeyEvent e) {
				handleGlobalKeys(e);
			}
		});
		window.addWindowListener(new WindowAdapter() {

			@Override
			public void windowClosing(WindowEvent e) {
				controller.endGameLoop();
			}
		});
		window.getContentPane().add(canvas);

		keyboard = new Keyboard(window);

		titleUpdateTimer = new Timer(1000,
				e -> window.setTitle(String.format("Swing: Pac-Man / Ms. Pac-Man (%d fps)", clock.frequency)));
		titleUpdateTimer.start();

		scenes[MS_PACMAN][0] = new MsPacMan_IntroScene(unscaledSize);
		scenes[MS_PACMAN][1] = new MsPacMan_IntermissionScene1(unscaledSize);
		scenes[MS_PACMAN][2] = new MsPacMan_IntermissionScene2(unscaledSize);
		scenes[MS_PACMAN][3] = new MsPacMan_IntermissionScene3(unscaledSize);
		scenes[MS_PACMAN][4] = new PlayScene(unscaledSize, msPacManGameRendering);

		scenes[PACMAN][0] = new PacMan_IntroScene(unscaledSize);
		scenes[PACMAN][1] = new PacMan_IntermissionScene1(unscaledSize);
		scenes[PACMAN][2] = new PacMan_IntermissionScene2(unscaledSize);
		scenes[PACMAN][3] = new PacMan_IntermissionScene3(unscaledSize);
		scenes[PACMAN][4] = new PlayScene(unscaledSize, pacManGameRendering);

		onGameChanged(controller.getGame());

		log("Pac-Man Swing UI created");
	}

	private int currentGameType() {
		if (game instanceof MsPacManGame) {
			return MS_PACMAN;
		} else {
			return PACMAN;
		}
	}

	private GameScene currentGameScene() {
		int gameType = currentGameType();
		switch (game.state) {
		case INTRO:
			return scenes[gameType][0];
		case INTERMISSION:
			return scenes[gameType][game.intermissionNumber];
		default:
			return scenes[gameType][4];
		}
	}

	@Override
	public void onGameChanged(GameModel newGame) {
		this.game = Objects.requireNonNull(newGame);
		Arrays.stream(scenes[currentGameType()]).forEach(scene -> scene.setGame(newGame));
		currentScene = currentGameScene();
		currentScene.start();
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
	public void showFlashMessage(String message, long ticks) {
		flashMessageQ.add(new FlashMessage(message, ticks));
	}

	@Override
	public void update() {
		GameScene newScene = currentGameScene();
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

		FlashMessage message = flashMessageQ.peek();
		if (message != null) {
			message.timer.run();
			if (message.timer.expired()) {
				flashMessageQ.remove();
			}
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
				g.scale(scaling, scaling);
				currentScene.render(g);
				drawFlashMessage(g);
				g.dispose();
			} while (buffers.contentsRestored());
			buffers.show();
		} while (buffers.contentsLost());
	}

	@Override
	public Optional<PacManGameAnimation> animation() {
		if (game instanceof MsPacManGame) {
			return Optional.of(msPacManGameRendering);
		} else {
			return Optional.of(pacManGameRendering);
		}
	}

	@Override
	public Optional<SoundManager> sound() {
		if (muted) {
			// TODO that's just a hack, should have real mute functionality
			return Optional.empty();
		}
		if (game instanceof MsPacManGame) {
			return Optional.of(msPacManGameSounds);
		} else {
			return Optional.of(pacManGameSounds);
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

	private void handleGlobalKeys(KeyEvent e) {
		switch (e.getKeyCode()) {
		case KEY_SLOW_MODE: {
			clock.targetFreq = clock.targetFreq != 30 ? 30 : 60;
			String text = clock.targetFreq == 60 ? "Normal speed" : "Slow speed";
			showFlashMessage(text, clock.sec(1.5));
			log("Clock frequency changed to %d Hz", clock.targetFreq);
			break;
		}
		case KEY_FAST_MODE: {
			clock.targetFreq = clock.targetFreq != 120 ? 120 : 60;
			String text = clock.targetFreq == 60 ? "Normal speed" : "Fast speed";
			showFlashMessage(text, clock.sec(1.5));
			log("Clock frequency changed to %d Hz", clock.targetFreq);
			break;
		}
		case KEY_DEBUG_MODE:
			DebugRendering.on = !DebugRendering.on;
			log("UI debug mode is %s", DebugRendering.on ? "on" : "off");
			break;
		default:
			break;
		}
	}

	private void drawFlashMessage(Graphics2D g) {
		FlashMessage message = flashMessageQ.peek();
		if (message != null) {
			double alpha = Math.cos(Math.PI * message.timer.running() / (2 * message.timer.getDuration()));
			g.setColor(Color.BLACK);
			g.fillRect(0, unscaledSize.height - 16, unscaledSize.width, 16);
			g.setColor(new Color(1, 1, 0, (float) alpha));
			g.setFont(new Font(Font.SERIF, Font.BOLD, 10));
			g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
			g.drawString(message.text, (unscaledSize.width - g.getFontMetrics().stringWidth(message.text)) / 2,
					unscaledSize.height - 3);
			g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_OFF);
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