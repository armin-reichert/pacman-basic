/*
Copyright (c) 2021-2023 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.games.pacman.controller;

import static de.amr.games.pacman.lib.Globals.TS;

import de.amr.games.pacman.event.GameEvents;
import de.amr.games.pacman.lib.Direction;
import de.amr.games.pacman.lib.TickTimer;
import de.amr.games.pacman.lib.Vector2f;
import de.amr.games.pacman.model.GameModel;
import de.amr.games.pacman.model.actors.Entity;
import de.amr.games.pacman.model.actors.Pac;
import de.amr.games.pacman.model.actors.PacAnimations;

/**
 * Intermission scene 3: "Junior".
 * 
 * <p>
 * Pac-Man and Ms. Pac-Man gradually wait for a stork, who flies overhead with a little blue bundle. The stork drops the
 * bundle, which falls to the ground in front of Pac-Man and Ms. Pac-Man, and finally opens up to reveal a tiny Pac-Man.
 * (Played after rounds 9, 13, and 17)
 * 
 * @author Armin Reichert
 */
public class MsPacManIntermission3 {

	public final GameController gameController;
	public int groundY = TS * (24);
	public boolean clapVisible = false;
	public Pac pacMan;
	public Pac msPacMan;
	public Entity stork;
	public Entity bag;
	public boolean bagOpen;
	public int numBagBounces;

	public static final byte STATE_FLAP = 0;
	public static final byte STATE_DELIVER_JUNIOR = 1;
	public static final byte STATE_STORK_LEAVES_SCENE = 2;

	private byte state;
	private TickTimer stateTimer = new TickTimer("MsPacIntermission3");

	public void changeState(byte state, long ticks) {
		this.state = state;
		stateTimer.reset(ticks);
		stateTimer.start();
	}

	public MsPacManIntermission3(GameController gameController) {
		this.gameController = gameController;
		clapVisible = true;
		pacMan = new Pac("Pac-Man");
		msPacMan = new Pac("Ms. Pac-Man");
		stork = new Entity();
		bag = new Entity();
	}

	public void tick() {
		switch (state) {
		case STATE_FLAP:
			updateFlap();
			break;
		case STATE_DELIVER_JUNIOR:
			updateDeliverJunior();
			break;
		case STATE_STORK_LEAVES_SCENE:
			updateStorkLeavesScene();
			break;
		default:
			throw new IllegalStateException("Illegal state: " + state);
		}
		stateTimer.advance();
	}

	private void updateFlap() {
		if (stateTimer.atSecond(1)) {
			GameEvents.publishSoundEvent(GameModel.SE_START_INTERMISSION_3, gameController.game());
		} else if (stateTimer.atSecond(2)) {
			clapVisible = false;
		} else if (stateTimer.atSecond(3)) {
			pacMan.setMoveDir(Direction.RIGHT);
			pacMan.setPosition(TS * (3), groundY - 4);
			pacMan.selectAnimation(PacAnimations.HUSBAND_MUNCHING);
			pacMan.show();

			msPacMan.setMoveDir(Direction.RIGHT);
			msPacMan.setPosition(TS * (5), groundY - 4);
			msPacMan.selectAnimation(PacAnimations.MUNCHING);
			msPacMan.show();

			stork.setPosition(TS * (30), TS * (12));
			stork.setVelocity(-0.8f, 0);
			stork.show();

			bag.setPosition(stork.position().plus(-14, 3));
			bag.setVelocity(stork.velocity());
			bag.setAcceleration(Vector2f.ZERO);
			bag.show();
			bagOpen = false;
			numBagBounces = 0;
			changeState(STATE_DELIVER_JUNIOR, TickTimer.INDEFINITE);
		}
	}

	private void updateDeliverJunior() {
		stork.move();
		bag.move();

		// release bag from storks beak?
		if ((int) stork.position().x() == TS * (20)) {
			bag.setAcceleration(0, 0.04f);
			stork.setVelocity(-1, 0);
		}

		// (closed) bag reaches ground for first time?
		if (!bagOpen && bag.position().y() > groundY) {
			++numBagBounces;
			if (numBagBounces < 3) {
				bag.setVelocity(-0.2f, -1f / numBagBounces);
				bag.setY(groundY);
			} else {
				bagOpen = true;
				bag.setVelocity(Vector2f.ZERO);
				changeState(STATE_STORK_LEAVES_SCENE, 3 * 60);
			}
		}
	}

	private void updateStorkLeavesScene() {
		stork.move();
		if (stateTimer.hasExpired()) {
			gameController.terminateCurrentState();
		}
	}
}