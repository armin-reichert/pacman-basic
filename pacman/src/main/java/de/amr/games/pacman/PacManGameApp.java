package de.amr.games.pacman;

import static java.awt.EventQueue.invokeLater;
import static java.lang.Float.parseFloat;

import de.amr.games.pacman.core.GameVariant;
import de.amr.games.pacman.core.PacManGame;
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
			PacManGame game = new PacManGame(GameVariant.CLASSIC);
			PacManGameUI ui = new PacManGameSwingUI(game.world.sizeInTiles(), scaling);
			ui.setGame(game);
			ui.showWindow();
			game.start();
		});
	}
}