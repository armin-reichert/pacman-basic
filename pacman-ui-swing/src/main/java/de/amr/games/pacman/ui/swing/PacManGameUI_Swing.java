package de.amr.games.pacman.ui.swing;

import static de.amr.games.pacman.lib.God.clock;
import static de.amr.games.pacman.lib.Logging.log;
import static de.amr.games.pacman.model.common.GameType.MS_PACMAN;
import static de.amr.games.pacman.model.common.GameType.PACMAN;
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
import de.amr.games.pacman.lib.V2d;
import de.amr.games.pacman.lib.V2i;
import de.amr.games.pacman.model.common.GameModel;
import de.amr.games.pacman.model.common.GameType;
import de.amr.games.pacman.ui.FlashMessage;
import de.amr.games.pacman.ui.PacManGameUI;
import de.amr.games.pacman.ui.animation.PacManGameAnimations;
import de.amr.games.pacman.ui.sound.SoundManager;
import de.amr.games.pacman.ui.swing.assets.AssetLoader;
import de.amr.games.pacman.ui.swing.assets.PacManGameSoundManager;
import de.amr.games.pacman.ui.swing.assets.PacManGameSounds;
import de.amr.games.pacman.ui.swing.common.GameScene;
import de.amr.games.pacman.ui.swing.common.PlayScene;
import de.amr.games.pacman.ui.swing.mspacman.MsPacMan_IntermissionScene1;
import de.amr.games.pacman.ui.swing.mspacman.MsPacMan_IntermissionScene2;
import de.amr.games.pacman.ui.swing.mspacman.MsPacMan_IntermissionScene3;
import de.amr.games.pacman.ui.swing.mspacman.MsPacMan_IntroScene;
import de.amr.games.pacman.ui.swing.pacman.PacMan_IntermissionScene1;
import de.amr.games.pacman.ui.swing.pacman.PacMan_IntermissionScene2;
import de.amr.games.pacman.ui.swing.pacman.PacMan_IntermissionScene3;
import de.amr.games.pacman.ui.swing.pacman.PacMan_IntroScene;
import de.amr.games.pacman.ui.swing.rendering.Debug;
import de.amr.games.pacman.ui.swing.rendering.PacManGameRendering2D;
import de.amr.games.pacman.ui.swing.rendering.standard.MsPacMan_StandardRendering;
import de.amr.games.pacman.ui.swing.rendering.standard.PacMan_StandardRendering;

/**
 * A Swing implementation of the Pac-Man game UI interface.
 * 
 * @author Armin Reichert
 */
public class PacManGameUI_Swing implements PacManGameUI {

	private final PacManGameController controller;
	private final EnumMap<GameType, PacManGameRendering2D> renderings = new EnumMap<>(GameType.class);
	private final EnumMap<GameType, SoundManager> sounds = new EnumMap<>(GameType.class);
	private final EnumMap<GameType, List<GameScene>> scenes = new EnumMap<>(GameType.class);
	private final Deque<FlashMessage> flashMessageQ = new ArrayDeque<>();
	private final Dimension unscaledSize;
	private final V2i scaledSize;
	private final double scaling;
	private final JFrame window;
	private final Timer titleUpdateTimer;
	private final Canvas canvas;
	private final Keyboard keyboard;

	private GameScene currentScene;
	private boolean muted;

	public PacManGameUI_Swing(PacManGameController controller, double height) {
		this.controller = controller;
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
				handleGlobalKeys(e);
			}
		});
		window.getContentPane().add(canvas);

		keyboard = new Keyboard(window);

		titleUpdateTimer = new Timer(1000,
				e -> window.setTitle(String.format("Pac-Man / Ms. Pac-Man (%d fps, JFC Swing)", clock.frequency)));

		renderings.put(MS_PACMAN, new MsPacMan_StandardRendering());
		renderings.put(PACMAN, new PacMan_StandardRendering());

		sounds.put(MS_PACMAN, new PacManGameSoundManager(PacManGameSounds::msPacManSoundURL));
		sounds.put(PACMAN, new PacManGameSoundManager(PacManGameSounds::mrPacManSoundURL));

		scenes.put(MS_PACMAN, Arrays.asList(//
				new MsPacMan_IntroScene(controller, unscaledSize, renderings.get(MS_PACMAN), sounds.get(MS_PACMAN)), //
				new MsPacMan_IntermissionScene1(controller, unscaledSize, renderings.get(MS_PACMAN), sounds.get(MS_PACMAN)), //
				new MsPacMan_IntermissionScene2(controller, unscaledSize, renderings.get(MS_PACMAN), sounds.get(MS_PACMAN)), //
				new MsPacMan_IntermissionScene3(controller, unscaledSize, renderings.get(MS_PACMAN), sounds.get(MS_PACMAN)), //
				new PlayScene(controller, unscaledSize, renderings.get(MS_PACMAN), sounds.get(MS_PACMAN))//
		));

		scenes.put(PACMAN, Arrays.asList(//
				new PacMan_IntroScene(controller, unscaledSize, renderings.get(PACMAN), sounds.get(PACMAN)), //
				new PacMan_IntermissionScene1(controller, unscaledSize, renderings.get(PACMAN), sounds.get(PACMAN)), //
				new PacMan_IntermissionScene2(controller, unscaledSize, renderings.get(PACMAN), sounds.get(PACMAN)), //
				new PacMan_IntermissionScene3(controller, unscaledSize, renderings.get(PACMAN), sounds.get(PACMAN)), //
				new PlayScene(controller, unscaledSize, renderings.get(PACMAN), sounds.get(PACMAN))//
		));
		log("Swing UI created at clock tick %d", clock.ticksTotal);
	}

	public void addWindowClosingHandler(Runnable handler) {
		window.addWindowListener(new WindowAdapter() {

			@Override
			public void windowClosing(WindowEvent e) {
				handler.run();
			}
		});
	}

	private GameType currentGame() {
		return Stream.of(GameType.values()).filter(controller::isPlaying).findFirst().get();
	}

	private GameScene getSceneForCurrentGameState() {
		GameType currentGame = currentGame();
		switch (controller.fsm.state) {
		case INTRO:
			return scenes.get(currentGame).get(0);
		case INTERMISSION:
			return scenes.get(currentGame).get(controller.game.intermissionNumber);
		default:
			return scenes.get(currentGame).get(4);
		}
	}

	@Override
	public void onGameChanged(GameModel newGame) {
		changeScene(getSceneForCurrentGameState());
	}

	private void changeScene(GameScene newScene) {
		if (newScene != currentScene) {
			currentScene = getSceneForCurrentGameState();
			currentScene.start();
		}
	}

	public void show() {
		window.pack();
		window.setLocationRelativeTo(null);
		window.setVisible(true);
		window.requestFocus();
		canvas.createBufferStrategy(2);
		moveMousePointerOutOfSight();
		titleUpdateTimer.start();
	}

	@Override
	public void update() {
		GameScene newScene = getSceneForCurrentGameState();
		if (newScene == null) {
			throw new IllegalStateException("No scene found for game state " + controller.fsm.state);
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
			if (!message.timer.isRunning()) {
				message.timer.start();
			}
			message.timer.tick();
			if (message.timer.hasExpired()) {
				flashMessageQ.remove();
			}
		}
		EventQueue.invokeLater(this::renderScreen);
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
				currentScene.render(g);
				drawFlashMessage(g);
				g.dispose();
			} while (buffers.contentsRestored());
			buffers.show();
		} while (buffers.contentsLost());
	}

	@Override
	public void reset() {
		currentScene.end();
	}

	@Override
	public void showFlashMessage(String message, double seconds) {
		flashMessageQ.add(new FlashMessage(message, (long) (60 * seconds)));
	}

	@Override
	public boolean keyPressed(String keySpec) {
		boolean pressed = keyboard.keyPressed(keySpec);
		keyboard.clearKey(keySpec); // TODO
		return pressed;
	}

	@Override
	public void mute(boolean b) {
		muted = b;
	}

	@Override
	public Optional<SoundManager> sound() {
		if (muted) {
			return Optional.empty(); // TODO that's just a hack, should have real mute functionality
		}
		return Optional.of(sounds.get(currentGame()));
	}

	@Override
	public Optional<PacManGameAnimations> animation() {
		return Optional.of(renderings.get(currentGame()));
	}

	private void handleGlobalKeys(KeyEvent e) {
		switch (e.getKeyCode()) {
		case KeyEvent.VK_V:
			controller.toggleGameType();
			break;
		case KeyEvent.VK_S: {
			clock.targetFreq = clock.targetFreq != 30 ? 30 : 60;
			String text = clock.targetFreq == 60 ? "Normal speed" : "Slow speed";
			showFlashMessage(text, clock.sec(1.5));
			log("Clock frequency changed to %d Hz", clock.targetFreq);
			break;
		}
		case KeyEvent.VK_F: {
			clock.targetFreq = clock.targetFreq != 120 ? 120 : 60;
			String text = clock.targetFreq == 60 ? "Normal speed" : "Fast speed";
			showFlashMessage(text, clock.sec(1.5));
			log("Clock frequency changed to %d Hz", clock.targetFreq);
			break;
		}
		case KeyEvent.VK_D:
			Debug.on = !Debug.on;
			log("UI debug mode is %s", Debug.on ? "on" : "off");
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