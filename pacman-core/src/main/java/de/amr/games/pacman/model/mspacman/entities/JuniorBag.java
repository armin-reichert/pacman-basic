package de.amr.games.pacman.model.mspacman.entities;

import de.amr.games.pacman.lib.V2d;
import de.amr.games.pacman.model.common.GameEntity;

/**
 * Blue bag dropped by the stork in intermission scene 3, contains Pac-Man junior.
 * 
 * @author Armin Reichert
 */
public class JuniorBag extends GameEntity {

	static final V2d GRAVITY = new V2d(0, 0.04);

	/** Bag is hold by stork in its beak */
	public boolean hold;

	/** Bag is open an shows Pac-Man baby */
	public boolean open;

	public JuniorBag() {
		hold = true;
		open = false;
	}

	public void move() {
		position = position.plus(velocity);
		if (!hold) {
			velocity = velocity.plus(GRAVITY);
		}
	}
}