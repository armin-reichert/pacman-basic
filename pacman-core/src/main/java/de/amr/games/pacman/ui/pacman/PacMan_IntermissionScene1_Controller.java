package de.amr.games.pacman.ui.pacman;

import static de.amr.games.pacman.lib.Direction.RIGHT;
import static de.amr.games.pacman.lib.God.clock;
import static de.amr.games.pacman.model.common.GhostState.FRIGHTENED;
import static de.amr.games.pacman.model.common.GhostState.HUNTING_PAC;
import static de.amr.games.pacman.model.world.PacManGameWorld.t;

import de.amr.games.pacman.controller.PacManGameController;
import de.amr.games.pacman.lib.Direction;
import de.amr.games.pacman.lib.TickTimer;
import de.amr.games.pacman.model.common.Ghost;
import de.amr.games.pacman.model.common.Pac;
import de.amr.games.pacman.ui.animation.Animation;
import de.amr.games.pacman.ui.animation.PacManGameAnimations;
import de.amr.games.pacman.ui.sound.PacManGameSound;
import de.amr.games.pacman.ui.sound.SoundManager;

public class PacMan_IntermissionScene1_Controller {

	public enum Phase {

		BLINKY_CHASING_PACMAN, BIGPACMAN_CHASING_BLINKY;

	}

	public static final int groundY = t(20);

	public final TickTimer timer = new TickTimer();
	public final PacManGameController controller;
	public final PacManGameAnimations animations;
	public final SoundManager sounds;
	public Ghost blinky;
	public Pac pac;
	public Phase phase;

	public PacMan_IntermissionScene1_Controller(PacManGameController controller, PacManGameAnimations animations,
			SoundManager sounds) {
		this.controller = controller;
		this.animations = animations;
		this.sounds = sounds;
	}

	public void start() {
		pac = new Pac("Pac-Man", Direction.LEFT);
		pac.visible = true;
		pac.setPosition(t(30), groundY);
		pac.speed = 1.0f;
		animations.playerAnimations().playerMunching(pac).forEach(Animation::restart);

		blinky = new Ghost(0, "Blinky", Direction.LEFT);
		blinky.visible = true;
		blinky.state = HUNTING_PAC;
		blinky.setPositionRelativeTo(pac, t(3), 0);
		blinky.speed = pac.speed * 1.04f;
		animations.ghostAnimations().ghostKicking(blinky, blinky.dir).restart();
		animations.ghostAnimations().ghostFrightened(blinky, blinky.dir).restart();

		sounds.loop(PacManGameSound.INTERMISSION_1, 2);

		phase = Phase.BLINKY_CHASING_PACMAN;
		timer.reset(clock.sec(5));
		timer.start();
	}

	public void update() {
		switch (phase) {
		case BLINKY_CHASING_PACMAN:
			if (timer.hasExpired()) {
				phase = Phase.BIGPACMAN_CHASING_BLINKY;
				timer.reset(clock.sec(7));
				timer.start();
			}
			timer.tick();
			break;
		case BIGPACMAN_CHASING_BLINKY:
			if (timer.ticked() == 1) {
				blinky.setPosition(-t(2), groundY);
				blinky.dir = blinky.wishDir = RIGHT;
				blinky.speed = 1f;
				blinky.state = FRIGHTENED;
				pac.dir = RIGHT;
				pac.speed = 1.3f;
				pac.setPositionRelativeTo(blinky, -t(13), 0);
			}
			if (timer.hasExpired()) {
				controller.getGame().state.timer.forceExpiration();
				return;
			}
			timer.tick();
			break;
		default:
			break;
		}
		pac.move();
		blinky.move();
	}
}