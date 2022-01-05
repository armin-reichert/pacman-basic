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
import static de.amr.games.pacman.model.world.PacManGameWorld.HTS;
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
		BEGIN, PRESENTING_GHOSTS, PRESENTING_MSPACMAN, WAITING_FOR_GAME;
	}

	public final V2i tileTitle = new V2i(9, 8);
	public final V2i tileBoardTopLeft = new V2i(7, 11);
	public final int yBelowBoard = t(20) + HTS;
	public final int xLeftOfBoard = t(5);
	public final PacManGameController gameController;
	public Pac msPacMan;
	public Ghost[] ghosts;
	public int currentGhostIndex;
	public final TimedSequence<Boolean> blinking = TimedSequence.pulse().frameDuration(30);
	public final TimedSequence<Boolean> slowBlinking = TimedSequence.pulse().frameDuration(30);

	public IntroController(PacManGameController gameController) {
		super(IntroState.values());
		configState(IntroState.BEGIN, this::state_BEGIN_enter, this::state_BEGIN_update, null);
		configState(IntroState.PRESENTING_GHOSTS, this::restartStateTimer, this::state_PRESENTING_GHOSTS_update, null);
		configState(IntroState.PRESENTING_MSPACMAN, this::restartStateTimer, this::state_PRESENTING_MSPACMAN_update,
				null);
		configState(IntroState.WAITING_FOR_GAME, this::restartStateTimer, this::state_WAITING_FOR_GAME_update, null);
		this.gameController = gameController;
	}

	public void update() {
		updateState();
	}

	public void init() {
		changeState(IntroState.BEGIN);
	}

	private void restartStateTimer() {
		stateTimer().reset();
		stateTimer().start();
	}

	private void state_BEGIN_enter() {
		restartStateTimer();
		msPacMan = new Pac("Ms. Pac-Man");
		msPacMan.setDir(LEFT);
		msPacMan.setPosition(t(36), yBelowBoard);
		ghosts = new Ghost[] { //
				new Ghost(GameModel.RED_GHOST, "Blinky"), //
				new Ghost(GameModel.PINK_GHOST, "Pinky"), //
				new Ghost(GameModel.CYAN_GHOST, "Inky"), //
				new Ghost(GameModel.ORANGE_GHOST, "Sue"),//
		};
		for (Ghost ghost : ghosts) {
			ghost.setDir(LEFT);
			ghost.setWishDir(LEFT);
			ghost.setPosition(t(36), yBelowBoard);
			ghost.state = GhostState.HUNTING_PAC;
		}
		currentGhostIndex = 0;
	}

	private void state_BEGIN_update() {
		if (stateTimer().isRunningSeconds(2)) {
			changeState(IntroState.PRESENTING_GHOSTS);
		}
	}

	private void state_PRESENTING_GHOSTS_update() {
		if (stateTimer().hasJustStarted()) {
			ghosts[currentGhostIndex].visible = true;
			ghosts[currentGhostIndex].setSpeed(0.95);
		}
		boolean reachedFinalPosition = letGhostEnterStage(ghosts[currentGhostIndex]);
		if (reachedFinalPosition) {
			if (currentGhostIndex == 3) {
				changeState(IntroState.PRESENTING_MSPACMAN);
				return;
			} else {
				currentGhostIndex++;
				restartStateTimer();
			}
		}
	}

	private void state_PRESENTING_MSPACMAN_update() {
		if (stateTimer().hasJustStarted()) {
			msPacMan.visible = true;
			msPacMan.setSpeed(0.95);
		}
		boolean reachedFinalPosition = letMsPacManEnterStage();
		if (reachedFinalPosition) {
			slowBlinking.restart();
			changeState(IntroState.WAITING_FOR_GAME);
			return;
		}
	}

	private void state_WAITING_FOR_GAME_update() {
		slowBlinking.animate();
		if (stateTimer().isRunningSeconds(5)) {
			gameController.stateTimer().expire();
		}
		blinking.animate();
	}

	private boolean letGhostEnterStage(Ghost ghost) {
		if (ghost.position.y <= t(tileBoardTopLeft.y) + ghost.id * 18) {
			ghost.setSpeed(0);
			return true;
		}
		if (ghost.position.x <= xLeftOfBoard) {
			ghost.setDir(UP);
			ghost.setWishDir(UP);
		}
		ghost.move();
		return false;
	}

	private boolean letMsPacManEnterStage() {
		if (msPacMan.position.x <= t(14)) {
			msPacMan.setSpeed(0);
			return true;
		}
		msPacMan.move();
		return false;
	}
}