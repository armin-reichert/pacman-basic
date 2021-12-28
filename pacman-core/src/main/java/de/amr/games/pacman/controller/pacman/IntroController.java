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

import static de.amr.games.pacman.model.world.PacManGameWorld.t;

import de.amr.games.pacman.controller.PacManGameController;
import de.amr.games.pacman.controller.pacman.IntroController.IntroState;
import de.amr.games.pacman.lib.Direction;
import de.amr.games.pacman.lib.FiniteStateMachine;
import de.amr.games.pacman.lib.TimedSequence;
import de.amr.games.pacman.model.common.GameModel;
import de.amr.games.pacman.model.common.Ghost;
import de.amr.games.pacman.model.common.GhostState;
import de.amr.games.pacman.model.common.Pac;

/**
 * Intro scene of the PacMan game.
 * <p>
 * The ghosts are presented one after another, then Pac-Man is chased by the ghosts, turns the card
 * and hunts the ghosts himself.
 * 
 * @author Armin Reichert
 */
public class IntroController extends FiniteStateMachine<IntroState> {

	public enum IntroState {
		BEGIN, PRESENTING_GHOSTS, SHOWING_POINTS, CHASING_PAC, CHASING_GHOSTS, READY_TO_PLAY;
	}

	public static class GhostPortrait {
		public Ghost ghost;
		public String character;
		public boolean characterVisible;
		public boolean nicknameVisible;
	}

	public final int topY = t(6);
	public final PacManGameController gameController;
	public final TimedSequence<Boolean> blinking = TimedSequence.pulse().frameDuration(20);
	public final GhostPortrait[] portraits = new GhostPortrait[4];
	public int selectedGhostIndex;
	public long ghostKilledTime;
	public Pac pacMan;
	public Ghost[] ghosts;

	public IntroController(PacManGameController gameController) {
		super(IntroState.values());
		configState(IntroState.BEGIN, this::restartStateTimer, this::state_BEGIN_update, null);
		configState(IntroState.PRESENTING_GHOSTS, this::restartStateTimer, this::state_PRESENTING_GHOSTS_update, null);
		configState(IntroState.SHOWING_POINTS, () -> restartStateTimer(2), this::state_SHOWING_POINTS_update, null);
		configState(IntroState.CHASING_PAC, this::restartStateTimer, this::state_CHASING_PAC_update, null);
		configState(IntroState.CHASING_GHOSTS, this::restartStateTimer, this::state_CHASING_GHOSTS_update, null);
		configState(IntroState.READY_TO_PLAY, this::restartStateTimer, this::state_READY_TO_PLAY_update, null);
		this.gameController = gameController;
		createGhostGallery();
		pacMan = new Pac("Pac-Man");
		ghosts = new Ghost[] { //
				new Ghost(GameModel.RED_GHOST, "Blinky"), //
				new Ghost(GameModel.PINK_GHOST, "Pinky"), //
				new Ghost(GameModel.CYAN_GHOST, "Inky"), //
				new Ghost(GameModel.ORANGE_GHOST, "Clyde"), //
		};
	}

	private void restartStateTimer() {
		stateTimer().reset();
		stateTimer().start();
	}

	private void restartStateTimer(double seconds) {
		stateTimer().resetSeconds(seconds);
		stateTimer().start();
	}

	private void createGhostGallery() {
		for (int i = 0; i < 4; ++i) {
			portraits[i] = new GhostPortrait();
		}
		portraits[0].ghost = new Ghost(GameModel.RED_GHOST, "Blinky");
		portraits[0].character = "SHADOW";
		portraits[1].ghost = new Ghost(GameModel.PINK_GHOST, "Pinky");
		portraits[1].character = "SPEEDY";
		portraits[2].ghost = new Ghost(GameModel.CYAN_GHOST, "Inky");
		portraits[2].character = "BASHFUL";
		portraits[3].ghost = new Ghost(GameModel.ORANGE_GHOST, "Clyde");
		portraits[3].character = "POKEY";
		for (int i = 0; i < 4; ++i) {
			portraits[i].ghost.setDir(Direction.RIGHT);
			portraits[i].ghost.setWishDir(Direction.RIGHT);
			portraits[i].ghost.setPosition(t(4), topY + t(1) + i * t(3));
		}
	}

	public void init() {
		changeState(IntroState.BEGIN);
	}

	public void update() {
		updateState();
	}

	private void state_BEGIN_update() {
		if (stateTimer().isRunningSeconds(2)) {
			selectGhost(0);
			changeState(IntroState.PRESENTING_GHOSTS);
		}
	}

	private void state_PRESENTING_GHOSTS_update() {
		if (stateTimer().isRunningSeconds(0.5)) {
			portraits[selectedGhostIndex].characterVisible = true;
		}

		else if (stateTimer().isRunningSeconds(1)) {
			portraits[selectedGhostIndex].nicknameVisible = true;
		}

		else if (stateTimer().isRunningSeconds(2)) {
			if (selectedGhostIndex < 3) {
				selectGhost(selectedGhostIndex + 1);
				restartStateTimer();
			} else {
				changeState(IntroState.SHOWING_POINTS);
				return;
			}
		}
	}

	private void state_SHOWING_POINTS_update() {
		if (stateTimer().hasExpired()) {
			blinking.restart();
			pacMan.visible = true;
			pacMan.setSpeed(1.0);
			pacMan.setPosition(t(28), t(21));
			pacMan.setDir(Direction.LEFT);
			for (Ghost ghost : ghosts) {
				ghost.position = pacMan.position.plus(26 + ghost.id * 18, 0);
				ghost.setWishDir(Direction.LEFT);
				ghost.setDir(Direction.LEFT);
				ghost.visible = true;
				ghost.setSpeed(1.05);
				ghost.state = GhostState.HUNTING_PAC;
			}
			changeState(IntroState.CHASING_PAC);
		}
	}

	private void state_CHASING_PAC_update() {
		blinking.animate();
		if (pacMan.position.x < t(2)) {
			pacMan.setDir(Direction.RIGHT);
			for (Ghost ghost : ghosts) {
				ghost.state = GhostState.FRIGHTENED;
				ghost.setWishDir(Direction.RIGHT);
				ghost.setDir(Direction.RIGHT);
				ghost.setSpeed(0.5);
			}
			changeState(IntroState.CHASING_GHOSTS);
			return;
		}
		pacMan.move();
		for (Ghost ghost : ghosts) {
			ghost.move();
		}
	}

	private void state_CHASING_GHOSTS_update() {
		blinking.animate();
		if (pacMan.position.x > t(29)) {
			changeState(IntroState.READY_TO_PLAY);
			return;
		}
		if (gameController.stateTimer().ticked() - ghostKilledTime == 15) {
			ghostKilledTime = 0;
			pacMan.visible = true;
			pacMan.setSpeed(1.0);
			for (Ghost ghost : ghosts) {
				if (ghost.state == GhostState.DEAD) {
					ghost.visible = false;
				}
			}
		}
		for (Ghost ghost : ghosts) {
			if (pacMan.meets(ghost) && ghost.state != GhostState.DEAD) {
				ghost.state = GhostState.DEAD;
				ghost.bounty = (int) Math.pow(2, ghost.id + 1) * 100;
				pacMan.visible = false;
				pacMan.setSpeed(0);
				ghostKilledTime = gameController.stateTimer().ticked();
			}
		}
		pacMan.move();
		for (Ghost ghost : ghosts) {
			ghost.move();
		}
	}

	private void state_READY_TO_PLAY_update() {
		blinking.animate();
		if (stateTimer().isRunningSeconds(5)) {
			gameController.stateTimer().expire();
		}
	}

	private void selectGhost(int index) {
		selectedGhostIndex = index;
		portraits[selectedGhostIndex].ghost.visible = true;
	}
}