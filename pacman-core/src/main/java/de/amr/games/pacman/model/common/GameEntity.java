package de.amr.games.pacman.model.common;

import de.amr.games.pacman.lib.V2d;

/**
 * Entity base class.
 * 
 * @author Armin Reichert
 */
public class GameEntity {

	/** If the creature is drawn on the screen. */
	protected boolean visible = false;

	/** Left upper corner of TSxTS collision box. Sprites can be larger. */
	public V2d position = V2d.NULL;

	/** Velocity vector. */
	protected V2d velocity = null;

	public void setVisible(boolean visible) {
		this.visible = visible;
	}

	public boolean isVisible() {
		return visible;
	}

	public void setPosition(V2d position) {
		this.position = position;
	}

	/** Sets the entity's position. */
	public void setPosition(double x, double y) {
		position = new V2d(x, y);
	}

	public void setVelocity(V2d velocity) {
		this.velocity = velocity;
	}

	public V2d getVelocity() {
		return velocity;
	}
}