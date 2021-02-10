package de.amr.games.pacman.ui.fx.pacman.scene;

import static de.amr.games.pacman.lib.Direction.LEFT;
import static de.amr.games.pacman.lib.Direction.RIGHT;
import static de.amr.games.pacman.lib.Direction.UP;
import static de.amr.games.pacman.world.PacManGameWorld.TS;
import static de.amr.games.pacman.world.PacManGameWorld.t;

import de.amr.games.pacman.lib.Direction;
import de.amr.games.pacman.model.Creature;
import de.amr.games.pacman.model.Ghost;
import de.amr.games.pacman.model.GhostState;
import de.amr.games.pacman.model.Pac;
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

	// TODO move this to renderers
	private Image spritesheet = new Image("/pacman/graphics/sprites.png", false);
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

		g.drawImage(mazeFull, 0, t(3));
		game.level.world.tiles().filter(tile -> game.level.world.isFoodTile(tile)).forEach(tile -> {
			if (game.level.containsEatenFood(tile)) {
				g.setFill(Color.BLACK);
				g.fillRect(t(tile.x), t(tile.y), TS, TS);
			}
		});

		drawPac(game.pac);
		for (Ghost ghost : game.ghosts) {
			drawGhost(ghost);
		}

		g.setFill(Color.WHITE);
		g.fillText(game.stateDescription(), 10, 35 * 8);
	}

	private void drawSprite(Creature guy, int sheetX, int sheetY, int sheetCols, int sheetRows) {
		if (guy.visible) {
			g.drawImage(spritesheet, sheetX * 16, sheetY * 16, sheetCols * 16, sheetRows * 16, guy.position.x - 4,
					guy.position.y - 4, sheetCols * 16, sheetRows * 16);
		}
	}

	private void drawPac(Pac pac) {
		drawSprite(pac, 1, index(pac.dir), 1, 1);
	}

	private void drawGhost(Ghost ghost) {
		if (ghost.is(GhostState.FRIGHTENED)) {
			drawSprite(ghost, 8, 4, 1, 1);
		} else {
			drawSprite(ghost, 2 * index(ghost.wishDir), 4 + ghost.id, 1, 1);
		}
	}

	private int index(Direction dir) {
		return dir == RIGHT ? 0 : dir == LEFT ? 1 : dir == UP ? 2 : 3;
	}

}