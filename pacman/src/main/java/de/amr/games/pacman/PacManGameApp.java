package de.amr.games.pacman;

import static java.awt.EventQueue.invokeLater;

import de.amr.games.pacman.core.GameVariant;
import de.amr.games.pacman.core.PacManGame;
import de.amr.games.pacman.ui.PacManGameUI;
import de.amr.games.pacman.ui.swing.PacManGameSwingUI;

/**
 * The Pac-Man game app.
 * 
 * @author Armin Reichert
 */
public class PacManGameApp {

	public static void main(String[] args) {
		GameVariant variant = GameVariant.CLASSIC;
		if (args.length > 0) {
			if ("classic".equals(args[0])) {
				variant = GameVariant.CLASSIC;
			} else if ("mspacman".equals(args[0])) {
				variant = GameVariant.MS_PACMAN;
			}
		}
		float scaling = args.length > 1 ? Float.parseFloat(args[1]) : 2;
		PacManGame game = new PacManGame();
		game.setVariant(variant);
		invokeLater(() -> {
			PacManGameUI ui = new PacManGameSwingUI(game.world.sizeInTiles(), scaling);
			ui.setGame(game);
			game.start();
		});
	}
}