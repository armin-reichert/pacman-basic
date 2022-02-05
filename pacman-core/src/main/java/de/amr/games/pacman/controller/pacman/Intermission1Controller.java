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

import static de.amr.games.pacman.controller.pacman.Intermission1Controller.IntermissionState.CHASING_BLINKY;
import static de.amr.games.pacman.controller.pacman.Intermission1Controller.IntermissionState.CHASING_PACMAN;
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
		CHASING_PACMAN, CHASING_BLINKY
	}

	public GameController gameController;
	public Runnable playIntermissionSound = NOP;
	public Ghost blinky;
	public Pac pac;

	public Intermission1Controller() {
		super(IntermissionState.values());
		configState(CHASING_PACMAN, //
				this::state_CHASING_PACMAN_enter, this::state_CHASING_PACMAN_update, null);
		configState(CHASING_BLINKY, //
				this::state_CHASING_BLINKY_enter, this::state_CHASING_BLINKY_update, null);
	}

	public void init(GameController gameController) {
		this.gameController = gameController;
		playIntermissionSound.run();
		changeState(CHASING_PACMAN);
	}

	private void state_CHASING_PACMAN_enter() {
		stateTimer().setSeconds(5).start();

		pac = new Pac("Pac-Man");
		pac.setDir(Direction.LEFT);
		pac.setPosition(t(30), t(20));
		pac.setSpeed(1.0);
		pac.show();

		blinky = new Ghost(GameModel.RED_GHOST, "Blinky");
		blinky.state = GhostState.HUNTING_PAC;
		blinky.setDir(Direction.LEFT);
		blinky.setWishDir(Direction.LEFT);
		blinky.position = pac.position.plus(t(3) + 0.5, 0);
		blinky.setSpeed(1.05);
		blinky.show();
	}

	private void state_CHASING_PACMAN_update() {
		if (stateTimer().ticked() < 60) {
			return;
		}
		if (stateTimer().hasExpired()) {
			changeState(CHASING_BLINKY);
			return;
		}
		pac.move();
		blinky.move();
	}

	private void state_CHASING_BLINKY_enter() {
		stateTimer().setSeconds(7).start();

		pac.setDir(Direction.RIGHT);
		pac.setPosition(-t(24), t(20));
		pac.setSpeed(1.0);

		blinky.state = GhostState.FRIGHTENED;
		blinky.setDir(Direction.RIGHT);
		blinky.setWishDir(Direction.RIGHT);
		blinky.setPosition(-t(1), t(20));
		blinky.setSpeed(0.6);
	}

	private void state_CHASING_BLINKY_update() {
		if (stateTimer().hasExpired()) {
			gameController.stateTimer().expire();
			return;
		}
		pac.move();
		blinky.move();
	}
}