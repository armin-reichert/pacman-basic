package de.amr.games.pacman.ui.mspacman;

import static de.amr.games.pacman.lib.Direction.LEFT;
import static de.amr.games.pacman.lib.Direction.UP;
import static de.amr.games.pacman.lib.God.clock;
import static de.amr.games.pacman.model.world.PacManGameWorld.t;

import de.amr.games.pacman.controller.PacManGameController;
import de.amr.games.pacman.lib.Animation;
import de.amr.games.pacman.lib.CountdownTimer;
import de.amr.games.pacman.lib.V2i;
import de.amr.games.pacman.model.common.Ghost;
import de.amr.games.pacman.model.common.GhostState;
import de.amr.games.pacman.model.common.Pac;
import de.amr.games.pacman.ui.animation.PacManGameAnimations;
import de.amr.games.pacman.ui.sound.SoundManager;

public class MsPacMan_IntroScene_Controller {

	public enum Phase {

		BEGIN, GHOSTS, MSPACMAN, END;
	}

	public final V2i frameTopLeftTile = new V2i(6, 8);
	public final int belowFrame = t(17);
	public final int leftOfFrame = t(4);

	public final PacManGameController controller;
	public final PacManGameAnimations animations;
	public final SoundManager sounds;
	public final CountdownTimer timer = new CountdownTimer();
	public final Animation<Boolean> blinking = Animation.pulse().frameDuration(30);
	public Pac msPac;
	public Ghost[] ghosts;
	public Ghost currentGhost;
	public boolean presentingMsPac;
	public Phase phase;

	public MsPacMan_IntroScene_Controller(PacManGameController controller, PacManGameAnimations animations,
			SoundManager sounds) {
		this.controller = controller;
		this.animations = animations;
		this.sounds = sounds;
	}

	public void enterPhase(Phase newPhase) {
		phase = newPhase;
		timer.setDuration(Long.MAX_VALUE);
	}

	public void start() {
		msPac = new Pac("Ms. Pac-Man", LEFT);
		msPac.setPosition(t(37), belowFrame);
		msPac.visible = true;
		msPac.speed = 0;
		msPac.dead = false;
		msPac.dir = LEFT;

		ghosts = new Ghost[] { //
				new Ghost(0, "Blinky", LEFT), //
				new Ghost(1, "Pinky", LEFT), //
				new Ghost(2, "Inky", LEFT), //
				new Ghost(3, "Sue", LEFT),//
		};

		for (Ghost ghost : ghosts) {
			ghost.setPosition(t(37), belowFrame);
			ghost.visible = true;
			ghost.bounty = 0;
			ghost.speed = 0;
			ghost.state = GhostState.HUNTING_PAC;
		}

		currentGhost = null;
		presentingMsPac = false;

		enterPhase(Phase.BEGIN);
	}

	public void update() {
		for (Ghost ghost : ghosts) {
			ghost.move();
		}
		msPac.move();
		switch (phase) {
		case BEGIN:
			if (timer.running() == clock.sec(1)) {
				currentGhost = ghosts[0];
				enterPhase(Phase.GHOSTS);
			}
			break;
		case GHOSTS:
			boolean ghostComplete = letCurrentGhostWalkToEndPosition();
			if (ghostComplete) {
				if (currentGhost == ghosts[3]) {
					currentGhost = null;
					presentingMsPac = true;
					enterPhase(Phase.MSPACMAN);
				} else {
					currentGhost = ghosts[currentGhost.id + 1];
					enterPhase(Phase.GHOSTS);
				}
			}
			break;
		case MSPACMAN:
			boolean msPacComplete = letMsPacManWalkToEndPosition();
			if (msPacComplete) {
				enterPhase(Phase.END);
			}
			break;
		case END:
			if (timer.running() == 0) {
				blinking.restart();
			}
			if (timer.running() == clock.sec(5)) {
				controller.getGame().attractMode = true;
			}
			blinking.animate();
			break;
		default:
			break;
		}
		timer.run();
	}

	public boolean letCurrentGhostWalkToEndPosition() {
		if (currentGhost == null) {
			return false;
		}
		if (timer.running() == 0) {
			currentGhost.speed = 1;
			animations.ghostAnimations().ghostKicking(currentGhost).forEach(Animation::restart);
		}
		if (currentGhost.dir == LEFT && currentGhost.position.x <= leftOfFrame) {
			currentGhost.dir = currentGhost.wishDir = UP;
		}
		if (currentGhost.dir == UP && currentGhost.position.y <= t(frameTopLeftTile.y) + currentGhost.id * 18) {
			currentGhost.speed = 0;
			animations.ghostAnimations().ghostKicking(currentGhost).forEach(Animation::reset);
			return true;
		}
		return false;
	}

	public boolean letMsPacManWalkToEndPosition() {
		if (timer.running() == 0) {
			msPac.visible = true;
			msPac.couldMove = true;
			msPac.speed = 1;
			msPac.dir = LEFT;
			animations.playerAnimations().playerMunching(msPac).forEach(Animation::restart);
		}
		if (msPac.speed != 0 && msPac.position.x <= t(13)) {
			msPac.speed = 0;
			animations.playerAnimations().playerMunching(msPac).forEach(Animation::reset);
			return true;
		}
		return false;
	}
}