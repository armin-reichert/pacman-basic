package de.amr.games.pacman.ui.mspacman;

import static de.amr.games.pacman.model.world.PacManGameWorld.t;

import de.amr.games.pacman.controller.PacManGameController;
import de.amr.games.pacman.lib.Direction;
import de.amr.games.pacman.lib.TickTimer;
import de.amr.games.pacman.model.common.GameEntity;
import de.amr.games.pacman.model.common.Ghost;
import de.amr.games.pacman.model.common.Pac;
import de.amr.games.pacman.model.mspacman.Flap;

/**
 * Intermission scene 1: "They meet".
 * <p>
 * Pac-Man leads Inky and Ms. Pac-Man leads Pinky. Soon, the two Pac-Men are
 * about to collide, they quickly move upwards, causing Inky and Pinky to
 * collide and vanish. Finally, Pac-Man and Ms. Pac-Man face each other at the
 * top of the screen and a big pink heart appears above them. (Played after
 * round 2)
 * 
 * @author Armin Reichert
 */
public abstract class MsPacMan_IntermissionScene1_Controller {

	public enum Phase {

		FLAP, CHASED_BY_GHOSTS, COMING_TOGETHER, READY_TO_PLAY;

	}

	public static final int upperY = t(12), lowerY = t(24), middleY = t(18);

	public final PacManGameController gameController;
	public final TickTimer timer = new TickTimer();
	public Phase phase;
	public Flap flap;
	public Pac pacMan, msPac;
	public Ghost pinky, inky;
	public GameEntity heart;
	public boolean ghostsMet;

	public MsPacMan_IntermissionScene1_Controller(PacManGameController gameController) {
		this.gameController = gameController;
	}

	public abstract void playIntermissionSound();

	public abstract void playFlapAnimation();

	public void init() {
		flap = new Flap(1, "THEY MEET");
		flap.setPosition(t(3), t(10));
		flap.visible = true;

		pacMan = new Pac("Pac-Man", Direction.RIGHT);
		pacMan.setPosition(-t(2), upperY);
		pacMan.visible = true;

		inky = new Ghost(2, "Inky", Direction.RIGHT);
		inky.setPositionRelativeTo(pacMan, -t(3), 0);
		inky.visible = true;

		msPac = new Pac("Ms. Pac-Man", Direction.LEFT);
		msPac.setPosition(t(30), lowerY);
		msPac.visible = true;

		pinky = new Ghost(1, "Pinky", Direction.LEFT);
		pinky.setPositionRelativeTo(msPac, t(3), 0);
		pinky.visible = true;

		heart = new GameEntity();
		ghostsMet = false;

		enterSeconds(Phase.FLAP, 2);
	}

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

	public void update() {
		switch (phase) {

		case FLAP:
			if (timer.isRunningSeconds(1)) {
				playFlapAnimation();
			}
			if (timer.hasExpired()) {
				flap.visible = false;
				playIntermissionSound();
				startChasedByGhosts();
				return;
			}
			timer.tick();
			break;

		case CHASED_BY_GHOSTS:
			inky.move();
			pacMan.move();
			pinky.move();
			msPac.move();
			if (inky.position.x > t(30)) {
				startComingTogether();
			}
			timer.tick();
			break;

		case COMING_TOGETHER:
			inky.move();
			pinky.move();
			pacMan.move();
			msPac.move();
			if (pacMan.dir() == Direction.LEFT && pacMan.position.x < t(15)) {
				pacMan.setDir(Direction.UP);
				msPac.setDir(Direction.UP);
			}
			if (pacMan.dir() == Direction.UP && pacMan.position.y < upperY) {
				pacMan.speed = msPac.speed = 0;
				pacMan.setDir(Direction.LEFT);
				msPac.setDir(Direction.RIGHT);
				heart.setPosition((pacMan.position.x + msPac.position.x) / 2, pacMan.position.y - t(2));
				heart.visible = true;
				inky.speed = pinky.speed = 0;
				enterSeconds(Phase.READY_TO_PLAY, 4);
			}
			if (!ghostsMet && inky.position.x - pinky.position.x < 16) {
				ghostsMet = true;
				inky.setDir(inky.dir().opposite());
				inky.setWishDir(inky.dir());
				pinky.setDir(pinky.dir().opposite());
				pinky.setWishDir(pinky.dir());
				inky.speed = pinky.speed = 0.2f;
			}
			timer.tick();
			break;

		case READY_TO_PLAY:
			if (timer.isRunningSeconds(2)) {
				inky.visible = false;
				pinky.visible = false;
			}
			if (timer.hasExpired()) {
				gameController.stateTimer().forceExpiration();
				return;
			}
			timer.tick();
			break;

		default:
			break;
		}
	}

	public void startChasedByGhosts() {
		pacMan.speed = msPac.speed = 1.0;
		inky.speed = pinky.speed = 1.0;
		enter(Phase.CHASED_BY_GHOSTS);
	}

	public void startComingTogether() {
		msPac.setPosition(t(-2), middleY);
		msPac.setDir(Direction.RIGHT);
		pacMan.setPosition(t(30), middleY);
		pacMan.setDir(Direction.LEFT);
		inky.setPosition(t(33), middleY);
		inky.setDir(Direction.LEFT);
		inky.setWishDir(Direction.LEFT);
		pinky.setPosition(t(-5), middleY);
		pinky.setDir(Direction.RIGHT);
		pinky.setWishDir(Direction.RIGHT);
		enter(Phase.COMING_TOGETHER);
	}
}