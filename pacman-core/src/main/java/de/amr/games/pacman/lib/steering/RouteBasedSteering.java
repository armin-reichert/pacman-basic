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

package de.amr.games.pacman.lib.steering;

import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import de.amr.games.pacman.controller.Steering;
import de.amr.games.pacman.model.GameLevel;
import de.amr.games.pacman.model.actors.Creature;

/**
 * Steering of a creature based on a route.
 * 
 * @author Armin Reichert
 */
public class RouteBasedSteering implements Steering {

	private static final Logger LOG = LogManager.getFormatterLogger();

	private List<NavigationPoint> route = List.of();
	private int targetIndex;
	private boolean complete;

	public RouteBasedSteering() {
	}

	public RouteBasedSteering(List<NavigationPoint> route) {
		setRoute(route);
	}

	public void setRoute(List<NavigationPoint> route) {
		this.route = route;
		init();
	}

	@Override
	public void init() {
		targetIndex = 0;
		complete = false;
	}

	@Override
	public void steer(GameLevel level, Creature guy) {
		guy.navigateTowardsTarget(level);
		if (targetIndex == route.size()) {
			complete = true;
		} else if (guy.targetTile().isEmpty()) {
			guy.setTargetTile(currentTarget().tile());
			guy.navigateTowardsTarget(level);
			LOG.trace("New target tile for %s=%ss", guy.name(), guy.targetTile().get());
		} else if (guy.tile().equals(currentTarget().tile())) {
			nextTarget(level, guy);
			LOG.trace("New target tile for %s=%s", guy.name(), guy.targetTile().get());
		}
	}

	public boolean isComplete() {
		return complete;
	}

	private void nextTarget(GameLevel level, Creature guy) {
		++targetIndex;
		if (targetIndex < route.size()) {
			guy.setTargetTile(currentTarget().tile());
			guy.navigateTowardsTarget(level);
		}
	}

	private NavigationPoint currentTarget() {
		return route.get(targetIndex);
	}
}