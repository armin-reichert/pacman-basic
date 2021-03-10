package de.amr.games.pacman.ui.swing.pacman;

import static de.amr.games.pacman.model.world.PacManGameWorld.t;

import java.awt.Dimension;
import java.awt.Graphics2D;

import de.amr.games.pacman.controller.PacManGameController;
import de.amr.games.pacman.ui.pacman.PacMan_IntermissionScene2_Controller;
import de.amr.games.pacman.ui.sound.SoundManager;
import de.amr.games.pacman.ui.swing.common.GameScene;
import de.amr.games.pacman.ui.swing.rendering.PacManGameRendering2D;

/**
 * Second intermission scene: Blinky pursues Pac but kicks a nail that tears his dress apart.
 * 
 * @author Armin Reichert
 */
public class PacMan_IntermissionScene2 extends GameScene {

	private PacMan_IntermissionScene2_Controller animation;

	public PacMan_IntermissionScene2(PacManGameController controller, Dimension size, PacManGameRendering2D rendering,
			SoundManager sounds) {
		super(controller, size, rendering, sounds);
	}

	@Override
	public void start() {
		animation = new PacMan_IntermissionScene2_Controller(controller, rendering, sounds);
		animation.start();
	}

	@Override
	public void update() {
		animation.update();
	}

	@Override
	public void render(Graphics2D g) {
		rendering.drawLevelCounter(g, controller.getGame(), t(25), t(34));
		rendering.drawNail(g, animation.nail);
		rendering.drawPlayer(g, animation.pac);
		if (animation.nailDistance() < 0) {
			rendering.drawGhost(g, animation.blinky, false);
		} else {
			rendering.drawBlinkyStretched(g, animation.blinky, animation.nail.position, animation.nailDistance() / 4);
		}
	}
}