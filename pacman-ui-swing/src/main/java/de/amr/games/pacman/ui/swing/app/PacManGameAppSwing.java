package de.amr.games.pacman.ui.swing.app;

import static java.awt.EventQueue.invokeLater;

import de.amr.games.pacman.controller.PacManGameController;
import de.amr.games.pacman.model.common.GameType;
import de.amr.games.pacman.ui.swing.PacManGameUI_Swing;

/**
 * The Pac-Man game app.
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
		CommandLineArgs options = new CommandLineArgs(args);
		invokeLater(() -> {
			PacManGameController controller = new PacManGameController();
			if (options.pacman) {
				controller.play(GameType.PACMAN);
			} else {
				controller.play(GameType.MS_PACMAN);
			}
			controller.addView(new PacManGameUI_Swing(controller, options.height));
			controller.showViews();
			controller.startGameLoop();
		});
	}
}