package de.amr.games.pacman.ui.swing.mspacman;

import static de.amr.games.pacman.heaven.God.clock;
import static de.amr.games.pacman.ui.swing.mspacman.MsPacManGameRendering.assets;
import static de.amr.games.pacman.ui.swing.mspacman.MsPacManGameScenes.rendering;
import static de.amr.games.pacman.ui.swing.mspacman.MsPacManGameScenes.soundManager;
import static de.amr.games.pacman.world.PacManGameWorld.t;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;

import de.amr.games.pacman.lib.Animation;
import de.amr.games.pacman.lib.CountdownTimer;
import de.amr.games.pacman.lib.Direction;
import de.amr.games.pacman.lib.V2f;
import de.amr.games.pacman.model.Pac;
import de.amr.games.pacman.model.PacManGameModel;
import de.amr.games.pacman.sound.PacManGameSound;
import de.amr.games.pacman.ui.swing.GameScene;

/**
 * Intermission scene 2: "The chase".
 * <p>
 * Pac-Man and Ms. Pac-Man chase each other across the screen over and over. After three turns, they
 * both rapidly run from left to right and right to left. (Played after round 5)
 * 
 * @author Armin Reichert
 */
public class IntermissionScene2 implements GameScene {

	enum Phase {

		ANIMATION;

		final CountdownTimer timer = new CountdownTimer();
	}

	private final Dimension size;
	private final PacManGameModel game;

	private Phase phase;
	private int upperY = t(12), lowerY = t(24), middleY = t(18);
	private Pac pac, msPac;
	private boolean flapVisible;

	private void enter(Phase newPhase, long ticks) {
		phase = newPhase;
		phase.timer.setDuration(ticks);
	}

	@Override
	public Dimension sizeInPixel() {
		return size;
	}

	public IntermissionScene2(Dimension size, PacManGameModel game) {
		this.size = size;
		this.game = game;
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
				soundManager.play(PacManGameSound.INTERMISSION_2);
				flapVisible = true;
				assets.flapAnim.restart();
			}
			if (phase.timer.running() == clock.sec(2)) {
				flapVisible = false;
			}
			if (phase.timer.running() == clock.sec(4.5)) {
				pac.visible = true;
				pac.position = new V2f(-t(2), upperY);
				msPac.visible = true;
				msPac.position = new V2f(-t(8), upperY);
				pac.dir = msPac.dir = Direction.RIGHT;
				pac.speed = msPac.speed = 2;
				assets.pacManMunching.values().forEach(Animation::restart);
				rendering.pacMunching(msPac).forEach(Animation::restart);
			}
			if (phase.timer.running() == clock.sec(9)) {
				msPac.position = new V2f(t(30), lowerY);
				msPac.visible = true;
				pac.position = new V2f(t(36), lowerY);
				msPac.dir = pac.dir = Direction.LEFT;
				msPac.speed = pac.speed = 2;
			}
			if (phase.timer.running() == clock.sec(13.5)) {
				msPac.position = new V2f(t(-8), middleY);
				pac.position = new V2f(t(-2), middleY);
				msPac.dir = pac.dir = Direction.RIGHT;
				msPac.speed = pac.speed = 2;
			}
			if (phase.timer.running() == clock.sec(18)) {
				msPac.position = new V2f(t(30), upperY);
				pac.position = new V2f(t(42), upperY);
				msPac.dir = pac.dir = Direction.LEFT;
				msPac.speed = pac.speed = 4;
			}
			if (phase.timer.running() == clock.sec(19)) {
				msPac.position = new V2f(t(-14), lowerY);
				pac.position = new V2f(t(-2), lowerY);
				msPac.dir = pac.dir = Direction.RIGHT;
				msPac.speed = pac.speed = 4;
			}
			if (phase.timer.running() == clock.sec(24)) {
				game.state.duration(0);
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
		drawFlapAnimation(g, t(3), t(10));
		drawPacMan(g);
		rendering.drawMsPacMan(g, msPac, game);
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

	private void drawFlapAnimation(Graphics2D g, int flapX, int flapY) {
		if (flapVisible) {
			rendering.drawImage(g, assets.flapAnim.animate(), flapX, flapY, true);
			g.setColor(new Color(222, 222, 225));
			g.setFont(assets.getScoreFont());
			g.drawString("2", flapX + 20, flapY + 30);
			if (assets.flapAnim.isRunning()) {
				g.drawString("THE CHASE", flapX + 40, flapY + 20);
			}
		}
	}
}