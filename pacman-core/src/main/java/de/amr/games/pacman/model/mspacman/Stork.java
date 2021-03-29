package de.amr.games.pacman.model.mspacman;

import de.amr.games.pacman.model.common.GameEntity;
import de.amr.games.pacman.ui.animation.TimedSequence;

public class Stork extends GameEntity {

	public TimedSequence<?> flying;

	@Override
	public void move() {
		super.move();
		flying.animate();
	}

}