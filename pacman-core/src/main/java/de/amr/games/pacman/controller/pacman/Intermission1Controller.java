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
import de.amr.games.pacman.controller.pacman.Intermission1Controller.IntermissionState;
import de.amr.games.pacman.lib.Direction;
import de.amr.games.pacman.lib.FiniteStateMachine;
import de.amr.games.pacman.model.common.GameModel;
import de.amr.games.pacman.model.common.Ghost;
import de.amr.games.pacman.model.common.GhostState;
import de.amr.games.pacman.model.common.Pac;

/**
 * First intermission scene: Blinky chases Pac-Man and is then chased by a huge Pac-Man.
 * 
 * @author Armin Reichert
 */
public class Intermission1Controller extends FiniteStateMachine<IntermissionState> {

	public enum IntermissionState {
		BLINKY_CHASING_PACMAN, BIGPACMAN_CHASING_BLINKY
	}

	public final int groundY = t(20);

	public GameController gameController;
	public Runnable playIntermissionSound = NOP;
	public Ghost blinky;
	public Pac pac;

	public Intermission1Controller() {
		super(IntermissionState.values());
		configState(IntermissionState.BLINKY_CHASING_PACMAN, () -> stateTimer().setSeconds(5).start(),
				this::state_BLINKY_CHASING_PACMAN_update, null);
		configState(IntermissionState.BIGPACMAN_CHASING_BLINKY, () -> stateTimer().setSeconds(7).start(),
				this::state_BIGPACMAN_CHASING_BLINKY_update, null);
	}

	public void init(GameController gameController) {
		this.gameController = gameController;

		pac = new Pac("Pac-Man");
		pac.setDir(Direction.LEFT);
		pac.show();
		pac.setPosition(t(30), groundY);
		pac.setSpeed(1.0);

		blinky = new Ghost(GameModel.RED_GHOST, "Blinky");
		blinky.setDir(Direction.LEFT);
		blinky.setWishDir(Direction.LEFT);
		blinky.show();
		blinky.position = pac.position.plus(t(3), 0);
		blinky.setSpeed(1.04);
		blinky.state = GhostState.HUNTING_PAC;

		playIntermissionSound.run();
		changeState(IntermissionState.BLINKY_CHASING_PACMAN);
	}

	private void state_BLINKY_CHASING_PACMAN_update() {
		pac.move();
		blinky.move();
		if (stateTimer().hasExpired()) {
			changeState(IntermissionState.BIGPACMAN_CHASING_BLINKY);
		}
	}

	private void state_BIGPACMAN_CHASING_BLINKY_update() {
		if (stateTimer().hasJustStarted()) {
			blinky.setPosition(-t(2), groundY);
			blinky.setWishDir(Direction.RIGHT);
			blinky.setDir(Direction.RIGHT);
			blinky.setSpeed(1.0);
			blinky.state = GhostState.FRIGHTENED;
			pac.setDir(Direction.RIGHT);
			pac.setSpeed(1.3);
			pac.position = blinky.position.plus(-t(13), 0);
		} else if (stateTimer().hasExpired()) {
			gameController.stateTimer().expire();
		} else {
			pac.move();
			blinky.move();
		}
	}
}