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

import java.util.Objects;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import de.amr.games.pacman.controller.common.Steering;
import de.amr.games.pacman.model.common.GameModel;
import de.amr.games.pacman.model.common.actors.Creature;

/**
 * @author Armin Reichert
 */
public class FollowDirections implements Steering {

	private static final Logger LOGGER = LogManager.getFormatterLogger();

	private static Direction dir(char ch) {
		return switch (ch) {
		case 'U', 'u' -> Direction.UP;
		case 'D', 'd' -> Direction.DOWN;
		case 'L', 'l' -> Direction.LEFT;
		case 'R', 'r' -> Direction.RIGHT;
		default -> throw new IllegalArgumentException("Illegal direction specifier: " + ch);
		};
	}

	private final String directions;
	private int currentIndex;
	private boolean gotDirection;
	private boolean complete;

	public FollowDirections(String directions) {
		this.directions = Objects.requireNonNull(directions);
		init();
	}

	@Override
	public void init() {
		currentIndex = 0;
		complete = false;
		gotDirection = false;
	}

	public boolean isComplete() {
		return complete;
	}

	@Override
	public void steer(GameModel game, Creature guy) {
		if (complete || directions.isEmpty()) {
			return;
		}
		boolean intersection = game.world().isIntersection(guy.tile());
		if (intersection && !gotDirection) {
			guy.setWishDir(dir(directions.charAt(currentIndex)));
			LOGGER.trace("At intersection %s go %s", guy.tile(), guy.wishDir());
			gotDirection = true;
			++currentIndex;
			if (currentIndex == directions.length()) {
				complete = true;
				LOGGER.trace("Route complete");
				return;
			}
		}
		if (!intersection) {
			gotDirection = false;
		}
		if (guy.stuck) {
			for (var dir : Direction.values()) {
				if (dir != guy.moveDir().opposite() && guy.canAccessTile(guy.tile().plus(dir.vec))) {
					guy.setWishDir(dir);
					LOGGER.trace("At corner go %s", guy.wishDir());
					break;
				}
			}
		}
	}
}