package de.amr.games.pacman.ui.swing.pacman;

import static de.amr.games.pacman.heaven.God.clock;
import static de.amr.games.pacman.lib.Direction.LEFT;
import static de.amr.games.pacman.lib.Logging.log;
import static de.amr.games.pacman.model.GhostState.HUNTING_PAC;
import static de.amr.games.pacman.world.PacManGameWorld.t;

import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;

import de.amr.games.pacman.lib.Animation;
import de.amr.games.pacman.lib.CountdownTimer;
import de.amr.games.pacman.lib.Direction;
import de.amr.games.pacman.lib.V2f;
import de.amr.games.pacman.lib.V2i;
import de.amr.games.pacman.model.Ghost;
import de.amr.games.pacman.model.Pac;
import de.amr.games.pacman.sound.PacManGameSound;
import de.amr.games.pacman.sound.SoundManager;
import de.amr.games.pacman.ui.swing.PacManGameSwingUI;
import de.amr.games.pacman.ui.swing.assets.Spritesheet;
import de.amr.games.pacman.ui.swing.common.AbstractGameScene;

/**
 * Second intermission scene: Blinky pursues Pac but kicks a nail that tears his dress apart.
 * 
 * @author Armin Reichert
 */
public class PacMan_IntermissionScene2 extends AbstractGameScene {

	enum Phase {

		APPROACHING_NAIL, HITTING_NAIL, STRETCHED_1, STRETCHED_2, STRETCHED_3, LOOKING_UP, LOOKING_RIGHT;

		final CountdownTimer timer = new CountdownTimer();
	}

	private final PacMan_GameRendering rendering = PacManGameSwingUI.pacManGameRendering;
	private final SoundManager sounds = PacManGameSwingUI.pacManGameSounds;

	private final Spritesheet spritesheet;

	private final int chaseTileY = 20;
	private final Ghost blinky;
	private final Pac pac;
	private final BufferedImage nail, blinkyLookingUp, blinkyLookingRight, shred, stretchedDress[];
	private final V2i nailPosition;

	private Phase phase;

	public PacMan_IntermissionScene2(Dimension size) {
		super(size);
		this.spritesheet = rendering.assets;

		blinky = new Ghost(0, "Blinky", Direction.LEFT);
		pac = new Pac("Pac-Man", Direction.LEFT);
		nailPosition = new V2i(t(14), t(chaseTileY) - 6);

		// Sprites
		nail = spritesheet.spriteAt(8, 6);
		shred = spritesheet.spriteAt(12, 6);
		blinkyLookingUp = spritesheet.spriteAt(8, 7);
		blinkyLookingRight = spritesheet.spriteAt(9, 7);
		stretchedDress = new BufferedImage[] { //
				spritesheet.spriteAt(9, 6), //
				spritesheet.spriteAt(10, 6), //
				spritesheet.spriteAt(11, 6) };
	}

	@Override
	public Dimension sizeInPixel() {
		return size;
	}

	@Override
	public void start() {
		log("Start of intermission scene %s at %d", this, clock.ticksTotal);

		pac.visible = true;
		pac.dead = false;
		pac.couldMove = true;
		pac.position = new V2f(t(30), t(chaseTileY));
		pac.speed = 1;
		pac.dir = LEFT;

		blinky.visible = true;
		blinky.state = HUNTING_PAC;
		blinky.position = pac.position.sum(t(14), 0);
		blinky.speed = 1;
		blinky.dir = blinky.wishDir = LEFT;

		rendering.pacMunching(pac).forEach(Animation::restart);
		rendering.ghostKickingToDir(blinky, blinky.dir).restart();
		sounds.play(PacManGameSound.INTERMISSION_2);

		enter(Phase.APPROACHING_NAIL, Long.MAX_VALUE);
	}

	private void enter(Phase nextPhase, long ticks) {
		phase = nextPhase;
		phase.timer.setDuration(ticks);
	}

	@Override
	public void update() {
		int distFromNail = (int) (blinky.position.x - nailPosition.x) - 6;
		switch (phase) {
		case APPROACHING_NAIL:
			if (distFromNail == 0) {
				blinky.speed = 0;
				enter(Phase.HITTING_NAIL, clock.sec(0.1));
			}
			break;
		case HITTING_NAIL:
			if (phase.timer.expired()) {
				blinky.speed = 0.2f;
				enter(Phase.STRETCHED_1, Long.MAX_VALUE);
			}
			break;
		case STRETCHED_1:
			if (distFromNail == -3) {
				blinky.speed = 0.15f;
				enter(Phase.STRETCHED_2, Long.MAX_VALUE);
			}
			break;
		case STRETCHED_2:
			if (distFromNail == -6) {
				blinky.speed = 0.1f;
				enter(Phase.STRETCHED_3, Long.MAX_VALUE);
			}
			break;
		case STRETCHED_3:
			if (distFromNail == -9) {
				blinky.speed = 0;
				enter(Phase.LOOKING_UP, clock.sec(3));
			}
			break;
		case LOOKING_UP:
			if (phase.timer.expired()) {
				enter(Phase.LOOKING_RIGHT, clock.sec(3));
			}
			break;
		case LOOKING_RIGHT:
			if (phase.timer.expired()) {
				game.state.duration(0); // signal end of this scene
			}
			break;
		default:
			throw new IllegalStateException("Illegal phase: " + phase);
		}
		blinky.move();
		pac.move();
		phase.timer.run();
	}

	@Override
	public void render(Graphics2D g) {
		rendering.drawLevelCounter(g, game, t(25), t(34));
		rendering.drawImage(g, nail, nailPosition.x, nailPosition.y, true);
		rendering.drawPac(g, pac, game);
		drawBlinky(g);
	}

	private void drawBlinky(Graphics2D g) {
		int baselineY = (int) blinky.position.y - 5;
		int blinkySpriteRightEdge = (int) blinky.position.x + 4;
		switch (phase) {
		case APPROACHING_NAIL:
		case HITTING_NAIL:
			rendering.drawGhost(g, blinky, game);
			break;
		case STRETCHED_1:
			rendering.drawImage(g, stretchedDress[0], blinkySpriteRightEdge - 8, baselineY, true);
			rendering.drawGhost(g, blinky, game);
			break;
		case STRETCHED_2:
			rendering.drawImage(g, stretchedDress[1], blinkySpriteRightEdge - 4, baselineY, true);
			rendering.drawGhost(g, blinky, game);
			break;
		case STRETCHED_3:
			rendering.drawImage(g, stretchedDress[2], blinkySpriteRightEdge - 2, baselineY, true);
			rendering.drawGhost(g, blinky, game);
			break;
		case LOOKING_UP:
			rendering.drawImage(g, blinkyLookingUp, blinky.position.x - 4, blinky.position.y - 4, true);
			rendering.drawImage(g, shred, nailPosition.x, baselineY, true);
			break;
		case LOOKING_RIGHT:
			rendering.drawImage(g, blinkyLookingRight, blinky.position.x - 4, blinky.position.y - 4, true);
			rendering.drawImage(g, shred, nailPosition.x, baselineY, true);
			break;
		default:
			break;
		}
	}
}