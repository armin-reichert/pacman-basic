/*
MIT License

Copyright (c) 2021-22 Armin Reichert

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

import static de.amr.games.pacman.model.common.world.World.HTS;
import static de.amr.games.pacman.model.common.world.World.originOfTile;
import static de.amr.games.pacman.model.common.world.World.tileAt;

import java.util.Objects;

import de.amr.games.pacman.lib.Vector2d;
import de.amr.games.pacman.lib.Vector2i;

/**
 * Base class for all "entities" that are part of the game.
 * 
 * @author Armin Reichert
 */
public class Entity {

	protected boolean visible;
	protected Vector2d position;
	protected Vector2d velocity;
	protected Vector2d acceleration;

	public Entity() {
		visible = false;
		position = Vector2d.ZERO;
		velocity = Vector2d.ZERO;
		acceleration = Vector2d.ZERO;
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

	public Vector2d position() {
		return position;
	}

	public void setPosition(double x, double y) {
		position = new Vector2d(x, y);
	}

	public void setPosition(Vector2d position) {
		this.position = Objects.requireNonNull(position, "Position of entity must not be null");
	}

	public Vector2d velocity() {
		return velocity;
	}

	public void setVelocity(Vector2d velocity) {
		this.velocity = Objects.requireNonNull(velocity, "Velocity of entity must not be null");
	}

	public void setVelocity(double vx, double vy) {
		velocity = new Vector2d(vx, vy);
	}

	public Vector2d acceleration() {
		return acceleration;
	}

	public void setAcceleration(Vector2d acceleration) {
		this.acceleration = Objects.requireNonNull(acceleration, "Acceleration of entity must not be null");
	}

	public void setAcceleration(double ax, double ay) {
		acceleration = new Vector2d(ax, ay);
	}

	/**
	 * Moves this entity by its current velocity and increases its velocity by its current acceleration.
	 */
	public void move() {
		position = position.plus(velocity);
		velocity = velocity.plus(acceleration);
	}

	/** Tile containing the center of the "bounding box" */
	public Vector2i tile() {
		return tileAt(position.x() + HTS, position.y() + HTS);
	}

	public boolean sameTile(Entity other) {
		Objects.requireNonNull(other, "Entity must not be null");
		return tile().equals(other.tile());
	}

	/** Center of "bounding box" (position stores upper left corner of bounding box). */
	public Vector2d center() {
		return position.plus(HTS, HTS);
	}

	/** Offset: (0, 0) if centered, range: [-4, +4) */
	public Vector2d offset() {
		return position.minus(originOfTile(tile()));
	}
}