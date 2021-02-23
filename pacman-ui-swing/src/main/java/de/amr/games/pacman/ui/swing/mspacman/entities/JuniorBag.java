package de.amr.games.pacman.ui.swing.mspacman.entities;

import de.amr.games.pacman.model.guys.GameEntity;

public class JuniorBag extends GameEntity {

	public boolean released;
	public boolean open;
	public int bounces;

	@Override
	public void move() {
		if (released) {
			velocity = velocity.sum(0, 0.04f);
		}
		super.move();
	}
}