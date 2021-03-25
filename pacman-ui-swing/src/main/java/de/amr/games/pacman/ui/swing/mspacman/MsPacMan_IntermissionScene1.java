package de.amr.games.pacman.ui.swing.mspacman;

import static de.amr.games.pacman.model.common.GameVariant.MS_PACMAN;

import java.awt.Dimension;
import java.awt.Graphics2D;

import de.amr.games.pacman.controller.PacManGameController;
import de.amr.games.pacman.ui.mspacman.MsPacMan_IntermissionScene1_Controller;
import de.amr.games.pacman.ui.swing.PacManGameUI_Swing;
import de.amr.games.pacman.ui.swing.common.GameScene;

/**
 * Intermission scene 1: "They meet".
 * <p>
 * Pac-Man leads Inky and Ms. Pac-Man leads Pinky. Soon, the two Pac-Men are about to collide, they
 * quickly move upwards, causing Inky and Pinky to collide and vanish. Finally, Pac-Man and Ms.
 * Pac-Man face each other at the top of the screen and a big pink heart appears above them. (Played
 * after round 2)
 * 
 * @author Armin Reichert
 */
public class MsPacMan_IntermissionScene1 extends GameScene {

	private MsPacMan_IntermissionScene1_Controller sceneController;

	public MsPacMan_IntermissionScene1(PacManGameController controller, Dimension size) {
		super(controller, size, PacManGameUI_Swing.RENDERING.get(MS_PACMAN), PacManGameUI_Swing.SOUND.get(MS_PACMAN));
	}

	@Override
	public void start() {
		sceneController = new MsPacMan_IntermissionScene1_Controller(gameController, rendering, sounds);
		sceneController.start();
	}

	@Override
	public void update() {
		sceneController.update();
	}

	@Override
	public void render(Graphics2D g) {
		rendering.drawFlap(g, sceneController.flap);
		rendering.drawPlayer(g, sceneController.msPac);
		rendering.drawSpouse(g, sceneController.pacMan);
		rendering.drawGhost(g, sceneController.inky, false);
		rendering.drawGhost(g, sceneController.pinky, false);
		rendering.drawHeart(g, sceneController.heart);
	}
}