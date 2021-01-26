package de.amr.games.pacman;

import static java.awt.EventQueue.invokeLater;
import static java.lang.Float.parseFloat;

import de.amr.games.pacman.game.core.PacManGameController;
import de.amr.games.pacman.game.worlds.PacManGameWorld;
import de.amr.games.pacman.ui.api.PacManGameUI;
import de.amr.games.pacman.ui.swing.PacManGameSwingUI;

/**
 * The Pac-Man game app.
 * 
 * @author Armin Reichert
 */
public class PacManGameApp {

	public static void main(String[] args) {
		float scaling = args.length > 1 ? parseFloat(args[1]) : 2;
		invokeLater(() -> {
			PacManGameController gameController = new PacManGameController();
			gameController.initMsPacManGame();
			PacManGameWorld world = gameController.game.world;
			PacManGameUI ui = new PacManGameSwingUI(world.xTiles(), world.yTiles(), scaling);
			ui.setController(gameController);
			ui.openWindow();
			new Thread(gameController, "PacManGame").start();
		});
	}
}