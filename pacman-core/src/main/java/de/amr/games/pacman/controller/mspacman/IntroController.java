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
import static de.amr.games.pacman.model.world.World.HTS;
import static de.amr.games.pacman.model.world.World.TS;
import static de.amr.games.pacman.model.world.World.t;

import de.amr.games.pacman.controller.GameController;
import de.amr.games.pacman.controller.mspacman.IntroController.IntroState;
import de.amr.games.pacman.lib.FiniteStateMachine;
import de.amr.games.pacman.lib.TimedSeq;
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
		BEGIN, GHOSTS, MSPACMAN, WAITING_FOR_GAME;
	}

	public final V2i adBoardTopLeft = new V2i(7, 11).scaled(TS);
	public final int yBelowBoard = t(20) + HTS;
	public final int xLeftOfBoard = t(5);
	public final TimedSeq<Boolean> blinking = TimedSeq.pulse().frameDuration(30);

	public GameController gameController;
	public Pac msPacMan;
	public Ghost[] ghosts;
	public int currentGhostIndex;

	public IntroController() {
		super(IntroState.values());
		configState(IntroState.BEGIN, this::state_BEGIN_enter, this::state_BEGIN_update, null);
		configState(IntroState.GHOSTS, this::startTimerIndefinite, this::state_GHOSTS_update, null);
		configState(IntroState.MSPACMAN, this::startTimerIndefinite, this::state_MSPACMAN_update, null);
		configState(IntroState.WAITING_FOR_GAME, this::startTimerIndefinite, this::state_WAITING_FOR_GAME_update, null);
	}

	public void init(GameController gameController) {
		this.gameController = gameController;
		state = null;
		changeState(IntroState.BEGIN);
	}

	private void startTimerIndefinite() {
		stateTimer().setIndefinite().start();
	}

	private void state_BEGIN_enter() {
		msPacMan = new Pac("Ms. Pac-Man");
		msPacMan.setMoveDir(LEFT);
		msPacMan.setPosition(t(36), yBelowBoard);
		ghosts = new Ghost[] { //
				new Ghost(GameModel.RED_GHOST, "Blinky"), //
				new Ghost(GameModel.PINK_GHOST, "Pinky"), //
				new Ghost(GameModel.CYAN_GHOST, "Inky"), //
				new Ghost(GameModel.ORANGE_GHOST, "Sue") //
		};
		for (Ghost ghost : ghosts) {
			ghost.setMoveDir(LEFT);
			ghost.setWishDir(LEFT);
			ghost.setPosition(t(36), yBelowBoard);
			ghost.state = GhostState.HUNTING_PAC;
		}
		currentGhostIndex = 0;
		startTimerIndefinite();
	}

	private void state_BEGIN_update() {
		if (stateTimer().isRunningSeconds(1)) {
			ghosts[currentGhostIndex].show();
			ghosts[currentGhostIndex].setSpeed(0.95);
			changeState(IntroState.GHOSTS);
		}
	}

	private boolean letGhostEnterStage(Ghost ghost) {
		ghost.move();
		if (ghost.moveDir() != UP && ghost.position.x <= xLeftOfBoard) {
			ghost.setMoveDir(UP);
			ghost.setWishDir(UP);
		}
		return ghost.position.y <= adBoardTopLeft.y + ghost.id * 18;
	}

	private void state_GHOSTS_update() {
		boolean reachedFinalPosition = letGhostEnterStage(ghosts[currentGhostIndex]);
		if (reachedFinalPosition) {
			ghosts[currentGhostIndex].setSpeed(0);
			if (currentGhostIndex == 3) {
				msPacMan.show();
				msPacMan.setSpeed(0.95);
				changeState(IntroState.MSPACMAN);
			} else {
				currentGhostIndex++;
				ghosts[currentGhostIndex].show();
				ghosts[currentGhostIndex].setSpeed(0.95);
				startTimerIndefinite();
			}
		}
	}

	private void state_MSPACMAN_update() {
		msPacMan.move();
		if (msPacMan.position.x <= t(14)) {
			msPacMan.setSpeed(0);
			blinking.restart();
			changeState(IntroState.WAITING_FOR_GAME);
		}
	}

	private void state_WAITING_FOR_GAME_update() {
		if (stateTimer().isRunningSeconds(5)) {
			gameController.stateTimer().expire();
		}
		blinking.animate();
	}
}