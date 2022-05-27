/*
MIT License

Copyright (c) 2021-22 Armin Reichert

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
package de.amr.games.pacman.model.mspacman;

import static de.amr.games.pacman.lib.Logging.log;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import de.amr.games.pacman.lib.Direction;
import de.amr.games.pacman.lib.TickTimer;
import de.amr.games.pacman.lib.V2d;
import de.amr.games.pacman.lib.V2i;
import de.amr.games.pacman.model.common.Bonus;
import de.amr.games.pacman.model.common.BonusState;
import de.amr.games.pacman.model.common.Creature;
import de.amr.games.pacman.model.common.world.Portal;
import de.amr.games.pacman.model.common.world.World;

/**
 * A bonus that tumbles through the world, starting at some portal, making one round around the ghost house and leaving
 * the maze at some portal at the other border.
 * 
 * TODO: That's not exactly the original Ms. Pac-Man behaviour with predefined "fruit paths".
 * 
 * @author Armin Reichert
 */
public class MovingBonus extends Creature implements Bonus {

	private BonusState state;
	private int symbol;
	private int value;
	private long timer;
	private final List<V2i> route = new ArrayList<>();

	public void setWorld(World world) {
		this.world = world;
	}

	@Override
	public V2d position() {
		return position;
	}

	@Override
	public BonusState state() {
		return state;
	}

	@Override
	public void enterState(BonusState state) {
		this.state = state;
	}

	@Override
	public void setTimerTicks(long ticks) {
		timer = ticks;
	}

	@Override
	public int symbol() {
		return symbol;
	}

	@Override
	public int value() {
		return value;
	}

	@Override
	public void init() {
		symbol = -1;
		value = -1;
		state = BonusState.INACTIVE;
		timer = TickTimer.INDEFINITE;
		route.clear();
		newTileEntered = true;
		forcedOnTrack = true;
		stuck = false;
		setSpeed(0.4); // TODO how fast should it walk?
		hide();
	}

	@Override
	public void activate(int symbol, int value) {
		init();
		this.symbol = symbol;
		this.value = value;
		int numPortals = world.portals().size();
		if (numPortals > 0) {
			Portal entryPortal = world.portals().get(new Random().nextInt(numPortals));
			Portal exitPortal = world.portals().get(new Random().nextInt(numPortals));
			computeNewRoute(entryPortal, exitPortal);
			show();
			state = BonusState.EDIBLE;
			log("MovingBonus symbol=%d, value=%d position=%s activated", symbol, value, position);
		}
	}

	private void computeNewRoute(Portal entryPortal, Portal exitPortal) {
		V2i houseEntry = world.ghostHouse().doorTileLeft().plus(Direction.UP.vec);
		Direction travelDir = new Random().nextBoolean() ? Direction.LEFT : Direction.RIGHT;
		route.add(houseEntry);
		route.add(houseEntry.plus(Direction.DOWN.vec.scaled(world.ghostHouse().size().y + 2)));
		route.add(houseEntry);
		route.add(travelDir == Direction.RIGHT ? exitPortal.right : exitPortal.left);
		placeAt(travelDir == Direction.RIGHT ? entryPortal.left : entryPortal.right, 0, 0);
		setBothDirs(travelDir);
	}

	public boolean followRoute() {
		if (tile().equals(targetTile)) {
			route.remove(0);
			if (route.isEmpty()) {
				return true;
			}
		}
		headForTile(route.get(0));
		tryMoving();
		return false;
	}

	@Override
	public boolean tick() {
		if (timer > 0) {
			--timer;
			if (timer == 0) {
				return true;
			}
		}
		return false;
	}
}