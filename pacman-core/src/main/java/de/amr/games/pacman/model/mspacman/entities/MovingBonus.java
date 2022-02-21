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

import static de.amr.games.pacman.lib.Logging.log;

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

	private enum Phase {
		GO_TO_HOUSE_ENTRY, GO_TO_OTHER_SIDE, GO_TO_HOUSE_ENTRY_AGAIN, LEAVE;
	}

	private V2i exitTile;
	private Phase phase;

	@Override
	public void init() {
		timer = 0;
		phase = null;
		exitTile = null;
		targetTile = null;
		newTileEntered = true;
		forcedOnTrack = true;
		stuck = false;
		setSpeed(0.4); // TODO how fast should it walk?
		hide();
		state = BonusState.INACTIVE;
	}

	@Override
	public void activate(long ticks, int symbol, int points) {
		this.symbol = symbol;
		this.points = points;
		timer = ticks;
		Direction moveDir = new Random().nextBoolean() ? Direction.LEFT : Direction.RIGHT;
		if (moveDir == Direction.RIGHT) {
			placeAt(world.randomPortal().left, 0, 0);
			exitTile = world.randomPortal().right;
		} else {
			placeAt(world.randomPortal().right, 0, 0);
			exitTile = world.randomPortal().left;
		}
		setMoveDir(moveDir);
		setWishDir(moveDir);
		setTargetTile(world.ghostHouse().entry);
		show();
		state = BonusState.EDIBLE;
		phase = Phase.GO_TO_HOUSE_ENTRY;
	}

	@Override
	public void update() {
		switch (state) {

		case EDIBLE -> {
			if (phase == Phase.LEAVE && tile().equals(targetTile)) {
				hide();
				state = BonusState.INACTIVE;
				return;
			}
			if (phase == Phase.GO_TO_HOUSE_ENTRY && tile().equals(targetTile)) {
				setTargetTile(world.ghostHouse().entry.plus(0, world.ghostHouse().size.y + 2));
				phase = Phase.GO_TO_OTHER_SIDE;
			} else if (phase == Phase.GO_TO_OTHER_SIDE && tile().equals(targetTile)) {
				setTargetTile(world.ghostHouse().entry);
				phase = Phase.GO_TO_HOUSE_ENTRY_AGAIN;
			} else if (phase == Phase.GO_TO_HOUSE_ENTRY_AGAIN && tile().equals(targetTile)) {
				setTargetTile(exitTile);
				phase = Phase.LEAVE;
			}
			headForTile(targetTile);
			tryMoving();
		}

		case EATEN -> {
			if (--timer == 0) {
				hide();
				state = BonusState.INACTIVE;
			}
		}

		default -> {
		}

		}
	}

	@Override
	public boolean hasExpired() {
		return switch (state) {
		case EATEN -> timer == 0;
		default -> false;
		};
	}

	private void setTargetTile(V2i tile) {
		targetTile = tile;
		log("Bonus target tile is now %s", targetTile);
	}
}