package de.amr.games.pacman;

import static de.amr.games.pacman.lib.Logging.log;
import static java.awt.EventQueue.invokeLater;

import de.amr.games.pacman.controller.PacManGameController;
import de.amr.games.pacman.ui.swing.PacManGameSwingUI;

/**
 * The Pac-Man game app.
 * 
 * @author Armin Reichert
 */
public class PacManGameApp {

	static class Options {

		float scaling = 2;
		boolean classic = false;

		Options(String[] args) {
			int i = -1;
			while (++i < args.length) {
				if ("-pacman".equals(args[i])) {
					classic = true;
					continue;
				}
				if ("-mspacman".equals(args[i])) {
					classic = false;
					continue;
				}
				if ("-scaling".equals(args[i])) {
					if (++i == args.length) {
						log("Error parsing options: missing scaling value.");
						break;
					}
					try {
						scaling = Float.parseFloat(args[i]);
					} catch (NumberFormatException x) {
						log("Error parsing options: '%s' is no legal scaling value.", args[i]);
					}
					continue;
				}
				log("Error parsing options: Found garbage '%s'", args[i]);
			}
		}
	}

	/**
	 * Starts the Pac-Man game application.
	 * 
	 * @param args command-line arguments
	 *             <ul>
	 *             <li><code>-scaling</code> <em>value</em>: scaling of UI, default is 2</li>
	 *             <li><code>-pacman</code>: start in Pac-Man mode</li>
	 *             <li><code>-mspacman</code>: start in Ms. Pac-Man mode</li>
	 *             </ul>
	 */
	public static void main(String[] args) {
		Options options = new Options(args);
		invokeLater(() -> {
			PacManGameController controller = new PacManGameController(options.classic);
			controller.setUI(new PacManGameSwingUI(controller.game, options.scaling));
			controller.showUI();
			new Thread(controller::gameLoop, "PacManGame").start();
		});
	}
}