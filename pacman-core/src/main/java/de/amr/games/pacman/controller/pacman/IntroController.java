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

	public static class GhostPortrait {
		public Ghost ghost;
		public String character;
		public boolean characterVisible;
		public boolean nicknameVisible;
	}

	public enum IntroState {
		BEGIN, PRESENTING_GHOST, CHASING_PAC, CHASING_GHOSTS, READY_TO_PLAY;
	}

	public static final int TOP_Y = t(6);

	public final PacManGameController gameController;
	public final TimedSequence<Boolean> blinking = TimedSequence.pulse().frameDuration(20);
	public GhostPortrait[] gallery;
	public int selectedGhost;
	public long ghostKilledTime;
	public Pac pac;
	public Ghost[] ghosts;

	public IntroController(PacManGameController gameController) {
		super(IntroState.values());
		configState(IntroState.BEGIN, this::startStateTimer, this::state_BEGIN_update, null);
		configState(IntroState.PRESENTING_GHOST, this::startStateTimer, this::state_PRESENTING_GHOST_update, null);
		configState(IntroState.CHASING_PAC, this::startStateTimer, this::state_CHASING_PAC_update, null);
		configState(IntroState.CHASING_GHOSTS, this::startStateTimer, this::state_CHASING_GHOSTS_update, null);
		configState(IntroState.READY_TO_PLAY, this::startStateTimer, this::state_READY_TO_PLAY_update, null);
		this.gameController = gameController;
	}

	private void startStateTimer() {
		stateTimer().start();
	}

	public void init() {
		gallery = new GhostPortrait[4];
		for (int i = 0; i < 4; ++i) {
			gallery[i] = new GhostPortrait();
		}
		gallery[0].ghost = new Ghost(GameModel.RED_GHOST, "Blinky");
		gallery[0].ghost.setDir(Direction.RIGHT);
		gallery[0].ghost.setWishDir(Direction.RIGHT);
		gallery[0].character = "SHADOW";
		gallery[0].ghost.setPosition(t(2), TOP_Y + t(2));

		gallery[1].ghost = new Ghost(GameModel.PINK_GHOST, "Pinky");
		gallery[1].ghost.setDir(Direction.RIGHT);
		gallery[1].ghost.setWishDir(Direction.RIGHT);
		gallery[1].character = "SPEEDY";
		gallery[1].ghost.setPosition(t(2), TOP_Y + t(5));

		gallery[2].ghost = new Ghost(GameModel.CYAN_GHOST, "Inky");
		gallery[2].ghost.setDir(Direction.RIGHT);
		gallery[2].ghost.setWishDir(Direction.RIGHT);
		gallery[2].character = "BASHFUL";
		gallery[2].ghost.setPosition(t(2), TOP_Y + t(8));

		gallery[3].ghost = new Ghost(GameModel.ORANGE_GHOST, "Clyde");
		gallery[3].ghost.setDir(Direction.RIGHT);
		gallery[3].ghost.setWishDir(Direction.RIGHT);
		gallery[3].character = "POKEY";
		gallery[3].ghost.setPosition(t(2), TOP_Y + t(11));

		pac = new Pac("Ms. Pac-Man");
		pac.setDir(Direction.LEFT);

		ghosts = new Ghost[] { //
				new Ghost(GameModel.RED_GHOST, "Blinky"), //
				new Ghost(GameModel.PINK_GHOST, "Pinky"), //
				new Ghost(GameModel.CYAN_GHOST, "Inky"), //
				new Ghost(GameModel.ORANGE_GHOST, "Clyde"), //
		};
		for (Ghost ghost : ghosts) {
			ghost.setDir(Direction.LEFT);
			ghost.setWishDir(Direction.LEFT);
		}

		changeState(IntroState.BEGIN);
	}

	private void state_BEGIN_update() {
		if (stateTimer().isRunningSeconds(2)) {
			selectGhost(0);
			changeState(IntroState.PRESENTING_GHOST);
		}
	}

	private void state_PRESENTING_GHOST_update() {
		if (stateTimer().isRunningSeconds(0.5)) {
			gallery[selectedGhost].characterVisible = true;
		}
		if (stateTimer().isRunningSeconds(1)) {
			gallery[selectedGhost].nicknameVisible = true;
		}
		if (stateTimer().isRunningSeconds(2)) {
			if (selectedGhost < 3) {
				selectGhost(selectedGhost + 1);
				stateTimer().reset();
				stateTimer().start();
			} else {
				startGhostsChasingPac();
				changeState(IntroState.CHASING_PAC);
			}
		}
	}

	private void state_CHASING_PAC_update() {
		if (pac.position.x < t(2)) {
			startPacChasingGhosts();
			changeState(IntroState.CHASING_GHOSTS);
		}
	}

	private void state_CHASING_GHOSTS_update() {
		if (pac.position.x > t(28)) {
			changeState(IntroState.READY_TO_PLAY);
		}
		if (gameController.stateTimer().ticked() - ghostKilledTime == 15) {
			ghostKilledTime = 0;
			pac.visible = true;
			pac.setSpeed(1.0);
			for (Ghost ghost : ghosts) {
				if (ghost.state == GhostState.DEAD) {
					ghost.visible = false;
				}
			}
		}
		for (Ghost ghost : ghosts) {
			if (pac.meets(ghost) && ghost.state != GhostState.DEAD) {
				ghost.state = GhostState.DEAD;
				ghost.bounty = (int) Math.pow(2, ghost.id + 1) * 100;
				pac.visible = false;
				pac.setSpeed(0);
				ghostKilledTime = gameController.stateTimer().ticked();
			}
		}
	}

	private void state_READY_TO_PLAY_update() {
		if (stateTimer().hasJustStarted()) {
			blinking.restart();
		}
		if (stateTimer().isRunningSeconds(5)) {
			gameController.stateTimer().expire();
		}
		blinking.animate();
	}

	public void update() {
		pac.move();
		for (Ghost ghost : ghosts) {
			ghost.move();
		}
		updateState();
	}

	private void startGhostsChasingPac() {
		pac.setPosition(t(28), t(22));
		pac.visible = true;
		pac.setSpeed(1.0);
		pac.setDir(Direction.LEFT);
		pac.stuck = false;

		for (Ghost ghost : ghosts) {
			ghost.position = pac.position.plus(8 + (ghost.id + 1) * 18, 0);
			ghost.visible = true;
			ghost.setWishDir(Direction.LEFT);
			ghost.setDir(Direction.LEFT);
			ghost.setSpeed(1.05);
			ghost.state = GhostState.HUNTING_PAC;
		}

		blinking.restart();
	}

	private void startPacChasingGhosts() {
		pac.setDir(Direction.RIGHT);
		for (Ghost ghost : ghosts) {
			ghost.state = GhostState.FRIGHTENED;
			ghost.setWishDir(Direction.RIGHT);
			ghost.setDir(Direction.RIGHT);
			ghost.setSpeed(0.5);
		}
	}

	private void selectGhost(int ghostIndex) {
		selectedGhost = ghostIndex;
		gallery[selectedGhost].ghost.visible = true;
	}
}