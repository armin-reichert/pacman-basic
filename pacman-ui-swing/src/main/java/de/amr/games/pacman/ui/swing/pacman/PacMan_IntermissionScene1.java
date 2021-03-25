package de.amr.games.pacman.ui.swing.pacman;

import static de.amr.games.pacman.model.common.GameVariant.PACMAN;
import static de.amr.games.pacman.model.world.PacManGameWorld.t;

import java.awt.Dimension;
import java.awt.Graphics2D;

import de.amr.games.pacman.controller.PacManGameController;
import de.amr.games.pacman.ui.pacman.PacMan_IntermissionScene1_Controller;
import de.amr.games.pacman.ui.pacman.PacMan_IntermissionScene1_Controller.Phase;
import de.amr.games.pacman.ui.swing.PacManGameUI_Swing;
import de.amr.games.pacman.ui.swing.common.GameScene;

/**
 * First intermission scene: Blinky chases Pac-Man and is then chased by a huge Pac-Man.
 * 
 * @author Armin Reichert
 */
public class PacMan_IntermissionScene1 extends GameScene {

	private PacMan_IntermissionScene1_Controller sceneController;

	public PacMan_IntermissionScene1(PacManGameController controller, Dimension size) {
		super(controller, size, PacManGameUI_Swing.RENDERING.get(PACMAN), PacManGameUI_Swing.SOUND.get(PACMAN));
	}

	@Override
	public void start() {
		sceneController = new PacMan_IntermissionScene1_Controller(gameController, rendering, sounds);
		sceneController.start();
	}

	@Override
	public void update() {
		sceneController.update();
	}

	@Override
	public void render(Graphics2D g) {
		rendering.drawGhost(g, sceneController.blinky, false);
		if (sceneController.phase == Phase.BLINKY_CHASING_PACMAN) {
			rendering.drawPlayer(g, sceneController.pac);
		} else {
			g.translate(0, -10);
			rendering.drawBigPacMan(g, sceneController.pac);
			g.translate(0, 10);
		}
		rendering.drawLevelCounter(g, gameController.game(), t(25), t(34));
	}
}