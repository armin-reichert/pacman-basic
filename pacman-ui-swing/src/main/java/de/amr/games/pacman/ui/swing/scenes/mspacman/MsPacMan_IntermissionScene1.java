package de.amr.games.pacman.ui.swing.scenes.mspacman;

import static de.amr.games.pacman.model.common.GameVariant.MS_PACMAN;

import java.awt.Dimension;
import java.awt.Graphics2D;

import de.amr.games.pacman.controller.PacManGameController;
import de.amr.games.pacman.ui.animation.PacManGameAnimations2D;
import de.amr.games.pacman.ui.animation.TimedSequence;
import de.amr.games.pacman.ui.mspacman.MsPacMan_IntermissionScene1_Controller;
import de.amr.games.pacman.ui.sound.PacManGameSound;
import de.amr.games.pacman.ui.swing.PacManGameUI_Swing;
import de.amr.games.pacman.ui.swing.rendering.common.Player2D;
import de.amr.games.pacman.ui.swing.rendering.mspacman.MsPacManGameRendering;
import de.amr.games.pacman.ui.swing.scenes.common.GameScene;

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

	private class SceneController extends MsPacMan_IntermissionScene1_Controller {

		public SceneController(PacManGameController gameController, PacManGameAnimations2D animations) {
			super(gameController, animations);
		}

		@Override
		public void playIntermissionSound() {
			sounds.loop(PacManGameSound.INTERMISSION_1, 1);
		}
	}

	private SceneController sceneController;
	private Player2D msPacMan2D;
	private Player2D pacMan2D;

	public MsPacMan_IntermissionScene1(PacManGameController controller, Dimension size) {
		super(controller, size, PacManGameUI_Swing.RENDERING_MS_PACMAN, PacManGameUI_Swing.SOUND.get(MS_PACMAN));
	}

	@Override
	public void start() {
		sceneController = new SceneController(gameController, rendering);
		sceneController.start();
		msPacMan2D = new Player2D(sceneController.msPac);
		msPacMan2D.setMunchingAnimations(rendering.createPlayerMunchingAnimations());
		msPacMan2D.getMunchingAnimations().values().forEach(TimedSequence::restart);
		pacMan2D = new Player2D(sceneController.pacMan);
		pacMan2D.setMunchingAnimations(rendering.createSpouseMunchingAnimations());
		pacMan2D.getMunchingAnimations().values().forEach(TimedSequence::restart);
	}

	@Override
	public void update() {
		sceneController.update();
	}

	@Override
	public void render(Graphics2D g) {
		MsPacManGameRendering r = (MsPacManGameRendering) rendering;
		r.drawFlap(g, sceneController.flap);
		msPacMan2D.render(g);
		pacMan2D.render(g);
		r.drawGhost(g, sceneController.inky, false);
		r.drawGhost(g, sceneController.pinky, false);
		r.drawHeart(g, sceneController.heart);
	}
}