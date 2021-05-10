package de.amr.games.pacman.model.mspacman;

import de.amr.games.pacman.lib.Direction;
import de.amr.games.pacman.model.pacman.Bonus;
import de.amr.games.pacman.model.world.PacManGameWorld;
import de.amr.games.pacman.model.world.Portal;

/**
 * In Ms. Pac-Man, the bonus walks through the world, starting at some portal
 * and leaving through some portal at the other side of the world.
 * 
 * @author Armin Reichert
 */
public class MovingBonus extends Bonus {
	
	public MovingBonus(PacManGameWorld world) {
		super(world);
	}

	@Override
	public void activate(long ticks) {
		edibleTicksLeft = ticks;
		stuck = false;
		speed = 0.25f; // TODO what is the correct speed of the bonus?
		int numPortals = world.portals().size();
		Portal portal = world.portals().get(random.nextInt(numPortals));
		if (random.nextBoolean()) {
			placeAt(portal.left, 0, 0);
			targetTile = world.portals().get(random.nextInt(numPortals)).right;
			setDir(Direction.RIGHT);
			setWishDir(Direction.RIGHT);
		} else {
			placeAt(portal.right, 0, 0);
			targetTile = world.portals().get(random.nextInt(numPortals)).left;
			setDir(Direction.LEFT);
			setWishDir(Direction.LEFT);
		}
	}

	@Override
	public void eaten(long ticks) {
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