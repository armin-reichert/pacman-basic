package de.amr.games.pacman.model;

import de.amr.games.pacman.lib.V2f;

public class GameEntity {

	/** If the creature is drawn on the screen. */
	public boolean visible = false;

	/** Left upper corner of TSxTS collision box. Sprites can be larger. */
	public V2f position = V2f.NULL;

	/** Velocity vector. */
	public V2f velocity = V2f.NULL;

	/** Move with current velocity. */
	public void move() {
		position = position.sum(velocity);
	}

	/** Sets the entity's position. */
	public void setPosition(double x, double y) {
		position = new V2f((float) x, (float) y);
	}
}