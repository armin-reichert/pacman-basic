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
import static de.amr.games.pacman.model.common.world.World.HTS;
import static de.amr.games.pacman.model.common.world.World.TS;
import static de.amr.games.pacman.model.common.world.World.t;

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
 * Intro scene of the Ms. Pac-Man game.
 * <p>
 * The ghosts and Ms. Pac-Man are introduced on a billboard and are marching in one after another.
 * 
 * @author Armin Reichert
 */
public class IntroController extends FiniteStateMachine<IntroState> {

	public enum IntroState {
		BEGIN, GHOSTS, MSPACMAN, READY;
	}

	public final V2i boardTopLeft = new V2i(7, 11).scaled(TS);
	public final int yBelowBoard = t(20) + HTS;
	public final int xLeftOfBoard = t(5);
	public final TimedSeq<Boolean> blinking = TimedSeq.pulse().frameDuration(30).restart();
	public final GameController gameController;
	public final Pac msPacMan;
	public final Ghost[] ghosts;
	public int ghostIndex;

	public IntroController(GameController gameController) {
		super(IntroState.values());
		configState(IntroState.BEGIN, this::state_BEGIN_enter, this::state_BEGIN_update, null);
		configState(IntroState.GHOSTS, null, this::state_GHOSTS_update, null);
		configState(IntroState.MSPACMAN, null, this::state_MSPACMAN_update, null);
		configState(IntroState.READY, this::startTimerIndefinite, this::state_READY_update, null);
		this.gameController = gameController;
		msPacMan = new Pac("Ms. Pac-Man");
		ghosts = new Ghost[] { //
				new Ghost(GameModel.RED_GHOST, "Blinky"), //
				new Ghost(GameModel.PINK_GHOST, "Pinky"), //
				new Ghost(GameModel.CYAN_GHOST, "Inky"), //
				new Ghost(GameModel.ORANGE_GHOST, "Sue") //
		};
	}

	public void init() {
		state = null;
		changeState(IntroState.BEGIN);
	}

	private void startTimerIndefinite() {
		stateTimer().setIndefinite().start();
	}

	private void state_BEGIN_enter() {
		startTimerIndefinite();
		msPacMan.setMoveDir(LEFT);
		msPacMan.setPosition(t(36), yBelowBoard);
		msPacMan.setSpeed(0.95);
		msPacMan.show();
		for (Ghost ghost : ghosts) {
			ghost.state = GhostState.HUNTING_PAC;
			ghost.setMoveDir(LEFT);
			ghost.setWishDir(LEFT);
			ghost.setPosition(t(36), yBelowBoard);
			ghost.setSpeed(0.95);
			ghost.show();
		}
		ghostIndex = 0;
	}

	private void state_BEGIN_update() {
		if (stateTimer().isRunningSeconds(1)) {
			changeState(IntroState.GHOSTS);
		}
	}

	private void state_GHOSTS_update() {
		Ghost ghost = ghosts[ghostIndex];
		ghost.move();
		if (ghost.moveDir() != UP && ghost.position.x <= xLeftOfBoard) {
			ghost.setMoveDir(UP);
			ghost.setWishDir(UP);
		}
		if (ghost.position.y <= boardTopLeft.y + ghost.id * 18) {
			ghost.setSpeed(0);
			if (++ghostIndex == ghosts.length) {
				changeState(IntroState.MSPACMAN);
			}
		}
	}

	private void state_MSPACMAN_update() {
		msPacMan.move();
		if (msPacMan.position.x <= t(14)) {
			msPacMan.setSpeed(0);
			changeState(IntroState.READY);
		}
	}

	private void state_READY_update() {
		blinking.advance();
		if (stateTimer().isRunningSeconds(5)) {
			gameController.stateTimer().expire();
		}
	}
}