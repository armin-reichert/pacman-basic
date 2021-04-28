package de.amr.games.pacman.ui.swing;

import static de.amr.games.pacman.lib.Logging.log;
import static de.amr.games.pacman.model.common.GameVariant.MS_PACMAN;
import static de.amr.games.pacman.model.common.GameVariant.PACMAN;
import static de.amr.games.pacman.model.world.PacManGameWorld.TS;

import java.awt.AWTException;
import java.awt.Canvas;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.EventQueue;
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
import java.util.EnumMap;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

import javax.swing.JFrame;
import javax.swing.Timer;

import de.amr.games.pacman.controller.PacManGameController;
import de.amr.games.pacman.controller.PacManGameState;
import de.amr.games.pacman.controller.event.PacManGameEvent;
import de.amr.games.pacman.controller.event.PacManGameStateChangeEvent;
import de.amr.games.pacman.lib.Direction;
import de.amr.games.pacman.lib.V2d;
import de.amr.games.pacman.lib.V2i;
import de.amr.games.pacman.model.common.GameVariant;
import de.amr.games.pacman.ui.FlashMessage;
import de.amr.games.pacman.ui.PacManGameUI;
import de.amr.games.pacman.ui.swing.app.GameLoop;
import de.amr.games.pacman.ui.swing.assets.AssetLoader;
import de.amr.games.pacman.ui.swing.assets.PacManGameSounds;
import de.amr.games.pacman.ui.swing.assets.SoundManager;
import de.amr.games.pacman.ui.swing.rendering.Debug;
import de.amr.games.pacman.ui.swing.rendering.mspacman.MsPacManGameRendering;
import de.amr.games.pacman.ui.swing.rendering.pacman.PacManGameRendering;
import de.amr.games.pacman.ui.swing.scenes.common.GameScene;
import de.amr.games.pacman.ui.swing.scenes.common.PlayScene;
import de.amr.games.pacman.ui.swing.scenes.mspacman.MsPacMan_IntermissionScene1;
import de.amr.games.pacman.ui.swing.scenes.mspacman.MsPacMan_IntermissionScene2;
import de.amr.games.pacman.ui.swing.scenes.mspacman.MsPacMan_IntermissionScene3;
import de.amr.games.pacman.ui.swing.scenes.mspacman.MsPacMan_IntroScene;
import de.amr.games.pacman.ui.swing.scenes.pacman.PacMan_IntermissionScene1;
import de.amr.games.pacman.ui.swing.scenes.pacman.PacMan_IntermissionScene2;
import de.amr.games.pacman.ui.swing.scenes.pacman.PacMan_IntermissionScene3;
import de.amr.games.pacman.ui.swing.scenes.pacman.PacMan_IntroScene;

/**
 * A Swing implementation of the Pac-Man game UI interface.
 * 
 * @author Armin Reichert
 */
public class PacManGameUI_Swing implements PacManGameUI {

	public static MsPacManGameRendering RENDERING_MS_PACMAN = new MsPacManGameRendering();
	public static PacManGameRendering RENDERING_PACMAN = new PacManGameRendering();

	public static final EnumMap<GameVariant, SoundManager> SOUND = new EnumMap<>(GameVariant.class);
	static {
		SOUND.put(MS_PACMAN, new SoundManager(PacManGameSounds::msPacManSoundURL));
		SOUND.put(PACMAN, new SoundManager(PacManGameSounds::mrPacManSoundURL));
	}

	private final EnumMap<GameVariant, List<GameScene>> scenes = new EnumMap<>(GameVariant.class);

	private final GameLoop gameLoop;
	private final PacManGameController gameController;
	private final Deque<FlashMessage> flashMessageQ = new ArrayDeque<>();
	private final Dimension unscaledSize;
	private final V2i scaledSize;
	private final double scaling;
	private final JFrame window;
	private final Timer titleUpdateTimer;
	private final Canvas canvas;
	private final Keyboard keyboard;

	private GameScene currentGameScene;

	public PacManGameUI_Swing(GameLoop gameLoop, PacManGameController controller, double height) {
		this.gameLoop = gameLoop;
		this.gameController = controller;

		createGameScenes();

		unscaledSize = new Dimension(28 * TS, 36 * TS);
		scaling = Math.round(height / unscaledSize.height);
		scaledSize = new V2d(unscaledSize.width, unscaledSize.height).scaled(this.scaling).toV2i();

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
				handleKey(e);
			}
		});
		window.addWindowListener(new WindowAdapter() {

			@Override
			public void windowClosing(WindowEvent e) {
				gameLoop.end();
			}
		});

		window.getContentPane().add(canvas);

		keyboard = new Keyboard(window);

		titleUpdateTimer = new Timer(1000,
				e -> window.setTitle(String.format("Pac-Man / Ms. Pac-Man (%d fps, JFC Swing)", gameLoop.clock.getLastFPS())));

		// start initial game scene
		onPacManGameStateChange(
				new PacManGameStateChangeEvent(currentGameVariant(), gameController.game(), null, controller.state));
		show();
	}

	private void createGameScenes() {
		scenes.put(MS_PACMAN, Arrays.asList(//
				new MsPacMan_IntroScene(gameController, unscaledSize), //
				new MsPacMan_IntermissionScene1(gameController, unscaledSize), //
				new MsPacMan_IntermissionScene2(gameController, unscaledSize), //
				new MsPacMan_IntermissionScene3(gameController, unscaledSize), //
				new PlayScene(gameController, unscaledSize, RENDERING_MS_PACMAN, SOUND.get(MS_PACMAN))//
		));

		scenes.put(PACMAN, Arrays.asList(//
				new PacMan_IntroScene(gameController, unscaledSize), //
				new PacMan_IntermissionScene1(gameController, unscaledSize), //
				new PacMan_IntermissionScene2(gameController, unscaledSize), //
				new PacMan_IntermissionScene3(gameController, unscaledSize), //
				new PlayScene(gameController, unscaledSize, RENDERING_PACMAN, SOUND.get(PACMAN))//
		));
	}

	@Override
	public void onGameEvent(PacManGameEvent event) {
		PacManGameUI.super.onGameEvent(event);
		// delegate to current scene
		currentGameScene.onGameEvent(event);
	}

	@Override
	public void onPacManGameStateChange(PacManGameStateChangeEvent e) {
		GameScene newScene = getSceneForGameState(e.newGameState);
		if (newScene == null) {
			throw new IllegalStateException("No scene found for game state " + e.newGameState);
		}
		if (currentGameScene != newScene) {
			if (currentGameScene != null) {
				currentGameScene.end();
			}
			newScene.init();
			log("Current scene changed from %s to %s", currentGameScene, newScene);
		}
		currentGameScene = newScene;
	}

	private void show() {
		window.pack();
		window.setLocationRelativeTo(null);
		window.setVisible(true);
		window.requestFocus();
		canvas.createBufferStrategy(2);
		moveMousePointerOutOfSight();
		titleUpdateTimer.start();
	}

	private GameVariant currentGameVariant() {
		return Stream.of(GameVariant.values()).filter(gameController::isPlaying).findFirst().get();
	}

	private GameScene getSceneForGameState(PacManGameState state) {
		GameVariant currentGame = currentGameVariant();
		switch (state) {
		case INTRO:
			return scenes.get(currentGame).get(0);
		case INTERMISSION:
			return scenes.get(currentGame).get(gameController.game().intermissionNumber());
		default:
			return scenes.get(currentGame).get(4);
		}
	}

	@Override
	public void update() {
		if (currentGameScene != null) {
			currentGameScene.update();
		}
		FlashMessage message = flashMessageQ.peek();
		if (message != null) {
			if (!message.timer.isRunning()) {
				message.timer.start();
			}
			message.timer.tick();
			if (message.timer.hasExpired()) {
				flashMessageQ.remove();
			}
		}
		EventQueue.invokeLater(this::renderScreen);
//		keyboard.clear();
	}

	private void renderScreen() {
		BufferStrategy buffers = canvas.getBufferStrategy();
		if (buffers == null) {
			return;
		}
		do {
			do {
				Graphics2D g = (Graphics2D) buffers.getDrawGraphics();
				g.setColor(Color.BLACK);
				g.fillRect(0, 0, canvas.getWidth(), canvas.getHeight());
				g.scale(scaling, scaling);
				g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
				currentGameScene.render(g);
				drawFlashMessage(g);
				g.dispose();
			} while (buffers.contentsRestored());
			buffers.show();
		} while (buffers.contentsLost());
	}

	@Override
	public void reset() {
		currentGameScene.end();
		SOUND.get(gameController.gameVariant()).stopAll();
	}

	@Override
	public void showFlashMessage(String message, double seconds) {
		flashMessageQ.add(new FlashMessage(message, (long) (60 * seconds)));
	}

	@Override
	public Optional<Direction> playerDirectionChangeRequested() {
		if (keyboard.keyPressed("Up")) {
			return Optional.of(Direction.UP);
		}
		if (keyboard.keyPressed("Down")) {
			return Optional.of(Direction.DOWN);
		}
		if (keyboard.keyPressed("Left")) {
			return Optional.of(Direction.LEFT);
		}
		if (keyboard.keyPressed("Right")) {
			return Optional.of(Direction.RIGHT);
		}
		return Optional.empty();
	}

	@Override
	public boolean gameStartRequested() {
		return keyboard.keyPressed("Space");
	}

	private void handleKey(KeyEvent e) {
		switch (e.getKeyCode()) {

		case KeyEvent.VK_A:
			gameController.autopilotOn = !gameController.autopilotOn;
			showFlashMessage(gameController.autopilotOn ? "Autopilot ON" : "Autopilot OFF");
			break;

		case KeyEvent.VK_D:
			Debug.on = !Debug.on;
			log("UI debug mode is %s", Debug.on ? "on" : "off");
			break;

		case KeyEvent.VK_E:
			gameController.eatAllPellets();
			break;

		case KeyEvent.VK_I:
			gameController.setPlayerImmune(!gameController.isPlayerImmune());
			showFlashMessage(gameController.isPlayerImmune() ? "Player IMMUNE" : "Player VULNERABLE");
			break;

		case KeyEvent.VK_F: {
			gameLoop.clock.setTargetFPS(gameLoop.clock.getTargetFPS() != 120 ? 120 : 60);
			String text = gameLoop.clock.getTargetFPS() == 60 ? "Normal speed" : "Fast speed";
			showFlashMessage(text);
			log("Clock frequency changed to %d Hz", gameLoop.clock.getTargetFPS());
			break;
		}

		case KeyEvent.VK_L:
			gameController.game().addLife();
			break;

		case KeyEvent.VK_N:
			if (gameController.isGameRunning()) {
				gameController.changeState(PacManGameState.LEVEL_COMPLETE);
			}
			break;

		case KeyEvent.VK_Q:
			reset();
			gameController.changeState(PacManGameState.INTRO);
			break;

		case KeyEvent.VK_S: {
			gameLoop.clock.setTargetFPS(gameLoop.clock.getTargetFPS() != 30 ? 30 : 60);
			String text = gameLoop.clock.getTargetFPS() == 60 ? "Normal speed" : "Slow speed";
			showFlashMessage(text);
			log("Clock frequency changed to %d Hz", gameLoop.clock.getTargetFPS());
			break;
		}

		case KeyEvent.VK_V:
			gameController.toggleGameVariant();
			break;

		case KeyEvent.VK_X:
			gameController.killGhosts();
			break;

		default:
			break;
		}
	}

	private void drawFlashMessage(Graphics2D g) {
		FlashMessage message = flashMessageQ.peek();
		if (message != null) {
			double alpha = Math.cos(Math.PI * message.timer.ticked() / (2 * message.timer.duration()));
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