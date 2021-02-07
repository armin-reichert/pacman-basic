package de.amr.games.pacman.ui.swing.pacman.scene;

import static de.amr.games.pacman.lib.Direction.LEFT;
import static de.amr.games.pacman.lib.Direction.RIGHT;
import static de.amr.games.pacman.lib.Logging.log;
import static de.amr.games.pacman.model.creatures.GhostState.FRIGHTENED;
import static de.amr.games.pacman.model.creatures.GhostState.HUNTING_PAC;
import static de.amr.games.pacman.world.PacManGameWorld.t;

import java.awt.Graphics2D;
import java.awt.image.BufferedImage;

import de.amr.games.pacman.lib.Animation;
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
 * Scene where the game is played.
 * 
 * @author Armin Reichert
 */
public class PacManGameIntermission1 implements PacManGameScene {

	private final V2i size;
	private final PacManGameRendering rendering;
	private final SoundManager soundManager;
	private final AbstractPacManGame game;

	private final Animation<BufferedImage> bigPacAnimation;
	private int chaseTile = 20;
	private final Ghost blinky;
	private final Pac pac;

	public PacManGameIntermission1(V2i size, PacManGameRendering rendering, SoundManager soundManager,
			AbstractPacManGame game) {
		this.size = size;
		this.rendering = rendering;
		this.soundManager = soundManager;
		this.game = game;

		pac = game.pac;
		blinky = game.ghosts[0];
		bigPacAnimation = Animation.of(rendering.assets.spritesAt(2, 1, 2, 2), rendering.assets.spritesAt(4, 1, 2, 2),
				rendering.assets.spritesAt(6, 1, 2, 2)).endless().frameDuration(4);
	}

	@Override
	public V2i sizeInPixel() {
		return size;
	}

	@Override
	public void start() {
		log("Start of intermission screen %s", getClass().getSimpleName());
		soundManager.loopSound(PacManGameSound.INTERMISSION_1, 1);
		pac.visible = true;
		pac.position = new V2f(size.x + 50, t(chaseTile));
		pac.speed = 0.5f;
		pac.dir = LEFT;
		pac.dead = false;
		rendering.letPacMunch(true);
		blinky.visible = true;
		blinky.state = HUNTING_PAC;
		blinky.position = pac.position.sum(new V2f(24, 0));
		blinky.speed = pac.speed * 1.04f;
		blinky.dir = game.ghosts[0].wishDir = LEFT;
		rendering.ghostWalking(blinky, blinky.dir).restart();
	}

	@Override
	public void update() {
		if (pac.dir == RIGHT && pac.position.x > size.x + 100) {
			game.state.duration(0);
			soundManager.stopAllSounds();
			log("End of intermission screen %s", getClass().getSimpleName());
			return;
		}
		if (blinky.position.x < -50) {
			blinky.speed = pac.speed * 0.8f;
			blinky.state = FRIGHTENED;
			blinky.dir = blinky.wishDir = RIGHT;
			blinky.position = new V2f(-50, blinky.position.y);
			rendering.ghostFrightened(blinky, blinky.dir).restart();
			pac.position = new V2f(blinky.position.x - 100, pac.position.y);
			pac.dir = RIGHT;
			bigPacAnimation.restart();
		}
		pac.moveFreely();
		blinky.moveFreely();
	}

	@Override
	public void draw(Graphics2D g) {
		if (pac.dir == LEFT) {
			rendering.drawGuy(g, pac, game);
		} else {
			Graphics2D g2 = rendering.smoothGC(g);
			g2.drawImage(bigPacAnimation.animate(), (int) pac.position.x, (int) pac.position.y - 22, null);
			g2.dispose();
		}
		rendering.drawGuy(g, blinky, game);
	}
}