package de.amr.games.pacman.model.common;

import de.amr.games.pacman.ui.animation.Animation;

public class Stork extends GameEntity {

	public Animation<?> flying;

	@Override
	public void move() {
		super.move();
		flying.animate();
	}

}