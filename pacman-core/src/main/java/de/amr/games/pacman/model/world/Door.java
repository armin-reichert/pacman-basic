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

package de.amr.games.pacman.model.world;

import static de.amr.games.pacman.lib.Globals.HTS;
import static de.amr.games.pacman.lib.Globals.TS;
import static de.amr.games.pacman.lib.Globals.checkNotNull;
import static de.amr.games.pacman.lib.Globals.v2f;

import java.util.Objects;

import de.amr.games.pacman.lib.math.Vector2f;
import de.amr.games.pacman.lib.math.Vector2i;

/**
 * @author Armin Reichert
 */
public final class Door {

	private final Vector2i leftWing;
	private final Vector2i rightWing;

	public Door(Vector2i leftWing, Vector2i rightWing) {
		checkNotNull(leftWing);
		checkNotNull(rightWing);
		this.leftWing = leftWing;
		this.rightWing = rightWing;
	}

	public Vector2i leftWing() {
		return leftWing;
	}

	public Vector2i rightWing() {
		return rightWing;
	}

	/**
	 * @param tile some tile
	 * @return tells if the given tile is occupied by this door
	 */
	public boolean occupies(Vector2i tile) {
		return leftWing.equals(tile) || rightWing.equals(tile);
	}

	/**
	 * @return position where ghost can enter the door
	 */
	public Vector2f entryPosition() {
		return v2f(TS * rightWing.x() - HTS, TS * (rightWing.y() - 1));
	}

	@Override
	public int hashCode() {
		return Objects.hash(leftWing, rightWing);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Door other = (Door) obj;
		return Objects.equals(leftWing, other.leftWing) && Objects.equals(rightWing, other.rightWing);
	}
}