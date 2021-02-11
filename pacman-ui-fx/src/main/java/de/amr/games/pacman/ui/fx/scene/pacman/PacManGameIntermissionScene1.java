package de.amr.games.pacman.ui.fx.scene.pacman;

import static de.amr.games.pacman.lib.Direction.LEFT;
import static de.amr.games.pacman.lib.Direction.RIGHT;
import static de.amr.games.pacman.lib.Logging.log;
import static de.amr.games.pacman.model.GhostState.FRIGHTENED;
import static de.amr.games.pacman.model.GhostState.HUNTING_PAC;
import static de.amr.games.pacman.world.PacManGameWorld.t;

import java.util.Optional;

import de.amr.games.pacman.heaven.God;
import de.amr.games.pacman.lib.Animation;
import de.amr.games.pacman.lib.V2f;
import de.amr.games.pacman.model.Ghost;
import de.amr.games.pacman.model.Pac;
import de.amr.games.pacman.model.PacManGameModel;
import de.amr.games.pacman.sound.PacManGameSound;
import de.amr.games.pacman.sound.SoundManager;
import de.amr.games.pacman.ui.PacManGameAnimations;
import de.amr.games.pacman.ui.fx.input.Keyboard;
import de.amr.games.pacman.ui.fx.rendering.PacManGameRendering;
import de.amr.games.pacman.ui.fx.rendering.RenderingWithAnimatedSprites;
import de.amr.games.pacman.ui.fx.scene.common.PacManGameScene;
import javafx.geometry.Rectangle2D;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;

/**
 * First intermission scene: Blinky chases Pac-Man and is then chased by a huge Pac-Man.
 * 
 * @author Armin Reichert
 */
public class PacManGameIntermissionScene1 implements PacManGameScene {

	enum Phase {
		BLINKY_CHASING_PACMAN, BIGPACMAN_CHASING_BLINKY;
	}

	private final Scene scene;
	private final Keyboard keyboard;
	private final PacManGameModel game;
	private final Canvas canvas;
	private final GraphicsContext g;
	private final RenderingWithAnimatedSprites rendering;
	private final SoundManager soundManager;

	private final Animation<Rectangle2D> bigPac;
	private final int baselineY = t(20);
	private final Ghost blinky;
	private final Pac pac;

	private Phase phase;

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
		blinky = game.ghosts[0];
		pac = game.pac;
		bigPac = Animation.of(tileRegion(2, 1, 2, 2), tileRegion(4, 1, 2, 2), tileRegion(6, 1, 2, 2));
		bigPac.endless().frameDuration(4).run();
	}

	private Rectangle2D tileRegion(int tileX, int tileY, int cols, int rows) {
		return new Rectangle2D(tileX * 16, tileY * 16, cols * 16, rows * 16);
	}

	@Override
	public Scene getFXScene() {
		return scene;
	}

	@Override
	public void start() {
		log("Start of intermission scene %s at tick %d", getClass().getSimpleName(), God.clock.ticksTotal);

		phase = Phase.BLINKY_CHASING_PACMAN;

		pac.visible = true;
		pac.dead = false;
		pac.couldMove = true;
		pac.position = new V2f(t(28) + 50, baselineY);
		pac.speed = 1f;
		pac.dir = LEFT;

		blinky.visible = true;
		blinky.state = HUNTING_PAC;
		blinky.position = pac.position.sum(30, 0);
		blinky.speed = pac.speed * 1.04f;
		blinky.dir = blinky.wishDir = LEFT;

		rendering.pacMunching().forEach(Animation::restart);
		rendering.ghostKickingToDir(blinky, blinky.dir).restart();
		rendering.ghostFrightenedToDir(blinky, blinky.dir).restart();
		soundManager.loop(PacManGameSound.INTERMISSION_1, 2);
	}

	@Override
	public void end() {
		log("End of intermission scene %s at tick %d", getClass().getSimpleName(), God.clock.ticksTotal);
	}

	@Override
	public void update() {
		switch (phase) {
		case BLINKY_CHASING_PACMAN:
			if (pac.position.x < -50) {
				pac.dir = RIGHT;
				pac.position = new V2f(-20, baselineY);
				pac.speed = 0;
				blinky.dir = blinky.wishDir = RIGHT;
				blinky.position = new V2f(-20, baselineY);
				blinky.speed = 0.8f;
				blinky.state = FRIGHTENED;
				phase = Phase.BIGPACMAN_CHASING_BLINKY;
			}
			break;
		case BIGPACMAN_CHASING_BLINKY:
			if ((int) blinky.position.x + 4 == t(14)) {
				pac.speed = blinky.speed * 1.9f;
			}
			if (pac.position.x > t(28) + 100) {
				game.state.duration(0);
			}
			break;
		default:
			break;
		}
		pac.move();
		blinky.move();
	}

	@Override
	public void render() {
		g.setFill(Color.BLACK);
		g.fillRect(0, 0, canvas.getWidth(), canvas.getHeight());
		rendering.drawGhost(blinky, game);
		if (phase == Phase.BLINKY_CHASING_PACMAN) {
			rendering.drawPac(pac, game);
		} else {
			rendering.drawRegion(g, bigPac.animate(), pac.position.x - 12, pac.position.y - 22);
		}
		rendering.drawLevelCounter(game, t(25), t(34));
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