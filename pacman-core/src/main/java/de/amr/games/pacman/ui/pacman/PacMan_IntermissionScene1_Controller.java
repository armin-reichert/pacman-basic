package de.amr.games.pacman.ui.pacman;

import static de.amr.games.pacman.model.world.PacManGameWorld.t;

import de.amr.games.pacman.controller.PacManGameController;
import de.amr.games.pacman.lib.Direction;
import de.amr.games.pacman.lib.TickTimer;
import de.amr.games.pacman.model.common.Ghost;
import de.amr.games.pacman.model.common.GhostState;
import de.amr.games.pacman.model.common.Pac;

/**
 * First intermission scene: Blinky chases Pac-Man and is then chased by a huge Pac-Man.
 * 
 * @author Armin Reichert
 */
public abstract class PacMan_IntermissionScene1_Controller {

	public enum Phase {

		BLINKY_CHASING_PACMAN, BIGPACMAN_CHASING_BLINKY;

	}

	public static final int groundY = t(20);

	public final TickTimer timer = new TickTimer();
	public final PacManGameController gameController;
	public Ghost blinky;
	public Pac pac;
	public Phase phase;

	public PacMan_IntermissionScene1_Controller(PacManGameController gameController) {
		this.gameController = gameController;
	}

	public abstract void playIntermissionSound();

	public void start() {
		pac = new Pac("Pac-Man", Direction.LEFT);
		pac.visible = true;
		pac.setPosition(t(30), groundY);
		pac.speed = 1.0f;

		blinky = new Ghost(0, "Blinky", Direction.LEFT);
		blinky.visible = true;
		blinky.state = GhostState.HUNTING_PAC;
		blinky.setPositionRelativeTo(pac, t(3), 0);
		blinky.speed = pac.speed * 1.04f;
		playIntermissionSound();

		phase = Phase.BLINKY_CHASING_PACMAN;
		timer.resetSeconds(5);
		timer.start();
	}

	public void update() {
		switch (phase) {
		case BLINKY_CHASING_PACMAN:
			if (timer.hasExpired()) {
				phase = Phase.BIGPACMAN_CHASING_BLINKY;
				timer.resetSeconds(7);
				timer.start();
			}
			timer.tick();
			break;
		case BIGPACMAN_CHASING_BLINKY:
			if (timer.hasJustStarted()) {
				blinky.setPosition(-t(2), groundY);
				blinky.dir = blinky.wishDir = Direction.RIGHT;
				blinky.speed = 1f;
				blinky.state = GhostState.FRIGHTENED;
				pac.dir = Direction.RIGHT;
				pac.speed = 1.3f;
				pac.setPositionRelativeTo(blinky, -t(13), 0);
			}
			if (timer.hasExpired()) {
				gameController.stateTimer().forceExpiration();
				return;
			}
			timer.tick();
			break;
		default:
			break;
		}
		pac.move();
		blinky.move();
	}
}