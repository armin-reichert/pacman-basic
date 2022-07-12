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

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import de.amr.games.pacman.lib.V2d;
import de.amr.games.pacman.lib.V2i;
import de.amr.games.pacman.model.common.actors.Creature;

/**
 * A portal connects two tunnel ends leading out of the map. This kind of portal connects a left tunnel end horizontally
 * with the right tunnel end at the other side of the map.
 * 
 * @author Armin Reichert
 */
public record HorizontalPortal(V2i leftTunnelEnd, V2i rightTunnelEnd) implements Portal {

	private static final Logger LOGGER = LogManager.getFormatterLogger();

	@Override
	public String toString() {
		return "[Portal left=%s right=%s]".formatted(leftTunnelEnd, rightTunnelEnd());
	}

	@Override
	public void teleport(Creature guy) {
		if (guy.tile().equals(leftTunnelEnd.minus(2, 0))) {
			guy.placeAtTile(rightTunnelEnd.plus(2, 0), 0, 0);
		} else if (guy.tile().equals(rightTunnelEnd.plus(2, 0))) {
			guy.placeAtTile(leftTunnelEnd.minus(2, 0), 0, 0);
		}
	}

	@Override
	public boolean contains(V2i tile) {
		return tile.y() == leftTunnelEnd.y() && (tile.x() < leftTunnelEnd.x() || tile.x() > rightTunnelEnd.x());
	}

	@Override
	public double closeness(Creature guy) {
		var leftEnd = new V2d(leftTunnelEnd.minus(2, 0)).scaled(World.TS);
		var rightEnd = new V2d(rightTunnelEnd.plus(2, 0)).scaled(World.TS);
		var guyPos = guy.getPosition();
		var dist = Math.abs(Math.min(guyPos.euclideanDistance(leftEnd), guyPos.euclideanDistance(rightEnd)));
		LOGGER.info("%s, distance of %s is %.2f", this, guy.name, dist);
		if (dist > 8.0) {
			return 1.0;
		}
		return dist / 8.0;
	}
}