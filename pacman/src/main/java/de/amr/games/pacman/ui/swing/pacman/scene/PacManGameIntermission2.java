package de.amr.games.pacman.ui.swing.pacman.scene;

import static de.amr.games.pacman.heaven.God.clock;
import static de.amr.games.pacman.heaven.God.differsAtMost;
import static de.amr.games.pacman.lib.Direction.LEFT;
import static de.amr.games.pacman.lib.Direction.RIGHT;
import static de.amr.games.pacman.lib.Direction.UP;
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

	private final V2i size;
	private final PacManGameRendering rendering;
	private final SoundManager soundManager;
	private final AbstractPacManGame game;

	private final int chaseTileY = 20;
	private final Ghost blinky;
	private final Pac pac;
	private final BufferedImage nail, blinkyDamagedLookingUp, blinkyDamagedLookingRight, shred;
	private final V2i nailPosition;
	private long nailHit;
	private int damage;

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
		blinky.speed = pac.speed * 1.04f;
		blinky.dir = blinky.wishDir = LEFT;

		rendering.letPacMunch(true);
		rendering.ghostWalking(blinky, blinky.dir).restart();
		soundManager.playSound(PacManGameSound.INTERMISSION_2);

		damage = 0;
		nailHit = -1;
	}

	@Override
	public void update() {
		// nail hit?
		if (differsAtMost(blinky.position.x, nailPosition.x + 7, 1)) {
			blinky.speed = 0.05f;
			nailHit = 0; // start animation
		}
		if (0 <= nailHit && nailHit < clock.sec(1)) {
			damage = 1;
		} else if (clock.sec(1) <= nailHit && nailHit < clock.sec(2)) {
			damage = 2;
		} else if (clock.sec(2) <= nailHit && nailHit < clock.sec(3)) {
			damage = 3;
		} else if (nailHit == clock.sec(3)) {
			blinky.speed = 0;
			rendering.ghostWalking(blinky, LEFT).stop();
		} else if (nailHit == clock.sec(5)) {
			damage = 4;
			blinky.dir = UP;
			blinky.visible = false; // damaged Blinky sprite gets drawn
		} else if (nailHit >= clock.sec(7) && nailHit < clock.sec(10)) {
			blinky.dir = RIGHT;
		} else if (nailHit == clock.sec(12)) {
			game.state.duration(0); // end scene
		}
		pac.move();
		blinky.move();
		if (nailHit != -1) {
			++nailHit;
		}
	}

	@Override
	public void draw(Graphics2D g) {
		Graphics2D g2 = rendering.smoothGC(g);
		g2.drawImage(nail, nailPosition.x, nailPosition.y, null);
		rendering.drawGuy(g, pac, game);
		int baselineY = (int) blinky.position.y - 5;
		switch (damage) {
		case 1:
			drawSprite(g2, rendering.assets.spriteAt(8 + damage, 6), blinky.position.x - 3, baselineY);
			break;
		case 2:
			drawSprite(g2, rendering.assets.spriteAt(8 + damage, 6), blinky.position.x + 1, baselineY);
			break;
		case 3:
			drawSprite(g2, rendering.assets.spriteAt(8 + damage, 6), blinky.position.x + 3, baselineY);
			break;
		case 4:
			if (blinky.dir == UP) {
				drawSprite(g2, blinkyDamagedLookingUp, blinky.position.x - 4, blinky.position.y - 4);
			} else if (blinky.dir == RIGHT) {
				// TODO is Blinky winking?
				drawSprite(g2, blinkyDamagedLookingRight, blinky.position.x - 4, blinky.position.y - 4);
			}
			drawSprite(g2, shred, nailPosition.x, baselineY);
			break;
		default:
			break;
		}
		rendering.drawGuy(g, blinky, game);
		g2.dispose();
	}

	private void drawSprite(Graphics2D g, BufferedImage sprite, float x, float y) {
		g.drawImage(sprite, (int) x, (int) y, null);
	}
}