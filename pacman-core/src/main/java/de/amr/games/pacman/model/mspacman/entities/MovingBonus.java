/*
MIT License

Copyright (c) 2021 Armin Reichert

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
package de.amr.games.pacman.model.mspacman.entities;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import de.amr.games.pacman.lib.Direction;
import de.amr.games.pacman.lib.V2i;
import de.amr.games.pacman.model.pacman.entities.Bonus;

/**
 * A bonus that tumbles through the world, starting at some portal, making one round around the ghost house and leaving
 * the maze at some portal at the other border.
 * 
 * TODO: That's not exactly the original Ms. Pac-Man behaviour with predefined "fruit paths".
 * 
 * @author Armin Reichert
 */
public class MovingBonus extends Bonus {

	private List<V2i> route;

	@Override
	public void init() {
		timer = 0;
		route = null;
		newTileEntered = true;
		forcedOnTrack = true;
		stuck = false;
		setSpeed(0.4); // TODO how fast should it walk?
		hide();
		state = BonusState.INACTIVE;
	}

	@Override
	public void activate(int symbol, int points) {
		this.symbol = symbol;
		this.points = points;
		route = new ArrayList<>();
		route.add(world.ghostHouse().entry);
		route.add(world.ghostHouse().entry.plus(0, world.ghostHouse().size.y + 2));
		route.add(world.ghostHouse().entry);
		Direction moveDir = new Random().nextBoolean() ? Direction.LEFT : Direction.RIGHT;
		if (moveDir == Direction.RIGHT) {
			placeAt(world.randomPortal().left, 0, 0);
			route.add(world.randomPortal().right);
		} else {
			placeAt(world.randomPortal().right, 0, 0);
			route.add(world.randomPortal().left);
		}
		setMoveDir(moveDir);
		setWishDir(moveDir);
		show();
		state = BonusState.EDIBLE;
	}

	@Override
	public void update() {
		switch (state) {

		case EDIBLE -> {
			if (tile().equals(targetTile)) {
				route.remove(0);
				if (route.isEmpty()) {
					init();
					return;
				}
			}
			headForTile(route.get(0));
			tryMoving();
		}

		case EATEN -> {
			if (timer == 0) {
				init();
			} else {
				--timer;
			}
		}

		default -> {
		}

		}
	}
}