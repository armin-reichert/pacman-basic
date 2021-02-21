package de.amr.games.pacman.ui.swing.mspacman;

import static de.amr.games.pacman.heaven.God.clock;
import static de.amr.games.pacman.lib.Direction.LEFT;
import static de.amr.games.pacman.lib.Direction.UP;
import static de.amr.games.pacman.lib.Logging.log;
import static de.amr.games.pacman.ui.swing.mspacman.MsPacMan_Rendering.assets;
import static de.amr.games.pacman.world.PacManGameWorld.t;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics2D;

import de.amr.games.pacman.lib.Animation;
import de.amr.games.pacman.lib.CountdownTimer;
import de.amr.games.pacman.lib.V2i;
import de.amr.games.pacman.model.guys.Ghost;
import de.amr.games.pacman.model.guys.GhostState;
import de.amr.games.pacman.model.guys.Pac;
import de.amr.games.pacman.ui.swing.PacManGameSwingUI;
import de.amr.games.pacman.ui.swing.scene.GameScene;

/**
 * Intro scene of the Ms. Pac-Man game. The ghosts and Ms. Pac-Man are introduced one after another.
 * 
 * @author Armin Reichert
 */
public class MsPacMan_IntroScene extends GameScene<MsPacMan_Rendering> {

	enum Phase {

		BEGIN, GHOSTS, MSPACMAN, END;

		private final CountdownTimer timer = new CountdownTimer();
	}

	private final V2i frameTopLeftTile = new V2i(6, 8);
	private final int belowFrame = t(17);
	private final int leftOfFrame = t(4);
	private final Animation<Boolean> blinking = Animation.pulse().frameDuration(30).restart();

	private Phase phase;

	private Pac msPac;
	private Ghost[] ghosts;

	private Ghost currentGhost;
	private boolean presentingMsPac;

	public MsPacMan_IntroScene(Dimension size) {
		super(size, PacManGameSwingUI.RENDERING_MSPACMAN);
	}

	private void enterPhase(Phase newPhase) {
		phase = newPhase;
		phase.timer.setDuration(Long.MAX_VALUE);
	}

	@Override
	public void start() {
		log("Intro scene started at clock time %d", clock.ticksTotal);

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

	@Override
	public void update() {
		for (Ghost ghost : ghosts) {
			ghost.move();
		}
		msPac.move();
		switch (phase) {
		case BEGIN:
			if (phase.timer.running() == clock.sec(1)) {
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
			if (phase.timer.running() == clock.sec(5)) {
				game.attractMode = true;
			}
			break;
		default:
			break;
		}
		phase.timer.run();
	}

	private boolean letCurrentGhostWalkToEndPosition() {
		if (currentGhost == null) {
			return false;
		}
		if (phase.timer.running() == 0) {
			currentGhost.speed = 1;
			rendering.ghostKicking(currentGhost).forEach(Animation::restart);
		}
		if (currentGhost.dir == LEFT && currentGhost.position.x <= leftOfFrame) {
			currentGhost.dir = currentGhost.wishDir = UP;
		}
		if (currentGhost.dir == UP && currentGhost.position.y <= t(frameTopLeftTile.y) + currentGhost.id * 18) {
			currentGhost.speed = 0;
			rendering.ghostKicking(currentGhost).forEach(Animation::reset);
			return true;
		}
		return false;
	}

	private boolean letMsPacManWalkToEndPosition() {
		if (phase.timer.running() == 0) {
			msPac.visible = true;
			msPac.couldMove = true;
			msPac.speed = 1;
			msPac.dir = LEFT;
			rendering.pacMunching(msPac).forEach(Animation::restart);
		}
		if (msPac.speed != 0 && msPac.position.x <= t(13)) {
			msPac.speed = 0;
			rendering.pacMunching(msPac).forEach(Animation::reset);
			return true;
		}
		return false;
	}

	@Override
	public void render(Graphics2D g) {
		g.setFont(assets.getScoreFont());
		g.setColor(Color.ORANGE);
		g.drawString("\"MS PAC-MAN\"", t(8), t(5));
		drawAnimatedFrame(g, 32, 16, game.state.timer.running());
		for (Ghost ghost : ghosts) {
			rendering.drawGhost(g, ghost, game);
		}
		rendering.drawPac(g, msPac, game);
		presentGhost(g);
		presentMsPacMan(g);
		if (phase == Phase.END) {
			drawPointsAnimation(g, 26);
			drawPressKeyToStart(g, 32);
		}
	}

	private void presentGhost(Graphics2D g) {
		if (currentGhost == null) {
			return;
		}
		g.setColor(Color.WHITE);
		g.setFont(assets.getScoreFont());
		if (currentGhost == ghosts[0]) {
			g.drawString("WITH", t(8), t(11));
		}
		g.setColor(currentGhost.id == 0 ? Color.RED
				: currentGhost.id == 1 ? Color.PINK : currentGhost.id == 2 ? Color.CYAN : Color.ORANGE);
		g.drawString(currentGhost.name.toUpperCase(), t(13 - currentGhost.name.length() / 2), t(14));
	}

	private void presentMsPacMan(Graphics2D g) {
		if (!presentingMsPac) {
			return;
		}
		g.setColor(Color.WHITE);
		g.setFont(assets.getScoreFont());
		g.drawString("STARRING", t(8), t(11));
		g.setColor(Color.YELLOW);
		g.drawString("MS PAC-MAN", t(8), t(14));
	}

	private void drawAnimatedFrame(Graphics2D g, int numDotsX, int numDotsY, long time) {
		int light = (int) (time / 2) % (numDotsX / 2);
		for (int dot = 0; dot < 2 * (numDotsX + numDotsY); ++dot) {
			int x = 0, y = 0;
			if (dot <= numDotsX) {
				x = dot;
			} else if (dot < numDotsX + numDotsY) {
				x = numDotsX;
				y = dot - numDotsX;
			} else if (dot < 2 * numDotsX + numDotsY + 1) {
				x = 2 * numDotsX + numDotsY - dot;
				y = numDotsY;
			} else {
				y = 2 * (numDotsX + numDotsY) - dot;
			}
			g.setColor((dot + light) % (numDotsX / 2) == 0 ? Color.PINK : Color.RED);
			g.fillRect(t(frameTopLeftTile.x) + 4 * x, t(frameTopLeftTile.y) + 4 * y, 2, 2);
		}
	}

	private void drawPressKeyToStart(Graphics2D g, int tileY) {
		if (blinking.animate()) {
			String text = "PRESS SPACE TO PLAY";
			g.setColor(Color.ORANGE);
			g.setFont(assets.getScoreFont());
			g.drawString(text, t(13 - text.length() / 2), t(tileY));
		}
	}

	private void drawPointsAnimation(Graphics2D g, int tileY) {
		int x = t(10), y = t(tileY);
		if (blinking.animate()) {
			g.setColor(Color.PINK);
			g.fillOval(x, y + t(1) - 2, 10, 10);
			g.fillRect(x + 6, y - t(1) + 2, 2, 2);
		}
		g.setColor(Color.WHITE);
		g.setFont(assets.getScoreFont());
		g.drawString("10", x + t(2), y);
		g.drawString("50", x + t(2), y + t(2));
		g.setFont(assets.getScoreFont().deriveFont(6f));
		g.drawString("PTS", x + t(5), y);
		g.drawString("PTS", x + t(5), y + t(2));
	}
}