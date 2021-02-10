package de.amr.games.pacman.ui.fx.pacman.rendering;

import static de.amr.games.pacman.lib.Direction.LEFT;
import static de.amr.games.pacman.lib.Direction.RIGHT;
import static de.amr.games.pacman.lib.Direction.UP;
import static de.amr.games.pacman.world.PacManGameWorld.TS;
import static de.amr.games.pacman.world.PacManGameWorld.t;

import java.util.EnumMap;
import java.util.Map;

import de.amr.games.pacman.lib.Animation;
import de.amr.games.pacman.lib.Direction;
import de.amr.games.pacman.lib.V2i;
import de.amr.games.pacman.model.Creature;
import de.amr.games.pacman.model.Ghost;
import de.amr.games.pacman.model.GhostState;
import de.amr.games.pacman.model.Pac;
import de.amr.games.pacman.ui.PacManGameAnimations;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.Image;
import javafx.scene.paint.Color;

public class Rendering implements PacManGameAnimations {

	private final GraphicsContext g;

	private final Image spritesheet = new Image("/pacman/graphics/sprites.png", false);
	private final Image mazeFull = new Image("/pacman/graphics/maze_full.png", false);

	private final Map<Direction, Animation<V2i>> pacMunching = new EnumMap<>(Direction.class);

	private int index(Direction dir) {
		return dir == RIGHT ? 0 : dir == LEFT ? 1 : dir == UP ? 2 : 3;
	}

	private V2i spr(int col, int row) {
		return new V2i(col, row);
	}

	private void drawSprite(Creature guy, int sheetX, int sheetY) {
		drawSprite(guy, sheetX, sheetY, 1, 1);
	}

	private void drawSprite(Creature guy, int sheetX, int sheetY, int sheetCols, int sheetRows) {
		if (guy.visible) {
			g.drawImage(spritesheet, sheetX * 16, sheetY * 16, sheetCols * 16, sheetRows * 16, guy.position.x - 4,
					guy.position.y - 4, sheetCols * 16, sheetRows * 16);
		}
	}

	public Rendering(GraphicsContext g) {
		this.g = g;

		// animations

		for (Direction dir : Direction.values()) {
			Animation<V2i> animation = Animation.of(spr(2, 0), spr(1, index(dir)), spr(0, index(dir)), spr(1, index(dir)));
			animation.frameDuration(2).endless().run();
			pacMunching.put(dir, animation);
		}

	}

	public void drawFullMaze(int x, int y) {
		g.drawImage(mazeFull, x, y);
	}

	public void hideTile(V2i tile) {
		g.setFill(Color.BLACK);
		g.fillRect(t(tile.x), t(tile.y), TS, TS);
	}

	public void drawPac(Pac pac) {
		V2i frame = pacMunching.get(pac.dir).animate();
		drawSprite(pac, frame.x, frame.y);
	}

	public void drawGhost(Ghost ghost) {
		if (ghost.is(GhostState.FRIGHTENED)) {
			drawSprite(ghost, 8, 4, 1, 1);
		} else {
			drawSprite(ghost, 2 * index(ghost.wishDir), 4 + ghost.id, 1, 1);
		}
	}

	private Animation<?> dummy = Animation.of();

	@Override
	public Animation<?> pacMunchingToDir(Direction dir) {
		return pacMunching.get(dir);
	}

	@Override
	public Animation<?> pacDying() {
		return dummy;
	}

	@Override
	public Animation<?> ghostKickingToDir(Ghost ghost, Direction dir) {
		return dummy;
	}

	@Override
	public Animation<?> ghostFrightenedToDir(Ghost ghost, Direction dir) {
		return dummy;
	}

	@Override
	public Animation<?> ghostFlashing() {
		return dummy;
	}

	@Override
	public Animation<?> ghostReturningHomeToDir(Ghost ghost, Direction dir) {
		return dummy;
	}

	@Override
	public Animation<?> mazeFlashing(int mazeNumber) {
		return dummy;
	}

	@Override
	public Animation<Boolean> energizerBlinking() {
		return Animation.of(true, false);
	}
}
