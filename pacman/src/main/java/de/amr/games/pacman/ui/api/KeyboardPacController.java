package de.amr.games.pacman.ui.api;

import static de.amr.games.pacman.lib.Direction.DOWN;
import static de.amr.games.pacman.lib.Direction.LEFT;
import static de.amr.games.pacman.lib.Direction.RIGHT;
import static de.amr.games.pacman.lib.Direction.UP;

import de.amr.games.pacman.game.core.PacManGame;
import de.amr.games.pacman.game.creatures.Pac;

public class KeyboardPacController implements Runnable {

	private final Pac pac;
	private final PacManGameUI ui;

	public KeyboardPacController(PacManGameUI ui, PacManGame game) {
		this.ui = ui;
		this.pac = game.pac;
	}

	@Override
	public void run() {
		if (ui.keyPressed("left")) {
			pac.wishDir = LEFT;
		} else if (ui.keyPressed("right")) {
			pac.wishDir = RIGHT;
		} else if (ui.keyPressed("up")) {
			pac.wishDir = UP;
		} else if (ui.keyPressed("down")) {
			pac.wishDir = DOWN;
		}
	}
}