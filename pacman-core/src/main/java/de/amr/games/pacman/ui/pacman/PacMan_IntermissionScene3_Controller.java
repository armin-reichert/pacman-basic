package de.amr.games.pacman.ui.pacman;

import static de.amr.games.pacman.model.world.PacManGameWorld.t;

import de.amr.games.pacman.controller.PacManGameController;
import de.amr.games.pacman.lib.Direction;
import de.amr.games.pacman.lib.TickTimer;
import de.amr.games.pacman.model.common.Ghost;
import de.amr.games.pacman.model.common.GhostState;
import de.amr.games.pacman.model.common.Pac;

/**
 * Third intermission scene: Blinky in shred dress chases Pac-Man, comes back
 * half-naked drawing dress over the floor.
 * 
 * @author Armin Reichert
 */
public abstract class PacMan_IntermissionScene3_Controller {

	public enum Phase {
		CHASING_PACMAN, RETURNING_HALF_NAKED;
	}

	public static final int chaseTileY = 20;

	public final TickTimer timer = new TickTimer(getClass().getSimpleName() + "-timer");
	public final PacManGameController gameController;

	public Ghost blinky;
	public Pac pac;
	public Phase phase;

	public PacMan_IntermissionScene3_Controller(PacManGameController gameController) {
		this.gameController = gameController;
	}

	public abstract void playIntermissionSound();

	public void init() {
		pac = new Pac("Pac-Man", null);
		pac.setDir(Direction.LEFT);
		pac.setPosition(t(30), t(chaseTileY));
		pac.setVisible(true);
		pac.dead = false;
		pac.setSpeed(1.2);
		pac.stuck = false;
		pac.setDir(Direction.LEFT);

		blinky = new Ghost(0, "Blinky", null);
		blinky.setDir(Direction.LEFT);
		blinky.setWishDir(Direction.LEFT);
		blinky.setPosition(pac.position().plus(t(8), 0));
		blinky.setVisible(true);
		blinky.setSpeed(1.2);
		blinky.setDir(Direction.LEFT);
		blinky.setWishDir(Direction.LEFT);
		blinky.state = GhostState.HUNTING_PAC;

		playIntermissionSound();

		phase = Phase.CHASING_PACMAN;
	}

	public void update() {
		switch (phase) {

		case CHASING_PACMAN:
			if (blinky.position().x <= -50) {
				pac.setSpeed(0);
				blinky.setDir(Direction.RIGHT);
				blinky.setWishDir(Direction.RIGHT);
				phase = Phase.RETURNING_HALF_NAKED;
			}
			break;

		case RETURNING_HALF_NAKED:
			if (blinky.position().x > t(28) + 200) {
				gameController.stateTimer().expire();
				return;
			}
			break;

		default:
			throw new IllegalStateException("Illegal phase: " + phase);
		}
		blinky.move();
		pac.move();
	}
}