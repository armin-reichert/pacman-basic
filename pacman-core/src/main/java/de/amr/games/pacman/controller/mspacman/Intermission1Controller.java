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
import de.amr.games.pacman.lib.Direction;
import de.amr.games.pacman.lib.TickTimer;
import de.amr.games.pacman.model.common.GameEntity;
import de.amr.games.pacman.model.common.GameModel;
import de.amr.games.pacman.model.common.Ghost;
import de.amr.games.pacman.model.common.Pac;
import de.amr.games.pacman.model.mspacman.entities.Flap;

/**
 * Intermission scene 1: "They meet".
 * <p>
 * Pac-Man leads Inky and Ms. Pac-Man leads Pinky. Soon, the two Pac-Men are about to collide, they
 * quickly move upwards, causing Inky and Pinky to collide and vanish. Finally, Pac-Man and Ms.
 * Pac-Man face each other at the top of the screen and a big pink heart appears above them. (Played
 * after round 2)
 * 
 * @author Armin Reichert
 */
public abstract class Intermission1Controller {

	public enum Phase {

		FLAP, CHASED_BY_GHOSTS, COMING_TOGETHER, READY_TO_PLAY;

	}

	public static final int upperY = t(12), lowerY = t(24), middleY = t(18);

	public final PacManGameController gameController;
	public final TickTimer timer = new TickTimer(getClass().getSimpleName() + "-timer");
	public Phase phase;
	public Flap flap;
	public Pac pacMan, msPac;
	public Ghost pinky, inky;
	public GameEntity heart;
	public boolean ghostsMet;

	public Intermission1Controller(PacManGameController gameController) {
		this.gameController = gameController;
	}

	public abstract void playIntermissionSound();

	public abstract void playFlapAnimation();

	public void init() {
		flap = new Flap(1, "THEY MEET");
		flap.setPosition(t(3), t(10));
		flap.setVisible(true);

		pacMan = new Pac("Pac-Man");
		pacMan.setDir(Direction.RIGHT);
		pacMan.setPosition(-t(2), upperY);
		pacMan.setVisible(true);

		inky = new Ghost(GameModel.CYAN_GHOST, "Inky");
		inky.setDir(Direction.RIGHT);
		inky.setWishDir(Direction.RIGHT);
		inky.setPosition(pacMan.position().plus(-t(3), 0));
		inky.setVisible(true);

		msPac = new Pac("Ms. Pac-Man");
		msPac.setDir(Direction.LEFT);
		msPac.setPosition(t(30), lowerY);
		msPac.setVisible(true);

		pinky = new Ghost(GameModel.PINK_GHOST, "Pinky");
		pinky.setDir(Direction.LEFT);
		pinky.setWishDir(Direction.LEFT);
		pinky.setPosition(msPac.position().plus(t(3), 0));
		pinky.setVisible(true);

		heart = new GameEntity();
		ghostsMet = false;

		enterSeconds(Phase.FLAP, 2);
	}

	private void enter(Phase newPhase) {
		phase = newPhase;
		timer.reset();
		timer.start();
	}

	private void enterSeconds(Phase newPhase, double seconds) {
		phase = newPhase;
		timer.resetSeconds(seconds);
		timer.start();
	}

	public void update() {
		switch (phase) {

		case FLAP:
			if (timer.isRunningSeconds(1)) {
				playFlapAnimation();
			}
			if (timer.hasExpired()) {
				flap.setVisible(false);
				playIntermissionSound();
				startChasedByGhosts();
				return;
			}
			timer.tick();
			break;

		case CHASED_BY_GHOSTS:
			inky.move();
			pacMan.move();
			pinky.move();
			msPac.move();
			if (inky.position().x > t(30)) {
				startComingTogether();
			}
			timer.tick();
			break;

		case COMING_TOGETHER:
			inky.move();
			pinky.move();
			pacMan.move();
			msPac.move();
			if (pacMan.dir() == Direction.LEFT && pacMan.position().x < t(15)) {
				pacMan.setDir(Direction.UP);
				msPac.setDir(Direction.UP);
			}
			if (pacMan.dir() == Direction.UP && pacMan.position().y < upperY) {
				pacMan.setSpeed(0);
				msPac.setSpeed(0);
				pacMan.setDir(Direction.LEFT);
				msPac.setDir(Direction.RIGHT);
				heart.setPosition((pacMan.position().x + msPac.position().x) / 2, pacMan.position().y - t(2));
				heart.setVisible(true);
				inky.setSpeed(0);
				pinky.setSpeed(0);
				enterSeconds(Phase.READY_TO_PLAY, 4);
			}
			if (!ghostsMet && inky.position().x - pinky.position().x < 16) {
				ghostsMet = true;
				inky.setDir(inky.dir().opposite());
				inky.setWishDir(inky.dir());
				inky.setSpeed(0.2);
				pinky.setDir(pinky.dir().opposite());
				pinky.setWishDir(pinky.dir());
				pinky.setSpeed(0.2);
			}
			timer.tick();
			break;

		case READY_TO_PLAY:
			if (timer.isRunningSeconds(2)) {
				inky.setVisible(false);
				pinky.setVisible(false);
			}
			if (timer.hasExpired()) {
				gameController.stateTimer().expire();
				return;
			}
			timer.tick();
			break;

		default:
			break;
		}
	}

	public void startChasedByGhosts() {
		pacMan.setSpeed(1.0);
		msPac.setSpeed(1.0);
		inky.setSpeed(1.0);
		pinky.setSpeed(1.0);
		enter(Phase.CHASED_BY_GHOSTS);
	}

	public void startComingTogether() {
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
		enter(Phase.COMING_TOGETHER);
	}
}