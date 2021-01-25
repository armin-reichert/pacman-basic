package de.amr.games.pacman.game.creatures;

import static de.amr.games.pacman.game.worlds.MsPacManWorld.BONUS_POINTS;
import static de.amr.games.pacman.lib.Direction.LEFT;
import static de.amr.games.pacman.lib.Direction.RIGHT;

import de.amr.games.pacman.game.worlds.PacManGameWorld;

public class MovingBonus extends Bonus {

	public MovingBonus(PacManGameWorld world) {
		super(world);
	}

	@Override
	public void activate(byte bonusSymbol, long ticks) {
		symbol = bonusSymbol;
		points = BONUS_POINTS[symbol];
		edibleTicksLeft = ticks;
		visible = true;
		couldMove = true;
		speed = 0.25f; // TODO what is the correct speed?
		if (rnd.nextBoolean()) {
			placeAt(world.portalLeft(rnd.nextInt(world.numPortals())), 0, 0);
			targetTile = world.portalRight(rnd.nextInt(world.numPortals()));
			dir = wishDir = RIGHT;
		} else {
			placeAt(world.portalRight(rnd.nextInt(world.numPortals())), 0, 0);
			targetTile = world.portalLeft(rnd.nextInt(world.numPortals()));
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