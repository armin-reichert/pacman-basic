package de.amr.games.pacman.model.mspacman.entities;

import java.util.Random;

import de.amr.games.pacman.controller.event.PacManGameEvent;
import de.amr.games.pacman.controller.event.PacManGameEvent.Info;
import de.amr.games.pacman.lib.Direction;
import de.amr.games.pacman.model.pacman.Bonus;
import de.amr.games.pacman.model.world.PacManGameWorld;
import de.amr.games.pacman.model.world.Portal;

/**
 * In Ms. Pac-Man, the bonus tumbles through the world, starting at some random portal and leaving
 * at some portal at the other border.
 * 
 * @author Armin Reichert
 */
public class MovingBonus extends Bonus {

	public MovingBonus(PacManGameWorld world) {
		super(world);
	}

	@Override
	public void init() {
		super.init();
		targetTile = null;
		stuck = false;
		speed = 0.25f; // TODO what is the correct speed of the bonus?
	}

	@Override
	public void activate(long ticks) {
		super.activate(ticks);
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
	}

	@Override
	public PacManGameEvent.Info update() {
		switch (state) {
		case INACTIVE:
			return null;

		case EDIBLE:
			if (tile().equals(targetTile)) {
				visible = false;
				state = INACTIVE;
				return Info.BONUS_EXPIRED;
			}
			setDirectionTowardsTarget();
			tryMoving();
			return null;

		case EATEN:
			if (timer == 0) {
				visible = false;
				state = INACTIVE;
				return Info.BONUS_EXPIRED;
			}
			timer--;
			return null;

		default:
			throw new IllegalStateException();
		}
	}
}