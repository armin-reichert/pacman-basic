package de.amr.games.pacman.ui.fx.mspacman.rendering;

import static de.amr.games.pacman.lib.Direction.LEFT;
import static de.amr.games.pacman.lib.Direction.RIGHT;
import static de.amr.games.pacman.lib.Direction.UP;
import static de.amr.games.pacman.model.GhostState.DEAD;
import static de.amr.games.pacman.model.GhostState.ENTERING_HOUSE;
import static de.amr.games.pacman.model.GhostState.FRIGHTENED;
import static de.amr.games.pacman.model.GhostState.LOCKED;
import static de.amr.games.pacman.world.PacManGameWorld.TS;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import de.amr.games.pacman.lib.Animation;
import de.amr.games.pacman.lib.Direction;
import de.amr.games.pacman.lib.V2i;
import de.amr.games.pacman.model.Bonus;
import de.amr.games.pacman.model.Creature;
import de.amr.games.pacman.model.Ghost;
import de.amr.games.pacman.model.Pac;
import de.amr.games.pacman.model.PacManGameModel;
import de.amr.games.pacman.ui.PacManGameAnimations;
import de.amr.games.pacman.ui.fx.RenderingWithAnimatedSprites;
import javafx.geometry.Rectangle2D;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.Image;
import javafx.scene.paint.Color;

public class MsPacManGameRendering implements RenderingWithAnimatedSprites, PacManGameAnimations {

	private final GraphicsContext g;

	private final Image spritesheet = new Image("/mspacman/graphics/sprites.png", false);

	private final Rectangle2D[] symbols;
	private final Map<Integer, Rectangle2D> bonusValues;
	private final Map<Integer, Rectangle2D> bountyValues;
	private final Map<Direction, Animation<Rectangle2D>> pacMunching;
	private final Animation<Rectangle2D> pacSpinning;
	private final List<EnumMap<Direction, Animation<Rectangle2D>>> ghostsKicking;
	private final EnumMap<Direction, Animation<Rectangle2D>> ghostEyes;
	private final Animation<Rectangle2D> ghostBlue;
	private final Animation<Rectangle2D> ghostFlashing;
	private final Animation<Integer> bonusJumps;

//	private final Animation<Image> mazeFlashing;
	private final Animation<Boolean> energizerBlinking;

	private int index(Direction dir) {
		return dir == RIGHT ? 0 : dir == LEFT ? 1 : dir == UP ? 2 : 3;
	}

	private Rectangle2D r(double x, double y, int tileX, int tileY, double xTiles, double yTiles) {
		return new Rectangle2D(x + tileX * 16, y + tileY * 16, xTiles * 16, yTiles * 16);
	}

	private Rectangle2D s(int tileX, int tileY) {
		return r(456, 0, tileX, tileY, 1, 1);
	}

	public MsPacManGameRendering(GraphicsContext g) {
		this.g = g;

		symbols = new Rectangle2D[] { s(3, 0), s(4, 0), s(5, 0), s(6, 0), s(7, 0), s(8, 0), s(9, 0) };

		//@formatter:off

		bonusValues = new HashMap<>();
		bonusValues.put(100,  s(3, 1));
		bonusValues.put(200,  s( 4, 1));
		bonusValues.put(500,  s(5, 1));
		bonusValues.put(700,  s(6, 1));
		bonusValues.put(1000, s(7, 1));
		bonusValues.put(2000, s(8, 1));
		bonusValues.put(5000, s(9, 1));
		
		bountyValues = new HashMap<>();
		bountyValues.put(200,  s(0, 8));
		bountyValues.put(400,  s(1, 8));
		bountyValues.put(800,  s(2, 8));
		bountyValues.put(1600, s(3, 8));
		//@formatter:on

		// Animations

		energizerBlinking = Animation.pulse().frameDuration(10);

		// TODO create flash effect
//		Image mazeEmptyBright = createBrightEffect(mazeEmpty, Color.rgb(33, 33, 255), Color.BLACK);
//		mazeFlashing = Animation.of(mazeEmptyBright, mazeEmpty).frameDuration(15);

		pacMunching = new EnumMap<>(Direction.class);
		for (Direction dir : Direction.values()) {
			int d = index(dir);
			Animation<Rectangle2D> munching = Animation.of(s(0, d), s(1, d), s(2, d), s(1, d));
			munching.frameDuration(2).endless();
			pacMunching.put(dir, munching);
		}

		pacSpinning = Animation.of(s(0, 3), s(0, 0), s(0, 1), s(0, 2));
		pacSpinning.frameDuration(10).repetitions(2);

		ghostsKicking = new ArrayList<>(4);
		for (int id = 0; id < 4; ++id) {
			EnumMap<Direction, Animation<Rectangle2D>> walkingTo = new EnumMap<>(Direction.class);
			for (Direction dir : Direction.values()) {
				int d = index(dir);
				Animation<Rectangle2D> walking = Animation.of(s(2 * d, 4 + id), s(2 * d + 1, 4 + id));
				walking.frameDuration(4).endless();
				walkingTo.put(dir, walking);
			}
			ghostsKicking.add(walkingTo);
		}

		ghostEyes = new EnumMap<>(Direction.class);
		for (Direction dir : Direction.values()) {
			ghostEyes.put(dir, Animation.ofSingle(s(8 + index(dir), 5)));
		}

		ghostBlue = Animation.of(s(8, 4), s(9, 4));
		ghostBlue.frameDuration(20).endless().run();

		ghostFlashing = Animation.of(s(8, 4), s(9, 4), s(10, 4), s(11, 4));
		ghostFlashing.frameDuration(5).endless();

		bonusJumps = Animation.of(0, 2, 0, -2).frameDuration(20).endless().run();
	}

	private Direction ensureNotNull(Direction dir) {
		return dir != null ? dir : Direction.RIGHT;
	}

	private Image createBrightEffect(Image mazeEmptyDark, Color color, Color black) {
		return mazeEmptyDark; // TODO
	}

	@Override
	public void drawFullMaze(int mazeNumber, int x, int y) {
		// 226, 248
		int index = mazeNumber - 1;
		Rectangle2D region = new Rectangle2D(0, 248 * index, 226, 248);
		g.drawImage(spritesheet, region.getMinX(), region.getMinY(), region.getWidth(), region.getHeight(), x, y,
				region.getWidth(), region.getHeight());
	}

	@Override
	public void hideTile(V2i tile) {
		g.setFill(Color.BLACK);
		g.fillRect(tile.x * TS, tile.y * TS, TS, TS);
	}

	private void drawRegion(Creature guy, Rectangle2D region) {
		if (guy.visible && region != null) {
			g.drawImage(spritesheet, region.getMinX(), region.getMinY(), region.getWidth(), region.getHeight(),
					guy.position.x - 4, guy.position.y - 4, region.getWidth(), region.getHeight());
		}
	}

	@Override
	public void drawPac(Pac pac, PacManGameModel game) {
		drawRegion(pac, pacSprite(pac, game));
	}

	@Override
	public void drawGhost(Ghost ghost, PacManGameModel game) {
		drawRegion(ghost, ghostSprite(ghost, game));
	}

	@Override
	public void drawBonus(Bonus bonus, PacManGameModel game) {
		int dy = bonusJumps.animate();
		g.save();
		g.translate(0, dy);
		drawRegion(bonus, bonusSprite(bonus, game));
		g.restore();
	}

	@Override
	public Rectangle2D bonusSprite(Bonus bonus, PacManGameModel game) {
		if (bonus.edibleTicksLeft > 0) {
			return symbols[bonus.symbol];
		}
		if (bonus.eatenTicksLeft > 0) {
			return bonusValues.get(bonus.points);
		}
		return null;
	}

	@Override
	public Rectangle2D pacSprite(Pac pac, PacManGameModel game) {
		if (pac.dead) {
			return pacDying().hasStarted() ? pacDying().animate() : pacMunchingToDir(pac.dir).frame();
		}
		if (pac.speed == 0) {
			return pacMunchingToDir(pac.dir).frame(0);
		}
		if (!pac.couldMove) {
			return pacMunchingToDir(pac.dir).frame(1);
		}
		return pacMunchingToDir(pac.dir).animate();
	}

	@Override
	public Rectangle2D ghostSprite(Ghost ghost, PacManGameModel game) {
		if (ghost.bounty > 0) {
			return bountyValues.get(ghost.bounty);
		}
		if (ghost.is(DEAD) || ghost.is(ENTERING_HOUSE)) {
			return ghostReturningHomeToDir(ghost, ghost.dir).animate();
		}
		if (ghost.is(FRIGHTENED)) {
			return ghostFlashing().isRunning() ? ghostFlashing().frame() : ghostFrightenedToDir(ghost, ghost.dir).animate();
		}
		if (ghost.is(LOCKED) && game.pac.powerTicksLeft > 0) {
			return ghostFrightenedToDir(ghost, ghost.dir).animate();
		}
		return ghostKickingToDir(ghost, ghost.wishDir).animate(); // Looks towards wish dir!
	}

	@Override
	public Animation<Rectangle2D> pacMunchingToDir(Direction dir) {
		return pacMunching.get(ensureNotNull(dir));
	}

	@Override
	public Animation<Rectangle2D> pacDying() {
		return pacSpinning;
	}

	@Override
	public Animation<Rectangle2D> ghostKickingToDir(Ghost ghost, Direction dir) {
		return ghostsKicking.get(ghost.id).get(ensureNotNull(dir));
	}

	@Override
	public Animation<Rectangle2D> ghostFrightenedToDir(Ghost ghost, Direction dir) {
		return ghostBlue;
	}

	@Override
	public Animation<Rectangle2D> ghostFlashing() {
		return ghostFlashing;
	}

	@Override
	public Animation<Rectangle2D> ghostReturningHomeToDir(Ghost ghost, Direction dir) {
		return ghostEyes.get(ensureNotNull(dir));
	}

	@Override
	public Animation<Image> mazeFlashing(int mazeNumber) {
//		return mazeFlashing;
		return Animation.of(); // TODO
	}

	@Override
	public Animation<Boolean> energizerBlinking() {
		return energizerBlinking;
	}
}