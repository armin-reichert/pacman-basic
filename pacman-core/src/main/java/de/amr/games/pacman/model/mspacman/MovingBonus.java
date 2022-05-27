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

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import de.amr.games.pacman.lib.Direction;
import de.amr.games.pacman.lib.TickTimer;
import de.amr.games.pacman.lib.V2i;
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
public class MovingBonus extends Creature {

	private long timer;
	private final List<V2i> route = new ArrayList<>();

	public MovingBonus(World world) {
		this.world = world;
		init();
	}

	public void setTimerTicks(long ticks) {
		timer = ticks;
	}

	public void init() {
		timer = TickTimer.INDEFINITE;
		route.clear();
		newTileEntered = true;
		forcedOnTrack = true;
		stuck = false;
		setSpeed(0.4); // TODO how fast should it walk?
		hide();
	}

	public void activate() {
		init();
		V2i houseEntry = world.ghostHouse().doorTileLeft().minus(0, 1);
		Direction moveDir = new Random().nextBoolean() ? Direction.LEFT : Direction.RIGHT;
		Portal entryPortal = world.portals().get(new Random().nextInt(world.portals().size()));
		Portal exitPortal = world.portals().get(new Random().nextInt(world.portals().size()));
		route.add(houseEntry);
		route.add(houseEntry.plus(0, world.ghostHouse().size().y + 2)); // middle tile below house
		route.add(houseEntry);
		route.add(moveDir == Direction.RIGHT ? exitPortal.right : exitPortal.left);
		placeAt(moveDir == Direction.RIGHT ? entryPortal.left : entryPortal.right, 0, 0);
		setBothDirs(moveDir);
		show();
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