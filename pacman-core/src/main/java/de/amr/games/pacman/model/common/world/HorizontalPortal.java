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

import static de.amr.games.pacman.model.common.world.World.TS;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import de.amr.games.pacman.lib.V2d;
import de.amr.games.pacman.lib.V2i;
import de.amr.games.pacman.model.common.actors.Creature;

/**
 * A portal connects two tunnel ends leading out of the map.
 * <p>
 * This kind of portal prolongates the right tunnel end by <code>DEPTH</code> tiles before wrapping with the left part
 * (also <code>DEPTH</code> tiles) of the portal.
 * 
 * @author Armin Reichert
 */
public record HorizontalPortal(V2i leftTunnelEnd, V2i rightTunnelEnd) implements Portal {

	private static final Logger LOGGER = LogManager.getFormatterLogger();

	private static final int DEPTH = 2;

	@Override
	public String toString() {
		return "[Portal left=%s right=%s]".formatted(leftTunnelEnd, rightTunnelEnd());
	}

	@Override
	public boolean teleport(Creature guy) {
		boolean teleported = false;
		var oldPos = guy.position();
		if (guy.tile().y() == leftTunnelEnd.y() && guy.position().x() < (leftTunnelEnd.x() - DEPTH) * TS) {
			guy.placeAtTile(rightTunnelEnd); // TODO fixme
			LOGGER.info("Teleported %s from %s to %s", guy.name(), oldPos, guy.position());
			teleported = true;
		} else if (guy.tile().equals(rightTunnelEnd.plus(DEPTH, 0))) {
			guy.placeAtTile(leftTunnelEnd.minus(DEPTH, 0), 0, 0);
			LOGGER.info("Teleported %s from %s to %s", guy.name(), oldPos, guy.position());
			teleported = true;
		}
		return teleported;
	}

	@Override
	public boolean contains(V2i tile) {
		for (int i = 1; i <= DEPTH; ++i) {
			if (tile.equals(leftTunnelEnd.minus(i, 0))) {
				return true;
			}
			if (tile.equals(rightTunnelEnd.plus(i, 0))) {
				return true;
			}
		}
		return false;
	}

	@Override
	public double distance(Creature guy) {
		var leftEndPosition = new V2d(leftTunnelEnd.minus(DEPTH, 0)).scaled(World.TS);
		var rightEndPosition = new V2d(rightTunnelEnd.plus(DEPTH, 0)).scaled(World.TS);
		var guyPos = guy.position();
		return Math.abs(Math.min(guyPos.euclideanDistance(leftEndPosition), guyPos.euclideanDistance(rightEndPosition)));
	}
}