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
package de.amr.games.pacman.model.common.world;

import java.util.Objects;

import de.amr.games.pacman.lib.Direction;
import de.amr.games.pacman.lib.V2i;
import de.amr.games.pacman.model.common.actors.Creature;

/**
 * A portal is a tunnel end that is connected to the tunnel end on the opposite side of the world.
 * 
 * @author Armin Reichert
 */
public class Portal {

	public final V2i left; // (-1, y)
	public final V2i right; // (world.numCols(), y)

	public Portal(V2i left, V2i right) {
		this.left = left;
		this.right = right;
	}

	public boolean attractsGuy(Creature guy) {
		var guyTile = guy.tile();
		return guyTile.equals(left) && guy.moveDir() == Direction.LEFT
				|| guyTile.equals(right) && guy.moveDir() == Direction.RIGHT;
	}

	public void teleport(Creature guy) {
		if (attractsGuy(guy)) {
			guy.placeAtTile(guy.moveDir() == Direction.LEFT ? right : left, 0, 0);
		}
	}

	@Override
	public int hashCode() {
		return Objects.hash(left, right);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Portal other = (Portal) obj;
		return Objects.equals(left, other.left) && Objects.equals(right, other.right);
	}
}