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
import java.util.Objects;
import java.util.function.Consumer;

import de.amr.games.pacman.model.common.actors.Creature;

/**
 * @author Armin Reichert
 */
public class FixedRouteByTiles implements Consumer<Creature> {

	private final List<V2i> route;
	private int currentTargetIndex;
	private boolean complete;

	public FixedRouteByTiles(List<V2i> route) {
		this.route = Objects.requireNonNull(route);
		currentTargetIndex = 0;
		complete = false;
	}

	public List<V2i> getRoute() {
		return route;
	}

	public boolean isComplete() {
		return complete;
	}

	@Override
	public void accept(Creature guy) {
		if (complete || route.isEmpty()) {
			return;
		}
		if (guy.tile().equals(route.get(currentTargetIndex))) {
			++currentTargetIndex;
			if (currentTargetIndex == route.size()) {
				complete = true;
				return;
			}
		}
		guy.targetTile = route.get(currentTargetIndex);
		guy.computeDirectionTowardsTarget();
		guy.tryMoving();
	}
}