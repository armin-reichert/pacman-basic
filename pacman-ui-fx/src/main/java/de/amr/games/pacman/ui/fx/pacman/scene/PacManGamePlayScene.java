package de.amr.games.pacman.ui.fx.pacman.scene;

import de.amr.games.pacman.model.PacManGameModel;
import de.amr.games.pacman.ui.fx.input.Keyboard;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;

public class PacManGamePlayScene implements PacManGameScene {

	private final Scene scene;
	private final Keyboard keyboard;
	private final PacManGameModel game;
	private final Canvas canvas;
	private final GraphicsContext g;

	public PacManGamePlayScene(PacManGameModel game, double width, double height) {
		this.game = game;
		canvas = new Canvas(width, height);
		g = canvas.getGraphicsContext2D();

		StackPane pane = new StackPane();
		pane.getChildren().add(canvas);

		scene = new Scene(pane, width, height);
		keyboard = new Keyboard(scene);
	}

	@Override
	public Scene getFXScene() {
		return scene;
	}

	@Override
	public Keyboard keyboard() {
		return keyboard;
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
		g.setFill(Color.BLACK);
		g.fillRect(0, 0, canvas.getWidth(), canvas.getHeight());
		g.setFill(Color.WHITE);
		g.fillText(game.stateDescription(), 100, 100);
	}
}