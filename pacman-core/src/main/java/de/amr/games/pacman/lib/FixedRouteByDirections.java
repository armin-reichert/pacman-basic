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

package de.amr.games.pacman.lib;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import de.amr.games.pacman.controller.common.Steering;
import de.amr.games.pacman.model.common.GameModel;
import de.amr.games.pacman.model.common.actors.Creature;

/**
 * @author Armin Reichert
 */
public class FixedRouteByDirections implements Steering {

	private static final Logger LOGGER = LogManager.getFormatterLogger();

	private final List<Direction> route;
	private int currentIndex;
	private boolean gotNewDirection;
	private boolean complete;

	public FixedRouteByDirections(List<Direction> route) {
		this.route = Objects.requireNonNull(route);
		init();
	}

	public FixedRouteByDirections(String routeSpec) {
		route = new ArrayList<>(routeSpec.length());
		for (var ch : routeSpec.toCharArray()) {
			switch (ch) {
			case 'U', 'u' -> route.add(Direction.UP);
			case 'D', 'd' -> route.add(Direction.DOWN);
			case 'L', 'l' -> route.add(Direction.LEFT);
			case 'R', 'r' -> route.add(Direction.RIGHT);
			default -> LOGGER.error("Illegal direction specifier: %", ch);
			}
		}
		init();
	}

	@Override
	public void init() {
		currentIndex = 0;
		complete = false;
		gotNewDirection = false;
	}

	public boolean isComplete() {
		return complete;
	}

	@Override
	public void steer(GameModel game, Creature guy) {
		if (complete || route.isEmpty()) {
			return;
		}
		boolean intersection = game.world().isIntersection(guy.tile());
		if (intersection && !gotNewDirection) {
			guy.setWishDir(route.get(currentIndex));
			LOGGER.info("At intersection %s go %s", guy.tile(), guy.wishDir());
			gotNewDirection = true;
			++currentIndex;
			if (currentIndex == route.size()) {
				complete = true;
				LOGGER.info("Route complete");
				return;
			}
		}
		if (!intersection) {
			gotNewDirection = false;
		}
		if (guy.stuck) {
			for (var dir : Direction.values()) {
				if (dir == guy.moveDir().opposite()) {
					continue;
				}
				if (guy.canAccessTile(guy.tile().plus(dir.vec))) {
					guy.setWishDir(dir);
					LOGGER.info("At corner go %s", guy.wishDir());
					break;
				}
			}
		}
	}
}