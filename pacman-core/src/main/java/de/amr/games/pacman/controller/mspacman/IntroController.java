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

import static de.amr.games.pacman.lib.Direction.LEFT;
import static de.amr.games.pacman.lib.Direction.UP;
import static de.amr.games.pacman.model.world.PacManGameWorld.t;

import de.amr.games.pacman.controller.PacManGameController;
import de.amr.games.pacman.controller.mspacman.IntroController.IntroState;
import de.amr.games.pacman.lib.FiniteStateMachine;
import de.amr.games.pacman.lib.TimedSequence;
import de.amr.games.pacman.lib.V2i;
import de.amr.games.pacman.model.common.GameModel;
import de.amr.games.pacman.model.common.Ghost;
import de.amr.games.pacman.model.common.GhostState;
import de.amr.games.pacman.model.common.Pac;

/**
 * Intro scene of the Ms. Pac-Man game. The ghosts and Ms. Pac-Man are introduced one after another.
 * 
 * @author Armin Reichert
 */
public class IntroController extends FiniteStateMachine<IntroState> {

	public enum IntroState {

		BEGIN, PRESENTING_GHOST, PRESENTING_MSPACMAN, WAITING_FOR_GAME;
	}

	// the board where the actors are presented
	public final V2i tileBoardTopLeft = new V2i(6, 8);
	public final int tileBelowBoard = 17;
	public final int tileLeftOfBoard = 4;
	public final PacManGameController gameController;
	public Pac msPacMan;
	public Ghost[] ghosts;
	public int currentGhostIndex;
	public final TimedSequence<Boolean> blinking = TimedSequence.pulse().frameDuration(30);

	public IntroController(PacManGameController gameController) {
		super(IntroState.values());
		configState(IntroState.BEGIN, this::startStateTimer, this::state_BEGIN_update, null);
		configState(IntroState.PRESENTING_GHOST, this::startStateTimer, this::state_PRESENTING_GHOST_update, null);
		configState(IntroState.PRESENTING_MSPACMAN, this::startStateTimer, this::state_PRESENTING_MSPACMAN_update, null);
		configState(IntroState.WAITING_FOR_GAME, this::startStateTimer, this::state_WAITING_FOR_GAMESTART_update,
				null);
		this.gameController = gameController;
	}

	public void update() {
		updateState();
	}

	public void init() {
		msPacMan = new Pac("Ms. Pac-Man");
		msPacMan.setDir(LEFT);
		msPacMan.setPosition(t(37), t(tileBelowBoard));

		ghosts = new Ghost[] { //
				new Ghost(GameModel.RED_GHOST, "Blinky"), //
				new Ghost(GameModel.PINK_GHOST, "Pinky"), //
				new Ghost(GameModel.CYAN_GHOST, "Inky"), //
				new Ghost(GameModel.ORANGE_GHOST, "Sue"),//
		};

		for (Ghost ghost : ghosts) {
			ghost.setDir(LEFT);
			ghost.setWishDir(LEFT);
			ghost.setPosition(t(37), t(tileBelowBoard));
			ghost.state = GhostState.HUNTING_PAC;
		}

		currentGhostIndex = -1;

		changeState(IntroState.BEGIN);
	}

	private void startStateTimer() {
		stateTimer().start();
	}

	private void state_BEGIN_update() {
		if (stateTimer().isRunningSeconds(2)) {
			currentGhostIndex = 0;
			changeState(IntroState.PRESENTING_GHOST);
		}
	}

	private void state_PRESENTING_GHOST_update() {
		if (stateTimer().hasJustStarted()) {
			ghosts[currentGhostIndex].visible = true;
			ghosts[currentGhostIndex].setSpeed(1.0);
		}
		boolean ghostReachedFinalPosition = ghostEnteringStage(ghosts[currentGhostIndex]);
		if (ghostReachedFinalPosition) {
			if (currentGhostIndex == 3) {
				changeState(IntroState.PRESENTING_MSPACMAN);
			} else {
				currentGhostIndex++;
				stateTimer().reset();
				stateTimer().start();
			}
		}
		for (Ghost ghost : ghosts) {
			ghost.move();
		}
	}

	private void state_PRESENTING_MSPACMAN_update() {
		if (stateTimer().hasJustStarted()) {
			msPacMan.visible = true;
			msPacMan.stuck = false;
			msPacMan.setSpeed(1.0);
		}
		boolean msPacReachedFinalPosition = msPacManEnteringStage();
		if (msPacReachedFinalPosition) {
			changeState(IntroState.WAITING_FOR_GAME);
		}
		msPacMan.move();
	}

	private void state_WAITING_FOR_GAMESTART_update() {
		if (stateTimer().hasJustStarted()) {
			blinking.restart();
		}
		if (stateTimer().isRunningSeconds(5)) {
			gameController.stateTimer().expire();
			return;
		}
		blinking.animate();
	}

	public boolean ghostEnteringStage(Ghost ghost) {
		if (ghost.dir() == LEFT && ghost.position.x <= t(tileLeftOfBoard)) {
			ghost.setDir(UP);
			ghost.setWishDir(UP);
			return false;
		}
		if (ghost.dir() == UP && ghost.position.y <= t(tileBoardTopLeft.y) + ghost.id * 18) {
			ghost.setSpeed(0);
			return true;
		}
		return false;
	}

	public boolean msPacManEnteringStage() {
		if (msPacMan.velocity.length() > 0 && msPacMan.position.x <= t(13)) {
			msPacMan.setSpeed(0);
			return true;
		}
		return false;
	}
}