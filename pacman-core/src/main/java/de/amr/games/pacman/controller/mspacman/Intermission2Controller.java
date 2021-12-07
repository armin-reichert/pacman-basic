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
package de.amr.games.pacman.controller.mspacman;

import static de.amr.games.pacman.model.world.PacManGameWorld.t;

import de.amr.games.pacman.controller.PacManGameController;
import de.amr.games.pacman.lib.Direction;
import de.amr.games.pacman.lib.TickTimer;
import de.amr.games.pacman.model.common.Pac;
import de.amr.games.pacman.model.mspacman.entities.Flap;

/**
 * Intermission scene 2: "The chase".
 * <p>
 * Pac-Man and Ms. Pac-Man chase each other across the screen over and over.
 * After three turns, they both rapidly run from left to right and right to
 * left. (Played after round 5)
 * 
 * @author Armin Reichert
 */
public abstract class Intermission2Controller {

	public enum Phase {

		FLAP, ACTION;
	}

	public static final int UPPER_Y = t(12), LOWER_Y = t(24), MIDDLE_Y = t(18);

	public final PacManGameController gameController;
	public final TickTimer timer = new TickTimer(getClass().getSimpleName() + "-timer");
	public Phase phase;
	public Flap flap;
	public Pac pacMan, msPacMan;

	public void enter(Phase newPhase) {
		phase = newPhase;
		timer.reset();
		timer.start();
	}

	public Intermission2Controller(PacManGameController gameController) {
		this.gameController = gameController;
	}

	public abstract void playIntermissionSound();

	public abstract void playFlapAnimation();

	public void init() {
		flap = new Flap(2, "THE CHASE");
		flap.setPosition(t(3), t(10));
		flap.setVisible(true);

		pacMan = new Pac("Pac-Man");
		pacMan.setDir(Direction.RIGHT);

		msPacMan = new Pac("Ms. Pac-Man");
		msPacMan.setDir(Direction.RIGHT);

		enter(Phase.FLAP);
	}

	public void update() {
		switch (phase) {

		case FLAP:
			if (timer.isRunningSeconds(1)) {
				playFlapAnimation();
			}
			if (timer.isRunningSeconds(2)) {
				flap.setVisible(false);
				playIntermissionSound();
			}
			if (timer.isRunningSeconds(4.5)) {
				enter(Phase.ACTION);
			}
			timer.tick();
			break;

		case ACTION:
			if (timer.isRunningSeconds(1.5)) {
				pacMan.setVisible(true);
				pacMan.setPosition(-t(2), UPPER_Y);
				pacMan.setDir(Direction.RIGHT);
				pacMan.setSpeed(2.0);
				msPacMan.setVisible(true);
				msPacMan.setPosition(-t(8), UPPER_Y);
				msPacMan.setDir(Direction.RIGHT);
				msPacMan.setSpeed(2.0);
			}
			if (timer.isRunningSeconds(6)) {
				msPacMan.setPosition(t(30), LOWER_Y);
				msPacMan.setVisible(true);
				msPacMan.setDir(Direction.LEFT);
				msPacMan.setSpeed(2.0);
				pacMan.setPosition(t(36), LOWER_Y);
				pacMan.setDir(Direction.LEFT);
				pacMan.setSpeed(2.0);
			}
			if (timer.isRunningSeconds(10.5)) {
				msPacMan.setPosition(t(-8), MIDDLE_Y);
				msPacMan.setDir(Direction.RIGHT);
				msPacMan.setSpeed(2.0);
				pacMan.setPosition(t(-2), MIDDLE_Y);
				pacMan.setDir(Direction.RIGHT);
				pacMan.setSpeed(2.0);
			}
			if (timer.isRunningSeconds(14.5)) {
				msPacMan.setPosition(t(30), UPPER_Y);
				msPacMan.setDir(Direction.LEFT);
				msPacMan.setSpeed(4.0);
				pacMan.setPosition(t(42), UPPER_Y);
				pacMan.setDir(Direction.LEFT);
				pacMan.setSpeed(4.0);
			}
			if (timer.isRunningSeconds(15.5)) {
				msPacMan.setPosition(t(-14), LOWER_Y);
				msPacMan.setDir(Direction.RIGHT);
				msPacMan.setSpeed(4.0);
				pacMan.setPosition(t(-2), LOWER_Y);
				pacMan.setDir(Direction.RIGHT);
				pacMan.setSpeed(4.0);
			}
			if (timer.isRunningSeconds(20)) {
				gameController.stateTimer().expire();
				return;
			}
			timer.tick();
			break;

		default:
			break;
		}
		pacMan.move();
		msPacMan.move();
	}
}