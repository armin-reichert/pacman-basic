package de.amr.games.pacman.model.common;

import de.amr.games.pacman.lib.V2d;

/**
 * Entity base class.
 * 
 * @author Armin Reichert
 */
public class GameEntity {

	/** If the creature is drawn on the screen. */
	public boolean visible = false;

	/** Left upper corner of TSxTS collision box. Sprites can be larger. */
	public V2d position = V2d.NULL;

	/** Velocity vector. */
	protected V2d velocity = null;

	/** Sets the entity's position. */
	public void setPosition(double x, double y) {
		position = new V2d(x, y);
	}

	/** Sets the position relative to another entity's position */
	public void setPositionRelativeTo(GameEntity e, double dx, double dy) {
		setPosition(e.position.x + dx, e.position.y + dy);
	}

	public void setVelocity(V2d velocity) {
		this.velocity = velocity;
	}

	public V2d getVelocity() {
		return velocity;
	}
}