package de.amr.games.pacman.model.common;

import static de.amr.games.pacman.world.PacManGameWorld.t;

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
	public V2d velocity = V2d.NULL;

	/** Move with current velocity. */
	public void move() {
		position = position.plus(velocity);
	}

	/** Sets the entity's position. */
	public void setPosition(double x, double y) {
		position = new V2d((float) x, (float) y);
	}

	/** Sets the position given in tile coordinates */
	public void setTilePosition(int col, int row) {
		setPosition(t(col), t(row));
	}

	/** Sets the position relative to another entity's position */
	public void setPositionRelativeTo(GameEntity other, float dx, float dy) {
		setPosition(other.position.x + dx, other.position.y + dy);
	}
}