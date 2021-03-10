package de.amr.games.pacman.ui.swing.pacman;

import static de.amr.games.pacman.model.world.PacManGameWorld.t;

import java.awt.Dimension;
import java.awt.Graphics2D;

import de.amr.games.pacman.controller.PacManGameController;
import de.amr.games.pacman.ui.pacman.PacMan_IntermissionScene3_Controller;
import de.amr.games.pacman.ui.pacman.PacMan_IntermissionScene3_Controller.Phase;
import de.amr.games.pacman.ui.sound.SoundManager;
import de.amr.games.pacman.ui.swing.common.GameScene;
import de.amr.games.pacman.ui.swing.rendering.PacManGameRendering2D;

/**
 * Third intermission scene: Blinky in shred dress chases Pac-Man, comes back half-naked drawing
 * dress over the floor.
 * 
 * @author Armin Reichert
 */
public class PacMan_IntermissionScene3 extends GameScene {

	private PacMan_IntermissionScene3_Controller animation;

	public PacMan_IntermissionScene3(PacManGameController controller, Dimension size, PacManGameRendering2D rendering,
			SoundManager sounds) {
		super(controller, size, rendering, sounds);
	}

	@Override
	public void start() {
		animation = new PacMan_IntermissionScene3_Controller(controller, rendering, sounds);
		animation.start();
	}

	@Override
	public void update() {
		animation.update();
	}

	@Override
	public void render(Graphics2D g) {
		rendering.drawLevelCounter(g, controller.getGame(), t(25), t(34));
		rendering.drawPlayer(g, animation.pac);
		if (animation.phase == Phase.CHASING_PACMAN) {
			rendering.drawBlinkyPatched(g, animation.blinky);
		} else {
			rendering.drawBlinkyNaked(g, animation.blinky);
		}
	}
}