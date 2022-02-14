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

import static de.amr.games.pacman.controller.pacman.Intermission2Controller.IntermissionState.CHASING;
import static de.amr.games.pacman.controller.pacman.Intermission2Controller.IntermissionState.STRETCHED;
import static de.amr.games.pacman.controller.pacman.Intermission2Controller.IntermissionState.STUCK;
import static de.amr.games.pacman.model.world.World.t;

import de.amr.games.pacman.controller.GameController;
import de.amr.games.pacman.controller.pacman.Intermission2Controller.IntermissionState;
import de.amr.games.pacman.lib.Direction;
import de.amr.games.pacman.lib.FiniteStateMachine;
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
public class Intermission2Controller extends FiniteStateMachine<IntermissionState> {

	public enum IntermissionState {
		CHASING, STRETCHED, STUCK;
	}

	public GameController gameController;
	public Runnable playIntermissionSound = NOP;

	public Ghost blinky;
	public Pac pac;
	public GameEntity nail;

	public Intermission2Controller() {
		super(IntermissionState.values());
		configState(CHASING, this::state_CHASING_enter, this::state_CHASING_update, null);
		configState(STRETCHED, this::startStateTimer, this::state_STRETCHED_update, null);
		configState(STUCK, this::startStateTimer, this::state_STUCK_update, null);
	}

	public void init(GameController gameController) {
		this.gameController = gameController;
		playIntermissionSound.run();
		changeState(CHASING);
	}

	private void startStateTimer() {
		stateTimer().setIndefinite().start();
	}

	public int nailDistance() {
		return (int) (nail.position.x - blinky.position.x);
	}

	private void state_CHASING_enter() {
		startStateTimer();

		pac = new Pac("Pac-Man");
		pac.setMoveDir(Direction.LEFT);
		pac.setPosition(t(30), t(20));
		pac.setSpeed(1.0);
		pac.show();

		blinky = new Ghost(GameModel.RED_GHOST, "Blinky");
		blinky.state = GhostState.HUNTING_PAC;
		blinky.setMoveDir(Direction.LEFT);
		blinky.setWishDir(Direction.LEFT);
		blinky.position = pac.position.plus(t(14), 0);
		blinky.setSpeed(1.0);
		blinky.show();

		nail = new GameEntity();
		nail.setPosition(t(14), t(20) - 1);
		nail.show();
	}

	private void state_CHASING_update() {
		if (nailDistance() == 0) {
			changeState(STRETCHED);
			return;
		}
		pac.move();
		blinky.move();
	}

	private void state_STRETCHED_update() {
		int stretching = nailDistance() / 4;
		if (stretching == 3) {
			blinky.setSpeed(0);
			blinky.setMoveDir(Direction.UP);
			changeState(IntermissionState.STUCK);
			return;
		}
		blinky.setSpeed(0.3 - 0.1 * stretching);
		blinky.move();
		pac.move();
	}

	private void state_STUCK_update() {
		if (stateTimer().isRunningSeconds(2)) {
			blinky.setMoveDir(Direction.RIGHT);
		} else if (stateTimer().isRunningSeconds(6)) {
			gameController.stateTimer().expire();
			return;
		}
		blinky.move();
		pac.move();
	}
}