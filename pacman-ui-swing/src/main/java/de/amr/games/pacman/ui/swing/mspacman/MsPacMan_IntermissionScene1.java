package de.amr.games.pacman.ui.swing.mspacman;

import java.awt.Dimension;
import java.awt.Graphics2D;

import de.amr.games.pacman.controller.PacManGameController;
import de.amr.games.pacman.sound.SoundManager;
import de.amr.games.pacman.ui.mspacman.MsPacMan_IntermissionScene1_Controller;
import de.amr.games.pacman.ui.swing.common.GameScene;
import de.amr.games.pacman.ui.swing.rendering.SwingRendering;

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

	private MsPacMan_IntermissionScene1_Controller animation;

	public MsPacMan_IntermissionScene1(PacManGameController controller, Dimension size, SwingRendering rendering,
			SoundManager sounds) {
		super(controller, size, rendering, sounds);
	}

	@Override
	public void start() {
		animation = new MsPacMan_IntermissionScene1_Controller(controller, rendering, sounds);
		animation.start();
	}

	@Override
	public void update() {
		animation.update();
	}

	@Override
	public void render(Graphics2D g) {
		rendering.drawFlap(g, animation.flap);
		rendering.drawPlayer(g, animation.msPac);
		rendering.drawSpouse(g, animation.pacMan);
		rendering.drawGhost(g, animation.inky, false);
		rendering.drawGhost(g, animation.pinky, false);
		rendering.drawHeart(g, animation.heart);
	}
}