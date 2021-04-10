package de.amr.games.pacman.ui.swing.app;

import static java.awt.EventQueue.invokeLater;

import de.amr.games.pacman.controller.PacManGameController;
import de.amr.games.pacman.ui.swing.PacManGameUI_Swing;

/**
 * The Pac-Man application.
 * 
 * Command-line arguments:
 * <ul>
 * <li><code>-height</code> &lt;pixels&gt;: Height of UI in pixels (default: 576)</li>
 * <li><code>-pacman</code>: Starts the game in Pac-Man mode</li>
 * <li><code>-mspacman</code>: Starts game in Ms. Pac-Man mode</li>
 * </ul>
 * 
 * @author Armin Reichert
 */
public class PacManGameAppSwing extends PacManGameController {

	public static void main(String[] args) {
		Options options = new Options(args);
		PacManGameAppSwing app = new PacManGameAppSwing();
		app.play(options.gameVariant);
		invokeLater(() -> {
			GameLoop gameLoop = new GameLoop(app);
			app.setUI(new PacManGameUI_Swing(gameLoop, app, options.height));
			gameLoop.start();
		});
	}
}