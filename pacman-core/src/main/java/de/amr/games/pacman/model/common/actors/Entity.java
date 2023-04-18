/*
MIT License

Copyright (c) 2021-2023 Armin Reichert

Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files (the "Software"), to deal
in the Software without restriction, including without limitation the rights
to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
copies of the Software, and to permit persons to whom the Software is
furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all
copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
SOFTWARE.
 */
package de.amr.games.pacman.model.common.actors;

import static de.amr.games.pacman.model.common.Validator.checkNotNull;
import static de.amr.games.pacman.model.common.world.World.HTS;
import static de.amr.games.pacman.model.common.world.World.TS;
import static de.amr.games.pacman.model.common.world.World.tileAt;

import de.amr.games.pacman.lib.math.Vector2f;
import de.amr.games.pacman.lib.math.Vector2i;

/**
 * Base class for all "entities" used inside the game, e.g. creatures and bonus entities.
 * 
 * @author Armin Reichert
 */
public class Entity {

	protected boolean visible;
	protected Vector2f position;
	protected Vector2f velocity;
	protected Vector2f acceleration;

	public Entity() {
		visible = false;
		position = Vector2f.ZERO;
		velocity = Vector2f.ZERO;
		acceleration = Vector2f.ZERO;
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
	 * @return Entity position. This is the upper left corner of the entity collision box which is a square of size one
	 *         tile.
	 */
	public Vector2f position() {
		return position;
	}

	public void setPosition(float x, float y) {
		position = new Vector2f(x, y);
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
		velocity = new Vector2f(vx, vy);
	}

	public Vector2f acceleration() {
		return acceleration;
	}

	public void setAcceleration(Vector2f acceleration) {
		checkNotNull(acceleration, "Acceleration of entity must not be null");
		this.acceleration = acceleration;
	}

	public void setAcceleration(float ax, float ay) {
		acceleration = new Vector2f(ax, ay);
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
		var tileOrigin = new Vector2f(tile.x() * TS, tile.y() * TS);
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