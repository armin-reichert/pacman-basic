package de.amr.games.pacman;

import static de.amr.games.pacman.lib.Logging.log;
import static java.awt.EventQueue.invokeLater;

import de.amr.games.pacman.controller.GameVariant;
import de.amr.games.pacman.controller.PacManGameController;
import de.amr.games.pacman.heaven.God;
import de.amr.games.pacman.ui.swing.PacManGameSwingUI;

/**
 * The Pac-Man game app.
 * 
 * @author Armin Reichert
 */
public class PacManGameApp {

	static class Options {

		float scaling = 2;
		GameVariant gameVariant = GameVariant.MS_PACMAN;

		Options(String[] strings) {
			int i = 0;
			while (i < strings.length) {
				String s = strings[i];
				if ("-pacman".equals(s)) {
					gameVariant = GameVariant.CLASSIC;
					++i;
					continue;
				}
				if ("-mspacman".equals(s)) {
					gameVariant = GameVariant.MS_PACMAN;
					++i;
					continue;
				}
				if ("-scaling".equals(s)) {
					if (i == strings.length - 1) {
						log("Error parsing options: missing scaling value.");
						++i;
						continue;
					}
					++i;
					s = strings[i];
					try {
						scaling = Float.parseFloat(s);
					} catch (NumberFormatException x) {
						log("Error parsing options: '%s' is no legal scaling value.", s);
					}
					++i;
					continue;
				}
				log("Error parsing options: Unknown option '%s'", s);
				++i;
			}
		}
	}

	private PacManGameController controller;

	public PacManGameApp(GameVariant gameVariant) {
		controller = new PacManGameController();
		controller.selectGame(gameVariant);
	}

	private void gameLoop() {
		while (true)
			God.clock.tick(controller::step);
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
		PacManGameApp app = new PacManGameApp(options.gameVariant);
		invokeLater(() -> {
			app.controller.game().ifPresent(game -> {
				PacManGameSwingUI ui = new PacManGameSwingUI(app.controller, game.level.world.xTiles(),
						game.level.world.yTiles(), options.scaling);
				ui.updateGame(game);
				ui.show();
				new Thread(app::gameLoop, "PacManGame").start();
			});
		});
	}
}