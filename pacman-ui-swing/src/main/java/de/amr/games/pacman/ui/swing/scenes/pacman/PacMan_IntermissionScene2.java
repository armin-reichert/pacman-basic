package de.amr.games.pacman.ui.swing.scenes.pacman;

import static de.amr.games.pacman.model.common.GameVariant.PACMAN;
import static de.amr.games.pacman.model.world.PacManGameWorld.t;

import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;

import de.amr.games.pacman.controller.PacManGameController;
import de.amr.games.pacman.lib.Direction;
import de.amr.games.pacman.lib.TimedSequence;
import de.amr.games.pacman.lib.V2d;
import de.amr.games.pacman.ui.PacManGameSound;
import de.amr.games.pacman.ui.pacman.PacMan_IntermissionScene2_Controller;
import de.amr.games.pacman.ui.swing.PacManGameUI_Swing;
import de.amr.games.pacman.ui.swing.rendering.common.Ghost2D;
import de.amr.games.pacman.ui.swing.rendering.common.Player2D;
import de.amr.games.pacman.ui.swing.rendering.pacman.PacManGameRendering;
import de.amr.games.pacman.ui.swing.scenes.common.GameScene;

/**
 * Second intermission scene: Blinky pursues Pac but kicks a nail that tears his dress apart.
 * 
 * @author Armin Reichert
 */
public class PacMan_IntermissionScene2 extends GameScene {

	private class SceneController extends PacMan_IntermissionScene2_Controller {

		public SceneController(PacManGameController gameController) {
			super(gameController);
		}

		@Override
		public void playIntermissionSound() {
			sounds.play(PacManGameSound.INTERMISSION_2);
		}
	}

	private SceneController sceneController;
	private Player2D pacMan2D;
	private Ghost2D blinky2D;
	private TimedSequence<BufferedImage> blinkyStretchedAnimation;
	private TimedSequence<BufferedImage> blinkyDamagedAnimation;

	public PacMan_IntermissionScene2(PacManGameController controller, Dimension size) {
		super(controller, size, PacManGameUI_Swing.RENDERING_PACMAN, PacManGameUI_Swing.SOUND.get(PACMAN));
	}

	@Override
	public void init() {
		sceneController = new SceneController(gameController);
		sceneController.init();
		pacMan2D = new Player2D(sceneController.pac);
		pacMan2D.setMunchingAnimations(rendering.createPlayerMunchingAnimations());
		pacMan2D.getMunchingAnimations().values().forEach(TimedSequence::restart);
		blinky2D = new Ghost2D(sceneController.blinky);
		blinky2D.setKickingAnimations(rendering.createGhostKickingAnimations(blinky2D.ghost.id));
		blinky2D.getKickingAnimations().values().forEach(TimedSequence::restart);
		blinkyStretchedAnimation = rendering.createBlinkyStretchedAnimation();
		blinkyDamagedAnimation = rendering.createBlinkyDamagedAnimation();
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
		PacManGameRendering r = (PacManGameRendering) rendering;
		r.drawLevelCounter(g, gameController.game(), t(25), t(34));
		r.drawNail(g, sceneController.nail);
		pacMan2D.render(g);
		if (sceneController.nailDistance() < 0) {
			blinky2D.render(g);
		} else {
			drawBlinkyStretched(g, sceneController.nail.position, sceneController.nailDistance() / 4);
		}
	}

	private void drawBlinkyStretched(Graphics2D g, V2d nailPosition, int stretching) {
		BufferedImage stretchedDress = blinkyStretchedAnimation.frame(stretching);
		g.drawImage(stretchedDress, (int) (nailPosition.x - 4), (int) (nailPosition.y - 4), null);
		if (stretching < 3) {
			blinky2D.render(g);
		} else {
			BufferedImage blinkyDamaged = blinkyDamagedAnimation.frame(blinky2D.ghost.dir() == Direction.UP ? 0 : 1);
			g.drawImage(blinkyDamaged, (int) (blinky2D.ghost.position.x - 4), (int) (blinky2D.ghost.position.y - 4), null);
		}
	}
}