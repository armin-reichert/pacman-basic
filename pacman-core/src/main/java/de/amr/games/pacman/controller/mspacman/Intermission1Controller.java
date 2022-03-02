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

import static de.amr.games.pacman.controller.mspacman.Intermission1Controller.IntermissonState.CHASED_BY_GHOSTS;
import static de.amr.games.pacman.controller.mspacman.Intermission1Controller.IntermissonState.COMING_TOGETHER;
import static de.amr.games.pacman.controller.mspacman.Intermission1Controller.IntermissonState.FLAP;
import static de.amr.games.pacman.controller.mspacman.Intermission1Controller.IntermissonState.IN_HEAVEN;
import static de.amr.games.pacman.model.world.World.t;

import de.amr.games.pacman.controller.GameController;
import de.amr.games.pacman.controller.mspacman.Intermission1Controller.IntermissonState;
import de.amr.games.pacman.lib.Direction;
import de.amr.games.pacman.lib.FiniteStateMachine;
import de.amr.games.pacman.lib.V2d;
import de.amr.games.pacman.model.common.GameEntity;
import de.amr.games.pacman.model.common.GameModel;
import de.amr.games.pacman.model.common.Ghost;
import de.amr.games.pacman.model.common.Pac;
import de.amr.games.pacman.model.mspacman.entities.Flap;

/**
 * Intermission scene 1: "They meet".
 * <p>
 * Pac-Man leads Inky and Ms. Pac-Man leads Pinky. Soon, the two Pac-Men are about to collide, they quickly move
 * upwards, causing Inky and Pinky to collide and vanish. Finally, Pac-Man and Ms. Pac-Man face each other at the top of
 * the screen and a big pink heart appears above them. (Played after round 2)
 * 
 * @author Armin Reichert
 */
public class Intermission1Controller extends FiniteStateMachine<IntermissonState> {

	public enum IntermissonState {
		FLAP, CHASED_BY_GHOSTS, COMING_TOGETHER, IN_HEAVEN;
	}

	public final int upperY = t(12), middleY = t(18), lowerY = t(24);
	public final GameController gameController;
	public Runnable playIntermissionSound = NOP;
	public Runnable playFlapAnimation = NOP;
	public Flap flap;
	public Pac pacMan, msPac;
	public Ghost pinky, inky;
	public GameEntity heart;
	public boolean ghostsMet;

	public Intermission1Controller(GameController gameController) {
		super(IntermissonState.values());
		configState(FLAP, this::state_FLAP_enter, this::state_FLAP_update, null);
		configState(CHASED_BY_GHOSTS, this::state_CHASED_BY_GHOSTS_enter, this::state_CHASED_BY_GHOSTS_update, null);
		configState(COMING_TOGETHER, this::state_COMING_TOGETHER_enter, this::state_COMING_TOGETHER_update, null);
		configState(IN_HEAVEN, this::state_IN_HEAVEN_enter, this::state_IN_HEAVEN_update, null);
		this.gameController = gameController;
	}

	public void init() {
		changeState(FLAP);
	}

	private void state_FLAP_enter() {
		stateTimer().setSeconds(2).start();
		playIntermissionSound.run();

		flap = new Flap();
		flap.number = 1;
		flap.text = "THEY MEET";
		flap.setPosition(t(3), t(10));
		flap.show();

		pacMan = new Pac("Pac-Man");
		pacMan.setMoveDir(Direction.RIGHT);
		pacMan.setPosition(-t(2), upperY);
		pacMan.show();

		inky = new Ghost(GameModel.CYAN_GHOST, "Inky");
		inky.setMoveDir(Direction.RIGHT);
		inky.setWishDir(Direction.RIGHT);
		inky.position = pacMan.position.minus(t(6), 0);
		inky.show();

		msPac = new Pac("Ms. Pac-Man");
		msPac.setMoveDir(Direction.LEFT);
		msPac.setPosition(t(30), lowerY);
		msPac.show();

		pinky = new Ghost(GameModel.PINK_GHOST, "Pinky");
		pinky.setMoveDir(Direction.LEFT);
		pinky.setWishDir(Direction.LEFT);
		pinky.position = msPac.position.plus(t(6), 0);
		pinky.show();

		heart = new GameEntity();
		ghostsMet = false;
	}

	private void state_FLAP_update() {
		if (stateTimer().isRunningSeconds(1)) {
			playFlapAnimation.run();
		}
		if (stateTimer().hasExpired()) {
			changeState(IntermissonState.CHASED_BY_GHOSTS);
		}
	}

	private void state_CHASED_BY_GHOSTS_enter() {
		flap.hide();
		pacMan.setSpeed(0.9);
		msPac.setSpeed(0.9);
		inky.setSpeed(1);
		pinky.setSpeed(1);
	}

	private void state_CHASED_BY_GHOSTS_update() {
		if (inky.position.x > t(30)) {
			changeState(IntermissonState.COMING_TOGETHER);
			return;
		}
		inky.move();
		pacMan.move();
		pinky.move();
		msPac.move();
	}

	private void state_COMING_TOGETHER_enter() {
		msPac.setPosition(t(-3), middleY);
		msPac.setMoveDir(Direction.RIGHT);

		pinky.position = msPac.position.minus(t(5), 0);
		pinky.setMoveDir(Direction.RIGHT);
		pinky.setWishDir(Direction.RIGHT);

		pacMan.setPosition(t(31), middleY);
		pacMan.setMoveDir(Direction.LEFT);

		inky.position = pacMan.position.plus(t(5), 0);
		inky.setMoveDir(Direction.LEFT);
		inky.setWishDir(Direction.LEFT);
	}

	private void state_COMING_TOGETHER_update() {

		// Pac-Man and Ms. Pac-Man reach end position
		if (pacMan.moveDir() == Direction.UP && pacMan.position.y < upperY) {
			changeState(IntermissonState.IN_HEAVEN);
			return;
		}

		// Pac-Man and Ms. Pac-Man meet
		if (pacMan.moveDir() == Direction.LEFT && pacMan.position.x - msPac.position.x < t(2)) {
			pacMan.setMoveDir(Direction.UP);
			pacMan.setSpeed(0.75);
			msPac.setMoveDir(Direction.UP);
			msPac.setSpeed(0.75);
		}

		// Inky and Pinky collide
		if (!ghostsMet && inky.position.x - pinky.position.x < t(2)) {
			ghostsMet = true;

			inky.setMoveDir(Direction.RIGHT);
			inky.setWishDir(Direction.RIGHT);
			inky.setSpeed(0.5);
			inky.velocity = inky.velocity.plus(new V2d(0, -2.0));
			inky.acceleration = new V2d(0, 0.4);

			pinky.setMoveDir(Direction.LEFT);
			pinky.setWishDir(Direction.LEFT);
			pinky.setSpeed(0.5);
			pinky.velocity = pinky.velocity.plus(new V2d(0, -2.0));
			pinky.acceleration = new V2d(0, 0.4);
		}

		pacMan.move();
		msPac.move();
		inky.move();
		pinky.move();

		// Avoid falling under ground level
		if (inky.position.y > middleY) {
			inky.setPosition(inky.position.x, middleY);
		}
		if (pinky.position.y > middleY) {
			pinky.setPosition(pinky.position.x, middleY);
		}
	}

	private void state_IN_HEAVEN_enter() {
		stateTimer().setSeconds(3).start();

		pacMan.setSpeed(0);
		pacMan.setMoveDir(Direction.LEFT);
		msPac.setSpeed(0);
		msPac.setMoveDir(Direction.RIGHT);
		inky.setSpeed(0);
		pinky.setSpeed(0);

		heart.setPosition((pacMan.position.x + msPac.position.x) / 2, pacMan.position.y - t(2));
		heart.show();
	}

	private void state_IN_HEAVEN_update() {
		if (stateTimer().isRunningSeconds(0.5)) {
			inky.hide();
			pinky.hide();
		}
		if (stateTimer().hasExpired()) {
			gameController.stateTimer().expire();
		}
	}
}