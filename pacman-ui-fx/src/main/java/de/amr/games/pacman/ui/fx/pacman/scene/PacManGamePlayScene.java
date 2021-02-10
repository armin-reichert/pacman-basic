package de.amr.games.pacman.ui.fx.pacman.scene;

import static de.amr.games.pacman.world.PacManGameWorld.TS;
import static de.amr.games.pacman.world.PacManGameWorld.t;

import de.amr.games.pacman.model.Creature;
import de.amr.games.pacman.model.Ghost;
import de.amr.games.pacman.model.GhostState;
import de.amr.games.pacman.model.PacManGameModel;
import de.amr.games.pacman.ui.fx.input.Keyboard;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.Image;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;

public class PacManGamePlayScene implements PacManGameScene {

	private final Scene scene;
	private final Keyboard keyboard;
	private final PacManGameModel game;
	private final Canvas canvas;
	private final GraphicsContext g;

	private Image mazeFull = new Image("/pacman/graphics/maze_full.png", false);

	public PacManGamePlayScene(PacManGameModel game, double width, double height, double scaling) {
		this.game = game;
		canvas = new Canvas(width, height);
		g = canvas.getGraphicsContext2D();
		g.scale(scaling, scaling);

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

		g.drawImage(mazeFull, 0, 3 * 8);
		game.level.world.tiles().filter(tile -> game.level.world.isFoodTile(tile)).forEach(tile -> {
			if (game.level.containsEatenFood(tile)) {
				g.setFill(Color.BLACK);
				g.fillRect(t(tile.x), t(tile.y), TS, TS);
			}
		});

		drawGuy(game.pac, Color.YELLOW);
		for (Ghost ghost : game.ghosts) {
			drawGhost(ghost);
		}

		g.setFill(Color.WHITE);
		g.fillText(game.stateDescription(), 10, 35 * 8);
	}

	private void drawGuy(Creature guy, Color color) {
		if (guy.visible) {
			g.setFill(color);
			g.fillOval(guy.position.x - 4, guy.position.y - 4, 16, 16);
		}
	}

	private void drawGhost(Ghost ghost) {
		if (ghost.is(GhostState.FRIGHTENED)) {
			drawGuy(ghost, Color.BLUE);
		} else if (ghost.id == 0) {
			drawGuy(ghost, Color.RED);
		} else if (ghost.id == 1) {
			drawGuy(ghost, Color.PINK);
		} else if (ghost.id == 2) {
			drawGuy(ghost, Color.CYAN);
			g.setFill(Color.CYAN);
		} else if (ghost.id == 3) {
			drawGuy(ghost, Color.ORANGE);
		}
	}
}