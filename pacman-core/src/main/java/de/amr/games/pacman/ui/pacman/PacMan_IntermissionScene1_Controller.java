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
		BLINKY_CHASING_PACMAN, BIGPACMAN_CHASING_BLINKY
	}

	public static final int groundY = t(20);

	public final TickTimer timer = new TickTimer(getClass().getSimpleName() + "-timer");
	public final PacManGameController gameController;
	public Ghost blinky;
	public Pac pac;
	public Phase phase;

	public PacMan_IntermissionScene1_Controller(PacManGameController gameController) {
		this.gameController = gameController;
	}

	public abstract void playIntermissionSound();

	public void init() {
		pac = new Pac("Pac-Man", null);
		pac.setDir(Direction.LEFT);
		pac.setVisible(true);
		pac.setPosition(t(30), groundY);
		pac.setSpeed(1.0);

		blinky = new Ghost(0, "Blinky", null);
		blinky.setDir(Direction.LEFT);
		blinky.setWishDir(Direction.LEFT);
		blinky.setVisible(true);
		blinky.state = GhostState.HUNTING_PAC;
		blinky.setPosition(pac.position().plus(t(3), 0));
		blinky.setSpeed(1.04);

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
			pac.move();
			blinky.move();
			timer.tick();
			break;

		case BIGPACMAN_CHASING_BLINKY:
			if (timer.hasJustStarted()) {
				blinky.setPosition(-t(2), groundY);
				blinky.setWishDir(Direction.RIGHT);
				blinky.setDir(Direction.RIGHT);
				blinky.setSpeed(1.0);
				blinky.state = GhostState.FRIGHTENED;
				pac.setDir(Direction.RIGHT);
				pac.setSpeed(1.3);
				pac.setPosition(blinky.position().plus(-t(13), 0));
			}
			if (timer.hasExpired()) {
				gameController.stateTimer().expire();
				return;
			}
			pac.move();
			blinky.move();
			timer.tick();
			break;

		default:
			break;
		}
	}
}