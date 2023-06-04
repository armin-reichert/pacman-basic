/*
Copyright (c) 2021-2023 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.games.pacman.controller;

import static de.amr.games.pacman.lib.Globals.TS;

import de.amr.games.pacman.event.GameEvents;
import de.amr.games.pacman.lib.Direction;
import de.amr.games.pacman.lib.TickTimer;
import de.amr.games.pacman.model.GameModel;
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
public class MsPacManIntermission2 {

	public final GameController gameController;

	public int upperY = TS * 12;
	public int middleY = TS * 18;
	public int lowerY = TS * 24;
	public Pac pacMan;
	public Pac msPac;

	public static final byte STATE_FLAP = 0;
	public static final byte STATE_CHASING = 1;

	private byte state;
	private TickTimer stateTimer = new TickTimer("MsPacIntermission2");

	public void changeState(byte state, long ticks) {
		this.state = state;
		stateTimer.reset(ticks);
		stateTimer.start();
	}

	public MsPacManIntermission2(GameController gameController) {
		this.gameController = gameController;
		pacMan = new Pac("Pac-Man");
		msPac = new Pac("Ms. Pac-Man");
	}

	public void tick() {
		switch (state) {
		case STATE_FLAP:
			updateFlap();
			break;
		case STATE_CHASING:
			updateChasing();
			break;
		default:
			throw new IllegalStateException("Illegal state: " + state);

		}
		stateTimer.advance();
	}

	private void updateFlap() {
		if (stateTimer.hasExpired()) {
			GameEvents.publishSoundEvent(GameModel.SE_START_INTERMISSION_2, gameController.game());
			pacMan.setMoveDir(Direction.RIGHT);
			pacMan.selectAnimation(PacAnimations.HUSBAND_MUNCHING);
			pacMan.startAnimation();
			msPac.setMoveDir(Direction.RIGHT);
			msPac.selectAnimation(PacAnimations.MUNCHING);
			msPac.startAnimation();
			changeState(STATE_CHASING, TickTimer.INDEFINITE);
		}
	}

	private void updateChasing() {
		if (stateTimer.atSecond(4.5)) {
			pacMan.setPosition(-TS * (2), upperY);
			pacMan.setMoveDir(Direction.RIGHT);
			pacMan.setPixelSpeed(2.0f);
			pacMan.show();
			msPac.setPosition(-TS * (8), upperY);
			msPac.setMoveDir(Direction.RIGHT);
			msPac.setPixelSpeed(2.0f);
			msPac.show();
		} else if (stateTimer.atSecond(9)) {
			pacMan.setPosition(TS * (36), lowerY);
			pacMan.setMoveDir(Direction.LEFT);
			pacMan.setPixelSpeed(2.0f);
			msPac.setPosition(TS * (30), lowerY);
			msPac.setMoveDir(Direction.LEFT);
			msPac.setPixelSpeed(2.0f);
		} else if (stateTimer.atSecond(13.5)) {
			pacMan.setMoveDir(Direction.RIGHT);
			pacMan.setPixelSpeed(2.0f);
			msPac.setPosition(TS * (-8), middleY);
			msPac.setMoveDir(Direction.RIGHT);
			msPac.setPixelSpeed(2.0f);
			pacMan.setPosition(TS * (-2), middleY);
		} else if (stateTimer.atSecond(17.5)) {
			pacMan.setPosition(TS * (42), upperY);
			pacMan.setMoveDir(Direction.LEFT);
			pacMan.setPixelSpeed(4.0f);
			msPac.setPosition(TS * (30), upperY);
			msPac.setMoveDir(Direction.LEFT);
			msPac.setPixelSpeed(4.0f);
		} else if (stateTimer.atSecond(18.5)) {
			pacMan.setPosition(TS * (-2), lowerY);
			pacMan.setMoveDir(Direction.RIGHT);
			pacMan.setPixelSpeed(4.0f);
			msPac.setPosition(TS * (-14), lowerY);
			msPac.setMoveDir(Direction.RIGHT);
			msPac.setPixelSpeed(4.0f);
		} else if (stateTimer.atSecond(23)) {
			gameController.terminateCurrentState();
			return;
		}
		pacMan.move();
		msPac.move();
	}
}