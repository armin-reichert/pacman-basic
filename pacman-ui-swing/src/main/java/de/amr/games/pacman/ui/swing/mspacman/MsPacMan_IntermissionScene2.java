package de.amr.games.pacman.ui.swing.mspacman;

import static de.amr.games.pacman.heaven.God.clock;
import static de.amr.games.pacman.ui.swing.mspacman.MsPacMan_GameRendering.assets;
import static de.amr.games.pacman.world.PacManGameWorld.t;

import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;

import de.amr.games.pacman.lib.Animation;
import de.amr.games.pacman.lib.CountdownTimer;
import de.amr.games.pacman.lib.Direction;
import de.amr.games.pacman.model.guys.Pac;
import de.amr.games.pacman.sound.PacManGameSound;
import de.amr.games.pacman.sound.SoundManager;
import de.amr.games.pacman.ui.swing.PacManGameSwingUI;
import de.amr.games.pacman.ui.swing.common.AbstractGameScene;

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

		ANIMATION;

		final CountdownTimer timer = new CountdownTimer();
	}

	private final MsPacMan_GameRendering rendering = PacManGameSwingUI.msPacManGameRendering;
	private final SoundManager sounds = PacManGameSwingUI.msPacManGameSounds;

	private Phase phase;
	private int upperY = t(12), lowerY = t(24), middleY = t(18);
	private Pac pac, msPac;
	private boolean flapVisible;

	private void enter(Phase newPhase, long ticks) {
		phase = newPhase;
		phase.timer.setDuration(ticks);
	}

	public MsPacMan_IntermissionScene2(Dimension size) {
		super(size);
	}

	@Override
	public void start() {
		pac = new Pac("Pac-Man", Direction.RIGHT);
		msPac = new Pac("Ms. Pac-Man", Direction.RIGHT);
		enter(Phase.ANIMATION, Long.MAX_VALUE);
	}

	@Override
	public void update() {
		switch (phase) {
		case ANIMATION:
			if (phase.timer.running() == 0) {
				sounds.play(PacManGameSound.INTERMISSION_2);
				flapVisible = true;
				assets.flapAnim.restart();
			}
			if (phase.timer.running() == clock.sec(2)) {
				flapVisible = false;
			}
			if (phase.timer.running() == clock.sec(4.5)) {
				pac.visible = true;
				pac.setPosition(-t(2), upperY);
				msPac.visible = true;
				msPac.setPosition(-t(8), upperY);
				pac.dir = msPac.dir = Direction.RIGHT;
				pac.speed = msPac.speed = 2;
				assets.pacManMunching.values().forEach(Animation::restart);
				rendering.pacMunching(msPac).forEach(Animation::restart);
			}
			if (phase.timer.running() == clock.sec(9)) {
				msPac.setPosition(t(30), lowerY);
				msPac.visible = true;
				pac.setPosition(t(36), lowerY);
				msPac.dir = pac.dir = Direction.LEFT;
				msPac.speed = pac.speed = 2;
			}
			if (phase.timer.running() == clock.sec(13.5)) {
				msPac.setPosition(t(-8), middleY);
				pac.setPosition(t(-2), middleY);
				msPac.dir = pac.dir = Direction.RIGHT;
				msPac.speed = pac.speed = 2;
			}
			if (phase.timer.running() == clock.sec(18)) {
				msPac.setPosition(t(30), upperY);
				pac.setPosition(t(42), upperY);
				msPac.dir = pac.dir = Direction.LEFT;
				msPac.speed = pac.speed = 4;
			}
			if (phase.timer.running() == clock.sec(19)) {
				msPac.setPosition(t(-14), lowerY);
				pac.setPosition(t(-2), lowerY);
				msPac.dir = pac.dir = Direction.RIGHT;
				msPac.speed = pac.speed = 4;
			}
			if (phase.timer.running() == clock.sec(24)) {
				game.state.timer.setDuration(0);
			}
			break;
		default:
			break;
		}
		pac.move();
		msPac.move();
		phase.timer.run();
	}

	@Override
	public void render(Graphics2D g) {
		if (flapVisible) {
			rendering.drawFlapAnimation(g, t(3), t(10), "2", "THE CHASE");
		}
		drawPacMan(g);
		rendering.drawPac(g, msPac, game);
	}

	private void drawPacMan(Graphics2D g) {
		if (pac.visible) {
			Animation<BufferedImage> munching = assets.pacManMunching.get(pac.dir);
			if (pac.speed > 0) {
				rendering.drawImage(g, munching.animate(), pac.position.x - 4, pac.position.y - 4, true);
			} else {
				rendering.drawImage(g, munching.frame(1), pac.position.x - 4, pac.position.y - 4, true);
			}
		}
	}
}