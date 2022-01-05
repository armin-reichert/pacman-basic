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

import static de.amr.games.pacman.model.world.PacManGameWorld.t;

import de.amr.games.pacman.controller.PacManGameController;
import de.amr.games.pacman.controller.mspacman.Intermission1Controller.IntermissonState;
import de.amr.games.pacman.lib.Direction;
import de.amr.games.pacman.lib.FiniteStateMachine;
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
public abstract class Intermission1Controller extends FiniteStateMachine<IntermissonState> {

	public enum IntermissonState {
		FLAP, CHASED_BY_GHOSTS, COMING_TOGETHER, READY_TO_PLAY;
	}

	public final int upperY = t(12), lowerY = t(24), middleY = t(18);
	public final PacManGameController gameController;
	public Flap flap;
	public Pac pacMan, msPac;
	public Ghost pinky, inky;
	public GameEntity heart;
	public boolean ghostsMet;

	public Intermission1Controller(PacManGameController gameController) {
		super(IntermissonState.values());
		configState(IntermissonState.FLAP, () -> startStateTimer(2), this::state_FLAP_update, null);
		configState(IntermissonState.CHASED_BY_GHOSTS, null, this::state_CHASED_BY_GHOSTS_update, null);
		configState(IntermissonState.COMING_TOGETHER, null, this::state_COMING_TOGETHER_update, null);
		configState(IntermissonState.READY_TO_PLAY, () -> startStateTimer(4), this::state_READY_TO_PLAY_update, null);
		this.gameController = gameController;
	}

	public abstract void playIntermissionSound();

	public abstract void playFlapAnimation();

	public void update() {
		updateState();
	}

	public void init() {
		flap = new Flap(1, "THEY MEET");
		flap.setPosition(t(3), t(10));
		flap.visible = true;

		pacMan = new Pac("Pac-Man");
		pacMan.setDir(Direction.RIGHT);
		pacMan.setPosition(-t(2), upperY);
		pacMan.visible = true;

		inky = new Ghost(GameModel.CYAN_GHOST, "Inky");
		inky.setDir(Direction.RIGHT);
		inky.setWishDir(Direction.RIGHT);
		inky.position = pacMan.position.plus(-t(3), 0);
		inky.visible = true;

		msPac = new Pac("Ms. Pac-Man");
		msPac.setDir(Direction.LEFT);
		msPac.setPosition(t(30), lowerY);
		msPac.visible = true;

		pinky = new Ghost(GameModel.PINK_GHOST, "Pinky");
		pinky.setDir(Direction.LEFT);
		pinky.setWishDir(Direction.LEFT);
		pinky.position = msPac.position.plus(t(3), 0);
		pinky.visible = true;

		heart = new GameEntity();
		ghostsMet = false;

		changeState(IntermissonState.FLAP);
	}

	private void startStateTimer(double seconds) {
		stateTimer().resetSeconds(seconds);
		stateTimer().start();
	}

	private void state_FLAP_update() {
		if (stateTimer().isRunningSeconds(1)) {
			playFlapAnimation();
		}
		if (stateTimer().hasExpired()) {
			flap.visible = false;
			playIntermissionSound();
			pacMan.setSpeed(1.0);
			msPac.setSpeed(1.0);
			inky.setSpeed(1.0);
			pinky.setSpeed(1.0);
			changeState(IntermissonState.CHASED_BY_GHOSTS);
		}
	}

	private void state_CHASED_BY_GHOSTS_update() {
		inky.move();
		pacMan.move();
		pinky.move();
		msPac.move();
		if (inky.position.x > t(30)) {
			msPac.setPosition(t(-2), middleY);
			msPac.setDir(Direction.RIGHT);
			pacMan.setPosition(t(30), middleY);
			pacMan.setDir(Direction.LEFT);
			inky.setPosition(t(33), middleY);
			inky.setDir(Direction.LEFT);
			inky.setWishDir(Direction.LEFT);
			pinky.setPosition(t(-5), middleY);
			pinky.setDir(Direction.RIGHT);
			pinky.setWishDir(Direction.RIGHT);
			changeState(IntermissonState.COMING_TOGETHER);
		}
	}

	private void state_COMING_TOGETHER_update() {
		inky.move();
		pinky.move();
		pacMan.move();
		msPac.move();
		if (pacMan.dir() == Direction.LEFT && pacMan.position.x < t(15)) {
			pacMan.setDir(Direction.UP);
			msPac.setDir(Direction.UP);
		}
		if (pacMan.dir() == Direction.UP && pacMan.position.y < upperY) {
			pacMan.setSpeed(0);
			msPac.setSpeed(0);
			pacMan.setDir(Direction.LEFT);
			msPac.setDir(Direction.RIGHT);
			heart.setPosition((pacMan.position.x + msPac.position.x) / 2, pacMan.position.y - t(2));
			heart.visible = true;
			inky.setSpeed(0);
			pinky.setSpeed(0);
			changeState(IntermissonState.READY_TO_PLAY);
		}
		if (!ghostsMet && inky.position.x - pinky.position.x < 16) {
			ghostsMet = true;
			inky.setDir(inky.dir().opposite());
			inky.setWishDir(inky.dir());
			inky.setSpeed(0.2);
			pinky.setDir(pinky.dir().opposite());
			pinky.setWishDir(pinky.dir());
			pinky.setSpeed(0.2);
		}
	}

	private void state_READY_TO_PLAY_update() {
		if (stateTimer().isRunningSeconds(2)) {
			inky.visible = false;
			pinky.visible = false;
		}
		if (stateTimer().hasExpired()) {
			gameController.stateTimer().expire();
		}
	}
}