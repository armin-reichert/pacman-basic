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
		Game game = new Game();
		invokeLater(() -> {
			game.ui = new PacManGameSwingUI(game, 2);
			new Thread(game, "GameLoop").start();
		});
	}
}
