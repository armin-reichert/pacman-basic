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
package de.amr.games.pacman.model.common;

import static de.amr.games.pacman.model.common.Ghost.ORANGE_GHOST;
import static de.amr.games.pacman.model.common.Ghost.RED_GHOST;

import java.util.List;

import de.amr.games.pacman.lib.Direction;
import de.amr.games.pacman.lib.V2i;

/**
 * Ghost behaviors ("AI").
 * 
 * @author Armin Reichert
 */
public interface GhostBehaviors {

	public static V2i chaseShadow(Pac player, List<Ghost> ghosts) {
		return player.tile();
	}

	public static V2i chaseSpeedy(Pac player, List<Ghost> ghosts) {
		return player.moveDir() != Direction.UP //
				? player.tilesAhead(4)
				: player.tilesAhead(4).plus(-4, 0);
	}

	public static V2i chaseBashful(Pac player, List<Ghost> ghosts) {
		return player.moveDir() != Direction.UP //
				? player.tilesAhead(2).scaled(2).minus(ghosts.get(RED_GHOST).tile())
				: player.tilesAhead(2).plus(-2, 0).scaled(2).minus(ghosts.get(RED_GHOST).tile());
	}

	public static V2i chasePokey(Pac player, List<Ghost> ghosts) {
		return ghosts.get(ORANGE_GHOST).tile().euclideanDistance(player.tile()) < 8 ? ghosts.get(ORANGE_GHOST).scatterTarget
				: player.tile();
	}
}