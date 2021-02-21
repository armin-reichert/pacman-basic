package de.amr.games.pacman.ui.swing.mspacman;

import static de.amr.games.pacman.heaven.God.clock;
import static de.amr.games.pacman.ui.swing.PacManGameSwingUI.msPacManGameRendering;
import static de.amr.games.pacman.ui.swing.PacManGameSwingUI.msPacManGameSounds;
import static de.amr.games.pacman.ui.swing.mspacman.MsPacMan_GameRendering.assets;
import static de.amr.games.pacman.world.PacManGameWorld.t;

import java.awt.Dimension;
import java.awt.Graphics2D;

import de.amr.games.pacman.lib.Animation;
import de.amr.games.pacman.lib.CountdownTimer;
import de.amr.games.pacman.lib.Direction;
import de.amr.games.pacman.model.guys.Pac;
import de.amr.games.pacman.sound.PacManGameSound;
import de.amr.games.pacman.ui.swing.scene.AbstractGameScene;

/**
 * Intermission scene 2: "The chase".
 * <p>
 * Pac-Man and Ms. Pac-Man chase each other across the screen over and over. After three turns, they
 * both rapidly run from left to right and right to left. (Played after round 5)
 * 
 * @author Armin Reichert
 */
public class MsPacMan_IntermissionScene2 extends AbstractGameScene {

	enum Phase {

		FLAP, ACTION;

		final CountdownTimer timer = new CountdownTimer();
	}

	private static final int UPPER_Y = t(12), LOWER_Y = t(24), MIDDLE_Y = t(18);

	private Phase phase;

	private Flap flap;
	private Pac pacMan, msPacMan;

	private void enter(Phase newPhase, long ticks) {
		phase = newPhase;
		phase.timer.setDuration(ticks);
	}

	public MsPacMan_IntermissionScene2(Dimension size) {
		super(size);
	}

	@Override
	public void start() {

		flap = new Flap();
		flap.setPosition(t(3), t(10));
		flap.visible = true;
		flap.sceneNumber = 2;
		flap.sceneTitle = "THE CHASE";
		flap.animation.restart();

		pacMan = new Pac("Pac-Man", Direction.RIGHT);
		msPacMan = new Pac("Ms. Pac-Man", Direction.RIGHT);

		msPacManGameSounds.play(PacManGameSound.INTERMISSION_2);
		enter(Phase.FLAP, Long.MAX_VALUE);
	}

	@Override
	public void update() {
		switch (phase) {
		case FLAP:
			if (phase.timer.running() == clock.sec(2)) {
				flap.visible = false;
			}
			if (phase.timer.running() == clock.sec(3)) {
				enter(Phase.ACTION, Long.MAX_VALUE);
			}
			break;

		case ACTION:
			if (phase.timer.running() == clock.sec(1.5)) {
				pacMan.visible = true;
				pacMan.setPosition(-t(2), UPPER_Y);
				msPacMan.visible = true;
				msPacMan.setPosition(-t(8), UPPER_Y);
				pacMan.dir = msPacMan.dir = Direction.RIGHT;
				pacMan.speed = msPacMan.speed = 2;
				assets.pacManMunching.values().forEach(Animation::restart);
				msPacManGameRendering.pacMunching(msPacMan).forEach(Animation::restart);
			}
			if (phase.timer.running() == clock.sec(6)) {
				msPacMan.setPosition(t(30), LOWER_Y);
				msPacMan.visible = true;
				pacMan.setPosition(t(36), LOWER_Y);
				msPacMan.dir = pacMan.dir = Direction.LEFT;
				msPacMan.speed = pacMan.speed = 2;
			}
			if (phase.timer.running() == clock.sec(10.5)) {
				msPacMan.setPosition(t(-8), MIDDLE_Y);
				pacMan.setPosition(t(-2), MIDDLE_Y);
				msPacMan.dir = pacMan.dir = Direction.RIGHT;
				msPacMan.speed = pacMan.speed = 2;
			}
			if (phase.timer.running() == clock.sec(15)) {
				msPacMan.setPosition(t(30), UPPER_Y);
				pacMan.setPosition(t(42), UPPER_Y);
				msPacMan.dir = pacMan.dir = Direction.LEFT;
				msPacMan.speed = pacMan.speed = 4;
			}
			if (phase.timer.running() == clock.sec(16)) {
				msPacMan.setPosition(t(-14), LOWER_Y);
				pacMan.setPosition(t(-2), LOWER_Y);
				msPacMan.dir = pacMan.dir = Direction.RIGHT;
				msPacMan.speed = pacMan.speed = 4;
			}
			if (phase.timer.running() == clock.sec(22)) {
				game.state.timer.setDuration(0);
			}
			break;
		default:
			break;
		}
		pacMan.move();
		msPacMan.move();
		phase.timer.run();
	}

	@Override
	public void render(Graphics2D g) {
		flap.draw(g);
		msPacManGameRendering.drawMrPacMan(g, pacMan);
		msPacManGameRendering.drawPac(g, msPacMan, game);
	}
}