package de.amr.games.pacman;

import static de.amr.games.pacman.lib.Logging.log;
import static java.awt.EventQueue.invokeLater;

import de.amr.games.pacman.game.core.PacManGameController;
import de.amr.games.pacman.game.core.PacManGameModel;
import de.amr.games.pacman.game.heaven.God;
import de.amr.games.pacman.ui.swing.PacManGameSwingUI;

/**
 * The Pac-Man game app.
 * 
 * @author Armin Reichert
 */
public class PacManGameApp {

	static class Options {

		float scaling = 2;
		byte variant = PacManGameModel.MS_PACMAN;

		static Options parse(String[] args) {
			Options options = new Options();
			int i = 0;
			while (i < args.length) {
				String arg = args[i];
				if ("-pacman".equals(arg)) {
					options.variant = PacManGameModel.CLASSIC;
					++i;
					continue;
				}
				if ("-mspacman".equals(arg)) {
					options.variant = PacManGameModel.MS_PACMAN;
					++i;
					continue;
				}
				if ("-scaling".equals(arg)) {
					if (i == args.length - 1) {
						log("Error parsing command-line: missing scaling value.");
						++i;
						continue;
					}
					++i;
					arg = args[i];
					try {
						options.scaling = Float.parseFloat(arg);
					} catch (NumberFormatException x) {
						log("Error parsing command-line: '%s' is no legal scaling value.", arg);
					}
					++i;
					continue;
				}
				log("Error parsing command-line: Unknown argument '%s'", arg);
				++i;
			}
			return options;
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
		Options options = Options.parse(args);
		PacManGameController controller = new PacManGameController();
		controller.initGame(options.variant);
		invokeLater(() -> {
			controller.game().ifPresent(game -> {
				PacManGameSwingUI ui = new PacManGameSwingUI(controller, game.world.xTiles(), game.world.yTiles(),
						options.scaling);
				ui.updateGame(game);
				ui.show();
				// start game loop in its own thread
				new Thread(() -> {
					while (true) {
						God.clock.tick(controller::step);
					}
				}, "PacManGame").start();
			});
		});
	}
}