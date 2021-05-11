package de.amr.games.pacman.ui.mspacman;

import static de.amr.games.pacman.model.world.PacManGameWorld.t;

import de.amr.games.pacman.controller.PacManGameController;
import de.amr.games.pacman.lib.Direction;
import de.amr.games.pacman.lib.TickTimer;
import de.amr.games.pacman.lib.V2d;
import de.amr.games.pacman.model.common.Pac;
import de.amr.games.pacman.model.mspacman.entities.Flap;
import de.amr.games.pacman.model.mspacman.entities.JuniorBag;
import de.amr.games.pacman.model.mspacman.entities.Stork;

/**
 * Intermission scene 3: "Junior".
 * 
 * <p>
 * Pac-Man and Ms. Pac-Man gradually wait for a stork, who flies overhead with a little blue bundle.
 * The stork drops the bundle, which falls to the ground in front of Pac-Man and Ms. Pac-Man, and
 * finally opens up to reveal a tiny Pac-Man. (Played after rounds 9, 13, and 17)
 * 
 * @author Armin Reichert
 */
public abstract class MsPacMan_IntermissionScene3_Controller {

	public enum Phase {
		FLAP, ACTION, READY_TO_PLAY;
	}

	static final int GROUND_Y = t(24);

	public final PacManGameController gameController;
	public final TickTimer timer = new TickTimer();
	public Phase phase;
	public Flap flap;
	public Pac pacMan;
	public Pac msPacMan;
	public Stork stork;
	public JuniorBag bag;
	public int numBagBounces;

	public MsPacMan_IntermissionScene3_Controller(PacManGameController gameController) {
		this.gameController = gameController;
	}

	public abstract void playIntermissionSound();

	public abstract void playFlapAnimation();

	private void enter(Phase newPhase) {
		phase = newPhase;
		timer.reset();
		timer.start();
	}

	private void enterSeconds(Phase newPhase, double seconds) {
		phase = newPhase;
		timer.resetSeconds(seconds);
		timer.start();
	}

	public void init() {
		flap = new Flap(3, "JUNIOR");
		flap.setPosition(t(3), t(10));
		flap.setVisible(true);

		pacMan = new Pac("Pac-Man", null);
		pacMan.setDir(Direction.RIGHT);
		pacMan.setPosition(t(3), GROUND_Y - 4);

		msPacMan = new Pac("Ms. Pac-Man", null);
		msPacMan.setDir(Direction.RIGHT);
		msPacMan.setPosition(t(5), GROUND_Y - 4);

		stork = new Stork();
		stork.setPosition(t(30), t(12));

		bag = new JuniorBag();
		bag.hold = true;
		bag.open = false;
		bag.setPosition(stork.position().plus(-14, 3));

		enter(Phase.FLAP);
	}

	public void update() {
		switch (phase) {

		case FLAP:
			if (timer.isRunningSeconds(1)) {
				playFlapAnimation();
			} else if (timer.isRunningSeconds(2)) {
				flap.setVisible(false);
				playIntermissionSound();
				enter(Phase.ACTION);
			}
			timer.tick();
			break;

		case ACTION:
			if (timer.hasJustStarted()) {
				pacMan.setVisible(true);
				msPacMan.setVisible(true);
				stork.setVisible(true);
				bag.setVisible(true);
				stork.setDir(Direction.LEFT);
				stork.setSpeed(1.25);
				bag.setVelocity(new V2d(-1.25f, 0));
			}
			// release bag from storks beak?
			if (bag.hold && (int) stork.position().x == t(24)) {
				bag.hold = false;
			}
			// (closed) bag reaches ground for first time?
			if (!bag.open && bag.position().y > GROUND_Y) {
				++numBagBounces;
				if (numBagBounces < 5) {
					bag.setVelocity(new V2d(-0.2f, -1f / numBagBounces));
					bag.setPosition(bag.position().x, GROUND_Y);
				} else {
					bag.open = true;
					bag.setVelocity(V2d.NULL);
					enterSeconds(Phase.READY_TO_PLAY, 3);
				}
			}
			stork.move();
			bag.move();
			timer.tick();
			break;

		case READY_TO_PLAY:
			if (timer.hasExpired()) {
				gameController.stateTimer().forceExpiration();
				return;
			}
			stork.move();
			timer.tick();
			break;

		default:
			break;
		}
	}
}