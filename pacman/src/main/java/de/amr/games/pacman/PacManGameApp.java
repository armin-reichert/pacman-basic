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
		String variant = args.length > 0 ? args[0] : "classic";
		float scaling = args.length > 1 ? Float.parseFloat(args[1]) : 2;
		PacManGame game = new PacManGame();
		if ("classic".equals(variant)) {
			game.setVariant(GameVariant.CLASSIC);
		} else if ("mspacman".equals(variant)) {
			game.setVariant(GameVariant.MS_PACMAN);
		}
		invokeLater(() -> {
			PacManGameUI ui = new PacManGameSwingUI(game.world.sizeInTiles(), scaling);
			ui.setGame(game);
			game.start();
		});
	}
}