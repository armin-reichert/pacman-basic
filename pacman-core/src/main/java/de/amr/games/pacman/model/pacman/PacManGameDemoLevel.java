/*
MIT License

Copyright (c) 2022 Armin Reichert

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
package de.amr.games.pacman.model.pacman;

import static de.amr.games.pacman.lib.steering.NavigationPoint.np;

import java.util.List;

import de.amr.games.pacman.lib.steering.Direction;
import de.amr.games.pacman.lib.steering.NavigationPoint;
import de.amr.games.pacman.lib.steering.RouteBasedSteering;
import de.amr.games.pacman.model.common.GameLevel;
import de.amr.games.pacman.model.common.GameModel;

/**
 * @author Armin Reichert
 */
public class PacManGameDemoLevel extends GameLevel {

	private static final List<NavigationPoint> PACMAN_ROUTE = List.of(np(12, 26), np(9, 26), np(12, 32), np(15, 32),
			np(24, 29), np(21, 23), np(18, 23), np(18, 20), np(18, 17), np(15, 14), np(12, 14), np(9, 17), np(6, 17),
			np(6, 11), np(6, 8), np(6, 4), np(1, 8), np(6, 8), np(9, 8), np(12, 8), np(6, 4), np(6, 8), np(6, 11), np(1, 8),
			np(6, 8), np(9, 8), np(12, 14), np(9, 17), np(6, 17), np(0, 17), np(21, 17), np(21, 23), np(21, 26), np(24, 29),
			/* avoid moving up: */ np(26, 29), np(15, 32), np(12, 32), np(3, 29), np(6, 23), np(9, 23), np(12, 26),
			np(15, 26), np(18, 23), np(21, 23), np(24, 29), /* avoid moving up: */ np(26, 29), np(15, 32), np(12, 32),
			np(3, 29), np(6, 23));

	private static final List<NavigationPoint> GHOST_0_ROUTE = List.of(np(21, 4, Direction.DOWN),
			np(21, 8, Direction.DOWN), np(21, 11, Direction.RIGHT), np(26, 8, Direction.LEFT), np(21, 8, Direction.DOWN),
			np(26, 8, Direction.UP), np(26, 8, Direction.DOWN), np(21, 11, Direction.DOWN), np(21, 17, Direction.RIGHT), // enters

			np(99, 99, Direction.DOWN));

	/**
	 * @param game
	 */
	public PacManGameDemoLevel(GameModel game) {
		super(game, 1);
		var pacSteering = new RouteBasedSteering();
		pacSteering.setRoute(PACMAN_ROUTE);
		pacSteering.init();
		setPacSteering(pacSteering);
	}
}