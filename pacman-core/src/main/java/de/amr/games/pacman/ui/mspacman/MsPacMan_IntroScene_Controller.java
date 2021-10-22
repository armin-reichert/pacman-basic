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
package de.amr.games.pacman.ui.mspacman;

import static de.amr.games.pacman.lib.Direction.LEFT;
import static de.amr.games.pacman.lib.Direction.UP;
import static de.amr.games.pacman.model.world.PacManGameWorld.t;

import de.amr.games.pacman.controller.PacManGameController;
import de.amr.games.pacman.lib.TickTimer;
import de.amr.games.pacman.lib.TimedSequence;
import de.amr.games.pacman.lib.V2i;
import de.amr.games.pacman.model.common.Ghost;
import de.amr.games.pacman.model.common.GhostState;
import de.amr.games.pacman.model.common.Pac;

/**
 * Intro scene of the Ms. Pac-Man game. The ghosts and Ms. Pac-Man are introduced one after another.
 * 
 * @author Armin Reichert
 */
public class MsPacMan_IntroScene_Controller {

	public enum Phase {

		BEGIN, PRESENTING_GHOST, PRESENTING_MSPACMAN, END;
	}

	// the board where the actors are presented
	public final V2i tileBoardTopLeft = new V2i(6, 8);
	public final int tileBelowBoard = 17;
	public final int tileLeftOfBoard = 4;

	public final PacManGameController gameController;

	public Phase phase;
	public final TickTimer phaseTimer = new TickTimer(getClass().getSimpleName() + "-timer");

	public Pac msPacMan;
	public Ghost[] ghosts;
	public int currentGhostIndex;
	public final TimedSequence<Boolean> blinking = TimedSequence.pulse().frameDuration(30);

	public MsPacMan_IntroScene_Controller(PacManGameController gameController) {
		this.gameController = gameController;
	}

	private void enterPhase(Phase newPhase) {
		phase = newPhase;
		phaseTimer.reset();
		phaseTimer.start();
	}

	public void init() {
		msPacMan = new Pac("Ms. Pac-Man", null);
		msPacMan.setDir(LEFT);
		msPacMan.setPosition(t(37), t(tileBelowBoard));

		ghosts = new Ghost[] { //
				new Ghost(0, "Blinky", null), //
				new Ghost(1, "Pinky", null), //
				new Ghost(2, "Inky", null), //
				new Ghost(3, "Sue", null),//
		};

		for (Ghost ghost : ghosts) {
			ghost.setDir(LEFT);
			ghost.setWishDir(LEFT);
			ghost.setPosition(t(37), t(tileBelowBoard));
			ghost.state = GhostState.HUNTING_PAC;
		}

		currentGhostIndex = -1;

		enterPhase(Phase.BEGIN);
	}

	public void update() {
		switch (phase) {

		case BEGIN:
			if (phaseTimer.isRunningSeconds(2)) {
				currentGhostIndex = 0;
				enterPhase(Phase.PRESENTING_GHOST);
			}
			phaseTimer.tick();
			break;

		case PRESENTING_GHOST:
			if (phaseTimer.hasJustStarted()) {
				ghosts[currentGhostIndex].setVisible(true);
				ghosts[currentGhostIndex].setSpeed(1.0);
			}
			boolean ghostReachedFinalPosition = ghostEnteringStage(ghosts[currentGhostIndex]);
			if (ghostReachedFinalPosition) {
				if (currentGhostIndex == 3) {
					enterPhase(Phase.PRESENTING_MSPACMAN);
				} else {
					currentGhostIndex++;
					enterPhase(Phase.PRESENTING_GHOST);
				}
			}
			for (Ghost ghost : ghosts) {
				ghost.move();
			}
			phaseTimer.tick();
			break;

		case PRESENTING_MSPACMAN:
			if (phaseTimer.hasJustStarted()) {
				msPacMan.setVisible(true);
				msPacMan.stuck = false;
				msPacMan.setSpeed(1.0);
			}
			boolean msPacReachedFinalPosition = msPacManEnteringStage();
			if (msPacReachedFinalPosition) {
				enterPhase(Phase.END);
			}
			msPacMan.move();
			phaseTimer.tick();
			break;

		case END:
			if (phaseTimer.hasJustStarted()) {
				blinking.restart();
			}
			if (phaseTimer.isRunningSeconds(5)) {
				gameController.stateTimer().expire();
				return;
			}
			blinking.animate();
			phaseTimer.tick();
			break;

		default:
			break;
		}
	}

	public boolean ghostEnteringStage(Ghost ghost) {
		if (ghost.dir() == LEFT && ghost.position().x <= t(tileLeftOfBoard)) {
			ghost.setDir(UP);
			ghost.setWishDir(UP);
			return false;
		}
		if (ghost.dir() == UP && ghost.position().y <= t(tileBoardTopLeft.y) + ghost.id * 18) {
			ghost.setSpeed(0);
			return true;
		}
		return false;
	}

	public boolean msPacManEnteringStage() {
		if (msPacMan.speed() > 0 && msPacMan.position().x <= t(13)) {
			msPacMan.setSpeed(0);
			return true;
		}
		return false;
	}
}