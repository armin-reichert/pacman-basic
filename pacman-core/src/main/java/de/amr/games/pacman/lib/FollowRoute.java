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
public class FollowRoute implements Steering {

	private static final Logger LOGGER = LogManager.getFormatterLogger();

	private List<NavigationPoint> route = List.of();
	private int targetIndex;
	private boolean complete;

	public void setRoute(List<NavigationPoint> route) {
		this.route = route;
		init();
	}

	public boolean isComplete() {
		return complete;
	}

	@Override
	public void init() {
		targetIndex = 0;
		complete = false;
	}

	@Override
	public void steer(GameModel game, Creature guy) {
		if (targetIndex == route.size()) {
			complete = true;
		} else if (guy.targetTile().isEmpty()) {
			guy.setTargetTile(currentTarget().tile());
		} else if (guy.tile().equals(currentTarget().tile())) {
			nextTarget(guy);
		}
		guy.navigateTowardsTarget(game);
		LOGGER.info("New target tile for %s=%s, wish dir=%s", guy.name, guy.targetTile().get(), guy.wishDir());
	}

	private void nextTarget(Creature guy) {
		++targetIndex;
		if (targetIndex < route.size()) {
			guy.setTargetTile(currentTarget().tile());
		}
	}

	private NavigationPoint currentTarget() {
		return route.get(targetIndex);
	}
}