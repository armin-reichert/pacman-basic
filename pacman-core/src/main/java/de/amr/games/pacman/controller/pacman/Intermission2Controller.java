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
		WALKING, GETTING_STUCK, STUCK;
	}

	public GameController gameController;
	public Runnable playIntermissionSound = NOP;

	public Ghost blinky;
	public Pac pac;
	public GameEntity nail;

	public Intermission2Controller() {
		super(IntermissionState.values());
		configState(IntermissionState.WALKING, this::startStateTimer, this::state_WALKING_update, null);
		configState(IntermissionState.GETTING_STUCK, this::startStateTimer, this::state_GETTING_STUCK_update, null);
		configState(IntermissionState.STUCK, this::startStateTimer, this::state_STUCK_update, null);
	}

	public void init(GameController gameController) {
		this.gameController = gameController;

		pac = new Pac("Pac-Man");
		pac.setDir(Direction.LEFT);
		pac.show();
		pac.setPosition(t(30), t(20));
		pac.setSpeed(1.0);

		blinky = new Ghost(GameModel.RED_GHOST, "Blinky");
		blinky.setDir(Direction.LEFT);
		blinky.setWishDir(Direction.LEFT);
		blinky.show();
		blinky.position = pac.position.plus(t(14), 0);
		blinky.setSpeed(1.0);
		blinky.state = GhostState.HUNTING_PAC;

		nail = new GameEntity();
		nail.show();
		nail.setPosition(t(14), t(20) - 1);

		playIntermissionSound.run();
		changeState(IntermissionState.WALKING);
	}

	private void startStateTimer() {
		stateTimer().setIndefinite().start();
	}

	public int nailDistance() {
		return (int) (nail.position.x - blinky.position.x);
	}

	private void state_WALKING_update() {
		blinky.move();
		pac.move();
		if (nailDistance() == 0) {
			changeState(IntermissionState.GETTING_STUCK);
		}
	}

	private void state_GETTING_STUCK_update() {
		blinky.move();
		pac.move();
		int stretching = nailDistance() / 4;
		blinky.setSpeed(0.3 - 0.1 * stretching);
		if (stretching == 3) {
			blinky.setSpeed(0);
			blinky.setDir(Direction.UP);
			changeState(IntermissionState.STUCK);
		}
	}

	private void state_STUCK_update() {
		blinky.move();
		pac.move();
		if (stateTimer().isRunningSeconds(2)) {
			blinky.setDir(Direction.RIGHT);
		} else if (stateTimer().isRunningSeconds(6)) {
			gameController.stateTimer().expire();
		}
	}
}