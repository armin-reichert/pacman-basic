package de.amr.games.pacman.ui.swing.pacman.scene;

import static de.amr.games.pacman.lib.Direction.LEFT;
import static de.amr.games.pacman.lib.Direction.RIGHT;
import static de.amr.games.pacman.lib.Logging.log;
import static de.amr.games.pacman.model.creatures.GhostState.FRIGHTENED;
import static de.amr.games.pacman.model.creatures.GhostState.HUNTING_PAC;
import static de.amr.games.pacman.ui.swing.pacman.scene.PacManGameIntermission1.Phase.BIGPACMAN_CHASING_BLINKY;
import static de.amr.games.pacman.ui.swing.pacman.scene.PacManGameIntermission1.Phase.BLINKY_CHASING_PACMAN;
import static de.amr.games.pacman.world.PacManGameWorld.t;

import java.awt.Graphics2D;
import java.awt.image.BufferedImage;

import de.amr.games.pacman.lib.Animation;
import de.amr.games.pacman.lib.V2f;
import de.amr.games.pacman.lib.V2i;
import de.amr.games.pacman.model.PacManGame;
import de.amr.games.pacman.model.creatures.Ghost;
import de.amr.games.pacman.model.creatures.Pac;
import de.amr.games.pacman.ui.api.PacManGameScene;
import de.amr.games.pacman.ui.sound.PacManGameSound;
import de.amr.games.pacman.ui.sound.SoundManager;
import de.amr.games.pacman.ui.swing.pacman.rendering.PacManGameSpriteBasedRendering;

/**
 * First intermission scene: Blinky chases Pac-Man and is then chased by a huge Pac-Man.
 * 
 * @author Armin Reichert
 */
public class PacManGameIntermission1 implements PacManGameScene {

	enum Phase {
		BLINKY_CHASING_PACMAN, BIGPACMAN_CHASING_BLINKY;
	}

	private final V2i size;
	private final PacManGameSpriteBasedRendering rendering;
	private final SoundManager soundManager;
	private final PacManGame game;

	private final Animation<BufferedImage> bigPac;
	private final int baselineY = t(20);
	private final Ghost blinky;
	private final Pac pac;

	private Phase phase;

	public PacManGameIntermission1(V2i size, PacManGameSpriteBasedRendering rendering, SoundManager soundManager, PacManGame game) {
		this.size = size;
		this.rendering = rendering;
		this.soundManager = soundManager;
		this.game = game;

		pac = game.pac;
		blinky = game.ghosts[0];
		bigPac = Animation.of(rendering.assets.spritesAt(2, 1, 2, 2), rendering.assets.spritesAt(4, 1, 2, 2),
				rendering.assets.spritesAt(6, 1, 2, 2));
		bigPac.endless().frameDuration(4).run();
	}

	@Override
	public V2i sizeInPixel() {
		return size;
	}

	@Override
	public void start() {
		log("Start of intermission scene %s", getClass().getSimpleName());

		phase = BLINKY_CHASING_PACMAN;

		pac.visible = true;
		pac.dead = false;
		pac.couldMove = true;
		pac.position = new V2f(size.x + 50, baselineY);
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
		soundManager.loopSound(PacManGameSound.INTERMISSION_1, 2);
	}

	@Override
	public void end() {
		log("End of intermission scene %s", getClass().getSimpleName());
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
				phase = BIGPACMAN_CHASING_BLINKY;
			}
			break;
		case BIGPACMAN_CHASING_BLINKY:
			if ((int) blinky.position.x + 4 == size.x / 2) {
				pac.speed = blinky.speed * 1.9f;
			}
			if (pac.position.x > size.x + 100) {
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
	public void draw(Graphics2D g) {
		Graphics2D g2 = rendering.smoothGC(g);
		rendering.drawLevelCounter(g2, game, size.x - t(4), size.y - t(2));
		rendering.drawGuy(g2, blinky, game);
		if (phase == BLINKY_CHASING_PACMAN) {
			rendering.drawGuy(g2, pac, game);
		} else {
			rendering.drawSprite(g2, bigPac.animate(), pac.position.x - 12, pac.position.y - 22);
		}
		g2.dispose();
	}
}