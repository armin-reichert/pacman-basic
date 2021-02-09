package de.amr.games.pacman.ui.swing.pacman.scene;

import static de.amr.games.pacman.lib.Direction.LEFT;
import static de.amr.games.pacman.lib.Direction.RIGHT;
import static de.amr.games.pacman.lib.Logging.log;
import static de.amr.games.pacman.world.PacManGameWorld.t;

import java.awt.Graphics2D;
import java.awt.image.BufferedImage;

import de.amr.games.pacman.lib.Animation;
import de.amr.games.pacman.lib.V2f;
import de.amr.games.pacman.lib.V2i;
import de.amr.games.pacman.model.PacManGame;
import de.amr.games.pacman.model.creatures.Ghost;
import de.amr.games.pacman.model.creatures.GhostState;
import de.amr.games.pacman.model.creatures.Pac;
import de.amr.games.pacman.ui.api.PacManGameScene;
import de.amr.games.pacman.ui.sound.PacManGameSound;
import de.amr.games.pacman.ui.sound.SoundManager;
import de.amr.games.pacman.ui.swing.assets.Spritesheet;
import de.amr.games.pacman.ui.swing.pacman.rendering.PacManGameSpriteBasedRendering;

/**
 * Third intermission scene: Blinky in shred dress chases Pac-Man, comes back half-naked drawing
 * dress over the floor.
 * 
 * @author Armin Reichert
 */
public class PacManGameIntermission3 implements PacManGameScene {

	enum Phase {
		CHASING_PACMAN, RETURNING_HALF_NAKED
	}

	private final V2i size;
	private final PacManGameSpriteBasedRendering rendering;
	private final SoundManager soundManager;
	private final PacManGame game;

	private final Spritesheet spritesheet;
	private final Animation<BufferedImage> blinkyDamaged, blinkyHalfNaked;

	private final int chaseTileY;
	private final Ghost blinky;
	private final Pac pac;

	private Phase phase;
	private long timer;

	public PacManGameIntermission3(V2i size, PacManGameSpriteBasedRendering rendering, SoundManager soundManager, PacManGame game) {
		this.size = size;
		this.rendering = rendering;
		this.soundManager = soundManager;
		this.game = game;

		pac = game.pac;
		blinky = game.ghosts[0];
		chaseTileY = 20;

		spritesheet = rendering.assets;
		blinkyDamaged = Animation.of(spritesheet.spriteAt(10, 7), spritesheet.spriteAt(11, 7));
		blinkyDamaged.frameDuration(4).endless();
		blinkyHalfNaked = Animation.of(spritesheet.spritesAt(8, 8, 2, 1), spritesheet.spritesAt(10, 8, 2, 1));
		blinkyHalfNaked.frameDuration(4).endless();
	}

	@Override
	public V2i sizeInPixel() {
		return size;
	}

	@Override
	public void start() {
		log("Start intermission scene %s", getClass().getSimpleName());

		pac.visible = true;
		pac.dead = false;
		pac.position = new V2f(size.x + 50, t(chaseTileY));
		pac.speed = 1;
		pac.dir = LEFT;

		blinky.visible = true;
		blinky.state = GhostState.HUNTING_PAC;
		blinky.position = pac.position.sum(64, 0);
		blinky.speed = pac.speed;
		blinky.dir = blinky.wishDir = LEFT;

		rendering.pacMunching().forEach(Animation::restart);
		blinkyDamaged.restart();
		soundManager.loopSound(PacManGameSound.INTERMISSION_3, 2);

		phase = Phase.CHASING_PACMAN;
	}

	@Override
	public void update() {
		switch (phase) {
		case CHASING_PACMAN:
			if (blinky.position.x == -50) {
				pac.speed = 0;
				blinky.dir = blinky.wishDir = RIGHT;
				blinkyHalfNaked.restart();
				phase = Phase.RETURNING_HALF_NAKED;
			}
			break;
		case RETURNING_HALF_NAKED:
			if (blinky.position.x > size.x + 50) {
				game.state.duration(0); // end scene
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
		Graphics2D g2 = rendering.smoothGC(g);
		rendering.drawLevelCounter(g2, game, t(game.level.world.xTiles() - 4), size.y - t(2));
		rendering.drawGuy(g2, pac, game);
		drawBlinky(g2);
		g2.dispose();
	}

	private void drawBlinky(Graphics2D g) {
		switch (phase) {
		case CHASING_PACMAN:
			rendering.drawSprite(g, blinkyDamaged.animate(), blinky.position.x - 4, blinky.position.y - 4);
			break;
		case RETURNING_HALF_NAKED:
			rendering.drawSprite(g, blinkyHalfNaked.animate(), blinky.position.x - 4, blinky.position.y - 4);
			break;
		default:
			break;
		}
	}
}