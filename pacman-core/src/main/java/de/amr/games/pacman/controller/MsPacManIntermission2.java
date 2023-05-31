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

	private final Context context;

	public MsPacManIntermission2(GameController gameController) {
		super(State.values());
		for (var state : State.values()) {
			state.intermission = this;
		}
		this.context = new Context(gameController);
	}

	@Override
	public Context context() {
		return context;
	}

	public static class Context {
		public GameController gameController;
		public int upperY = TS * (12);
		public int middleY = TS * (18);
		public int lowerY = TS * (24);
		public Clapperboard clapperboard;
		public Pac pacMan;
		public Pac msPac;

		public Context(GameController gameController) {
			this.gameController = gameController;
		}

		public GameModel game() {
			return gameController.game();
		}
	}

	public enum State implements FsmState<Context> {

		INIT {
			@Override
			public void onEnter(Context ctx) {
				ctx.clapperboard = new Clapperboard("2", "THE CHASE");
				ctx.pacMan = new Pac("Pac-Man");
				ctx.msPac = new Pac("Ms. Pac-Man");
			}

			@Override
			public void onUpdate(Context ctx) {
				intermission.changeState(FLAP);
			}
		},

		FLAP {
			@Override
			public void onEnter(Context ctx) {
				timer.resetSeconds(2);
				timer.start();
				ctx.clapperboard.setPosition(TS * 3, TS * 10);
				ctx.clapperboard.setVisible(true);
			}

			@Override
			public void onUpdate(Context ctx) {
				if (timer.hasExpired()) {
					ctx.clapperboard.setVisible(false);
					GameEvents.publishSoundEvent(GameModel.SE_START_INTERMISSION_2, ctx.game());
					intermission.changeState(State.CHASING);
				}
			}
		},

		CHASING {
			@Override
			public void onEnter(Context ctx) {
				timer.restartIndefinitely();
				ctx.pacMan.setMoveDir(Direction.RIGHT);
				ctx.pacMan.selectAnimation(PacAnimations.HUSBAND_MUNCHING);
				ctx.pacMan.startAnimation();
				ctx.msPac.setMoveDir(Direction.RIGHT);
				ctx.msPac.selectAnimation(PacAnimations.PAC_MUNCHING);
				ctx.msPac.startAnimation();
			}

			@Override
			public void onUpdate(Context ctx) {
				if (timer.atSecond(4.5)) {
					ctx.pacMan.setPosition(-TS * (2), ctx.upperY);
					ctx.pacMan.setMoveDir(Direction.RIGHT);
					ctx.pacMan.setPixelSpeed(2.0f);
					ctx.pacMan.show();
					ctx.msPac.setPosition(-TS * (8), ctx.upperY);
					ctx.msPac.setMoveDir(Direction.RIGHT);
					ctx.msPac.setPixelSpeed(2.0f);
					ctx.msPac.show();
				} else if (timer.atSecond(9)) {
					ctx.pacMan.setPosition(TS * (36), ctx.lowerY);
					ctx.pacMan.setMoveDir(Direction.LEFT);
					ctx.pacMan.setPixelSpeed(2.0f);
					ctx.msPac.setPosition(TS * (30), ctx.lowerY);
					ctx.msPac.setMoveDir(Direction.LEFT);
					ctx.msPac.setPixelSpeed(2.0f);
				} else if (timer.atSecond(13.5)) {
					ctx.pacMan.setMoveDir(Direction.RIGHT);
					ctx.pacMan.setPixelSpeed(2.0f);
					ctx.msPac.setPosition(TS * (-8), ctx.middleY);
					ctx.msPac.setMoveDir(Direction.RIGHT);
					ctx.msPac.setPixelSpeed(2.0f);
					ctx.pacMan.setPosition(TS * (-2), ctx.middleY);
				} else if (timer.atSecond(17.5)) {
					ctx.pacMan.setPosition(TS * (42), ctx.upperY);
					ctx.pacMan.setMoveDir(Direction.LEFT);
					ctx.pacMan.setPixelSpeed(4.0f);
					ctx.msPac.setPosition(TS * (30), ctx.upperY);
					ctx.msPac.setMoveDir(Direction.LEFT);
					ctx.msPac.setPixelSpeed(4.0f);
				} else if (timer.atSecond(18.5)) {
					ctx.pacMan.setPosition(TS * (-2), ctx.lowerY);
					ctx.pacMan.setMoveDir(Direction.RIGHT);
					ctx.pacMan.setPixelSpeed(4.0f);
					ctx.msPac.setPosition(TS * (-14), ctx.lowerY);
					ctx.msPac.setMoveDir(Direction.RIGHT);
					ctx.msPac.setPixelSpeed(4.0f);
				} else if (timer.atSecond(23)) {
					ctx.gameController.terminateCurrentState();
					return;
				}
				ctx.pacMan.move();
				ctx.msPac.move();
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