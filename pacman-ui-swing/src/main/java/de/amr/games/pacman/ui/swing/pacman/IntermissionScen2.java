package de.amr.games.pacman.ui.swing.pacman;

import static de.amr.games.pacman.heaven.God.clock;
import static de.amr.games.pacman.lib.Direction.LEFT;
import static de.amr.games.pacman.lib.Logging.log;
import static de.amr.games.pacman.model.GhostState.HUNTING_PAC;
import static de.amr.games.pacman.world.PacManGameWorld.t;

import java.awt.Graphics2D;
import java.awt.image.BufferedImage;

import de.amr.games.pacman.lib.Animation;
import de.amr.games.pacman.lib.V2f;
import de.amr.games.pacman.lib.V2i;
import de.amr.games.pacman.model.Ghost;
import de.amr.games.pacman.model.Pac;
import de.amr.games.pacman.model.PacManGame;
import de.amr.games.pacman.sound.PacManGameSound;
import de.amr.games.pacman.sound.SoundManager;
import de.amr.games.pacman.ui.swing.PacManGameScene;
import de.amr.games.pacman.ui.swing.assets.Spritesheet;
import de.amr.games.pacman.ui.swing.rendering.DefaultGameRendering;

/**
 * Second intermission scene: Blinky pursues Pac but kicks a nail that tears his dress apart.
 * 
 * @author Armin Reichert
 */
public class IntermissionScen2 implements PacManGameScene {

	enum Phase {
		APPROACHING_NAIL, HITTING_NAIL, STRETCHED_1, STRETCHED_2, STRETCHED_3, LOOKING_UP, LOOKING_RIGHT;
	}

	private final V2i size;
	private final DefaultGameRendering rendering;
	private final SoundManager soundManager;
	private final PacManGame game;

	private final Spritesheet spritesheet;
	private final int chaseTileY;
	private final Ghost blinky;
	private final Pac pac;
	private final BufferedImage nail, blinkyLookingUp, blinkyLookingRight, shred;
	private final BufferedImage stretchedDress[];
	private final V2i nailPosition;

	private Phase phase;
	private long timer;

	public IntermissionScen2(V2i size, DefaultPacManGameRendering rendering, SoundManager soundManager,
			PacManGame game) {
		this.size = size;
		this.rendering = rendering;
		this.soundManager = soundManager;
		this.game = game;

		pac = game.pac;
		blinky = game.ghosts[0];
		chaseTileY = 20;
		nailPosition = new V2i(size.x / 2, t(chaseTileY) - 6);

		// Sprites
		spritesheet = rendering.assets;
		nail = spritesheet.spriteAt(8, 6);
		shred = spritesheet.spriteAt(12, 6);
		blinkyLookingUp = spritesheet.spriteAt(8, 7);
		blinkyLookingRight = spritesheet.spriteAt(9, 7);
		stretchedDress = new BufferedImage[] { spritesheet.spriteAt(9, 6), spritesheet.spriteAt(10, 6),
				spritesheet.spriteAt(11, 6) };
	}

	@Override
	public V2i sizeInPixel() {
		return size;
	}

	@Override
	public void start() {
		log("Start of intermission scene %s", getClass().getSimpleName());

		pac.visible = true;
		pac.dead = false;
		pac.position = new V2f(size.x + 50, t(chaseTileY));
		pac.speed = 1;
		pac.dir = LEFT;

		blinky.visible = true;
		blinky.state = HUNTING_PAC;
		blinky.position = pac.position.sum(size.x / 2, 0);
		blinky.speed = 1;
		blinky.dir = blinky.wishDir = LEFT;

		rendering.pacMunching().forEach(Animation::restart);
		rendering.ghostKickingToDir(blinky, blinky.dir).restart();
		soundManager.play(PacManGameSound.INTERMISSION_2);

		phase = Phase.APPROACHING_NAIL;
		timer = -1;
	}

	private void enter(Phase nextPhase, long ticks) {
		phase = nextPhase;
		timer = ticks;
	}

	private void enter(Phase nextPhase) {
		phase = nextPhase;
		timer = -1;
	}

	private boolean timeout() {
		return timer == -1;
	}

	private void update() {
		int distFromNail = (int) (blinky.position.x - nailPosition.x) - 6;
		switch (phase) {
		case APPROACHING_NAIL:
			if (distFromNail == 0) {
				blinky.speed = 0;
				enter(Phase.HITTING_NAIL, clock.sec(0.1));
			}
			break;
		case HITTING_NAIL:
			if (timeout()) {
				blinky.speed = 0.3f;
				enter(Phase.STRETCHED_1);
			}
			break;
		case STRETCHED_1:
			if (distFromNail == -3) {
				blinky.speed = 0.2f;
				enter(Phase.STRETCHED_2);
			}
			break;
		case STRETCHED_2:
			if (distFromNail == -6) {
				blinky.speed = 0.1f;
				enter(Phase.STRETCHED_3);
			}
			break;
		case STRETCHED_3:
			if (distFromNail == -9) {
				blinky.speed = 0;
				enter(Phase.LOOKING_UP, clock.sec(3));
			}
			break;
		case LOOKING_UP:
			if (timeout()) {
				enter(Phase.LOOKING_RIGHT, clock.sec(3));
			}
			break;
		case LOOKING_RIGHT:
			if (timeout()) {
				game.state.duration(0); // signal end of this scene
			}
			break;
		default:
			throw new IllegalStateException("Illegal phase: " + phase);
		}
		blinky.move();
		pac.move();
		if (timer >= 0) {
			--timer;
		}
	}

	@Override
	public void draw(Graphics2D g) {
		update();
		Graphics2D g2 = rendering.smoothGC(g);
		rendering.drawLevelCounter(g2, game, t(game.level.world.xTiles() - 4), size.y - t(2));
		g2.drawImage(nail, nailPosition.x, nailPosition.y, null);
		rendering.drawPac(g, pac, game);
		drawBlinky(g2);
		g2.dispose();
	}

	private void drawBlinky(Graphics2D g) {
		int baselineY = (int) blinky.position.y - 5;
		int blinkySpriteRightEdge = (int) blinky.position.x + 6;
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