package de.amr.games.pacman;

import static de.amr.games.pacman.worlds.PacManGameWorld.t;
import static java.awt.EventQueue.invokeLater;

import java.awt.Dimension;

import de.amr.games.pacman.core.PacManGame;
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
		invokeLater(() -> {
			PacManGameWorld world = new PacManClassicWorld();
			PacManGame game = new PacManGame();
			game.setWorld(world);
			PacManGameSwingUI ui = new PacManGameSwingUI(new Dimension(t(world.size().x), t(world.size().y)), scaling);
			ui.setGame(game);
			game.start();
		});
	}
}
