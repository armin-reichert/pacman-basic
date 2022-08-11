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

import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import de.amr.games.pacman.controller.common.Steering;
import de.amr.games.pacman.model.common.GameModel;
import de.amr.games.pacman.model.common.actors.Creature;

/**
 * @author Armin Reichert
 */
public class FollowTargetTiles implements Steering {

	private static final Logger LOGGER = LogManager.getFormatterLogger();

	private List<V2i> route = List.of();
	private int currentTargetIndex;
	private boolean complete;

	public void setRoute(List<V2i> route) {
		this.route = route;
		init();
	}

	public List<V2i> getRoute() {
		return route;
	}

	public boolean isComplete() {
		return complete;
	}

	@Override
	public void init() {
		currentTargetIndex = 0;
		complete = false;
	}

	@Override
	public void steer(GameModel game, Creature guy) {
		if (currentTargetIndex == route.size()) {
			complete = true;
			return;
		}
		if (guy.tile().equals(route.get(currentTargetIndex))) {
			++currentTargetIndex;
			if (currentTargetIndex < route.size()) {
				guy.setTargetTile(route.get(currentTargetIndex));
				LOGGER.info("New target tile for %s is %s", guy.name, guy.targetTile());
			}
		}
	}
}