package de.amr.games.pacman.ui.swing.mspacman.scene;

import static de.amr.games.pacman.heaven.God.clock;
import static de.amr.games.pacman.heaven.God.differsAtMost;
import static de.amr.games.pacman.lib.Direction.LEFT;
import static de.amr.games.pacman.lib.Direction.UP;
import static de.amr.games.pacman.model.creatures.GhostState.HUNTING_PAC;
import static de.amr.games.pacman.world.PacManGameWorld.TS;
import static de.amr.games.pacman.world.PacManGameWorld.t;

import java.awt.Color;
import java.awt.Graphics2D;

import de.amr.games.pacman.lib.Animation;
import de.amr.games.pacman.lib.V2f;
import de.amr.games.pacman.lib.V2i;
import de.amr.games.pacman.model.MsPacManGame;
import de.amr.games.pacman.model.creatures.Ghost;
import de.amr.games.pacman.ui.api.PacManGameScene;
import de.amr.games.pacman.ui.swing.mspacman.rendering.MsPacManGameRendering;

public class MsPacManGameIntroScene implements PacManGameScene {

	private static final Color[] GHOST_COLORS = { Color.RED, Color.PINK, Color.CYAN, Color.ORANGE };

	private final V2i size;
	private final MsPacManGameRendering rendering;
	private final MsPacManGame game;

	private final Animation<Boolean> blinking = Animation.pulse().frameDuration(30);
	private final V2i frameDots = new V2i(32, 16);
	private final V2i frameTopLeftTile = new V2i(6, 8);
	private final int leftOfFrame = t(frameTopLeftTile.x) - 16;
	private final int belowFrame = t(frameTopLeftTile.y) + 4 * (frameDots.y + 1) + 6;
	private final int belowFrameCenterX = t(frameTopLeftTile.x) + 2 * frameDots.x;
	private final float walkSpeed = 1.2f;

	private long completedTime;

	public MsPacManGameIntroScene(V2i size, MsPacManGameRendering rendering, MsPacManGame game) {
		this.size = size;
		this.game = game;
		this.rendering = rendering;
	}

	@Override
	public V2i sizeInPixel() {
		return size;
	}

	@Override
	public void start() {
		completedTime = -1;
		for (Ghost ghost : game.ghosts) {
			ghost.position = new V2f(size.x + TS, belowFrame);
			ghost.speed = 0;
			ghost.dir = ghost.wishDir = LEFT;
			ghost.visible = true;
			ghost.state = HUNTING_PAC;
		}
		game.pac.position = new V2f(size.x + TS, belowFrame);
		game.pac.speed = 0;
		game.pac.dir = LEFT;
		game.pac.visible = true;
		game.pac.couldMove = true;
		game.pac.dead = false;

		blinking.restart();
		rendering.letGhostsFidget(game.ghosts(), true);
		rendering.letPacMunch();
	}

	@Override
	public void draw(Graphics2D g) {

		// wait 1 second before animation starts
		long time = game.state.ticksRun() - clock.sec(1);
		if (time < 0) {
			return;
		}

		// animation start:
		if (time == 0) {
			game.ghosts[0].speed = walkSpeed;
		}

		drawAnimatedFrame(g, time);
		g.setFont(rendering.assets.getScoreFont());
		g.setColor(Color.ORANGE);
		drawHCenteredText(g, "\"MS PAC-MAN\"", t(5));

		for (Ghost ghost : game.ghosts) {
			if (reachedEndPosition(ghost)) {
				continue;
			}
			if (ghost.id == 0 && ghost.speed != 0) {
				g.setColor(Color.WHITE);
				g.drawString(rendering.translations.getString("WITH"), t(8), t(11));
			}
			if (ghost.speed != 0) {
				g.setColor(GHOST_COLORS[ghost.id]);
				drawHCenteredText(g, rendering.translations.getString("MSPACMAN.GHOST." + ghost.id + ".NICKNAME"), t(14));
				V2f velocity = new V2f(ghost.dir.vec).scaled(ghost.speed);
				ghost.position = ghost.position.sum(velocity);
				if (ghost.dir == LEFT && differsAtMost(ghost.position.x, leftOfFrame, 1)) {
					ghost.dir = ghost.wishDir = UP;
				}
			}
			if (reachedEndPosition(ghost)) {
				ghost.speed = 0;
				ghost.position = new V2f(ghost.position.x, ghostEndPositionY(ghost));
				rendering.ghostWalking(ghost, ghost.dir).reset();
				if (ghost.id < 3) { // start next ghost
					game.ghosts[ghost.id + 1].speed = walkSpeed;
				} else { // start Pac
					game.pac.speed = walkSpeed;
				}
			}
		}
		for (Ghost ghost : game.ghosts) {
			rendering.drawGuy(g, ghost, game);
		}
		if (reachedEndPosition(game.ghosts[3])) {
			g.setColor(Color.WHITE);
			g.drawString(rendering.translations.getString("STARRING"), t(8), t(11));
			g.setColor(Color.YELLOW);
			g.drawString("MS PAC-MAN", t(11), t(14));
		}

		if (completedTime < 0) {
			game.pac.moveFreely();
			if (game.pac.position.x <= belowFrameCenterX) {
				game.pac.speed = 0;
				completedTime = game.state.ticksRun();
			}
		}
		rendering.drawGuy(g, game.pac, game);

		// Pac animation over?
		if (completedTime > 0) {
			drawPointsAnimation(g, 26);
			drawPressKeyToStart(g);
			game.state.runAt(completedTime + clock.sec(3), () -> {
				end();
				game.attractMode = true;
			});
		}
	}

	private int ghostEndPositionY(Ghost ghost) {
		return t(frameTopLeftTile.y) + 16 * ghost.id + 2;
	}

	private boolean reachedEndPosition(Ghost ghost) {
		int endPositionY = ghostEndPositionY(ghost);
		return differsAtMost(ghost.position.x, leftOfFrame, 1) && differsAtMost(ghost.position.y, endPositionY, 2);
	}

	private void drawAnimatedFrame(Graphics2D g, long time) {
		int light = (int) (time / 2) % (frameDots.x / 2);
		for (int dot = 0; dot < 2 * (frameDots.x + frameDots.y); ++dot) {
			int x = 0, y = 0;
			if (dot <= frameDots.x) {
				x = dot;
			} else if (dot < frameDots.x + frameDots.y) {
				x = frameDots.x;
				y = dot - frameDots.x;
			} else if (dot < 2 * frameDots.x + frameDots.y + 1) {
				x = 2 * frameDots.x + frameDots.y - dot;
				y = frameDots.y;
			} else {
				y = 2 * (frameDots.x + frameDots.y) - dot;
			}
			g.setColor((dot + light) % (frameDots.x / 2) == 0 ? Color.PINK : Color.RED);
			g.fillRect(t(frameTopLeftTile.x) + 4 * x, t(frameTopLeftTile.y) + 4 * y, 2, 2);
		}
	}

	private void drawPressKeyToStart(Graphics2D g) {
		g.setColor(Color.ORANGE);
		g.setFont(rendering.assets.getScoreFont());
		if (blinking.animate()) {
			drawHCenteredText(g, rendering.translations.getString("PRESS_KEY_TO_PLAY"), t(31));
		}
	}

	private void drawPointsAnimation(Graphics2D g, int yTile) {
		g.setColor(Color.PINK);
		g.fillRect(t(9) + 6, t(yTile - 1) + 2, 2, 2);
		if (blinking.animate()) {
			g.fillOval(t(9), t(yTile + 1) - 2, 10, 10);
		}
		g.setColor(Color.WHITE);
		g.setFont(rendering.assets.getScoreFont());
		g.drawString("10", t(12), t(yTile));
		g.drawString("50", t(12), t(yTile + 2));
		g.setFont(rendering.assets.getScoreFont().deriveFont(6f));
		g.drawString(rendering.translations.getString("POINTS"), t(15), t(yTile));
		g.drawString(rendering.translations.getString("POINTS"), t(15), t(yTile + 2));
	}
}