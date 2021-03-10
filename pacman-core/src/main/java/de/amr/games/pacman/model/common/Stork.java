package de.amr.games.pacman.model.common;

import de.amr.games.pacman.lib.Animation;

public class Stork extends GameEntity {

	public Animation<?> flying;

	@Override
	public void move() {
		super.move();
		flying.animate();
	}

}