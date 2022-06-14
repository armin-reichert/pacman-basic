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
import java.util.function.Consumer;

import de.amr.games.pacman.model.common.actors.Creature;
import de.amr.games.pacman.model.common.world.World;

/**
 * @author Armin Reichert
 */
public class FixedRouteSteering implements Consumer<Creature> {

	private World world;
	private List<V2i> route = new ArrayList<>();
	private boolean complete;

	public boolean isComplete() {
		return complete;
	}

	public void setRoute(World world, List<V2i> route) {
		this.world = world;
		this.route = route;
		complete = false;
	}

	public List<V2i> getRoute() {
		return route;
	}

	@Override
	public void accept(Creature guy) {
		if (guy.tile().equals(route.get(0))) {
			route.remove(0);
			if (route.isEmpty()) {
				complete = true;
				return;
			}
			guy.targetTile = route.get(0);
			Logging.log("Next target: %s", route.get(0));
		}
		guy.computeDirectionTowardsTarget(world);
		guy.tryMoving(world);
	}
}