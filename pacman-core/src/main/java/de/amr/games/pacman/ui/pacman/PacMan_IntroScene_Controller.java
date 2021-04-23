package de.amr.games.pacman.ui.pacman;

import static de.amr.games.pacman.model.world.PacManGameWorld.t;

import de.amr.games.pacman.controller.PacManGameController;
import de.amr.games.pacman.lib.Direction;
import de.amr.games.pacman.lib.TickTimer;
import de.amr.games.pacman.lib.TimedSequence;
import de.amr.games.pacman.model.common.Ghost;
import de.amr.games.pacman.model.common.GhostState;
import de.amr.games.pacman.model.common.Pac;

/**
 * Intro scene of the PacMan game.
 * <p>
 * The ghost are presented one after another, then Pac-Man is chased by the
 * ghosts, turns the card and hunts the ghost himself.
 * 
 * @author Armin Reichert
 */
public class PacMan_IntroScene_Controller {

	public static class GhostPortrait {

		public Ghost ghost;
		public String character;
		public boolean characterVisible;
		public boolean nicknameVisible;
	}

	public enum Phase {

		BEGIN, PRESENTING_GHOST, CHASING_PAC, CHASING_GHOSTS, READY_TO_PLAY;
	}

	public static final int TOP_Y = t(6);

	public final TickTimer timer = new TickTimer();
	public final PacManGameController gameController;

	public final TimedSequence<Boolean> blinking = TimedSequence.pulse().frameDuration(20);
	public GhostPortrait[] gallery;
	public int selectedGhost;
	public long ghostKilledTime;
	public Pac pac;
	public Ghost[] ghosts;

	public Phase phase;

	public PacMan_IntroScene_Controller(PacManGameController gameController) {
		this.gameController = gameController;
	}

	private void enterPhase(Phase newPhase) {
		phase = newPhase;
		timer.reset();
		timer.start();
	}

	public void init() {
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
			if (timer.isRunningSeconds(2)) {
				selectGhost(0);
				enterPhase(Phase.PRESENTING_GHOST);
			}
			timer.tick();
			break;

		case PRESENTING_GHOST:
			if (timer.isRunningSeconds(0.5)) {
				gallery[selectedGhost].characterVisible = true;
			}
			if (timer.isRunningSeconds(1)) {
				gallery[selectedGhost].nicknameVisible = true;
			}
			if (timer.isRunningSeconds(2)) {
				if (selectedGhost < 3) {
					selectGhost(selectedGhost + 1);
					enterPhase(Phase.PRESENTING_GHOST);
				} else {
					startGhostsChasingPac();
					enterPhase(Phase.CHASING_PAC);
				}
			}
			timer.tick();
			break;

		case CHASING_PAC:
			if (pac.position.x < t(2)) {
				startPacChasingGhosts();
				enterPhase(Phase.CHASING_GHOSTS);
			}
			timer.tick();
			break;

		case CHASING_GHOSTS:
			if (pac.position.x > t(28)) {
				enterPhase(Phase.READY_TO_PLAY);
			}
			if (gameController.stateTimer().ticked() - ghostKilledTime == 15) {
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
					ghostKilledTime = gameController.stateTimer().ticked();
				}
			}
			timer.tick();
			break;

		case READY_TO_PLAY:
			if (timer.hasJustStarted()) {
				blinking.restart();
			}
			if (timer.isRunningSeconds(5)) {
				gameController.stateTimer().forceExpiration();
			}
			blinking.animate();
			timer.tick();
			break;

		default:
			break;
		}
	}

	public void startGhostsChasingPac() {
		pac.setPosition(t(28), t(22));
		pac.visible = true;
		pac.speed = 1;
		pac.setDir(Direction.LEFT);
		pac.stuck = false;

		for (Ghost ghost : ghosts) {
			ghost.setPositionRelativeTo(pac, 8 + (ghost.id + 1) * 18, 0);
			ghost.visible = true;
			ghost.setWishDir(Direction.LEFT);
			ghost.setDir(Direction.LEFT);
			ghost.speed = pac.speed * 1.05f;
			ghost.state = GhostState.HUNTING_PAC;
		}

		blinking.restart();
	}

	public void startPacChasingGhosts() {
		pac.setDir(Direction.RIGHT);
		for (Ghost ghost : ghosts) {
			ghost.state = GhostState.FRIGHTENED;
			ghost.setWishDir(Direction.RIGHT);
			ghost.setDir(Direction.RIGHT);
			ghost.speed = 0.5f;
		}
	}

	public void selectGhost(int ghostIndex) {
		selectedGhost = ghostIndex;
		gallery[selectedGhost].ghost.visible = true;
	}
}