package de.amr.games.pacman.ui.fx.pacman.scene;

import static de.amr.games.pacman.world.PacManGameWorld.t;

import de.amr.games.pacman.model.PacManGameModel;
import de.amr.games.pacman.ui.fx.PacManGameScene;
import de.amr.games.pacman.ui.fx.input.Keyboard;
import de.amr.games.pacman.ui.fx.pacman.rendering.Rendering;
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
	private final Rendering rendering;

	public PacManGamePlayScene(PacManGameModel game, double width, double height, double scaling) {
		this.game = game;
		canvas = new Canvas(width, height);
		g = canvas.getGraphicsContext2D();
		g.scale(scaling, scaling);
		StackPane pane = new StackPane();
		pane.getChildren().add(canvas);
		scene = new Scene(pane, width, height);
		keyboard = new Keyboard(scene);
		rendering = new Rendering(g);
	}

	@Override
	public Rendering rendering() {
		return rendering;
	}

	@Override
	public void render() {
		g.setFill(Color.BLACK);
		g.fillRect(0, 0, canvas.getWidth(), canvas.getHeight());
		rendering.drawFullMaze(0, t(3));
		game.level.world.tiles().filter(game.level::containsEatenFood).forEach(rendering::hideTile);
		rendering.drawPac(game.pac, game);
		game.ghosts().forEach(ghost -> rendering.drawGhost(ghost, game));
		rendering.drawBonus(game.bonus, game);
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
}