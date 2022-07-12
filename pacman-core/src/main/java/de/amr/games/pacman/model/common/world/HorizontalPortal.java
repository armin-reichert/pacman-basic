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
 * A portal connects two tunnel ends leading out of the map. This kind of portal connects a left tunnel end horizontally
 * with the right tunnel end at the other side of the map.
 * 
 * @author Armin Reichert
 */
public class HorizontalPortal implements Portal {

	private final V2i leftTunnelEnd; // (0, y)
	private final V2i rightTunnelEnd; // (world.numCols() - 1, y)

	public HorizontalPortal(V2i leftTunnelEnd, V2i rightTunnelEnd) {
		this.leftTunnelEnd = leftTunnelEnd;
		this.rightTunnelEnd = rightTunnelEnd;
	}

	public V2i getLeftTunnelEnd() {
		return leftTunnelEnd;
	}

	public V2i getRightTunnelEnd() {
		return rightTunnelEnd;
	}

	@Override
	public boolean contains(V2i tile) {
		return tile.y == leftTunnelEnd.y && (tile.x < leftTunnelEnd.x || tile.x > rightTunnelEnd.x);
	}

	private boolean attractsGuy(Creature guy) {
		return guy.tile().equals(leftTunnelEnd.minus(2, 0)) && guy.moveDir() == Direction.LEFT
				|| guy.tile().equals(rightTunnelEnd.plus(2, 0)) && guy.moveDir() == Direction.RIGHT;
	}

	@Override
	public void teleport(Creature guy) {
		if (attractsGuy(guy)) {
			guy.placeAtTile(guy.moveDir() == Direction.RIGHT ? leftTunnelEnd.minus(2, 0) : rightTunnelEnd.plus(2, 0), 0, 0);
		}
	}

	@Override
	public int hashCode() {
		return Objects.hash(leftTunnelEnd, rightTunnelEnd);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		HorizontalPortal other = (HorizontalPortal) obj;
		return Objects.equals(leftTunnelEnd, other.leftTunnelEnd) && Objects.equals(rightTunnelEnd, other.rightTunnelEnd);
	}
}