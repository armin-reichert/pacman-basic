package de.amr.games.pacman;

import static java.awt.EventQueue.invokeLater;
import static java.lang.Float.parseFloat;

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
		GameVariant variant = args.length > 0 ? parseGameVariant(args[0]) : GameVariant.CLASSIC;
		float scaling = args.length > 1 ? parseFloat(args[1]) : 2;
		invokeLater(() -> {
			PacManGame game = new PacManGame(variant);
			PacManGameUI ui = new PacManGameSwingUI(game.world.sizeInTiles(), scaling);
			ui.setGame(game);
			game.start();
		});
	}

	private static GameVariant parseGameVariant(String spec) {
		GameVariant variant = GameVariant.CLASSIC;
		if ("classic".equals(spec)) {
			variant = GameVariant.CLASSIC;
		} else if ("mspacman".equals(spec)) {
			variant = GameVariant.MS_PACMAN;
		}
		return variant;
	}
}