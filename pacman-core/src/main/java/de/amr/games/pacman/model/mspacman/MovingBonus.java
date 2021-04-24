package de.amr.games.pacman.model.mspacman;

import de.amr.games.pacman.lib.Direction;
import de.amr.games.pacman.model.pacman.Bonus;

/**
 * In Ms. Pac-Man, the bonus walks through the world, starting at some portal
 * and leaving through some portal at the other side of the world.
 * 
 * @author Armin Reichert
 */
public class MovingBonus extends Bonus {

	@Override
	public void activate(long ticks) {
		edibleTicksLeft = ticks;
		stuck = false;
		speed = 0.25f; // TODO what is the correct speed?
		if (random.nextBoolean()) {
			placeAt(world.portalLeft(random.nextInt(world.numPortals())), 0, 0);
			targetTile = world.portalRight(random.nextInt(world.numPortals()));
			setDir(Direction.RIGHT);
			setWishDir(Direction.RIGHT);
		} else {
			placeAt(world.portalRight(random.nextInt(world.numPortals())), 0, 0);
			targetTile = world.portalLeft(random.nextInt(world.numPortals()));
			setDir(Direction.LEFT);
			setWishDir(Direction.LEFT);
		}
	}

	@Override
	public void eatAndDisplayValue(long ticks) {
		edibleTicksLeft = 0;
		eatenTicksLeft = ticks;
		speed = 0;
	}

	@Override
	public void update() {
		if (edibleTicksLeft > 0) {
			selectDirectionTowardsTarget();
			tryMoving();
			if (world.isPortal(tile())) {
				edibleTicksLeft = 0;
				visible = false;
			}
		}
		if (eatenTicksLeft > 0) {
			eatenTicksLeft--;
			if (eatenTicksLeft == 0) {
				eatenTicksLeft = 0;
				visible = false;
			}
		}
	}
}