package de.amr.games.pacman;

import java.awt.EventQueue;

import de.amr.games.pacman.ui.swing.PacManGameSwingUI;

/**
 * The Pac-Man game app.
 * 
 * @author Armin Reichert
 */
public class PacManGameApp {

	public static void main(String[] args) {
		PacManGame game = new PacManGame();
		EventQueue.invokeLater(() -> {
			game.ui = new PacManGameSwingUI(game, 2);
			game.ui.show();
			new Thread(game, "GameLoop").start();
		});
	}
}
