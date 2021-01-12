package de.amr.games.pacman;

import static java.awt.EventQueue.invokeLater;

import de.amr.games.pacman.core.PacManGame;
import de.amr.games.pacman.ui.PacManGameUI;
import de.amr.games.pacman.ui.swing.PacManGameSwingUI;
import de.amr.games.pacman.worlds.classic.PacManClassicWorld;
import de.amr.games.pacman.worlds.mspacman.MsPacManWorld;

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
			game.setWorld(new PacManClassicWorld());
		} else if ("mspacman".equals(variant)) {
			MsPacManWorld msPacmanWorld = new MsPacManWorld();
			msPacmanWorld.setMapIndex(1);
			game.setWorld(msPacmanWorld);
		}
		invokeLater(() -> {
			PacManGameUI ui = new PacManGameSwingUI(game.world.sizeInTiles(), scaling);
			ui.setGame(game);
			game.start();
		});
	}
}