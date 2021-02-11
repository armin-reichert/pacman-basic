package de.amr.games.pacman.ui.fx.scene.common;

import static de.amr.games.pacman.world.PacManGameWorld.t;

import java.util.Optional;

import de.amr.games.pacman.model.PacManGameModel;
import de.amr.games.pacman.ui.PacManGameAnimations;
import de.amr.games.pacman.ui.fx.input.Keyboard;
import de.amr.games.pacman.ui.fx.rendering.MsPacManGameRendering;
import de.amr.games.pacman.ui.fx.rendering.PacManGameRendering;
import de.amr.games.pacman.ui.fx.rendering.RenderingWithAnimatedSprites;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;

public class PlayScene implements PacManGameScene {

	private final Scene scene;
	private final Keyboard keyboard;
	private final PacManGameModel game;
	private final Canvas canvas;
	private final GraphicsContext g;
	private final RenderingWithAnimatedSprites rendering;

	public PlayScene(PacManGameModel game, double width, double height, double scaling, boolean msPacMan) {
		this.game = game;
		canvas = new Canvas(width, height);
		g = canvas.getGraphicsContext2D();
		g.scale(scaling, scaling);
		StackPane pane = new StackPane();
		pane.getChildren().add(canvas);
		scene = new Scene(pane, width, height);
		keyboard = new Keyboard(scene);
		this.rendering = msPacMan ? new MsPacManGameRendering(g) : new PacManGameRendering(g);
	}

	@Override
	public void render() {
		g.setFill(Color.BLACK);
		g.fillRect(0, 0, canvas.getWidth(), canvas.getHeight());
		boolean flashing = rendering.mazeFlashing(game.level.mazeNumber).isRunning();
		if (flashing) {
			rendering.drawMaze(game.level.mazeNumber, 0, t(3), true);
		} else {
			rendering.drawMaze(game.level.mazeNumber, 0, t(3), false);
			rendering.drawFoodTiles(game.level.world.tiles().filter(game.level.world::isFoodTile),
					game.level::containsEatenFood);
			rendering.drawEnergizerTiles(game.level.world.energizerTiles());
		}
		rendering.drawPac(game.pac, game);
		game.ghosts().forEach(ghost -> rendering.drawGhost(ghost, game));
		rendering.drawBonus(game.bonus, game);
		rendering.drawScore(game);
		if (!game.attractMode) {
			rendering.drawLivesCounter(game, t(2), t(34));
			rendering.drawLevelCounter(game, t(25), t(34));
		}
	}

	@Override
	public Optional<PacManGameAnimations> animations() {
		return rendering instanceof PacManGameAnimations ? Optional.of(rendering) : Optional.empty();
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