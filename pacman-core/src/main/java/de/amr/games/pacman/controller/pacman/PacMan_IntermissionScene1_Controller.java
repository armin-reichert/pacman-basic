/*
MIT License

Copyright (c) 2021 Armin Reichert

Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files (the "Software"), to deal
in the Software without restriction, including without limitation the rights
to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
copies of the Software, and to permit persons to whom the Software is
furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all
copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
SOFTWARE.
 */
package de.amr.games.pacman.controller.pacman;

import static de.amr.games.pacman.model.world.PacManGameWorld.t;

import de.amr.games.pacman.controller.PacManGameController;
import de.amr.games.pacman.lib.Direction;
import de.amr.games.pacman.lib.TickTimer;
import de.amr.games.pacman.model.common.Ghost;
import de.amr.games.pacman.model.common.GhostState;
import de.amr.games.pacman.model.common.Pac;
import de.amr.games.pacman.model.common.PacManGameModel;

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

	/**
	 * Plays the sound for this intermission scene, differs for Pac-Man and Ms. Pac-Man.
	 */
	public abstract void playIntermissionSound();

	public void init() {
		pac = new Pac("Pac-Man");
		pac.setDir(Direction.LEFT);
		pac.setVisible(true);
		pac.setPosition(t(30), groundY);
		pac.setSpeed(1.0);

		blinky = new Ghost(PacManGameModel.RED_GHOST, "Blinky");
		blinky.setDir(Direction.LEFT);
		blinky.setWishDir(Direction.LEFT);
		blinky.setVisible(true);
		blinky.setPosition(pac.position().plus(t(3), 0));
		blinky.setSpeed(1.04);
		blinky.state = GhostState.HUNTING_PAC;

		playIntermissionSound();

		phase = Phase.BLINKY_CHASING_PACMAN;
		timer.resetSeconds(5);
		timer.start();
	}

	public void update() {
		switch (phase) {

		case BLINKY_CHASING_PACMAN:
			pac.move();
			blinky.move();
			timer.tick();
			if (timer.hasExpired()) {
				phase = Phase.BIGPACMAN_CHASING_BLINKY;
				timer.resetSeconds(7);
				timer.start();
			}
			break;

		case BIGPACMAN_CHASING_BLINKY:
			pac.move();
			blinky.move();
			timer.tick();
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
			}
			break;

		default:
			throw new IllegalStateException("Illegal phase: " + phase);
		}
	}
}