package de.amr.games.pacman.game.creatures;

import static de.amr.games.pacman.game.worlds.MsPacManWorld.BONUS_POINTS;
import static de.amr.games.pacman.lib.Direction.LEFT;
import static de.amr.games.pacman.lib.Direction.RIGHT;

import java.util.List;
import java.util.stream.Collectors;

import de.amr.games.pacman.game.worlds.PacManGameWorld;
import de.amr.games.pacman.lib.Direction;
import de.amr.games.pacman.lib.V2i;

public class MsPacManBonus extends ClassicBonus {

	public Direction wanderingDirection;

	public MsPacManBonus(PacManGameWorld world) {
		super(world);
	}

	@Override
	public void activate(byte bonusSymbol, long ticks) {
		visible = true;
		symbol = bonusSymbol;
		points = BONUS_POINTS[symbol];
		edibleTicksLeft = ticks;
		boolean entersMazeFromLeft = rnd.nextBoolean();
		int portal = rnd.nextInt(world.numPortals());
		V2i startTile = entersMazeFromLeft ? world.portalLeft(portal) : world.portalRight(portal);
		placeAt(startTile, 0, 0);
		wanderingDirection = entersMazeFromLeft ? RIGHT : LEFT;
		dir = wishDir = wanderingDirection;
		couldMove = true;
		speed = 0.25f; // TODO what is the correct speed?
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
			wander();
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

	private void wander() {
		V2i location = tile();
		if (!couldMove || world.isIntersection(location)) {
			List<Direction> dirs = accessibleDirections(location, dir.opposite()).collect(Collectors.toList());
			if (dirs.size() > 1) {
				// give random movement a bias towards the wandering direction
				dirs.remove(wanderingDirection.opposite());
			}
			wishDir = dirs.get(rnd.nextInt(dirs.size()));
		}
		tryMoving();
	}
}