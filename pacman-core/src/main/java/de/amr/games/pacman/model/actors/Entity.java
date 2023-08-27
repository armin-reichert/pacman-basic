/*
Copyright (c) 2021-2023 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.games.pacman.model.actors;

import de.amr.games.pacman.lib.Vector2f;
import de.amr.games.pacman.lib.Vector2i;

import static de.amr.games.pacman.lib.Globals.*;
import static de.amr.games.pacman.model.world.World.tileAt;

/**
 * Base class for all "entities" used inside the game, e.g. creatures and bonus entities.
 * 
 * @author Armin Reichert
 */
public class Entity {

	protected String name;
	protected boolean visible = false;
	protected Vector2f position = Vector2f.ZERO;
	protected Vector2f velocity = Vector2f.ZERO;
	protected Vector2f acceleration = Vector2f.ZERO;

	public Entity(String name) {
		this.name = name;
	}

	public Entity() {
		this.name = getClass().getSimpleName() + "@" + hashCode();
	}

	@Override
	public String toString() {
		return "Entity [name=" + name + ", visible=" + visible + ", position=" + position + ", velocity=" + velocity
				+ ", acceleration=" + acceleration + "]";
	}

	/** Readable name, for display and logging purposes. */
	public String name() {
		return name;
	}

	public boolean isVisible() {
		return visible;
	}

	public void setVisible(boolean visible) {
		this.visible = visible;
	}

	public void show() {
		visible = true;
	}

	public void hide() {
		visible = false;
	}

	/**
	 * @return upper left corner of the entity collision box which is a square of size one tile.
	 */
	public Vector2f position() {
		return position;
	}

	public void setPosition(float x, float y) {
		position = v2f(x, y);
	}

	public void setX(float x) {
		position = v2f(x, position.y());
	}

	public void setY(float y) {
		position = v2f(position.x(), y);
	}

	public void setPosition(Vector2f position) {
		checkNotNull(position, "Position of entity must not be null");
		this.position = position;
	}

	/** @return Center position of entity collision box (position property stores *upper left corner* of box). */
	public Vector2f center() {
		return position.plus(HTS, HTS);
	}

	public Vector2f velocity() {
		return velocity;
	}

	public void setVelocity(Vector2f velocity) {
		checkNotNull(velocity, "Velocity of entity must not be null");
		this.velocity = velocity;
	}

	public void setVelocity(float vx, float vy) {
		velocity = v2f(vx, vy);
	}

	public Vector2f acceleration() {
		return acceleration;
	}

	public void setAcceleration(Vector2f acceleration) {
		checkNotNull(acceleration, "Acceleration of entity must not be null");
		this.acceleration = acceleration;
	}

	public void setAcceleration(float ax, float ay) {
		acceleration = v2f(ax, ay);
	}

	/**
	 * Moves this entity by its current velocity and increases its velocity by its current acceleration.
	 */
	public void move() {
		position = position.plus(velocity);
		velocity = velocity.plus(acceleration);
	}

	/** @return Tile containing the center of the entity collision box. */
	public Vector2i tile() {
		return tileAt(position.x() + HTS, position.y() + HTS);
	}

	/** @return Offset inside current tile: (0, 0) if centered, range: [-4, +4) */
	public Vector2f offset() {
		var tile = tile();
		var tileOrigin = v2f(TS * tile.x(), TS * tile.y());
		return position.minus(tileOrigin);
	}

	/**
	 * @param other some entity
	 * @return <code>true</code> if both entities occupy same tile
	 */
	public boolean sameTile(Entity other) {
		checkNotNull(other, "Entity to check for same tile must not be null");
		return tile().equals(other.tile());
	}
}