package de.amr.games.pacman.model.mspacman;

import static de.amr.games.pacman.heaven.God.random;
import static de.amr.games.pacman.lib.Direction.LEFT;
import static de.amr.games.pacman.lib.Direction.RIGHT;

import de.amr.games.pacman.model.pacman.PacManBonus;

public class MsPacManBonus extends PacManBonus {

	@Override
	public void activate(long ticks) {
		edibleTicksLeft = ticks;
		couldMove = true;
		speed = 0.25f; // TODO what is the correct speed?
		if (random.nextBoolean()) {
			placeAt(world.portalLeft(random.nextInt(world.numPortals())), 0, 0);
			targetTile = world.portalRight(random.nextInt(world.numPortals()));
			dir = wishDir = RIGHT;
		} else {
			placeAt(world.portalRight(random.nextInt(world.numPortals())), 0, 0);
			targetTile = world.portalLeft(random.nextInt(world.numPortals()));
			dir = wishDir = LEFT;
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
			headForTargetTile();
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