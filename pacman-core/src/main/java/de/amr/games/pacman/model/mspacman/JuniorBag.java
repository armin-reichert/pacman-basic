package de.amr.games.pacman.model.mspacman;

import de.amr.games.pacman.lib.V2d;
import de.amr.games.pacman.model.common.Creature;

/**
 * Blue bag dropped by the stork in intermission scene 3, contains Pac-Man junior.
 * 
 * @author Armin Reichert
 */
public class JuniorBag extends Creature {

	static final V2d GRAVITY = new V2d(0, 0.04);

	private V2d gravity = V2d.NULL;
	public boolean open;

	@Override
	public void move() {
		if (velocity != null) {
			position = position.plus(velocity);
			velocity = velocity.plus(gravity);
		}
	}

	public void release() {
		gravity = GRAVITY;
	}
}