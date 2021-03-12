package de.amr.games.pacman.ui.swing.app;

import static java.awt.EventQueue.invokeLater;

import de.amr.games.pacman.controller.PacManGameController;
import de.amr.games.pacman.model.common.GameType;
import de.amr.games.pacman.ui.swing.PacManGameUI_Swing;

/**
 * The Pac-Man game Swing application variant. Runs the game loop in its own thread.
 * 
 * @author Armin Reichert
 */
public class PacManGameAppSwing {

	/**
	 * Starts the Pac-Man game application.
	 * 
	 * @param args command-line arguments
	 *             <ul>
	 *             <li><code>-height</code> <em>value</em>: height of UI, default is 600</li>
	 *             <li><code>-pacman</code>: start in Pac-Man mode</li>
	 *             <li><code>-mspacman</code>: start in Ms. Pac-Man mode</li>
	 *             </ul>
	 */
	public static void main(String[] args) {
		Options options = new Options(args);
		PacManGameController controller = new PacManGameController(options.pacman ? GameType.PACMAN : GameType.MS_PACMAN);
		GameLoop gameLoop = new GameLoop(controller);
		invokeLater(() -> {
			controller.userInterface = new PacManGameUI_Swing(gameLoop, controller, options.height);
			gameLoop.start();
		});
	}
}