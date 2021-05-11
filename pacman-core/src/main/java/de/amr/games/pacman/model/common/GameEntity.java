package de.amr.games.pacman.model.common;

import de.amr.games.pacman.lib.V2d;

/**
 * Entity base class.
 * 
 * @author Armin Reichert
 */
public class GameEntity {

	protected boolean visible = false;

	protected V2d position = V2d.NULL;

	protected V2d velocity = V2d.NULL;

	public void setVisible(boolean visible) {
		this.visible = visible;
	}

	public boolean isVisible() {
		return visible;
	}

	public V2d position() {
		return position;
	}

	public void setPosition(V2d position) {
		this.position = position;
	}

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