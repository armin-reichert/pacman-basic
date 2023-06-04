/*
Copyright (c) 2021-2023 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.games.pacman.controller;

import static de.amr.games.pacman.lib.Globals.TS;

import org.tinylog.Logger;

import de.amr.games.pacman.event.GameEvents;
import de.amr.games.pacman.lib.Direction;
import de.amr.games.pacman.lib.TickTimer;
import de.amr.games.pacman.lib.Vector2f;
import de.amr.games.pacman.model.GameModel;
import de.amr.games.pacman.model.actors.Entity;
import de.amr.games.pacman.model.actors.Ghost;
import de.amr.games.pacman.model.actors.GhostAnimations;
import de.amr.games.pacman.model.actors.Pac;
import de.amr.games.pacman.model.actors.PacAnimations;

/**
 * Intermission scene 1: "They meet".
 * <p>
 * Pac-Man leads Inky and Ms. Pac-Man leads Pinky. Soon, the two Pac-Men are about to collide, they quickly move
 * upwards, causing Inky and Pinky to collide and vanish. Finally, Pac-Man and Ms. Pac-Man face each other at the top of
 * the screen and a big pink heart appears above them. (Played after round 2)
 * 
 * @author Armin Reichert
 */
public class MsPacManIntermission1 {

	public final GameController gameController;

	public int upperY = TS * (12);
	public int middleY = TS * (18);
	public int lowerY = TS * (24);
	public boolean clapVisible = false;
	public float pacSpeedChased = 1.125f;
	public float pacSpeedRising = 0.75f;
	public float ghostSpeedAfterColliding = 0.3f;
	public float ghostSpeedChasing = 1.25f;
	public Pac pacMan;
	public Pac msPac;
	public Ghost pinky;
	public Ghost inky;
	public Entity heart;

	public static final byte STATE_FLAP = 0;
	public static final byte STATE_CHASED_BY_GHOSTS = 1;
	public static final byte STATE_COMING_TOGETHER = 2;
	public static final byte STATE_IN_HEAVEN = 3;

	private byte state;
	private TickTimer stateTimer = new TickTimer("MsPacIntermission1");

	public void changeState(byte state, long ticks) {
		this.state = state;
		stateTimer.reset(ticks);
		stateTimer.start();
	}

	public MsPacManIntermission1(GameController gameController) {
		this.gameController = gameController;
		Logger.trace("Creating guys for intermission 1");
		pacMan = new Pac("Pac-Man");
		inky = new Ghost(GameModel.CYAN_GHOST, "Inky");
		msPac = new Pac("Ms. Pac-Man");
		pinky = new Ghost(GameModel.PINK_GHOST, "Pinky");
		heart = new Entity();
		clapVisible = true;
	}

	public void tick() {
		switch (state) {
		case STATE_FLAP:
			updateFlap();
			break;
		case STATE_CHASED_BY_GHOSTS:
			updateChasedByGhosts();
			break;
		case STATE_COMING_TOGETHER:
			updateComingTogether();
			break;
		case STATE_IN_HEAVEN:
			if (stateTimer.hasExpired()) {
				gameController.terminateCurrentState();
				return;
			}
			break;
		default:
			throw new IllegalStateException("Illegal state: " + state);
		}
		stateTimer.advance();
	}

	private void updateFlap() {
		if (stateTimer.atSecond(1)) {
			GameEvents.publishSoundEvent(GameModel.SE_START_INTERMISSION_1, gameController.game());
		} else if (stateTimer.hasExpired()) {
			clapVisible = false;
			pacMan.setMoveDir(Direction.RIGHT);
			pacMan.setPosition(-TS * (2), upperY);
			pacMan.setPixelSpeed(pacSpeedChased);
			pacMan.selectAnimation(PacAnimations.HUSBAND_MUNCHING);
			pacMan.startAnimation();
			pacMan.show();

			inky.setMoveAndWishDir(Direction.RIGHT);
			inky.setPosition(pacMan.position().minus(TS * (6), 0));
			inky.setPixelSpeed(ghostSpeedChasing);
			inky.selectAnimation(GhostAnimations.GHOST_NORMAL);
			inky.startAnimation();
			inky.show();

			msPac.setMoveDir(Direction.LEFT);
			msPac.setPosition(TS * (30), lowerY);
			msPac.setPixelSpeed(pacSpeedChased);
			msPac.selectAnimation(PacAnimations.MUNCHING);
			msPac.startAnimation();
			msPac.show();

			pinky.setMoveAndWishDir(Direction.LEFT);
			pinky.setPosition(msPac.position().plus(TS * (6), 0));
			pinky.setPixelSpeed(ghostSpeedChasing);
			pinky.selectAnimation(GhostAnimations.GHOST_NORMAL);
			pinky.startAnimation();
			pinky.show();

			changeState(STATE_CHASED_BY_GHOSTS, TickTimer.INDEFINITE);
		}
	}

	private void updateChasedByGhosts() {
		if (inky.position().x() > TS * 30) {
			msPac.setPosition(TS * (-3), middleY);
			msPac.setMoveDir(Direction.RIGHT);
			pinky.setPosition(msPac.position().minus(TS * (5), 0));
			pinky.setMoveAndWishDir(Direction.RIGHT);
			pacMan.setPosition(TS * (31), middleY);
			pacMan.setMoveDir(Direction.LEFT);
			inky.setPosition(pacMan.position().plus(TS * (5), 0));
			inky.setMoveAndWishDir(Direction.LEFT);
			changeState(STATE_COMING_TOGETHER, TickTimer.INDEFINITE);
		} else {
			pacMan.move();
			msPac.move();
			inky.move();
			pinky.move();
		}
	}

	private void updateComingTogether() {
		// Pac-Man and Ms. Pac-Man reach end position?
		if (pacMan.moveDir() == Direction.UP && pacMan.position().y() < upperY) {
			pacMan.setPixelSpeed(0);
			pacMan.setMoveDir(Direction.LEFT);
			pacMan.stopAnimation();
			pacMan.resetAnimation();

			msPac.setPixelSpeed(0);
			msPac.setMoveDir(Direction.RIGHT);
			msPac.stopAnimation();
			msPac.resetAnimation();

			inky.setPixelSpeed(0);
			inky.hide();

			pinky.setPixelSpeed(0);
			pinky.hide();

			heart.setPosition((pacMan.position().x() + msPac.position().x()) / 2, pacMan.position().y() - TS * (2));
			heart.show();
			changeState(STATE_IN_HEAVEN, 3 * 60);
		}

		// Pac-Man and Ms. Pac-Man meet?
		else if (pacMan.moveDir() == Direction.LEFT && pacMan.position().x() - msPac.position().x() < TS * (2)) {
			pacMan.setMoveDir(Direction.UP);
			pacMan.setPixelSpeed(pacSpeedRising);
			msPac.setMoveDir(Direction.UP);
			msPac.setPixelSpeed(pacSpeedRising);
		}

		// Inky and Pinky collide?
		else if (inky.moveDir() == Direction.LEFT && inky.position().x() - pinky.position().x() < TS * (2)) {
			inky.setMoveAndWishDir(Direction.RIGHT);
			inky.setPixelSpeed(ghostSpeedAfterColliding);
			inky.setVelocity(inky.velocity().minus(0, 2.0f));
			inky.setAcceleration(0, 0.4f);

			pinky.setMoveAndWishDir(Direction.LEFT);
			pinky.setPixelSpeed(ghostSpeedAfterColliding);
			pinky.setVelocity(pinky.velocity().minus(0, 2.0f));
			pinky.setAcceleration(0, 0.4f);
		}

		else {
			pacMan.move();
			msPac.move();
			inky.move();
			pinky.move();
			if (inky.position().y() > middleY) {
				inky.setPosition(inky.position().x(), middleY);
				inky.setAcceleration(Vector2f.ZERO);
			}
			if (pinky.position().y() > middleY) {
				pinky.setPosition(pinky.position().x(), middleY);
				pinky.setAcceleration(Vector2f.ZERO);
			}
		}
	}
}