package de.amr.games.pacman.ui.fx.mspacman.scene;

import java.util.Optional;

import de.amr.games.pacman.model.PacManGameModel;
import de.amr.games.pacman.ui.PacManGameAnimations;
import de.amr.games.pacman.ui.fx.PacManGameScene;
import de.amr.games.pacman.ui.fx.RenderingWithAnimatedSprites;
import de.amr.games.pacman.ui.fx.input.Keyboard;
import de.amr.games.pacman.ui.fx.mspacman.rendering.MsPacManGameRendering;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;

public class MsPacManGameIntroScene implements PacManGameScene {

	private final Scene scene;
	private final Keyboard keyboard;
	private final PacManGameModel game;
	private final Canvas canvas;
	private final GraphicsContext g;
	private final RenderingWithAnimatedSprites rendering;

	public MsPacManGameIntroScene(PacManGameModel game, double width, double height, double scaling) {
		this.game = game;
		canvas = new Canvas(width, height);
		g = canvas.getGraphicsContext2D();
		g.scale(scaling, scaling);
		StackPane pane = new StackPane();
		pane.getChildren().add(canvas);
		scene = new Scene(pane, width, height);
		keyboard = new Keyboard(scene);
		rendering = new MsPacManGameRendering(g);
	}

	@Override
	public Scene getFXScene() {
		return scene;
	}

	@Override
	public void start() {
	}

	@Override
	public void end() {
	}

	@Override
	public void update() {
	}

	@Override
	public void render() {
		g.setFill(Color.PINK);
		g.fillRect(0, 0, canvas.getWidth(), canvas.getHeight());
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