package de.amr.games.pacman.ui.swing.scenes.mspacman;

import static de.amr.games.pacman.model.common.GameVariant.MS_PACMAN;

import java.awt.Dimension;
import java.awt.Graphics2D;

import de.amr.games.pacman.controller.PacManGameController;
import de.amr.games.pacman.lib.TimedSequence;
import de.amr.games.pacman.ui.PacManGameSound;
import de.amr.games.pacman.ui.mspacman.MsPacMan_IntermissionScene1_Controller;
import de.amr.games.pacman.ui.swing.PacManGameUI_Swing;
import de.amr.games.pacman.ui.swing.rendering.common.Ghost2D;
import de.amr.games.pacman.ui.swing.rendering.common.Player2D;
import de.amr.games.pacman.ui.swing.rendering.mspacman.Flap2D;
import de.amr.games.pacman.ui.swing.rendering.mspacman.Heart2D;
import de.amr.games.pacman.ui.swing.scenes.common.GameScene;

/**
 * Intermission scene 1: "They meet".
 * <p>
 * Pac-Man leads Inky and Ms. Pac-Man leads Pinky. Soon, the two Pac-Men are
 * about to collide, they quickly move upwards, causing Inky and Pinky to
 * collide and vanish. Finally, Pac-Man and Ms. Pac-Man face each other at the
 * top of the screen and a big pink heart appears above them. (Played after
 * round 2)
 * 
 * @author Armin Reichert
 */
public class MsPacMan_IntermissionScene1 extends GameScene {

	private class SceneController extends MsPacMan_IntermissionScene1_Controller {

		public SceneController(PacManGameController gameController) {
			super(gameController);
		}

		@Override
		public void playIntermissionSound() {
			sounds.loop(PacManGameSound.INTERMISSION_1, 1);
		}

		@Override
		public void playFlapAnimation() {
			flap2D.getAnimation().restart();
		}
	}

	private SceneController sceneController;
	private Player2D msPacMan2D;
	private Player2D pacMan2D;
	private Ghost2D inky2D;
	private Ghost2D pinky2D;
	private Flap2D flap2D;
	private Heart2D heart2D;

	public MsPacMan_IntermissionScene1(PacManGameController controller, Dimension size) {
		super(controller, size, PacManGameUI_Swing.RENDERING_MS_PACMAN, PacManGameUI_Swing.SOUND.get(MS_PACMAN));
	}

	@Override
	public void init() {
		sceneController = new SceneController(gameController);
		sceneController.init();
		flap2D = new Flap2D(sceneController.flap);
		flap2D.setFont(rendering.getScoreFont());
		flap2D.setAnimation(rendering.createFlapAnimation());
		msPacMan2D = new Player2D(sceneController.msPac);
		msPacMan2D.setMunchingAnimations(rendering.createPlayerMunchingAnimations());
		msPacMan2D.getMunchingAnimations().values().forEach(TimedSequence::restart);
		pacMan2D = new Player2D(sceneController.pacMan);
		pacMan2D.setMunchingAnimations(rendering.createSpouseMunchingAnimations());
		pacMan2D.getMunchingAnimations().values().forEach(TimedSequence::restart);
		inky2D = new Ghost2D(sceneController.inky);
		inky2D.setKickingAnimations(rendering.createGhostKickingAnimations(inky2D.ghost.id));
		inky2D.getKickingAnimations().values().forEach(TimedSequence::restart);
		pinky2D = new Ghost2D(sceneController.pinky);
		pinky2D.setKickingAnimations(rendering.createGhostKickingAnimations(pinky2D.ghost.id));
		pinky2D.getKickingAnimations().values().forEach(TimedSequence::restart);
		heart2D = new Heart2D(sceneController.heart);
		heart2D.setImage(rendering.getHeart());
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
		inky2D.render(g);
		pinky2D.render(g);
		heart2D.render(g);
	}
}