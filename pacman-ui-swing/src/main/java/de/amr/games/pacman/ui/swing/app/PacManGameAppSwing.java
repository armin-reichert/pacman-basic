package de.amr.games.pacman.ui.swing.app;

import static de.amr.games.pacman.lib.God.clock;
import static de.amr.games.pacman.lib.Logging.log;
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

	private static class GameLoop {

		private final PacManGameController controller;
		private Thread thread;
		private boolean running;

		public GameLoop(PacManGameController controller) {
			this.controller = controller;
		}

		private void start() {
			if (running) {
				log("Cannot start: Game loop is already running");
				return;
			}
			thread = new Thread(this::run, "GameLoop");
			thread.start();
			running = true;
		}

		private void run() {
			while (running) {
				clock.tick(controller::step);
			}
		}

		private void end() {
			running = false;
			try {
				thread.join();
			} catch (Exception x) {
				x.printStackTrace();
			}
			log("Exit game and terminate VM");
			System.exit(0);
		}

	}

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
		invokeLater(() -> {
			PacManGameUI_Swing ui = new PacManGameUI_Swing(controller, options.height);
			GameLoop gameLoop = new GameLoop(controller);
			ui.addWindowClosingHandler(gameLoop::end);
			controller.setUserInterface(ui);
			ui.show();
			gameLoop.start();
		});
	}
}