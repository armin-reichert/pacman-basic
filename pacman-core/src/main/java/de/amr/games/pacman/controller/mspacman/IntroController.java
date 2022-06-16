/*
MIT License

Copyright (c) 2021-22 Armin Reichert

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

import static de.amr.games.pacman.lib.Direction.LEFT;
import static de.amr.games.pacman.lib.Direction.UP;
import static de.amr.games.pacman.lib.V2i.v;
import static de.amr.games.pacman.model.common.actors.Ghost.CYAN_GHOST;
import static de.amr.games.pacman.model.common.actors.Ghost.ORANGE_GHOST;
import static de.amr.games.pacman.model.common.actors.Ghost.PINK_GHOST;
import static de.amr.games.pacman.model.common.actors.Ghost.RED_GHOST;
import static de.amr.games.pacman.model.common.world.World.HTS;
import static de.amr.games.pacman.model.common.world.World.t;

import de.amr.games.pacman.controller.common.GameController;
import de.amr.games.pacman.controller.common.GameState;
import de.amr.games.pacman.lib.TickTimer;
import de.amr.games.pacman.lib.V2i;
import de.amr.games.pacman.lib.animation.SimpleAnimation;
import de.amr.games.pacman.lib.animation.Animations;
import de.amr.games.pacman.lib.fsm.Fsm;
import de.amr.games.pacman.lib.fsm.FsmState;
import de.amr.games.pacman.model.common.actors.Ghost;
import de.amr.games.pacman.model.common.actors.GhostState;
import de.amr.games.pacman.model.common.actors.Pac;

/**
 * Intro scene of the Ms. Pac-Man game.
 * <p>
 * The ghosts and Ms. Pac-Man are introduced on a billboard and are marching in one after another.
 * 
 * @author Armin Reichert
 */
public class IntroController extends Fsm<IntroController.State, IntroController.Context> {

	public final Context $;

	public IntroController(GameController gameController) {
		super(State.values());
		$ = new Context(gameController);
		logging = true;
	}

	@Override
	public Context context() {
		return $;
	}

	public static class Context {
		public boolean creditVisible = false;
		public double actorSpeed = 1.1f;
		public final V2i lightsTopLeft = v(t(8), t(11));
		public final V2i titlePosition = v(t(10), t(8));
		public final V2i turningPoint = v(t(6), t(20)).plus(0, HTS);
		public final int msPacManStopX = t(15);
		public final SimpleAnimation<Boolean> blinking = SimpleAnimation.pulse(30);
		public final TickTimer lightsTimer = new TickTimer("lights-timer");
		public final Pac msPacMan = new Pac("Ms. Pac-Man");
		public final Ghost[] ghosts = new Ghost[] { //
				new Ghost(RED_GHOST, "Blinky"), //
				new Ghost(PINK_GHOST, "Pinky"), //
				new Ghost(CYAN_GHOST, "Inky"), //
				new Ghost(ORANGE_GHOST, "Sue") //
		};
		public int ghostIndex;

		public final GameController gameController;

		public Context(GameController gameController) {
			this.gameController = gameController;
		}
	}

	public enum State implements FsmState<IntroController.Context> {

		START {
			@Override
			public void onEnter(Context $) {
				$.gameController.game().scores.gameScore.showContent = false;
				$.gameController.game().scores.highScore.showContent = true;
				$.lightsTimer.resetIndefinitely();
				$.lightsTimer.start();
				$.msPacMan.setMoveDir(LEFT);
				$.msPacMan.setPosition(t(34), $.turningPoint.y);
				$.msPacMan.setAbsSpeed($.actorSpeed);
				$.msPacMan.show();
				for (Ghost ghost : $.ghosts) {
					ghost.state = GhostState.HUNTING_PAC;
					ghost.setMoveDir(LEFT);
					ghost.setWishDir(LEFT);
					ghost.setPosition(t(34), $.turningPoint.y);
					ghost.setAbsSpeed($.actorSpeed);
					ghost.show();
				}
				$.ghostIndex = 0;
			}

			@Override
			public void onUpdate(Context $) {
				if (timer.tick() == 1) {
					$.gameController.game().scores.gameScore.visible = true;
					$.gameController.game().scores.highScore.visible = true;
				} else if (timer.tick() == 2) {
					$.creditVisible = true;
				} else if (timer.atSecond(1)) {
					changeState(State.GHOSTS);
				}
				$.lightsTimer.advance();
			}
		},

		GHOSTS {
			@Override
			public void onUpdate(Context $) {
				$.lightsTimer.advance();
				Ghost ghost = $.ghosts[$.ghostIndex];
				ghost.move();
				if (ghost.moveDir() != UP && ghost.position.x <= $.turningPoint.x) {
					ghost.setMoveDir(UP);
					ghost.setWishDir(UP);
				}
				if (ghost.position.y <= $.lightsTopLeft.y + ghost.id * 18) {
					ghost.setAbsSpeed(0);
					ghost.animations().ifPresent(Animations::stop);
					if (++$.ghostIndex == $.ghosts.length) {
						changeState(State.MSPACMAN);
					}
				}
			}
		},

		MSPACMAN {
			@Override
			public void onUpdate(Context $) {
				$.lightsTimer.advance();
				$.msPacMan.move();
				if ($.msPacMan.position.x <= $.msPacManStopX) {
					$.msPacMan.setAbsSpeed(0);
					$.msPacMan.animations().get().byName("munching").reset();
					changeState(State.READY_TO_PLAY);
				}
			}
		},

		READY_TO_PLAY {
			@Override
			public void onUpdate(Context $) {
				if (timer.atSecond(1.5) && $.gameController.game().credit() == 0) {
					$.gameController.changeState(GameState.READY);
					return;
				}
				if (timer.atSecond(5)) {
					$.gameController.restartIntro();
					return;
				}
				$.lightsTimer.advance();
				$.blinking.advance();
			}
		};

		protected Fsm<FsmState<Context>, Context> controller;
		protected final TickTimer timer = new TickTimer("Timer-" + name());

		@Override
		public void setOwner(Fsm<FsmState<Context>, Context> fsm) {
			controller = fsm;
		}

		@Override
		public Fsm<FsmState<Context>, Context> getOwner() {
			return controller;
		}

		@Override
		public TickTimer timer() {
			return timer;
		}
	}
}