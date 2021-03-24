package de.amr.games.pacman.ui.pacman;

import static de.amr.games.pacman.lib.Direction.LEFT;
import static de.amr.games.pacman.lib.Direction.RIGHT;
import static de.amr.games.pacman.model.world.PacManGameWorld.t;

import de.amr.games.pacman.controller.PacManGameController;
import de.amr.games.pacman.lib.Direction;
import de.amr.games.pacman.lib.TickTimer;
import de.amr.games.pacman.model.common.Ghost;
import de.amr.games.pacman.model.common.GhostState;
import de.amr.games.pacman.model.common.Pac;
import de.amr.games.pacman.ui.animation.PacManGameAnimations2D;
import de.amr.games.pacman.ui.animation.TimedSequence;
import de.amr.games.pacman.ui.sound.PacManGameSound;
import de.amr.games.pacman.ui.sound.SoundManager;

/**
 * Third intermission scene: Blinky in shred dress chases Pac-Man, comes back half-naked drawing
 * dress over the floor.
 * 
 * @author Armin Reichert
 */
public class PacMan_IntermissionScene3_Controller {

	public enum Phase {
		CHASING_PACMAN, RETURNING_HALF_NAKED;
	}

	public static final int chaseTileY = 20;

	public final TickTimer timer = new TickTimer();
	public final PacManGameController controller;
	public final PacManGameAnimations2D animations;
	public final SoundManager sounds;

	public Ghost blinky;
	public Pac pac;
	public Phase phase;

	public PacMan_IntermissionScene3_Controller(PacManGameController controller, PacManGameAnimations2D animations,
			SoundManager sounds) {
		this.controller = controller;
		this.animations = animations;
		this.sounds = sounds;
	}

	public void start() {
		pac = new Pac("Pac-Man", Direction.LEFT);
		pac.setTilePosition(30, chaseTileY);
		pac.visible = true;
		pac.dead = false;
		pac.speed = 1.2f;
		pac.stuck = false;
		pac.dir = LEFT;
		animations.playerAnimations().playerMunching(pac).forEach(TimedSequence::restart);

		blinky = new Ghost(0, "Blinky", Direction.LEFT);
		blinky.setPositionRelativeTo(pac, t(8), 0);
		blinky.visible = true;
		blinky.state = GhostState.HUNTING_PAC;
		blinky.speed = pac.speed;
		blinky.dir = blinky.wishDir = LEFT;

		sounds.loop(PacManGameSound.INTERMISSION_3, 2);

		phase = Phase.CHASING_PACMAN;
	}

	public void update() {
		switch (phase) {
		case CHASING_PACMAN:
			if (blinky.position.x <= -50) {
				pac.speed = 0;
				blinky.dir = blinky.wishDir = RIGHT;
				phase = Phase.RETURNING_HALF_NAKED;
			}
			break;
		case RETURNING_HALF_NAKED:
			if (blinky.position.x > t(28) + 200) {
				controller.stateTimer().forceExpiration();
				return;
			}
			break;
		default:
			throw new IllegalStateException("Illegal phase: " + phase);
		}
		blinky.move();
		pac.move();
	}
}