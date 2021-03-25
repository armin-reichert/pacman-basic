package de.amr.games.pacman.ui.swing.mspacman;

import static de.amr.games.pacman.model.common.GameVariant.MS_PACMAN;

import java.awt.Dimension;
import java.awt.Graphics2D;

import de.amr.games.pacman.controller.PacManGameController;
import de.amr.games.pacman.ui.mspacman.MsPacMan_IntermissionScene2_Controller;
import de.amr.games.pacman.ui.swing.PacManGameUI_Swing;
import de.amr.games.pacman.ui.swing.common.GameScene;

/**
 * Intermission scene 2: "The chase".
 * <p>
 * Pac-Man and Ms. Pac-Man chase each other across the screen over and over. After three turns, they
 * both rapidly run from left to right and right to left. (Played after round 5)
 * 
 * @author Armin Reichert
 */
public class MsPacMan_IntermissionScene2 extends GameScene {

	private MsPacMan_IntermissionScene2_Controller sceneController;

	public MsPacMan_IntermissionScene2(PacManGameController controller, Dimension size) {
		super(controller, size, PacManGameUI_Swing.RENDERING.get(MS_PACMAN), PacManGameUI_Swing.SOUND.get(MS_PACMAN));
	}

	@Override
	public void start() {
		sceneController = new MsPacMan_IntermissionScene2_Controller(gameController, rendering, sounds);
		sceneController.start();
	}

	@Override
	public void update() {
		sceneController.update();
	}

	@Override
	public void render(Graphics2D g) {
		rendering.drawFlap(g, sceneController.flap);
		rendering.drawPlayer(g, sceneController.msPacMan);
		rendering.drawSpouse(g, sceneController.pacMan);
	}
}