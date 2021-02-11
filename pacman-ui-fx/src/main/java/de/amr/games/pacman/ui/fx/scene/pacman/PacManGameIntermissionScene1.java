package de.amr.games.pacman.ui.fx.scene.pacman;

import java.util.Optional;

import de.amr.games.pacman.heaven.God;
import de.amr.games.pacman.model.PacManGameModel;
import de.amr.games.pacman.sound.PacManGameSound;
import de.amr.games.pacman.sound.SoundManager;
import de.amr.games.pacman.ui.PacManGameAnimations;
import de.amr.games.pacman.ui.fx.input.Keyboard;
import de.amr.games.pacman.ui.fx.rendering.PacManGameRendering;
import de.amr.games.pacman.ui.fx.rendering.RenderingWithAnimatedSprites;
import de.amr.games.pacman.ui.fx.scene.common.PacManGameScene;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;

public class PacManGameIntermissionScene1 implements PacManGameScene {

	private final Scene scene;
	private final Keyboard keyboard;
	private final PacManGameModel game;
	private final Canvas canvas;
	private final GraphicsContext g;
	private final RenderingWithAnimatedSprites rendering;
	private final SoundManager soundManager;

	public PacManGameIntermissionScene1(PacManGameModel game, SoundManager soundManager, double width, double height,
			double scaling) {
		this.game = game;
		canvas = new Canvas(width, height);
		g = canvas.getGraphicsContext2D();
		g.scale(scaling, scaling);
		StackPane pane = new StackPane();
		pane.getChildren().add(canvas);
		scene = new Scene(pane, width, height);
		keyboard = new Keyboard(scene);
		rendering = new PacManGameRendering(g);
		this.soundManager = soundManager;
	}

	@Override
	public Scene getFXScene() {
		return scene;
	}

	@Override
	public void start() {
		soundManager.loop(PacManGameSound.INTERMISSION_1, 2);
	}

	@Override
	public void end() {
	}

	@Override
	public void update() {
		if (game.state.ticksRun() == God.clock.sec(12)) {
			game.state.duration(0);
		}
	}

	@Override
	public void render() {
		g.setFont(Font.getDefault());
		g.setFill(Color.RED);
		g.fillText("Intermission 1", 10, 10);
	}

	@Override
	public Keyboard keyboard() {
		return keyboard;
	}

	@Override
	public Optional<PacManGameAnimations> animations() {
		return rendering instanceof PacManGameAnimations ? Optional.of(rendering) : Optional.empty();
	}
}