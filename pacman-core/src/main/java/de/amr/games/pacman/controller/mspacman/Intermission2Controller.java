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

import static de.amr.games.pacman.controller.mspacman.Intermission2Controller.IntermissionState.CHASING;
import static de.amr.games.pacman.controller.mspacman.Intermission2Controller.IntermissionState.FLAP;
import static de.amr.games.pacman.model.common.world.World.t;

import de.amr.games.pacman.controller.GameController;
import de.amr.games.pacman.controller.mspacman.Intermission2Controller.IntermissionState;
import de.amr.games.pacman.lib.Direction;
import de.amr.games.pacman.lib.FiniteStateMachine;
import de.amr.games.pacman.model.common.Pac;
import de.amr.games.pacman.model.mspacman.Flap;

/**
 * Intermission scene 2: "The chase".
 * <p>
 * Pac-Man and Ms. Pac-Man chase each other across the screen over and over. After three turns, they both rapidly run
 * from left to right and right to left. (Played after round 5)
 * 
 * @author Armin Reichert
 */
public class Intermission2Controller extends FiniteStateMachine<IntermissionState> {

	public enum IntermissionState {
		FLAP, CHASING;
	}

	public final int upperY = t(12), middleY = t(18), lowerY = t(24);
	public final GameController gameController;
	public Runnable playIntermissionSound = NOP;
	public Runnable playFlapAnimation = NOP;
	public Flap flap;
	public Pac pacMan, msPacMan;

	public Intermission2Controller(GameController gameController) {
		super(IntermissionState.values());
		configState(FLAP, this::state_FLAP_enter, this::state_FLAP_update, null);
		configState(CHASING, this::state_CHASING_enter, this::state_CHASING_update, null);
		this.gameController = gameController;
	}

	public void init() {
		state = null;
		changeState(FLAP);
	}

	private void state_FLAP_enter() {
		stateTimer().setIndefinite().start();

		playIntermissionSound.run();

		flap = new Flap();
		flap.number = 2;
		flap.text = "THE CHASE";
		flap.setPosition(t(3), t(10));
		flap.show();

		pacMan = new Pac("Pac-Man");
		pacMan.setMoveDir(Direction.RIGHT);

		msPacMan = new Pac("Ms. Pac-Man");
		msPacMan.setMoveDir(Direction.RIGHT);
	}

	private void state_FLAP_update() {
		if (stateTimer().isRunningSeconds(1)) {
			playFlapAnimation.run();
		} else if (stateTimer().isRunningSeconds(2)) {
			flap.hide();
		} else if (stateTimer().isRunningSeconds(4.5)) {
			changeState(IntermissionState.CHASING);
		}
	}

	private void state_CHASING_enter() {
		stateTimer().setIndefinite().start();
	}

	private void state_CHASING_update() {
		if (stateTimer().isRunningSeconds(1.5)) {
			pacMan.setPosition(-t(2), upperY);
			pacMan.setMoveDir(Direction.RIGHT);
			pacMan.setSpeed(2.0);
			pacMan.show();
			msPacMan.setPosition(-t(8), upperY);
			msPacMan.setMoveDir(Direction.RIGHT);
			msPacMan.setSpeed(2.0);
			msPacMan.show();
		} else if (stateTimer().isRunningSeconds(6)) {
			pacMan.setPosition(t(36), lowerY);
			pacMan.setMoveDir(Direction.LEFT);
			pacMan.setSpeed(2.0);
			msPacMan.setPosition(t(30), lowerY);
			msPacMan.setMoveDir(Direction.LEFT);
			msPacMan.setSpeed(2.0);
		} else if (stateTimer().isRunningSeconds(10.5)) {
			pacMan.setMoveDir(Direction.RIGHT);
			pacMan.setSpeed(2.0);
			msPacMan.setPosition(t(-8), middleY);
			msPacMan.setMoveDir(Direction.RIGHT);
			msPacMan.setSpeed(2.0);
			pacMan.setPosition(t(-2), middleY);
		} else if (stateTimer().isRunningSeconds(14.5)) {
			pacMan.setPosition(t(42), upperY);
			pacMan.setMoveDir(Direction.LEFT);
			pacMan.setSpeed(4.0);
			msPacMan.setPosition(t(30), upperY);
			msPacMan.setMoveDir(Direction.LEFT);
			msPacMan.setSpeed(4.0);
		} else if (stateTimer().isRunningSeconds(15.5)) {
			pacMan.setPosition(t(-2), lowerY);
			pacMan.setMoveDir(Direction.RIGHT);
			pacMan.setSpeed(4.0);
			msPacMan.setPosition(t(-14), lowerY);
			msPacMan.setMoveDir(Direction.RIGHT);
			msPacMan.setSpeed(4.0);
		} else if (stateTimer().isRunningSeconds(20)) {
			gameController.stateTimer().expire();
			return;
		}
		pacMan.move();
		msPacMan.move();
	}
}