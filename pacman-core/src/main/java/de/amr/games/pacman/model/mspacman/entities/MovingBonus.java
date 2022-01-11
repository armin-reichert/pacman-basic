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

import java.util.Random;

import de.amr.games.pacman.controller.event.PacManGameEvent;
import de.amr.games.pacman.controller.event.PacManGameEvent.Info;
import de.amr.games.pacman.lib.Direction;
import de.amr.games.pacman.model.pacman.entities.Bonus;
import de.amr.games.pacman.model.pacman.entities.BonusState;
import de.amr.games.pacman.model.world.Portal;

/**
 * In Ms. Pac-Man, the bonus tumbles through the world, starting at some random portal and leaving at some portal at the
 * other border.
 * 
 * @author Armin Reichert
 */
public class MovingBonus extends Bonus {

	@Override
	public void init() {
		state = BonusState.INACTIVE;
		timer = 0;
		targetTile = null;
		newTileEntered = true;
		forcedOnTrack = true;
		stuck = false;
		setSpeed(0.25);
		hide();
	}

	@Override
	public void activate(long ticks) {
		state = BonusState.EDIBLE;
		timer = ticks;
		// place at random portal tile
		int numPortals = world.portals().size();
		Random random = new Random();
		Portal randomPortal = world.portals().get(random.nextInt(numPortals));
		if (random.nextBoolean()) {
			placeAt(randomPortal.left, 0, 0);
			targetTile = world.portals().get(random.nextInt(numPortals)).right;
			setDir(Direction.RIGHT);
			setWishDir(Direction.RIGHT);
		} else {
			placeAt(randomPortal.right, 0, 0);
			targetTile = world.portals().get(random.nextInt(numPortals)).left;
			setDir(Direction.LEFT);
			setWishDir(Direction.LEFT);
		}
		show();
	}

	@Override
	public PacManGameEvent.Info updateState() {
		switch (state) {
		case INACTIVE:
			return null;

		case EDIBLE:
			if (tile().equals(targetTile)) {
				hide();
				state = BonusState.INACTIVE;
				return Info.BONUS_EXPIRED;
			}
			headForTile(targetTile);
			tryMoving();
			return null;

		case EATEN:
			if (timer == 0) {
				hide();
				state = BonusState.INACTIVE;
				return Info.BONUS_EXPIRED;
			}
			timer--;
			return null;

		default:
			throw new IllegalStateException();
		}
	}
}