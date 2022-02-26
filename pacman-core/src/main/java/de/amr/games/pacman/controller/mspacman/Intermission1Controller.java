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
import static de.amr.games.pacman.controller.mspacman.Intermission1Controller.IntermissonState.READY_TO_PLAY;
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
		FLAP, CHASED_BY_GHOSTS, COMING_TOGETHER, READY_TO_PLAY;
	}

	public final int upperY = t(12), lowerY = t(24), middleY = t(18);

	public GameController gameController;
	public Runnable playIntermissionSound = NOP;
	public Runnable playFlapAnimation = NOP;

	public Flap flap;
	public Pac pacMan, msPac;
	public Ghost pinky, inky;
	public GameEntity heart;
	public boolean ghostsMet;

	public Intermission1Controller() {
		super(IntermissonState.values());
		configState(FLAP, this::state_FLAP_enter, this::state_FLAP_update, null);
		configState(CHASED_BY_GHOSTS, null, this::state_CHASED_BY_GHOSTS_update, null);
		configState(COMING_TOGETHER, null, this::state_COMING_TOGETHER_update, null);
		configState(READY_TO_PLAY, () -> stateTimer().setSeconds(4).start(), this::state_READY_TO_PLAY_update, null);
	}

	public void init(GameController gameController) {
		this.gameController = gameController;
		changeState(FLAP);
	}

	private void state_FLAP_enter() {
		stateTimer().setSeconds(2).start();

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
		inky.position = pacMan.position.plus(-t(3), 0);
		inky.show();

		msPac = new Pac("Ms. Pac-Man");
		msPac.setMoveDir(Direction.LEFT);
		msPac.setPosition(t(30), lowerY);
		msPac.show();

		pinky = new Ghost(GameModel.PINK_GHOST, "Pinky");
		pinky.setMoveDir(Direction.LEFT);
		pinky.setWishDir(Direction.LEFT);
		pinky.position = msPac.position.plus(t(3), 0);
		pinky.show();

		heart = new GameEntity();
		ghostsMet = false;
	}

	private void state_FLAP_update() {
		if (stateTimer().isRunningSeconds(1)) {
			playFlapAnimation.run();
		}
		if (stateTimer().hasExpired()) {
			flap.hide();
			pacMan.setSpeed(1.0);
			msPac.setSpeed(1.0);
			inky.setSpeed(1.0);
			pinky.setSpeed(1.0);
			playIntermissionSound.run();
			changeState(IntermissonState.CHASED_BY_GHOSTS);
		}
	}

	private void state_CHASED_BY_GHOSTS_update() {
		if (inky.position.x > t(30)) {
			msPac.setPosition(t(-2), middleY);
			msPac.setMoveDir(Direction.RIGHT);
			pacMan.setPosition(t(30), middleY);
			pacMan.setMoveDir(Direction.LEFT);
			inky.setPosition(t(33), middleY);
			inky.setMoveDir(Direction.LEFT);
			inky.setWishDir(Direction.LEFT);
			pinky.setPosition(t(-5), middleY);
			pinky.setMoveDir(Direction.RIGHT);
			pinky.setWishDir(Direction.RIGHT);
			changeState(IntermissonState.COMING_TOGETHER);
			return;
		}
		inky.move();
		pacMan.move();
		pinky.move();
		msPac.move();
	}

	private void state_COMING_TOGETHER_update() {

		// Pac-Man and Ms. Pac-Man reach end position
		if (pacMan.moveDir() == Direction.UP && pacMan.position.y < upperY) {
			pacMan.setSpeed(0);
			msPac.setSpeed(0);
			pacMan.setMoveDir(Direction.LEFT);
			msPac.setMoveDir(Direction.RIGHT);
			heart.setPosition((pacMan.position.x + msPac.position.x) / 2, pacMan.position.y - t(2));
			heart.show();
			inky.setSpeed(0);
			pinky.setSpeed(0);
			changeState(IntermissonState.READY_TO_PLAY);
			return;
		}

		// Pac-Man and Ms. Pac-Man meet
		if (pacMan.moveDir() == Direction.LEFT && pacMan.position.x < t(15)) {
			pacMan.setMoveDir(Direction.UP);
			msPac.setMoveDir(Direction.UP);
		}

		// Inky and Pinky collide
		if (!ghostsMet && inky.position.x - pinky.position.x < 16) {
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

		// Guys move, avoid falling under ground level
		inky.move();
		if (inky.position.y > middleY) {
			inky.setPosition(inky.position.x, middleY);
		}

		pinky.move();
		if (pinky.position.y > middleY) {
			pinky.setPosition(pinky.position.x, middleY);
		}

		pacMan.move();
		msPac.move();
	}

	private void state_READY_TO_PLAY_update() {
		if (stateTimer().isRunningSeconds(2)) {
			inky.hide();
			pinky.hide();
		}
		if (stateTimer().hasExpired()) {
			gameController.stateTimer().expire();
		}
	}
}