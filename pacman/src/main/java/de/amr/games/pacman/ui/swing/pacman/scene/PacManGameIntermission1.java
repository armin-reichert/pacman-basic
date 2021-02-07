package de.amr.games.pacman.ui.swing.pacman.scene;

import static de.amr.games.pacman.lib.Logging.log;
import static de.amr.games.pacman.world.PacManGameWorld.t;

import java.awt.Graphics2D;
import java.awt.image.BufferedImage;

import de.amr.games.pacman.heaven.God;
import de.amr.games.pacman.lib.Animation;
import de.amr.games.pacman.lib.Direction;
import de.amr.games.pacman.lib.V2f;
import de.amr.games.pacman.lib.V2i;
import de.amr.games.pacman.model.AbstractPacManGame;
import de.amr.games.pacman.model.creatures.Ghost;
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

	private boolean complete;
	private Animation<BufferedImage> bigPac;
	private Ghost blinky;

	public PacManGameIntermission1(V2i size, PacManGameRendering rendering, SoundManager soundManager,
			AbstractPacManGame game) {
		this.size = size;
		this.rendering = rendering;
		this.soundManager = soundManager;
		this.game = game;
		bigPac = Animation.of(rendering.assets.spritesAt(2, 1, 2, 2), rendering.assets.spritesAt(4, 1, 2, 2),
				rendering.assets.spritesAt(6, 1, 2, 2));
	}

	@Override
	public V2i sizeInPixel() {
		return size;
	}

	@Override
	public void start() {
		log("Start of intermission screen %s", getClass().getSimpleName());
		soundManager.loopSound(PacManGameSound.INTERMISSION_1, 2);
		complete = false;
		game.pac.position = new V2f(size.x, t(17));
		game.pac.speed = 0.5f;
		game.pac.dir = Direction.LEFT;
		game.pac.dead = false;
		rendering.letPacMunch(true);
		blinky = game.ghosts[0];
		blinky.position = game.pac.position.sum(new V2f(24, 0));
		blinky.speed = game.pac.speed * 1.04f;
		blinky.dir = game.ghosts[0].wishDir = Direction.LEFT;
		rendering.letGhostsFidget(game.ghosts(), true);
	}

	@Override
	public void update() {
		// TODO
		if (game.state.ticksRun() == God.clock.sec(10)) {
			complete = true;
			game.state.duration(0);
			log("End of intermission screen %s", getClass().getSimpleName());
			return;
		}
		game.pac.moveFreely();
		blinky.moveFreely();
	}

	@Override
	public void draw(Graphics2D g) {
		rendering.drawGuy(g, game.pac, game);
		rendering.drawGuy(g, blinky, game);
	}
}