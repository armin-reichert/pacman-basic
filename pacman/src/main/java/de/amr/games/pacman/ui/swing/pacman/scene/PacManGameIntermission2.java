package de.amr.games.pacman.ui.swing.pacman.scene;

import static de.amr.games.pacman.heaven.God.clock;
import static de.amr.games.pacman.heaven.God.differsAtMost;
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
 * Scene where the game is played.
 * 
 * @author Armin Reichert
 */
public class PacManGameIntermission2 implements PacManGameScene {

	private final V2i size;
	private final PacManGameRendering rendering;
	private final SoundManager soundManager;
	private final AbstractPacManGame game;

	private final int chaseTile = 20;
	private final Ghost blinky;
	private final Pac pac;
	private final BufferedImage nail;
	private long nailMetTimer;

	public PacManGameIntermission2(V2i size, PacManGameRendering rendering, SoundManager soundManager,
			AbstractPacManGame game) {
		this.size = size;
		this.rendering = rendering;
		this.soundManager = soundManager;
		this.game = game;
		pac = game.pac;
		blinky = game.ghosts[0];
		nail = rendering.assets.spriteAt(8, 6);
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
		pac.position = new V2f(size.x + 50, t(chaseTile));
		pac.speed = 0.5f;
		pac.dir = LEFT;
		blinky.visible = true;
		blinky.state = HUNTING_PAC;
		blinky.position = pac.position.sum(new V2f(size.x / 2, 0));
		blinky.speed = pac.speed * 1.04f;
		blinky.dir = blinky.wishDir = LEFT;

		rendering.letPacMunch(true);
		rendering.ghostWalking(blinky, blinky.dir).restart();
		soundManager.playSound(PacManGameSound.INTERMISSION_2);

		nailMetTimer = -1;
	}

	@Override
	public void update() {

		// nail met?
		if (differsAtMost(blinky.position.x, (size.x / 2) + 7, 1)) {
			blinky.speed = 0.05f;
			nailMetTimer = clock.sec(3);
		}
		if (nailMetTimer > 0) {
			--nailMetTimer;
			if (nailMetTimer == 0) {
				blinky.speed = 0;
			}
		}
		pac.move();
		blinky.move();
	}

	@Override
	public void draw(Graphics2D g) {
		rendering.drawGuy(g, pac, game);
		Graphics2D g2 = rendering.smoothGC(g);
		if (nailMetTimer == -1) {
			g2.drawImage(nail, size.x / 2, t(chaseTile) - 4, null); // nail
		} else {
			int x = (int) blinky.position.x + 5, y = (int) blinky.position.y - 5;
			if (nailMetTimer > clock.sec(2)) {
				g2.drawImage(rendering.assets.spriteAt(9, 6), x - 8, y, null);
			} else if (nailMetTimer > clock.sec(1)) {
				g2.drawImage(rendering.assets.spriteAt(10, 6), x - 4, y, null);
			} else if (nailMetTimer >= 0) {
				g2.drawImage(rendering.assets.spriteAt(11, 6), x - 2, y, null);
			}
		}
		g2.dispose();
		rendering.drawGuy(g, blinky, game);
	}

}