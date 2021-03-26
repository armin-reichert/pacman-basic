package de.amr.games.pacman.ui.swing.mspacman;

import static de.amr.games.pacman.model.common.GameVariant.MS_PACMAN;

import java.awt.Dimension;
import java.awt.Graphics2D;

import de.amr.games.pacman.controller.PacManGameController;
import de.amr.games.pacman.ui.animation.PacManGameAnimations2D;
import de.amr.games.pacman.ui.mspacman.MsPacMan_IntermissionScene3_Controller;
import de.amr.games.pacman.ui.sound.PacManGameSound;
import de.amr.games.pacman.ui.swing.PacManGameUI_Swing;
import de.amr.games.pacman.ui.swing.common.GameScene;

/**
 * Intermission scene 3: "Junior".
 * 
 * <p>
 * Pac-Man and Ms. Pac-Man gradually wait for a stork, who flies overhead with a little blue bundle.
 * The stork drops the bundle, which falls to the ground in front of Pac-Man and Ms. Pac-Man, and
 * finally opens up to reveal a tiny Pac-Man. (Played after rounds 9, 13, and 17)
 * 
 * @author Armin Reichert
 */
public class MsPacMan_IntermissionScene3 extends GameScene {

	private class SceneController extends MsPacMan_IntermissionScene3_Controller {

		public SceneController(PacManGameController gameController, PacManGameAnimations2D animations) {
			super(gameController, animations);
		}

		@Override
		public void playIntermissionSound() {
			sounds.play(PacManGameSound.INTERMISSION_3);
		}

	}

	private SceneController sceneController;

	public MsPacMan_IntermissionScene3(PacManGameController controller, Dimension size) {
		super(controller, size, PacManGameUI_Swing.RENDERING.get(MS_PACMAN), PacManGameUI_Swing.SOUND.get(MS_PACMAN));
	}

	@Override
	public void start() {
		sceneController = new SceneController(gameController, rendering);
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
		rendering.drawStork(g, sceneController.stork);
		rendering.drawJuniorBag(g, sceneController.bag);
	}
}