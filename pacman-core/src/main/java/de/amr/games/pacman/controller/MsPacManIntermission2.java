/*
Copyright (c) 2021-2023 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.games.pacman.controller;

import static de.amr.games.pacman.lib.Globals.TS;

import de.amr.games.pacman.event.GameEvents;
import de.amr.games.pacman.lib.fsm.Fsm;
import de.amr.games.pacman.lib.fsm.FsmState;
import de.amr.games.pacman.lib.steering.Direction;
import de.amr.games.pacman.lib.timer.TickTimer;
import de.amr.games.pacman.model.GameModel;
import de.amr.games.pacman.model.actors.Clapperboard;
import de.amr.games.pacman.model.actors.Pac;
import de.amr.games.pacman.model.actors.PacAnimations;

/**
 * Intermission scene 2: "The chase".
 * <p>
 * Pac-Man and Ms. Pac-Man chase each other across the screen over and over. After three turns, they both rapidly run
 * from left to right and right to left. (Played after round 5)
 * 
 * @author Armin Reichert
 */
public class MsPacManIntermission2 extends Fsm<MsPacManIntermission2.State, MsPacManIntermission2.Context> {

	private final Context intermissionData;

	public MsPacManIntermission2(GameController gameController) {
		super(State.values());
		for (var state : State.values()) {
			state.intermission = this;
		}
		this.intermissionData = new Context(gameController);
	}

	@Override
	public Context context() {
		return intermissionData;
	}

	public static class Context {
		public GameController gameController;
		public int upperY = TS * (12);
		public int middleY = TS * (18);
		public int lowerY = TS * (24);
		public Clapperboard clapperboard;
		public Pac pacMan;
		public Pac msPacMan;

		public Context(GameController gameController) {
			this.gameController = gameController;
		}
	}

	public enum State implements FsmState<Context> {

		FLAP {
			@Override
			public void onEnter(Context ctx) {
				timer.restartIndefinitely();
				ctx.clapperboard = new Clapperboard("2", "THE CHASE");
				ctx.clapperboard.setPosition(TS * (3), TS * (10));
				ctx.clapperboard.setVisible(true);
				ctx.pacMan = new Pac("Pac-Man");
				ctx.pacMan.setMoveDir(Direction.RIGHT);
				ctx.pacMan.selectAnimation(PacAnimations.PAC_MUNCHING);
				ctx.msPacMan = new Pac("Ms. Pac-Man");
				ctx.msPacMan.setMoveDir(Direction.RIGHT);
				ctx.msPacMan.selectAnimation(PacAnimations.PAC_MUNCHING);
			}

			@Override
			public void onUpdate(Context ctx) {
				if (timer.atSecond(1)) {
					GameEvents.publishSoundEvent(GameModel.SE_START_INTERMISSION_2);
				} else if (timer.atSecond(2)) {
					ctx.clapperboard.setVisible(false);
				} else if (timer.atSecond(3)) {
					intermission.changeState(State.CHASING);
				}
			}
		},

		CHASING {
			@Override
			public void onEnter(Context ctx) {
				timer.restartIndefinitely();
			}

			@Override
			public void onUpdate(Context ctx) {
				if (timer.atSecond(2.5)) {
					ctx.pacMan.setPosition(-TS * (2), ctx.upperY);
					ctx.pacMan.setMoveDir(Direction.RIGHT);
					ctx.pacMan.setPixelSpeed(2.0f);
					ctx.pacMan.show();
					ctx.msPacMan.setPosition(-TS * (8), ctx.upperY);
					ctx.msPacMan.setMoveDir(Direction.RIGHT);
					ctx.msPacMan.setPixelSpeed(2.0f);
					ctx.msPacMan.show();
				} else if (timer.atSecond(7)) {
					ctx.pacMan.setPosition(TS * (36), ctx.lowerY);
					ctx.pacMan.setMoveDir(Direction.LEFT);
					ctx.pacMan.setPixelSpeed(2.0f);
					ctx.msPacMan.setPosition(TS * (30), ctx.lowerY);
					ctx.msPacMan.setMoveDir(Direction.LEFT);
					ctx.msPacMan.setPixelSpeed(2.0f);
				} else if (timer.atSecond(11.5)) {
					ctx.pacMan.setMoveDir(Direction.RIGHT);
					ctx.pacMan.setPixelSpeed(2.0f);
					ctx.msPacMan.setPosition(TS * (-8), ctx.middleY);
					ctx.msPacMan.setMoveDir(Direction.RIGHT);
					ctx.msPacMan.setPixelSpeed(2.0f);
					ctx.pacMan.setPosition(TS * (-2), ctx.middleY);
				} else if (timer.atSecond(15.5)) {
					ctx.pacMan.setPosition(TS * (42), ctx.upperY);
					ctx.pacMan.setMoveDir(Direction.LEFT);
					ctx.pacMan.setPixelSpeed(4.0f);
					ctx.msPacMan.setPosition(TS * (30), ctx.upperY);
					ctx.msPacMan.setMoveDir(Direction.LEFT);
					ctx.msPacMan.setPixelSpeed(4.0f);
				} else if (timer.atSecond(16.5)) {
					ctx.pacMan.setPosition(TS * (-2), ctx.lowerY);
					ctx.pacMan.setMoveDir(Direction.RIGHT);
					ctx.pacMan.setPixelSpeed(4.0f);
					ctx.msPacMan.setPosition(TS * (-14), ctx.lowerY);
					ctx.msPacMan.setMoveDir(Direction.RIGHT);
					ctx.msPacMan.setPixelSpeed(4.0f);
				} else if (timer.atSecond(21)) {
					ctx.gameController.terminateCurrentState();
					return;
				}
				ctx.pacMan.move();
				ctx.msPacMan.move();
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