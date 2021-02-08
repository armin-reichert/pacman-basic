package de.amr.games.pacman.ui.swing.pacman.scene;

import static de.amr.games.pacman.heaven.God.clock;
import static de.amr.games.pacman.lib.Direction.LEFT;
import static de.amr.games.pacman.lib.Logging.log;
import static de.amr.games.pacman.model.creatures.GhostState.HUNTING_PAC;
import static de.amr.games.pacman.world.PacManGameWorld.t;

import java.awt.Graphics2D;
import java.awt.image.BufferedImage;

import de.amr.games.pacman.lib.V2f;
import de.amr.games.pacman.lib.V2i;
import de.amr.games.pacman.model.AbstractPacManGame;
import de.amr.games.pacman.model.creatures.Ghost;
import de.amr.games.pacman.model.creatures.Pac;
import de.amr.games.pacman.ui.api.PacManGameScene;
import de.amr.games.pacman.ui.sound.PacManGameSound;
import de.amr.games.pacman.ui.sound.SoundManager;
import de.amr.games.pacman.ui.swing.pacman.rendering.PacManGameRendering;

/**
 * Second intermission scene: Blinky pursues Pac but kicks a nail that tears his dress apart.
 * 
 * @author Armin Reichert
 */
public class PacManGameIntermission2 implements PacManGameScene {

	enum Phase {
		APPROACHING, HITTING_NAIL, GETTING_STRETCHED_1, GETTING_STRETCHED_2, GETTING_STRETCHED_3, LOOKING_UP, LOOKING_RIGHT;
	};

	private final V2i size;
	private final PacManGameRendering rendering;
	private final SoundManager soundManager;
	private final AbstractPacManGame game;

	private final int chaseTileY = 20;
	private final Ghost blinky;
	private final Pac pac;
	private final BufferedImage nail, blinkyDamagedLookingUp, blinkyDamagedLookingRight, shred;
	private final BufferedImage stretchedDress[] = new BufferedImage[3];
	private final V2i nailPosition;

	private Phase phase;
	private long timer;

	public PacManGameIntermission2(V2i size, PacManGameRendering rendering, SoundManager soundManager,
			AbstractPacManGame game) {
		this.size = size;
		this.rendering = rendering;
		this.soundManager = soundManager;
		this.game = game;
		pac = game.pac;
		blinky = game.ghosts[0];
		nailPosition = new V2i(size.x / 2, t(chaseTileY) - 6);
		nail = rendering.assets.spriteAt(8, 6);
		shred = rendering.assets.spriteAt(12, 6);
		blinkyDamagedLookingUp = rendering.assets.spriteAt(8, 7);
		blinkyDamagedLookingRight = rendering.assets.spriteAt(9, 7);
		for (int s = 0; s < 3; ++s) {
			stretchedDress[s] = rendering.assets.spriteAt(9 + s, 6);
		}
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
		pac.speed = 0.5f;
		pac.dir = LEFT;
		blinky.visible = true;
		blinky.state = HUNTING_PAC;
		blinky.position = pac.position.sum(new V2f(size.x / 2, 0));
		blinky.speed = 0.5f;
		blinky.dir = blinky.wishDir = LEFT;

		rendering.letPacMunch(true);
		rendering.ghostWalking(blinky, blinky.dir).restart();
		soundManager.playSound(PacManGameSound.INTERMISSION_2);

		phase = Phase.APPROACHING;
		timer = -1;
	}

	@Override
	public void update() {
		log("scene update, state timer is %d", game.state.ticksRun());
		int distanceFromNail = (int) (blinky.position.x - nailPosition.x) - 5;
		log("distance from nail: %d, %s, timer: %d", distanceFromNail, phase, timer);
		switch (phase) {
		case APPROACHING: {
			blinky.move();
			pac.move();
			if (distanceFromNail == 0) {
				blinky.speed = 0;
				phase = Phase.HITTING_NAIL;
				timer = clock.sec(1);
			}
			break;
		}
		case HITTING_NAIL: {
			if (timer == -1) {
				phase = Phase.GETTING_STRETCHED_1;
				blinky.speed = 0.1f;
			}
			break;
		}
		case GETTING_STRETCHED_1: {
			if (distanceFromNail == -3) {
				phase = Phase.GETTING_STRETCHED_2;
			}
			break;
		}
		case GETTING_STRETCHED_2: {
			if (distanceFromNail == -6) {
				phase = Phase.GETTING_STRETCHED_3;
			}
			break;
		}
		case GETTING_STRETCHED_3: {
			if (distanceFromNail == -9) {
				blinky.speed = 0;
				phase = Phase.LOOKING_UP;
				timer = clock.sec(4);
			}
			break;
		}
		case LOOKING_UP: {
			if (timer == -1) {
				phase = Phase.LOOKING_RIGHT;
				timer = clock.sec(20);
			}
			break;
		}
		case LOOKING_RIGHT: {
			if (timer == -1) {
				game.state.duration(0); // signal end of this scene
			}
			break;
		}
		default:
			break;
		}
		blinky.move();
		pac.move();
		if (timer >= 0) {
			--timer;
		}
	}

	@Override
	public void draw(Graphics2D g) {
		Graphics2D g2 = rendering.smoothGC(g);
		g2.drawImage(nail, nailPosition.x, nailPosition.y, null);
		rendering.drawGuy(g, pac, game);
		drawBlinky(g2);
		g2.dispose();
	}

	private void drawBlinky(Graphics2D g) {
		int baselineY = (int) blinky.position.y - 5;
		int blinkySpriteRightEdge = (int) blinky.position.x + 9;
		switch (phase) {
		case APPROACHING:
		case HITTING_NAIL:
			rendering.drawGuy(g, blinky, game);
			break;
		case GETTING_STRETCHED_1:
			rendering.drawSprite(g, stretchedDress[0], blinkySpriteRightEdge - 8, baselineY);
			rendering.drawGuy(g, blinky, game);
			break;
		case GETTING_STRETCHED_2:
			rendering.drawSprite(g, stretchedDress[1], blinkySpriteRightEdge - 4, baselineY);
			rendering.drawGuy(g, blinky, game);
			break;
		case GETTING_STRETCHED_3:
			rendering.drawSprite(g, stretchedDress[2], blinkySpriteRightEdge - 2, baselineY);
			rendering.drawGuy(g, blinky, game);
			break;
		case LOOKING_UP:
			rendering.drawSprite(g, blinkyDamagedLookingUp, blinky.position.x - 4, blinky.position.y - 4);
			rendering.drawSprite(g, shred, nailPosition.x, baselineY);
			break;
		case LOOKING_RIGHT:
			rendering.drawSprite(g, blinkyDamagedLookingRight, blinky.position.x - 4, blinky.position.y - 4);
			rendering.drawSprite(g, shred, nailPosition.x, baselineY);
			break;
		default:
			break;
		}
	}
}