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

import static de.amr.games.pacman.model.common.world.World.t;

import java.util.Objects;
import java.util.Optional;

import de.amr.games.pacman.lib.V2d;
import de.amr.games.pacman.lib.V2i;
import de.amr.games.pacman.lib.animation.EntityAnimation;
import de.amr.games.pacman.lib.animation.EntityAnimations;
import de.amr.games.pacman.model.common.world.World;

/**
 * Base class for all entities. Entities can have sprite animations.
 * 
 * @author Armin Reichert
 */
public class Entity {

	protected boolean visible = false;

	protected V2d position = V2d.NULL;

	protected V2d velocity = V2d.NULL;

	protected V2d acceleration = V2d.NULL;

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

	public V2d getPosition() {
		return position;
	}

	public void setPosition(double x, double y) {
		position = new V2d(x, y);
	}

	public void setPosition(V2d position) {
		this.position = position;
	}

	public V2d getVelocity() {
		return velocity;
	}

	public void setVelocity(V2d velocity) {
		this.velocity = velocity;
	}

	public void setVelocity(double vx, double vy) {
		velocity = new V2d(vx, vy);
	}

	public void setAcceleration(V2d acceleration) {
		this.acceleration = acceleration;
	}

	public void setAcceleration(double ax, double ay) {
		acceleration = new V2d(ax, ay);
	}

	public V2d offset() {
		return World.offset(position);
	}

	public void setOffset(double offsetX, double offsetY) {
		var tile = tile();
		position = new V2d(t(tile.x) + offsetX, t(tile.y) + offsetY);
	}

	public V2i tile() {
		return World.tile(position);
	}

	/**
	 * Moves this entity by its current velocity and increases its velocity by its current acceleration.
	 */
	public void move() {
		position = position.plus(velocity);
		velocity = velocity.plus(acceleration);
	}

	/**
	 * @param other another entity
	 * @return if both entities occupy the same tile
	 */
	public boolean sameTile(Entity other) {
		Objects.requireNonNull(other);
		return tile().equals(other.tile());
	}

	// Animations

	private EntityAnimations animations;

	public void setAnimations(EntityAnimations animations) {
		this.animations = animations;
	}

	public Optional<EntityAnimations> animations() {
		return Optional.ofNullable(animations);
	}

	public Optional<EntityAnimation> animation(String key) {
		return animations().map(anim -> anim.byName(key));
	}

	public void selectAnimation(String name) {
		selectAnimation(name, true);
	}

	public void selectAnimation(String name, boolean ensureRunning) {
		if (animations != null) {
			animations.select(name);
			if (ensureRunning) {
				animations.selectedAnimation().ensureRunning();
			}
		}
	}

	public void animate() {
		animations().map(EntityAnimations::selectedAnimation).ifPresent(EntityAnimation::advance);
	}
}