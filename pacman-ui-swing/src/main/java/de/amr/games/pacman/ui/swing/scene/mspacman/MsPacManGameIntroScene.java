package de.amr.games.pacman.ui.swing.scene.mspacman;

import static de.amr.games.pacman.heaven.God.clock;
import static de.amr.games.pacman.lib.Direction.LEFT;
import static de.amr.games.pacman.lib.Direction.UP;
import static de.amr.games.pacman.lib.Logging.log;
import static de.amr.games.pacman.world.PacManGameWorld.t;

import java.awt.Color;
import java.awt.Graphics2D;

import de.amr.games.pacman.lib.Animation;
import de.amr.games.pacman.lib.V2f;
import de.amr.games.pacman.lib.V2i;
import de.amr.games.pacman.model.Ghost;
import de.amr.games.pacman.model.GhostState;
import de.amr.games.pacman.model.Pac;
import de.amr.games.pacman.model.PacManGameModel;
import de.amr.games.pacman.ui.swing.rendering.mspacman.MsPacManGameSpriteBasedRendering;
import de.amr.games.pacman.ui.swing.scene.PacManGameScene;

public class MsPacManGameIntroScene implements PacManGameScene {

	enum Phase {
		BEGIN, BLINKY, PINKY, INKY, SUE, MSPACMAN, END
	}

	private final V2i size;
	private final PacManGameModel game;
	private final MsPacManGameSpriteBasedRendering rendering;

	private final V2i frameTopLeftTile = new V2i(6, 8);
	private final int belowFrame = t(17);
	private final int leftOfFrame = t(4);
	private final Animation<Boolean> blinking = Animation.pulse().frameDuration(30).restart();
	private Pac pac;
	private Ghost[] ghosts;
	private Phase phase;
	private long phaseStartTime;

	public MsPacManGameIntroScene(V2i size, MsPacManGameSpriteBasedRendering rendering, PacManGameModel game) {
		this.game = game;
		this.size = size;
		this.rendering = rendering;
	}

	private boolean phaseAt(long ticks) {
		return game.state.ticksRun() - phaseStartTime == ticks;
	}

	private boolean phaseAfter(long ticks) {
		return game.state.ticksRun() - phaseStartTime >= ticks;
	}

	private void enterPhase(Phase newPhase) {
		phase = newPhase;
		phaseStartTime = game.state.ticksRun();
	}

	@Override
	public V2i sizeInPixel() {
		return size;
	}

	@Override
	public void start() {
		log("Intro scene started at clock time %d", clock.ticksTotal);

		pac = new Pac("Ms. Pac-Man", LEFT);
		pac.position = new V2f(t(37), belowFrame);
		pac.visible = false;
		pac.speed = 0;
		pac.dead = false;
		pac.dir = LEFT;

		ghosts = new Ghost[] { new Ghost(0, "Blinky", LEFT), new Ghost(1, "Pinky", LEFT), new Ghost(2, "Inky", LEFT),
				new Ghost(3, "Sue", LEFT), };
		for (Ghost ghost : ghosts) {
			ghost.position = new V2f(t(37), belowFrame);
			ghost.visible = false;
			ghost.dir = ghost.wishDir = LEFT;
			ghost.bounty = 0;
			ghost.speed = 0;
			ghost.state = GhostState.HUNTING_PAC;
		}
		enterPhase(Phase.BEGIN);
	}

	@Override
	public void draw(Graphics2D g) {
		for (Ghost ghost : ghosts) {
			ghost.move();
		}
		pac.move();

		g.setFont(rendering.assets.getScoreFont());
		g.setColor(Color.ORANGE);
		g.drawString("\"MS PAC-MAN\"", t(8), t(5));
		drawAnimatedFrame(g, 32, 16, game.state.ticksRun());
		for (Ghost ghost : ghosts) {
			rendering.drawGuy(g, ghost, game);
		}
		rendering.drawGuy(g, pac, game);

		switch (phase) {
		case BEGIN:
			if (phaseAfter(clock.sec(1))) {
				enterPhase(Phase.BLINKY);
			}
			break;
		case BLINKY:
			showGhostName(g, "WITH", "BLINKY", Color.RED, 11);
			letGhostWalkToEndPosition(ghosts[0], Phase.PINKY);
			break;
		case PINKY:
			showGhostName(g, "", "PINKY", Color.PINK, 11);
			letGhostWalkToEndPosition(ghosts[1], Phase.INKY);
			break;
		case INKY:
			showGhostName(g, "", "INKY", Color.CYAN, 11);
			letGhostWalkToEndPosition(ghosts[2], Phase.SUE);
			break;
		case SUE:
			showGhostName(g, "", "Sue", Color.ORANGE, 12);
			letGhostWalkToEndPosition(ghosts[3], Phase.MSPACMAN);
			break;
		case MSPACMAN:
			showPacName(g);
			letMsPacManWalkToEndPosition(pac, Phase.END);
			break;
		case END:
			showPacName(g);
			showPointsAnimation(g, 26);
			showPressKeyToStart(g, 32);
			if (phaseAt(clock.sec(10))) {
				game.attractMode = true;
			}
			break;
		default:
			break;
		}
	}

	private void showGhostName(Graphics2D g, String with, String name, Color color, int tileX) {
		g.setColor(Color.WHITE);
		g.setFont(rendering.assets.getScoreFont());
		if (with.length() > 0) {
			g.drawString(with, t(8), t(11));
		}
		g.setColor(color);
		g.drawString(name, t(tileX), t(14));
	}

	private void letGhostWalkToEndPosition(Ghost ghost, Phase nextPhase) {
		if (phaseAt(1)) {
			ghost.visible = true;
			ghost.speed = 1;
			rendering.ghostKicking(ghost).forEach(Animation::restart);
		}
		if (ghost.dir == LEFT && ghost.position.x <= leftOfFrame) {
			ghost.dir = ghost.wishDir = UP;
		}
		if (ghost.dir == UP && ghost.position.y <= t(frameTopLeftTile.y) + ghost.id * 18) {
			ghost.speed = 0;
			rendering.ghostKicking(ghost).forEach(Animation::reset);
			enterPhase(nextPhase);
		}
	}

	private void showPacName(Graphics2D g) {
		g.setColor(Color.WHITE);
		g.setFont(rendering.assets.getScoreFont());
		g.drawString("STARRING", t(8), t(11));
		g.setColor(Color.YELLOW);
		g.drawString("MS PAC-MAN", t(8), t(14));
	}

	private void letMsPacManWalkToEndPosition(Pac msPac, Phase nextPhase) {
		if (phaseAt(1)) {
			msPac.visible = true;
			msPac.couldMove = true;
			msPac.speed = 1;
			msPac.dir = LEFT;
			rendering.pacMunching().forEach(Animation::restart);
		}
		if (msPac.speed != 0 && msPac.position.x <= t(13)) {
			msPac.speed = 0;
			rendering.pacMunching().forEach(Animation::reset);
			enterPhase(nextPhase);
		}
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

	private void showPressKeyToStart(Graphics2D g, int yTile) {
		if (blinking.animate()) {
			g.setColor(Color.ORANGE);
			g.setFont(rendering.assets.getScoreFont());
			g.drawString("PRESS SPACE KEY TO PLAY", t(2), t(yTile));
		}
	}

	private void showPointsAnimation(Graphics2D g, int yTile) {
		if (blinking.animate()) {
			g.setColor(Color.PINK);
			g.fillRect(t(8) + 6, t(yTile - 1) + 2, 2, 2);
			g.fillOval(t(8), t(yTile + 1) - 2, 10, 10);
		}
		g.setColor(Color.WHITE);
		g.setFont(rendering.assets.getScoreFont());
		g.drawString("10", t(10), t(yTile));
		g.drawString("50", t(10), t(yTile + 2));
		g.setFont(rendering.assets.getScoreFont());
		g.drawString("POINTS", t(13), t(yTile));
		g.drawString("POINTS", t(13), t(yTile + 2));
	}
}
