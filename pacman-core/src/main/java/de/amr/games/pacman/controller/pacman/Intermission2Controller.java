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
import de.amr.games.pacman.model.common.GameEntity;
import de.amr.games.pacman.model.common.GameModel;
import de.amr.games.pacman.model.common.Ghost;
import de.amr.games.pacman.model.common.GhostState;
import de.amr.games.pacman.model.common.Pac;

/**
 * Second intermission scene: Blinky pursues Pac but kicks a nail that tears his dress apart.
 * 
 * @author Armin Reichert
 */
public abstract class Intermission2Controller {

	public enum Phase {
		WALKING, GETTING_STUCK, STUCK;
	}

	public static final int groundY = t(20);

	public final TickTimer timer = new TickTimer(getClass().getSimpleName() + "-timer");
	public final PacManGameController gameController;

	public Ghost blinky;
	public Pac pac;
	public GameEntity nail;

	public Phase phase;

	public Intermission2Controller(PacManGameController gameController) {
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

		blinky = new Ghost(GameModel.RED_GHOST, "Blinky");
		blinky.setDir(Direction.LEFT);
		blinky.setWishDir(Direction.LEFT);
		blinky.setVisible(true);
		blinky.setPosition(pac.position().plus(t(14), 0));
		blinky.setSpeed(1.0);
		blinky.state = GhostState.HUNTING_PAC;

		nail = new GameEntity();
		nail.setVisible(true);
		nail.setPosition(t(14), groundY - 1);

		playIntermissionSound();

		enter(Phase.WALKING);
	}

	public void enter(Phase nextPhase) {
		phase = nextPhase;
		timer.reset();
		timer.start();
	}

	public int nailDistance() {
		return (int) (nail.position().x - blinky.position().x);
	}

	public void update() {
		switch (phase) {

		case WALKING:
			blinky.move();
			pac.move();
			if (nailDistance() == 0) {
				enter(Phase.GETTING_STUCK);
			}
			timer.tick();
			break;

		case GETTING_STUCK:
			blinky.move();
			pac.move();
			int stretching = nailDistance() / 4;
			blinky.setSpeed(0.3 - 0.1 * stretching);
			if (stretching == 3) {
				blinky.setSpeed(0);
				blinky.setDir(Direction.UP);
				enter(Phase.STUCK);
			}
			timer.tick();
			break;

		case STUCK:
			blinky.move();
			pac.move();
			if (timer.isRunningSeconds(3)) {
				blinky.setDir(Direction.RIGHT);
			}
			if (timer.isRunningSeconds(6)) {
				gameController.stateTimer().expire();
				return;
			}
			timer.tick();
			break;

		default:
			throw new IllegalStateException("Illegal phase: " + phase);
		}
	}
}