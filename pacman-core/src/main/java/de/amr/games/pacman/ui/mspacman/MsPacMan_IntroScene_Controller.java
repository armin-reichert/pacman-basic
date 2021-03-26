package de.amr.games.pacman.ui.mspacman;

import static de.amr.games.pacman.lib.Direction.LEFT;
import static de.amr.games.pacman.lib.Direction.UP;
import static de.amr.games.pacman.model.world.PacManGameWorld.t;

import de.amr.games.pacman.controller.PacManGameController;
import de.amr.games.pacman.lib.TickTimer;
import de.amr.games.pacman.lib.V2i;
import de.amr.games.pacman.model.common.Ghost;
import de.amr.games.pacman.model.common.GhostState;
import de.amr.games.pacman.model.common.Pac;
import de.amr.games.pacman.ui.animation.PacManGameAnimations2D;
import de.amr.games.pacman.ui.animation.TimedSequence;

/**
 * Intro scene of the Ms. Pac-Man game. The ghosts and Ms. Pac-Man are introduced one after another.
 * 
 * @author Armin Reichert
 */
public class MsPacMan_IntroScene_Controller {

	public enum Phase {

		BEGIN, PRESENTING_GHOST, PRESENTING_MSPACMAN, END;
	}

	// the board where the actors are presented
	public final V2i tileBoardTopLeft = new V2i(6, 8);
	public final int tileBelowBoard = 17;
	public final int tileLeftOfBoard = 4;

	public final PacManGameController gameController;
	public final PacManGameAnimations2D animations;

	public Phase phase;
	public final TickTimer phaseTimer = new TickTimer();

	public Pac msPacMan;
	public Ghost[] ghosts;
	public int currentGhostIndex;
	public final TimedSequence<Boolean> blinking = TimedSequence.pulse().frameDuration(30);

	public MsPacMan_IntroScene_Controller(PacManGameController gameController, PacManGameAnimations2D animations) {
		this.gameController = gameController;
		this.animations = animations;
	}

	private void enterPhase(Phase newPhase) {
		phase = newPhase;
		phaseTimer.reset();
		phaseTimer.start();
	}

	public void start() {
		msPacMan = new Pac("Ms. Pac-Man", LEFT);
		msPacMan.setPosition(t(37), t(tileBelowBoard));

		ghosts = new Ghost[] { //
				new Ghost(0, "Blinky", LEFT), //
				new Ghost(1, "Pinky", LEFT), //
				new Ghost(2, "Inky", LEFT), //
				new Ghost(3, "Sue", LEFT),//
		};

		for (Ghost ghost : ghosts) {
			ghost.setPosition(t(37), t(tileBelowBoard));
			ghost.state = GhostState.HUNTING_PAC;
		}

		currentGhostIndex = -1;

		enterPhase(Phase.BEGIN);
	}

	public void update() {
		switch (phase) {

		case BEGIN:
			if (phaseTimer.isRunningSeconds(2)) {
				currentGhostIndex = 0;
				enterPhase(Phase.PRESENTING_GHOST);
			}
			phaseTimer.tick();
			break;

		case PRESENTING_GHOST:
			if (phaseTimer.hasJustStarted()) {
				ghosts[currentGhostIndex].visible = true;
				ghosts[currentGhostIndex].speed = 1.0;
				animations.ghostAnimations().ghostKicking(ghosts[currentGhostIndex]).forEach(TimedSequence::restart);
			}
			boolean ghostReachedFinalPosition = ghostEnteringStage(ghosts[currentGhostIndex]);
			if (ghostReachedFinalPosition) {
				if (currentGhostIndex == 3) {
					enterPhase(Phase.PRESENTING_MSPACMAN);
				} else {
					currentGhostIndex++;
					enterPhase(Phase.PRESENTING_GHOST);
				}
			}
			for (Ghost ghost : ghosts) {
				ghost.move();
			}
			phaseTimer.tick();
			break;

		case PRESENTING_MSPACMAN:
			if (phaseTimer.hasJustStarted()) {
				msPacMan.visible = true;
				msPacMan.stuck = false;
				msPacMan.speed = 1;
				animations.playerAnimations().playerMunching(msPacMan).forEach(TimedSequence::restart);
			}
			boolean msPacReachedFinalPosition = msPacManEnteringStage();
			if (msPacReachedFinalPosition) {
				enterPhase(Phase.END);
			}
			msPacMan.move();
			phaseTimer.tick();
			break;

		case END:
			if (phaseTimer.hasJustStarted()) {
				blinking.restart();
			}
			if (phaseTimer.isRunningSeconds(5)) {
				gameController.stateTimer().forceExpiration();
				return;
			}
			blinking.animate();
			phaseTimer.tick();
			break;

		default:
			break;
		}
	}

	public boolean ghostEnteringStage(Ghost ghost) {
		if (ghost.dir == LEFT && ghost.position.x <= t(tileLeftOfBoard)) {
			ghost.dir = ghost.wishDir = UP;
			return false;
		}
		if (ghost.dir == UP && ghost.position.y <= t(tileBoardTopLeft.y) + ghost.id * 18) {
			ghost.speed = 0;
			animations.ghostAnimations().ghostKicking(ghost).forEach(TimedSequence::reset);
			return true;
		}
		return false;
	}

	public boolean msPacManEnteringStage() {
		if (msPacMan.speed != 0 && msPacMan.position.x <= t(13)) {
			msPacMan.speed = 0;
			animations.playerAnimations().playerMunching(msPacMan).forEach(TimedSequence::reset);
			return true;
		}
		return false;
	}
}