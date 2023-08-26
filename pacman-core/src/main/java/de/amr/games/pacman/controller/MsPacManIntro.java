/*
Copyright (c) 2021-2023 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.games.pacman.controller;

import static de.amr.games.pacman.lib.Globals.TS;
import static de.amr.games.pacman.lib.Globals.isOdd;
import static de.amr.games.pacman.lib.Globals.v2i;

import java.util.BitSet;
import java.util.List;

import org.tinylog.Logger;

import de.amr.games.pacman.lib.Direction;
import de.amr.games.pacman.lib.Fsm;
import de.amr.games.pacman.lib.FsmState;
import de.amr.games.pacman.lib.TickTimer;
import de.amr.games.pacman.lib.Vector2i;
import de.amr.games.pacman.model.GameModel;
import de.amr.games.pacman.model.actors.Animations;
import de.amr.games.pacman.model.actors.Ghost;
import de.amr.games.pacman.model.actors.Pac;
import de.amr.games.pacman.model.actors.PacAnimations;

/**
 * Intro scene of the Ms. Pac-Man game.
 * <p>
 * The ghosts and Ms. Pac-Man are introduced on a billboard and are marching in one after another.
 * 
 * @author Armin Reichert
 */
public class MsPacManIntro extends Fsm<MsPacManIntro.State, MsPacManIntro.Context> {

	public static class Context {
		public float          speed                = 1.1f;
		public int            stopY                = TS * 11 + 1;
		public int            stopX                = TS * 6 - 4; 
		public int            stopMsPacX           = TS * 15 + 2;
		public int            ticksUntilLifting    = 0;
		public Vector2i       titlePosition        = v2i(TS * 10, TS * 8);
		public TickTimer      marqueeTimer         = new TickTimer("marquee-timer");
		public int            numBulbs             = 96;
		public int            bulbOnDistance       = 16;
		public Pac            msPacMan             = new Pac("Ms. Pac-Man");
		public Ghost[]        ghosts               = {
				new Ghost(GameModel.RED_GHOST,   "Blinky"),
				new Ghost(GameModel.PINK_GHOST,  "Pinky"),
				new Ghost(GameModel.CYAN_GHOST,  "Inky"),
				new Ghost(GameModel.ORANGE_GHOST,"Sue")
		};
		public int ghostIndex = 0;

		/**
		 * In the Arcade game, 6 of the 96 bulbs are switched-on every frame, shifting every tick. The bulbs in the leftmost
		 * column however are switched-off every second frame. Maybe a bug?
		 * 
		 * @return bitset indicating which marquee bulbs are on
		 */
		public BitSet marqueeState() {
			var state = new BitSet(numBulbs);
			long t = marqueeTimer.tick();
			for (int b = 0; b < 6; ++b) {
				state.set((int) (b * bulbOnDistance + t) % numBulbs);
			}
			for (int i = 81; i < numBulbs; ++i) {
				if (isOdd(i)) {
					state.clear(i);
				}
			}
			return state;
		}
	}

	public enum State implements FsmState<MsPacManIntro.Context> {

		START {
			@Override
			public void onEnter(MsPacManIntro.Context ctx) {
				ctx.marqueeTimer.restartIndefinitely();
				ctx.msPacMan.setPosition(TS * 31, TS * 20);
				ctx.msPacMan.setMoveDir(Direction.LEFT);
				ctx.msPacMan.setPixelSpeed(ctx.speed);
				ctx.msPacMan.selectAnimation(PacAnimations.MUNCHING);
				ctx.msPacMan.startAnimation();
				for (var ghost : ctx.ghosts) {
					ghost.setPosition(TS * 33.5f, TS * 20);
					ghost.setMoveAndWishDir(Direction.LEFT);
					ghost.setPixelSpeed(ctx.speed);
					ghost.enterStateHuntingPac();
					ghost.startAnimation();
				};
				ctx.ghostIndex = 0;
			}

			@Override
			public void onUpdate(MsPacManIntro.Context ctx) {
				ctx.marqueeTimer.advance();
				if (timer.atSecond(1)) {
					intro.changeState(State.GHOSTS);
				}
			}
		},

		GHOSTS {
			@Override
			public void onUpdate(MsPacManIntro.Context ctx) {
				ctx.marqueeTimer.advance();
				var ghost = ctx.ghosts[ctx.ghostIndex];
				ghost.show();

				if (ghost.moveDir() == Direction.LEFT) {
					if (ghost.position().x() <= ctx.stopX) {
						ghost.setX(ctx.stopX);
						ghost.setMoveAndWishDir(Direction.UP);
						ctx.ticksUntilLifting = 2;
					} else {
						ghost.move();
					}
					return;
				}

				if (ghost.moveDir() == Direction.UP) {
					if (ctx.ticksUntilLifting > 0) {
						ctx.ticksUntilLifting -= 1;
						Logger.trace("Ticks until lifting {}: {}", ghost.name(), ctx.ticksUntilLifting);
						return;
					}
					if (ghost.position().y() <= ctx.stopY + ghost.id() * 16) {
						ghost.setPixelSpeed(0);
						ghost.animations().ifPresent(ani -> {
							ani.stopSelected();
							ani.resetSelected();
						});
						if (ctx.ghostIndex == 3) {
							intro.changeState(State.MSPACMAN);
						} else {
							++ctx.ghostIndex;
						}
					} else {
						ghost.move();
					}
				}
			}
		},

		MSPACMAN {
			@Override
			public void onUpdate(MsPacManIntro.Context ctx) {
				ctx.marqueeTimer.advance();
				ctx.msPacMan.show();
				ctx.msPacMan.move();
				if (ctx.msPacMan.position().x() <= ctx.stopMsPacX) {
					ctx.msPacMan.setPixelSpeed(0);
					ctx.msPacMan.animations().ifPresent(Animations::resetSelected);
					intro.changeState(State.READY_TO_PLAY);
				}
			}
		},

		READY_TO_PLAY {
			@Override
			public void onUpdate(MsPacManIntro.Context ctx) {
				ctx.marqueeTimer.advance();
				if (timer.atSecond(2.0) && !GameController.it().hasCredit()) {
					GameController.it().changeState(GameState.READY);
					// go into demo mode
				} else if (timer.atSecond(5)) {
					GameController.it().changeState(GameState.CREDIT);
				}
			}
		};

		MsPacManIntro intro;
		final TickTimer timer = new TickTimer("Timer-" + name());

		@Override
		public TickTimer timer() {
			return timer;
		}
	}

	private final Context introData;

	public MsPacManIntro() {
		super(State.values());
		for (var state : states) {
			state.intro = this;
		}
		introData = new Context();
	}

	@Override
	public Context context() {
		return introData;
	}
}