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

import static de.amr.games.pacman.model.common.world.World.t;

import de.amr.games.pacman.controller.GameController;
import de.amr.games.pacman.controller.pacman.IntroController.Context;
import de.amr.games.pacman.lib.Direction;
import de.amr.games.pacman.lib.FiniteStateMachine;
import de.amr.games.pacman.lib.TimedSeq;
import de.amr.games.pacman.model.common.GameModel;
import de.amr.games.pacman.model.common.Ghost;
import de.amr.games.pacman.model.common.Pac;

/**
 * Intro scene of the PacMan game.
 * <p>
 * The ghosts are presented one after another, then Pac-Man is chased by the ghosts, turns the card and hunts the ghosts
 * himself.
 * 
 * @author Armin Reichert
 */
public class IntroController extends FiniteStateMachine<IntroState, Context> {

	public static class GhostPortrait {
		public Ghost ghost;
		public String character;
		public boolean characterVisible = false;
		public boolean nicknameVisible = false;

		public GhostPortrait(int id, String name, String character, int tileY) {
			ghost = new Ghost(id, name);
			ghost.setMoveDir(Direction.RIGHT);
			ghost.setWishDir(Direction.RIGHT);
			ghost.setPosition(t(4), t(tileY));
			this.character = character;
		}
	}

	public static class Context {
		public GameController gameController;
		public TimedSeq<Boolean> fastBlinking = TimedSeq.pulse().frameDuration(10);
		public TimedSeq<Boolean> slowBlinking = TimedSeq.pulse().frameDuration(30);
		public int topY = t(6);
		public GhostPortrait[] portraits;
		public Pac pacMan;
		public Ghost[] ghosts;
		public int ghostIndex;
		public long ghostKilledTime;
	}

	public final Context context = new Context();

	public IntroController(GameController gameController) {
		context.gameController = gameController;
		for (var state : IntroState.values()) {
			state.controller = this;
		}
		setContext(context);
	}

	public void init() {
		context.portraits = new GhostPortrait[] { //
				new GhostPortrait(GameModel.RED_GHOST, "Blinky", "SHADOW", 7), //
				new GhostPortrait(GameModel.PINK_GHOST, "Pinky", "SPEEDY", 10), //
				new GhostPortrait(GameModel.CYAN_GHOST, "Inky", "BASHFUL", 13), //
				new GhostPortrait(GameModel.ORANGE_GHOST, "Clyde", "POKEY", 16), //
		};
		context.pacMan = new Pac("Pac-Man");
		context.ghosts = new Ghost[] { //
				new Ghost(GameModel.RED_GHOST, "Blinky"), //
				new Ghost(GameModel.PINK_GHOST, "Pinky"), //
				new Ghost(GameModel.CYAN_GHOST, "Inky"), //
				new Ghost(GameModel.ORANGE_GHOST, "Clyde"), //
		};

		// TODO fixme
		for (var s : IntroState.values()) {
			s.timer.set(0);
		}

		state = null;
		changeState(IntroState.BEGIN);
	}

	public void restartStateTimer() {
		state.timer().setIndefinite().start();
	}

	public void selectGhost(int index) {
		context.ghostIndex = index;
		context.portraits[context.ghostIndex].ghost.show();
	}
}