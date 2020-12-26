package de.amr.games.pacman;

import static java.awt.EventQueue.invokeLater;

import de.amr.games.pacman.core.Game;
import de.amr.games.pacman.ui.swing.PacManGameSwingUI;

/**
 * The Pac-Man game app.
 * 
 * @author Armin Reichert
 */
public class PacManGameApp {

	public static void main(String[] args) {
		float scaling = args.length > 0 ? Float.parseFloat(args[0]) : 2;
		invokeLater(() -> {
			Game game = new Game();
			game.ui = new PacManGameSwingUI(game, scaling);
			game.start();
		});
	}
}
