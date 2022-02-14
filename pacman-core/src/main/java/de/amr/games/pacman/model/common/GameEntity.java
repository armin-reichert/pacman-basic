/*
MIT License

Copyright (c) 2021 Armin Reichert

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
package de.amr.games.pacman.model.common;

import static de.amr.games.pacman.model.world.World.t;

import de.amr.games.pacman.lib.V2d;
import de.amr.games.pacman.lib.V2i;
import de.amr.games.pacman.model.world.World;

/**
 * Entity base class.
 * 
 * @author Armin Reichert
 */
public class GameEntity {

	public boolean visible = false;

	public V2d position = V2d.NULL;

	public V2d velocity = V2d.NULL;

	/**
	 * @return the current tile position
	 */
	public V2i tile() {
		return World.tile(position);
	}

	/**
	 * @return the current pixel offset
	 */
	public V2d offset() {
		return World.offset(position);
	}

	public void setPosition(double x, double y) {
		position = new V2d(x, y);
	}

	/**
	 * Places the creature at the given tile with the given position offsets. Sets the {@link #newTileEntered} flag to
	 * trigger steering.
	 * 
	 * @param tile    the tile where this creature will be placed
	 * @param offsetX the pixel offset in x-direction
	 * @param offsetY the pixel offset in y-direction
	 */
	public void placeAt(V2i tile, double offsetX, double offsetY) {
		setPosition(t(tile.x) + offsetX, t(tile.y) + offsetY);
	}

	/**
	 * Places the creature on its current tile with given offset. This is for example used to place a ghost exactly
	 * between two tiles like in the initial ghosthouse position.
	 * 
	 * @param offsetX offset in x-direction
	 * @param offsetY offset in y-direction
	 */
	public void setOffset(double offsetX, double offsetY) {
		placeAt(tile(), offsetX, offsetY);
	}

	/**
	 * @param other other creature
	 * @return if both creatures occupy the same tile
	 */
	public boolean meets(GameEntity other) {
		return tile().equals(other.tile());
	}

	public void setVelocity(double vx, double vy) {
		velocity = new V2d(vx, vy);
	}

	public void move() {
		position = position.plus(velocity);
	}

	public void show() {
		visible = true;
	}

	public void hide() {
		visible = false;
	}
}