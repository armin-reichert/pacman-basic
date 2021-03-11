package de.amr.games.pacman.ui.mspacman;

import static de.amr.games.pacman.lib.God.clock;
import static de.amr.games.pacman.model.world.PacManGameWorld.t;

import de.amr.games.pacman.controller.PacManGameController;
import de.amr.games.pacman.lib.Direction;
import de.amr.games.pacman.lib.TickTimer;
import de.amr.games.pacman.lib.V2d;
import de.amr.games.pacman.model.common.Flap;
import de.amr.games.pacman.model.common.JuniorBag;
import de.amr.games.pacman.model.common.Pac;
import de.amr.games.pacman.model.common.Stork;
import de.amr.games.pacman.ui.animation.PacManGameAnimations;
import de.amr.games.pacman.ui.sound.PacManGameSound;
import de.amr.games.pacman.ui.sound.SoundManager;

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
public class MsPacMan_IntermissionScene3_Controller {

	public enum Phase {

		FLAP, ACTION, READY_TO_PLAY;
	}

	public static final int BIRD_Y = t(12), GROUND_Y = t(24);

	public final PacManGameController controller;
	public final PacManGameAnimations animations;
	public final SoundManager sounds;
	public final TickTimer timer = new TickTimer();

	public Flap flap;
	public Pac pacMan;
	public Pac msPacMan;
	public Stork stork;
	public JuniorBag bag;

	public Phase phase;

	public MsPacMan_IntermissionScene3_Controller(PacManGameController controller, PacManGameAnimations animations,
			SoundManager sounds) {
		this.controller = controller;
		this.animations = animations;
		this.sounds = sounds;
	}

	public void enter(Phase newPhase, long ticks) {
		phase = newPhase;
		timer.reset(ticks);
		timer.start();
	}

	public void start() {
		flap = new Flap(3, "JUNIOR", animations.flapFlapping());
		flap.setTilePosition(3, 10);
		flap.visible = true;

		pacMan = new Pac("Pac-Man", Direction.RIGHT);
		pacMan.setPosition(t(3), GROUND_Y - 4);

		msPacMan = new Pac("Ms. Pac-Man", Direction.RIGHT);
		msPacMan.setPosition(t(5), GROUND_Y - 4);

		stork = new Stork();
		stork.setPosition(t(30), BIRD_Y);
		stork.flying = animations.storkFlying();

		bag = new JuniorBag();
		bag.setPositionRelativeTo(stork, -14, 3);

		enter(Phase.FLAP, Long.MAX_VALUE);
	}

	public void update() {
		switch (phase) {
		case FLAP:
			if (timer.ticked() == clock.sec(1)) {
				flap.flapping.restart();
			}
			if (timer.ticked() == clock.sec(2)) {
				flap.visible = false;
				sounds.play(PacManGameSound.INTERMISSION_3);
				enter(Phase.ACTION, Long.MAX_VALUE);
			}
			flap.flapping.animate();
			timer.tick();
			break;

		case ACTION:
			stork.move();
			bag.move();
			if (timer.ticked() == 1) {
				pacMan.visible = msPacMan.visible = stork.visible = bag.visible = true;
				stork.velocity = bag.velocity = new V2d(-1.25f, 0);
				stork.flying.restart();
			}
			// release bag?
			if (!bag.released && stork.position.x <= t(24)) {
				bag.released = true;
			}
			// closed bag reaches ground?
			if (!bag.open && bag.position.y > GROUND_Y) {
				++bag.bounces;
				if (bag.bounces < 5) {
					bag.velocity = new V2d(-0.2f, -1f / bag.bounces);
					bag.setPosition(bag.position.x, GROUND_Y);
				} else {
					bag.open = true;
					bag.velocity = V2d.NULL;
					enter(Phase.READY_TO_PLAY, clock.sec(3));
				}
			}
			timer.tick();
			break;

		case READY_TO_PLAY:
			stork.move();
			if (timer.hasExpired()) {
				controller.letCurrentGameStateExpire();
				return;
			}
			timer.tick();
			break;

		default:
			break;
		}
	}
}