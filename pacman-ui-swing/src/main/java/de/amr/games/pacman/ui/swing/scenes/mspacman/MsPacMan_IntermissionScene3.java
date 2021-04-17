package de.amr.games.pacman.ui.swing.scenes.mspacman;

import static de.amr.games.pacman.model.common.GameVariant.MS_PACMAN;

import java.awt.Dimension;
import java.awt.Graphics2D;

import de.amr.games.pacman.controller.PacManGameController;
import de.amr.games.pacman.ui.PacManGameSound;
import de.amr.games.pacman.ui.mspacman.MsPacMan_IntermissionScene3_Controller;
import de.amr.games.pacman.ui.swing.PacManGameUI_Swing;
import de.amr.games.pacman.ui.swing.rendering.common.Player2D;
import de.amr.games.pacman.ui.swing.rendering.mspacman.Flap2D;
import de.amr.games.pacman.ui.swing.rendering.mspacman.JuniorBag2D;
import de.amr.games.pacman.ui.swing.rendering.mspacman.Stork2D;
import de.amr.games.pacman.ui.swing.scenes.common.GameScene;

/**
 * Intermission scene 3: "Junior".
 * 
 * <p>
 * Pac-Man and Ms. Pac-Man gradually wait for a stork, who flies overhead with a
 * little blue bundle. The stork drops the bundle, which falls to the ground in
 * front of Pac-Man and Ms. Pac-Man, and finally opens up to reveal a tiny
 * Pac-Man. (Played after rounds 9, 13, and 17)
 * 
 * @author Armin Reichert
 */
public class MsPacMan_IntermissionScene3 extends GameScene {

	private class SceneController extends MsPacMan_IntermissionScene3_Controller {

		public SceneController(PacManGameController gameController) {
			super(gameController);
		}

		@Override
		public void playIntermissionSound() {
			sounds.play(PacManGameSound.INTERMISSION_3);
		}

		@Override
		public void playFlapAnimation() {
			flap2D.getAnimation().restart();
		}
	}

	private SceneController sceneController;
	private Player2D msPacMan2D;
	private Player2D pacMan2D;
	private Flap2D flap2D;
	private Stork2D stork2D;
	private JuniorBag2D bag2D;

	public MsPacMan_IntermissionScene3(PacManGameController controller, Dimension size) {
		super(controller, size, PacManGameUI_Swing.RENDERING_MS_PACMAN, PacManGameUI_Swing.SOUND.get(MS_PACMAN));
	}

	@Override
	public void init() {
		sceneController = new SceneController(gameController);
		sceneController.init();
		flap2D = new Flap2D(sceneController.flap);
		flap2D.setFont(rendering.getScoreFont());
		flap2D.setAnimation(rendering.createFlapAnimation());
		msPacMan2D = new Player2D(sceneController.msPacMan);
		msPacMan2D.setMunchingAnimations(rendering.createPlayerMunchingAnimations());
		pacMan2D = new Player2D(sceneController.pacMan);
		pacMan2D.setMunchingAnimations(rendering.createSpouseMunchingAnimations());
		stork2D = new Stork2D(sceneController.stork);
		stork2D.setAnimation(rendering.createStorkFlyingAnimation());
		stork2D.getAnimation().restart();
		bag2D = new JuniorBag2D(sceneController.bag);
		bag2D.setBlueBag(rendering.getBlueBag());
		bag2D.setJunior(rendering.getJunior());
	}

	@Override
	public void update() {
		sceneController.update();
	}

	@Override
	public void end() {
	}

	@Override
	public void render(Graphics2D g) {
		flap2D.render(g);
		msPacMan2D.render(g);
		pacMan2D.render(g);
		stork2D.render(g);
		bag2D.render(g);
	}
}