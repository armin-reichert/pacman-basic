package de.amr.games.pacman;

import static java.awt.EventQueue.invokeLater;

import de.amr.games.pacman.core.PacManGame;
import de.amr.games.pacman.ui.PacManGameUI;
import de.amr.games.pacman.ui.swing.PacManGameSwingUI;
import de.amr.games.pacman.worlds.PacManGameWorld;
import de.amr.games.pacman.worlds.classic.PacManClassicWorld;

/**
 * The Pac-Man game app.
 * 
 * @author Armin Reichert
 */
public class PacManGameApp {

	public static void main(String[] args) {
		float scaling = args.length > 0 ? Float.parseFloat(args[0]) : 2;
		PacManGame game = new PacManGame();
		game.setWorld(new PacManClassicWorld());
		invokeLater(() -> {
			PacManGameUI ui = new PacManGameSwingUI(game.world.sizeInTiles().scaled(PacManGameWorld.TS), scaling);
			ui.setGame(game);
			game.start();
		});
	}
}
