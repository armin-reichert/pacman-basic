/*
MIT License

Copyright (c) 2021-2023 Armin Reichert

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

import static de.amr.games.pacman.model.common.world.World.t;

import de.amr.games.pacman.controller.common.GameController;
import de.amr.games.pacman.controller.common.SceneControllerContext;
import de.amr.games.pacman.controller.mspacman.MsPacManIntermission2.IntermissionData;
import de.amr.games.pacman.controller.mspacman.MsPacManIntermission2.IntermissionState;
import de.amr.games.pacman.event.GameEvents;
import de.amr.games.pacman.lib.anim.EntityAnimation;
import de.amr.games.pacman.lib.fsm.Fsm;
import de.amr.games.pacman.lib.fsm.FsmState;
import de.amr.games.pacman.lib.steering.Direction;
import de.amr.games.pacman.lib.timer.TickTimer;
import de.amr.games.pacman.model.common.GameModel;
import de.amr.games.pacman.model.common.actors.Pac;
import de.amr.games.pacman.model.mspacman.Clapperboard;

/**
 * Intermission scene 2: "The chase".
 * <p>
 * Pac-Man and Ms. Pac-Man chase each other across the screen over and over. After three turns, they both rapidly run
 * from left to right and right to left. (Played after round 5)
 * 
 * @author Armin Reichert
 */
public class MsPacManIntermission2 extends Fsm<IntermissionState, IntermissionData> {

	private final IntermissionData intermissionData;

	public MsPacManIntermission2(GameController gameController) {
		states = IntermissionState.values();
		for (var state : IntermissionState.values()) {
			state.intermission = this;
		}
		this.intermissionData = new IntermissionData(gameController);
	}

	@Override
	public IntermissionData context() {
		return intermissionData;
	}

	public static class IntermissionData extends SceneControllerContext {
		public final int upperY = t(12);
		public final int middleY = t(18);
		public final int lowerY = t(24);
		public Clapperboard clapperboard;
		public Pac pacMan;
		public Pac msPacMan;

		public IntermissionData(GameController gameController) {
			super(gameController);
		}
	}

	public enum IntermissionState implements FsmState<IntermissionData> {

		FLAP {
			@Override
			public void onEnter(IntermissionData ctx) {
				timer.restartIndefinitely();
				ctx.clapperboard = new Clapperboard(2, "THE CHASE");
				ctx.clapperboard.setPosition(t(3), t(10));
				ctx.clapperboard.show();
				ctx.pacMan = new Pac("Pac-Man");
				ctx.pacMan.setMoveDir(Direction.RIGHT);
				ctx.pacMan.selectAndRunAnimation(GameModel.AK_PAC_MUNCHING);
				ctx.msPacMan = new Pac("Ms. Pac-Man");
				ctx.msPacMan.setMoveDir(Direction.RIGHT);
				ctx.msPacMan.selectAndRunAnimation(GameModel.AK_GHOST_COLOR);
			}

			@Override
			public void onUpdate(IntermissionData ctx) {
				if (timer.atSecond(1)) {
					GameEvents.publishSoundEvent(GameModel.SE_START_INTERMISSION_2);
					ctx.clapperboard.animation().ifPresent(EntityAnimation::restart);
				} else if (timer.atSecond(2)) {
					ctx.clapperboard.hide();
				} else if (timer.atSecond(3)) {
					intermission.changeState(IntermissionState.CHASING);
				}
			}
		},

		CHASING {
			@Override
			public void onEnter(IntermissionData ctx) {
				timer.restartIndefinitely();
			}

			@Override
			public void onUpdate(IntermissionData ctx) {
				if (timer.atSecond(2.5)) {
					ctx.pacMan.setPosition(-t(2), ctx.upperY);
					ctx.pacMan.setMoveDir(Direction.RIGHT);
					ctx.pacMan.setPixelSpeed(2.0f);
					ctx.pacMan.show();
					ctx.msPacMan.setPosition(-t(8), ctx.upperY);
					ctx.msPacMan.setMoveDir(Direction.RIGHT);
					ctx.msPacMan.setPixelSpeed(2.0f);
					ctx.msPacMan.show();
				} else if (timer.atSecond(7)) {
					ctx.pacMan.setPosition(t(36), ctx.lowerY);
					ctx.pacMan.setMoveDir(Direction.LEFT);
					ctx.pacMan.setPixelSpeed(2.0f);
					ctx.msPacMan.setPosition(t(30), ctx.lowerY);
					ctx.msPacMan.setMoveDir(Direction.LEFT);
					ctx.msPacMan.setPixelSpeed(2.0f);
				} else if (timer.atSecond(11.5)) {
					ctx.pacMan.setMoveDir(Direction.RIGHT);
					ctx.pacMan.setPixelSpeed(2.0f);
					ctx.msPacMan.setPosition(t(-8), ctx.middleY);
					ctx.msPacMan.setMoveDir(Direction.RIGHT);
					ctx.msPacMan.setPixelSpeed(2.0f);
					ctx.pacMan.setPosition(t(-2), ctx.middleY);
				} else if (timer.atSecond(15.5)) {
					ctx.pacMan.setPosition(t(42), ctx.upperY);
					ctx.pacMan.setMoveDir(Direction.LEFT);
					ctx.pacMan.setPixelSpeed(4.0f);
					ctx.msPacMan.setPosition(t(30), ctx.upperY);
					ctx.msPacMan.setMoveDir(Direction.LEFT);
					ctx.msPacMan.setPixelSpeed(4.0f);
				} else if (timer.atSecond(16.5)) {
					ctx.pacMan.setPosition(t(-2), ctx.lowerY);
					ctx.pacMan.setMoveDir(Direction.RIGHT);
					ctx.pacMan.setPixelSpeed(4.0f);
					ctx.msPacMan.setPosition(t(-14), ctx.lowerY);
					ctx.msPacMan.setMoveDir(Direction.RIGHT);
					ctx.msPacMan.setPixelSpeed(4.0f);
				} else if (timer.atSecond(21)) {
					ctx.gameController().terminateCurrentState();
					return;
				}
				ctx.pacMan.move();
				ctx.pacMan.animate();
				ctx.msPacMan.move();
				ctx.msPacMan.animate();
			}
		};

		MsPacManIntermission2 intermission;
		final TickTimer timer = new TickTimer("Timer-" + name());

		@Override
		public TickTimer timer() {
			return timer;
		}
	}
}