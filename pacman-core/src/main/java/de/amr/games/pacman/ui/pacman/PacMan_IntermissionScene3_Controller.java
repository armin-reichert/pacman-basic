package de.amr.games.pacman.ui.pacman;

import static de.amr.games.pacman.model.world.PacManGameWorld.t;

import de.amr.games.pacman.controller.PacManGameController;
import de.amr.games.pacman.lib.Direction;
import de.amr.games.pacman.lib.TickTimer;
import de.amr.games.pacman.model.common.Ghost;
import de.amr.games.pacman.model.common.GhostState;
import de.amr.games.pacman.model.common.Pac;

/**
 * Third intermission scene: Blinky in shred dress chases Pac-Man, comes back half-naked drawing
 * dress over the floor.
 * 
 * @author Armin Reichert
 */
public abstract class PacMan_IntermissionScene3_Controller {

	public enum Phase {
		CHASING_PACMAN, RETURNING_HALF_NAKED;
	}

	public static final int chaseTileY = 20;

	public final TickTimer timer = new TickTimer();
	public final PacManGameController gameController;

	public Ghost blinky;
	public Pac pac;
	public Phase phase;

	public PacMan_IntermissionScene3_Controller(PacManGameController gameController) {
		this.gameController = gameController;
	}

	public abstract void playIntermissionSound();

	public void init() {
		pac = new Pac("Pac-Man", Direction.LEFT);
		pac.setTilePosition(30, chaseTileY);
		pac.visible = true;
		pac.dead = false;
		pac.speed = 1.2f;
		pac.stuck = false;
		pac.turnTo(Direction.LEFT);

		blinky = new Ghost(0, "Blinky", Direction.LEFT);
		blinky.setPositionRelativeTo(pac, t(8), 0);
		blinky.visible = true;
		blinky.state = GhostState.HUNTING_PAC;
		blinky.speed = pac.speed;
		blinky.turnTo(Direction.LEFT);
		blinky.wishDir = Direction.LEFT;

		playIntermissionSound();

		phase = Phase.CHASING_PACMAN;
	}

	public void update() {
		switch (phase) {
		case CHASING_PACMAN:
			if (blinky.position.x <= -50) {
				pac.speed = 0;
				blinky.turnTo(Direction.RIGHT);
				blinky.wishDir = Direction.RIGHT;
				phase = Phase.RETURNING_HALF_NAKED;
			}
			break;
		case RETURNING_HALF_NAKED:
			if (blinky.position.x > t(28) + 200) {
				gameController.stateTimer().forceExpiration();
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