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

import de.amr.games.pacman.lib.Direction;
import de.amr.games.pacman.lib.Logging;
import de.amr.games.pacman.lib.V2i;
import de.amr.games.pacman.model.common.BonusState;
import de.amr.games.pacman.model.pacman.entities.Bonus;
import de.amr.games.pacman.model.world.Portal;

/**
 * In Ms. Pac-Man, the bonus tumbles through the world, starting at some portal and leaving the maze at some portal at
 * the other border.
 * 
 * TODO: That's not quite true, in the original game there are predefined "fruit paths" and the behavior is not random.
 * 
 * @author Armin Reichert
 */
public class MovingBonus extends Bonus {

	private enum Phase {
		GO_TO_HOUSE_ENTRY, GO_TO_OTHER_SIDE, GO_TO_HOUSE_ENTRY_AGAIN, LEAVE;
	}

	private V2i entryTile;
	private V2i exitTile;
	private Phase phase;

	@Override
	public void init() {
		state = BonusState.INACTIVE;
		timer = 0;
		phase = null;
		entryTile = null;
		exitTile = null;
		targetTile = null;
		newTileEntered = true;
		forcedOnTrack = true;
		stuck = false;
		setSpeed(0.4); // TODO how fast should it walk?
		hide();
	}

	@Override
	public void activate(long ticks, int symbol, int points) {
		state = BonusState.EDIBLE;
		timer = ticks;
		this.symbol = symbol;
		this.points = points;
		boolean leftToRight = new Random().nextBoolean();
		if (leftToRight) {
			entryTile = randomPortal().left;
			exitTile = randomPortal().right;
			setDir(Direction.RIGHT);
			setWishDir(Direction.RIGHT);
		} else {
			entryTile = randomPortal().right;
			exitTile = randomPortal().left;
			setDir(Direction.LEFT);
			setWishDir(Direction.LEFT);
		}
		placeAt(entryTile, 0, 0);
		setTargetTile(world.ghostHouse().entryTile());
		phase = Phase.GO_TO_HOUSE_ENTRY;
		show();
	}

	private Portal randomPortal() {
		return world.portals().get(new Random().nextInt(world.portals().size()));
	}

	private void setTargetTile(V2i tile) {
		targetTile = tile;
		Logging.log("Bonus target tile is now %s", targetTile);
	}

	@Override
	public boolean updateState() {
		switch (state) {

		case INACTIVE -> {
			return false;
		}

		case EDIBLE -> {
			if (phase == Phase.LEAVE && tile().equals(targetTile)) {
				state = BonusState.INACTIVE;
				hide();
				return true;
			}
			if (phase == Phase.GO_TO_HOUSE_ENTRY && tile().equals(targetTile)) {
				phase = Phase.GO_TO_OTHER_SIDE;
				int dy = world.ghostHouse().numTilesY() + 2;
				setTargetTile(world.ghostHouse().entryTile().plus(0, dy));
			}
			if (phase == Phase.GO_TO_OTHER_SIDE && tile().equals(targetTile)) {
				phase = Phase.GO_TO_HOUSE_ENTRY_AGAIN;
				setTargetTile(world.ghostHouse().entryTile());
			}
			if (phase == Phase.GO_TO_HOUSE_ENTRY_AGAIN && tile().equals(targetTile)) {
				phase = Phase.LEAVE;
				setTargetTile(exitTile);
			}
			headForTile(targetTile);
			tryMoving();
			return false;
		}

		case EATEN -> {
			if (timer == 0) {
				state = BonusState.INACTIVE;
				hide();
				return true;
			}
			timer--;
			return false;
		}

		default -> throw new IllegalStateException();
		}
	}
}