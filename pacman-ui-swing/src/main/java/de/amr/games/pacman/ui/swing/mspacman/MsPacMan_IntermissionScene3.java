package de.amr.games.pacman.ui.swing.mspacman;

import java.awt.Dimension;
import java.awt.Graphics2D;

import de.amr.games.pacman.controller.PacManGameController;
import de.amr.games.pacman.sound.SoundManager;
import de.amr.games.pacman.ui.mspacman.MsPacMan_IntermissionScene3_Controller;
import de.amr.games.pacman.ui.swing.common.GameScene;
import de.amr.games.pacman.ui.swing.rendering.SwingRendering;

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

	private MsPacMan_IntermissionScene3_Controller animation;

	public MsPacMan_IntermissionScene3(PacManGameController controller, Dimension size, SwingRendering rendering,
			SoundManager sounds) {
		super(controller, size, rendering, sounds);
	}

	@Override
	public void start() {
		animation = new MsPacMan_IntermissionScene3_Controller(controller, rendering, sounds);
		animation.start();
	}

	@Override
	public void update() {
		animation.update();
	}

	@Override
	public void render(Graphics2D g) {
		rendering.drawFlap(g, animation.flap);
		rendering.drawPlayer(g, animation.msPacMan);
		rendering.drawSpouse(g, animation.pacMan);
		rendering.drawStork(g, animation.stork);
		rendering.drawJuniorBag(g, animation.bag);
	}
}