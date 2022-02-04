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
import de.amr.games.pacman.controller.pacman.Intermission3Controller.IntermissionState;
import de.amr.games.pacman.lib.Direction;
import de.amr.games.pacman.lib.FiniteStateMachine;
import de.amr.games.pacman.model.common.GameModel;
import de.amr.games.pacman.model.common.Ghost;
import de.amr.games.pacman.model.common.GhostState;
import de.amr.games.pacman.model.common.Pac;

/**
 * Third intermission scene: Blinky in shred dress chases Pac-Man, comes back half-naked drawing its dress over the
 * floor.
 * 
 * @author Armin Reichert
 */
public class Intermission3Controller extends FiniteStateMachine<IntermissionState> {

	public enum IntermissionState {
		CHASING_PACMAN, RETURNING_HALF_NAKED;
	}

	public GameController gameController;
	public Runnable playIntermissionSound = NOP;

	public Ghost blinky;
	public Pac pac;

	public Intermission3Controller() {
		super(IntermissionState.values());
		configState(IntermissionState.CHASING_PACMAN, this::startStateTimer, this::state_CHASING_PACMAN_update, null);
		configState(IntermissionState.RETURNING_HALF_NAKED, this::startStateTimer, this::state_RETURNING_HALF_NAKED_update,
				null);
	}

	public void init(GameController gameController) {
		this.gameController = gameController;

		pac = new Pac("Pac-Man");
		pac.setDir(Direction.LEFT);
		pac.show();
		pac.setPosition(t(40), t(20));
		pac.setSpeed(1.2);

		blinky = new Ghost(GameModel.RED_GHOST, "Blinky");
		blinky.setDir(Direction.LEFT);
		blinky.setWishDir(Direction.LEFT);
		blinky.show();
		blinky.position = pac.position.plus(t(8), 0);
		blinky.setSpeed(1.2);
		blinky.state = GhostState.HUNTING_PAC;

		playIntermissionSound.run();
		changeState(IntermissionState.CHASING_PACMAN);
	}

	private void startStateTimer() {
		stateTimer().setIndefinite().start();
	}

	private void state_CHASING_PACMAN_update() {
		pac.move();
		blinky.move();
		if (blinky.position.x <= -t(15)) {
			pac.setSpeed(0);
			blinky.setDir(Direction.RIGHT);
			blinky.setWishDir(Direction.RIGHT);
			changeState(IntermissionState.RETURNING_HALF_NAKED);
		}
	}

	private void state_RETURNING_HALF_NAKED_update() {
		blinky.move();
		pac.move();
		if (blinky.position.x > t(53)) {
			gameController.stateTimer().expire();
		}
	}
}