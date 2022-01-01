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

import static de.amr.games.pacman.lib.TickTimer.sec_to_ticks;
import static de.amr.games.pacman.model.world.PacManGameWorld.t;

import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

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

		public GhostPortrait(int id, String name, String character, int tileY) {
			ghost = new Ghost(id, name);
			ghost.setDir(Direction.RIGHT);
			ghost.setWishDir(Direction.RIGHT);
			ghost.setPosition(t(4), t(tileY));
			this.character = character;
		}
	}

	public final PacManGameController gameController;
	public final GhostPortrait[] portraits;
	public final Pac pacMan;
	public final Ghost[] ghosts;
	public final TimedSequence<Boolean> blinking = TimedSequence.pulse().frameDuration(10);
	public final int topY = t(6);
	public int selectedGhostIndex;
	public long ghostKilledTime;

	public IntroController(PacManGameController gameController) {
		super(IntroState.values());
		configState(IntroState.BEGIN, this::restartStateTimer, this::state_BEGIN_update, null);
		configState(IntroState.PRESENTING_GHOSTS, this::restartStateTimer, this::state_PRESENTING_GHOSTS_update, null);
		configState(IntroState.SHOWING_POINTS, this::restartStateTimer, this::state_SHOWING_POINTS_update, null);
		configState(IntroState.CHASING_PAC, this::state_CHASING_PAC_enter, this::state_CHASING_PAC_update, null);
		configState(IntroState.CHASING_GHOSTS, this::state_CHASING_GHOSTS_enter, this::state_CHASING_GHOSTS_update, null);
		configState(IntroState.READY_TO_PLAY, this::restartStateTimer, this::state_READY_TO_PLAY_update, null);
		this.gameController = gameController;
		portraits = new GhostPortrait[] { //
				new GhostPortrait(GameModel.RED_GHOST, "Blinky", "SHADOW", 7), //
				new GhostPortrait(GameModel.PINK_GHOST, "Pinky", "SPEEDY", 10), //
				new GhostPortrait(GameModel.CYAN_GHOST, "Inky", "BASHFUL", 13), //
				new GhostPortrait(GameModel.ORANGE_GHOST, "Clyde", "POKEY", 16), //
		};
		pacMan = new Pac("Pac-Man");
		ghosts = new Ghost[] { //
				new Ghost(GameModel.RED_GHOST, "Blinky"), //
				new Ghost(GameModel.PINK_GHOST, "Pinky"), //
				new Ghost(GameModel.CYAN_GHOST, "Inky"), //
				new Ghost(GameModel.ORANGE_GHOST, "Clyde"), //
		};
	}

	public void init() {
		for (GhostPortrait portrait : portraits) {
			portrait.characterVisible = false;
			portrait.nicknameVisible = false;
		}
		changeState(IntroState.BEGIN);
	}

	public void update() {
		updateState();
	}

	private void restartStateTimer() {
		stateTimer().reset();
		stateTimer().start();
	}

	private void selectGhost(int index) {
		selectedGhostIndex = index;
		portraits[selectedGhostIndex].ghost.visible = true;
	}

	private void state_BEGIN_update() {
		if (stateTimer().isRunningSeconds(1)) {
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
				blinking.restart();
				blinking.advance();
				changeState(IntroState.SHOWING_POINTS);
				return;
			}
		}
	}

	private void state_SHOWING_POINTS_update() {
		if (stateTimer().isRunningSeconds(2)) {
			changeState(IntroState.CHASING_PAC);
		}
	}

	private void state_CHASING_PAC_enter() {
		restartStateTimer();
		pacMan.visible = true;
		pacMan.setSpeed(0.95);
		pacMan.setPosition(t(28), t(20));
		pacMan.setDir(Direction.LEFT);
		for (Ghost ghost : ghosts) {
			ghost.position = pacMan.position.plus(26 + ghost.id * 16, 0);
			ghost.setWishDir(Direction.LEFT);
			ghost.setDir(Direction.LEFT);
			ghost.setSpeed(1.0);
			ghost.visible = true;
			ghost.state = GhostState.HUNTING_PAC;
		}
	}

	private void state_CHASING_PAC_update() {
		if (pacMan.position.x < t(2)) {
			changeState(IntroState.CHASING_GHOSTS);
			return;
		}
		pacMan.move();
		for (Ghost ghost : ghosts) {
			ghost.move();
		}
		blinking.animate();
	}

	private void state_CHASING_GHOSTS_enter() {
		restartStateTimer();
		pacMan.setSpeed(1.0);
		pacMan.setDir(Direction.RIGHT);
		for (Ghost ghost : ghosts) {
			ghost.state = GhostState.FRIGHTENED;
			ghost.setWishDir(Direction.RIGHT);
			ghost.setDir(Direction.RIGHT);
			ghost.setSpeed(0.6);
		}
		ghostKilledTime = stateTimer().ticked();
	}

	private void state_CHASING_GHOSTS_update() {
		if (pacMan.position.x > t(29)) {
			changeState(IntroState.READY_TO_PLAY);
			return;
		}
		// check if Pac-Man kills a ghost
		Optional<Ghost> killedGhost = Stream.of(ghosts).filter(ghost -> ghost.state != GhostState.DEAD)
				.filter(pacMan::meets).findFirst();
		killedGhost.ifPresent(victim -> {
			ghostKilledTime = stateTimer().ticked();
			victim.state = GhostState.DEAD;
			victim.bounty = List.of(200, 400, 800, 1600).get(victim.id);
			pacMan.visible = false;
			pacMan.setSpeed(0);
			Stream.of(ghosts).forEach(ghost -> ghost.setSpeed(0));
		});
		// After some time, Pac-Man and the surviving ghosts get visible and move again
		if (stateTimer().ticked() - ghostKilledTime == sec_to_ticks(1)) {
			pacMan.visible = true;
			pacMan.setSpeed(1.0);
			for (Ghost ghost : ghosts) {
				if (ghost.state == GhostState.DEAD) {
					ghost.visible = false;
				} else {
					ghost.visible = true;
					ghost.setSpeed(0.6);
				}
			}
			ghostKilledTime = stateTimer().ticked();
		}
		// When the last ghost has been killed, make Pac-Man invisible
		if (Stream.of(ghosts).allMatch(ghost -> ghost.state == GhostState.DEAD)) {
			pacMan.visible = false;
		}
		pacMan.move();
		for (Ghost ghost : ghosts) {
			ghost.move();
		}
		blinking.animate();
	}

	private void state_READY_TO_PLAY_update() {
		if (stateTimer().isRunningSeconds(5)) {
			gameController.stateTimer().expire();
		}
		blinking.animate();
	}
}