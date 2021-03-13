package de.amr.games.pacman.ui.mspacman;

import static de.amr.games.pacman.model.world.PacManGameWorld.t;

import de.amr.games.pacman.controller.PacManGameController;
import de.amr.games.pacman.lib.Direction;
import de.amr.games.pacman.lib.TickTimer;
import de.amr.games.pacman.model.common.Flap;
import de.amr.games.pacman.model.common.Pac;
import de.amr.games.pacman.ui.animation.TimedSequence;
import de.amr.games.pacman.ui.animation.PacManGameAnimations2D;
import de.amr.games.pacman.ui.sound.PacManGameSound;
import de.amr.games.pacman.ui.sound.SoundManager;

/**
 * Intermission scene 2: "The chase".
 * <p>
 * Pac-Man and Ms. Pac-Man chase each other across the screen over and over. After three turns, they
 * both rapidly run from left to right and right to left. (Played after round 5)
 * 
 * @author Armin Reichert
 */
public class MsPacMan_IntermissionScene2_Controller {

	public enum Phase {

		FLAP, ACTION;
	}

	public static final int UPPER_Y = t(12), LOWER_Y = t(24), MIDDLE_Y = t(18);

	public final PacManGameController controller;
	public final PacManGameAnimations2D animations;
	public final SoundManager sounds;
	public final TickTimer timer = new TickTimer();
	public Phase phase;
	public Flap flap;
	public Pac pacMan, msPacMan;

	public void enter(Phase newPhase) {
		phase = newPhase;
		timer.reset();
		timer.start();
	}

	public MsPacMan_IntermissionScene2_Controller(PacManGameController controller, PacManGameAnimations2D animations,
			SoundManager sounds) {
		this.controller = controller;
		this.animations = animations;
		this.sounds = sounds;
	}

	public void start() {
		flap = new Flap(2, "THE CHASE", animations.flapFlapping());
		flap.setTilePosition(3, 10);
		flap.visible = true;

		pacMan = new Pac("Pac-Man", Direction.RIGHT);
		msPacMan = new Pac("Ms. Pac-Man", Direction.RIGHT);

		enter(Phase.FLAP);
	}

	public void update() {
		switch (phase) {
		case FLAP:
			if (timer.isRunningSeconds(1)) {
				flap.flapping.restart();
			}
			if (timer.isRunningSeconds(2)) {
				flap.visible = false;
				sounds.play(PacManGameSound.INTERMISSION_2);
			}
			if (timer.isRunningSeconds(4.5)) {
				enter(Phase.ACTION);
			}
			flap.flapping.animate();
			timer.tick();
			break;

		case ACTION:
			if (timer.isRunningSeconds(1.5)) {
				pacMan.visible = true;
				pacMan.setPosition(-t(2), UPPER_Y);
				msPacMan.visible = true;
				msPacMan.setPosition(-t(8), UPPER_Y);
				pacMan.dir = msPacMan.dir = Direction.RIGHT;
				pacMan.speed = msPacMan.speed = 2;
				animations.playerAnimations().spouseMunching(pacMan).forEach(TimedSequence::restart);
				animations.playerAnimations().playerMunching(msPacMan).forEach(TimedSequence::restart);
			}
			if (timer.isRunningSeconds(6)) {
				msPacMan.setPosition(t(30), LOWER_Y);
				msPacMan.visible = true;
				pacMan.setPosition(t(36), LOWER_Y);
				msPacMan.dir = pacMan.dir = Direction.LEFT;
				msPacMan.speed = pacMan.speed = 2;
			}
			if (timer.isRunningSeconds(10.5)) {
				msPacMan.setPosition(t(-8), MIDDLE_Y);
				pacMan.setPosition(t(-2), MIDDLE_Y);
				msPacMan.dir = pacMan.dir = Direction.RIGHT;
				msPacMan.speed = pacMan.speed = 2;
			}
			if (timer.isRunningSeconds(14.5)) {
				msPacMan.setPosition(t(30), UPPER_Y);
				pacMan.setPosition(t(42), UPPER_Y);
				msPacMan.dir = pacMan.dir = Direction.LEFT;
				msPacMan.speed = pacMan.speed = 4;
			}
			if (timer.isRunningSeconds(15.5)) {
				msPacMan.setPosition(t(-14), LOWER_Y);
				pacMan.setPosition(t(-2), LOWER_Y);
				msPacMan.dir = pacMan.dir = Direction.RIGHT;
				msPacMan.speed = pacMan.speed = 4;
			}
			if (timer.isRunningSeconds(20)) {
				controller.letCurrentGameStateExpire();
				return;
			}
			timer.tick();
			break;
		default:
			break;
		}
		pacMan.move();
		msPacMan.move();
	}
}