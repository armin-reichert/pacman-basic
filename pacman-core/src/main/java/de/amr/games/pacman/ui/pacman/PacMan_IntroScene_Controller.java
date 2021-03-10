package de.amr.games.pacman.ui.pacman;

import static de.amr.games.pacman.lib.God.clock;
import static de.amr.games.pacman.lib.Logging.log;
import static de.amr.games.pacman.world.PacManGameWorld.t;

import de.amr.games.pacman.controller.PacManGameController;
import de.amr.games.pacman.lib.Animation;
import de.amr.games.pacman.lib.CountdownTimer;
import de.amr.games.pacman.lib.Direction;
import de.amr.games.pacman.model.common.Ghost;
import de.amr.games.pacman.model.common.GhostState;
import de.amr.games.pacman.model.common.Pac;
import de.amr.games.pacman.ui.animation.PacManGameAnimations;

public class PacMan_IntroScene_Controller {

	public static class GhostPortrait {

		public Ghost ghost;
		public String character;
		public boolean characterVisible;
		public boolean nicknameVisible;
	}

	public enum Phase {

		BEGIN, PRESENTING, CHASING_PAC, CHASING_GHOSTS, READY_TO_PLAY;
	}

	public static final int TOP_Y = t(6);

	public final CountdownTimer timer = new CountdownTimer();
	public final PacManGameController controller;
	public final PacManGameAnimations animations;

	public final Animation<Boolean> blinking = Animation.pulse().frameDuration(20);
	public GhostPortrait[] gallery;
	public int presentedGhostIndex;
	public long ghostKilledTime;
	public Pac pac;
	public Ghost[] ghosts;

	public Phase phase;

	public PacMan_IntroScene_Controller(PacManGameController controller, PacManGameAnimations animations) {
		this.controller = controller;
		this.animations = animations;
	}

	public void enterPhase(Phase newPhase) {
		phase = newPhase;
		timer.setDuration(Long.MAX_VALUE);
		log("%s: Phase %s entered at clock tick %d", this, phase, clock.ticksTotal);
	}

	public void start() {
		gallery = new GhostPortrait[4];
		for (int i = 0; i < 4; ++i) {
			gallery[i] = new GhostPortrait();
		}
		gallery[0].ghost = new Ghost(0, "Blinky", Direction.RIGHT);
		gallery[0].character = "SHADOW";
		gallery[0].ghost.setPosition(t(2), TOP_Y + t(2));

		gallery[1].ghost = new Ghost(1, "Pinky", Direction.RIGHT);
		gallery[1].character = "SPEEDY";
		gallery[1].ghost.setPosition(t(2), TOP_Y + t(5));

		gallery[2].ghost = new Ghost(2, "Inky", Direction.RIGHT);
		gallery[2].character = "BASHFUL";
		gallery[2].ghost.setPosition(t(2), TOP_Y + t(8));

		gallery[3].ghost = new Ghost(3, "Clyde", Direction.RIGHT);
		gallery[3].character = "POKEY";
		gallery[3].ghost.setPosition(t(2), TOP_Y + t(11));

		for (int i = 0; i < 4; ++i) {
			animations.ghostAnimations().ghostKicking(gallery[i].ghost).forEach(Animation::reset);
		}

		pac = new Pac("Ms. Pac-Man", Direction.LEFT);

		ghosts = new Ghost[] { //
				new Ghost(0, "Blinky", Direction.LEFT), //
				new Ghost(1, "Pinky", Direction.LEFT), //
				new Ghost(2, "Inky", Direction.LEFT), //
				new Ghost(3, "Clyde", Direction.LEFT), //
		};

		enterPhase(Phase.BEGIN);
	}

	public void update() {
		pac.move();
		for (Ghost ghost : ghosts) {
			ghost.move();
		}
		switch (phase) {
		case BEGIN:
			if (timer.running() == clock.sec(2)) {
				presentedGhostIndex = -1;
				enterPhase(Phase.PRESENTING);
			}
			break;
		case PRESENTING:
			if (timer.running() == 0) {
				presentGhost(presentedGhostIndex + 1);
			}
			if (timer.running() == clock.sec(0.5)) {
				gallery[presentedGhostIndex].characterVisible = true;
			}
			if (timer.running() == clock.sec(1)) {
				gallery[presentedGhostIndex].nicknameVisible = true;
			}
			if (timer.running() == clock.sec(2)) {
				if (presentedGhostIndex < 3) {
					enterPhase(Phase.PRESENTING);
				} else {
					startGhostsChasingPac();
					enterPhase(Phase.CHASING_PAC);
				}
			}
			break;
		case CHASING_PAC:
			if (pac.position.x < t(2)) {
				startPacChasingGhosts();
				enterPhase(Phase.CHASING_GHOSTS);
			}
			break;
		case CHASING_GHOSTS:
			if (pac.position.x > t(28)) {
				enterPhase(Phase.READY_TO_PLAY);
			}
			if (clock.ticksTotal - ghostKilledTime == clock.sec(0.25)) {
				ghostKilledTime = 0;
				pac.visible = true;
				pac.speed = 1;
				for (Ghost ghost : ghosts) {
					if (ghost.state == GhostState.DEAD) {
						ghost.visible = false;
					}
				}
			}
			for (Ghost ghost : ghosts) {
				if (pac.meets(ghost) && ghost.state != GhostState.DEAD) {
					ghost.state = GhostState.DEAD;
					ghost.bounty = (int) Math.pow(2, ghost.id + 1) * 100;
					pac.visible = false;
					pac.speed = 0;
					ghostKilledTime = clock.ticksTotal;
				}
			}
			break;
		case READY_TO_PLAY:
			if (timer.running() == 0) {
				blinking.restart();
			}
			if (timer.running() == clock.sec(5)) {
				controller.getGame().attractMode = true;
				log("Entering attract mode at clock tick %d", clock.ticksTotal);
			}
			blinking.animate();
			break;
		default:
			break;
		}
		timer.run();
	}

	public void startGhostsChasingPac() {
		pac.setTilePosition(28, 22);
		pac.visible = true;
		pac.speed = 1;
		pac.dir = Direction.LEFT;
		pac.couldMove = true;
		animations.playerAnimations().playerMunching(pac).forEach(Animation::restart);

		for (Ghost ghost : ghosts) {
			ghost.setPositionRelativeTo(pac, 8 + (ghost.id + 1) * 18, 0);
			ghost.visible = true;
			ghost.dir = ghost.wishDir = Direction.LEFT;
			ghost.speed = pac.speed * 1.05f;
			ghost.state = GhostState.HUNTING_PAC;
			animations.ghostAnimations().ghostKicking(ghost).forEach(Animation::restart);
		}
	}

	public void startPacChasingGhosts() {
		pac.dir = Direction.RIGHT;
		for (Ghost ghost : ghosts) {
			ghost.state = GhostState.FRIGHTENED;
			ghost.dir = ghost.wishDir = Direction.RIGHT;
			ghost.speed = 0.5f;
			animations.ghostAnimations().ghostFrightened(ghost).forEach(Animation::restart);
		}
	}

	public void presentGhost(int ghostIndex) {
		presentedGhostIndex = ghostIndex;
		gallery[presentedGhostIndex].ghost.visible = true;
	}
}