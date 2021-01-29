package de.amr.games.pacman.ui.swing.mspacman;

import static de.amr.games.pacman.game.core.PacManGameWorld.t;
import static de.amr.games.pacman.game.heaven.God.clock;
import static de.amr.games.pacman.lib.Direction.LEFT;
import static de.amr.games.pacman.lib.Direction.UP;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;

import de.amr.games.pacman.game.core.PacManGameModel;
import de.amr.games.pacman.game.creatures.Ghost;
import de.amr.games.pacman.lib.Direction;
import de.amr.games.pacman.lib.V2f;
import de.amr.games.pacman.lib.V2i;
import de.amr.games.pacman.ui.api.PacManGameScene;
import de.amr.games.pacman.ui.api.PacManGameSound;
import de.amr.games.pacman.ui.swing.PacManGameSwingUI;

public class MsPacManIntroScene implements PacManGameScene {

	private static final Color[] GHOST_COLORS = { Color.RED, Color.PINK, Color.CYAN, Color.ORANGE };

	private final PacManGameSwingUI ui;
	private final V2i size;
	private final MsPacManAssets assets;
	private final PacManGameModel game;
	private final V2i frameDots = new V2i(32, 16);
	private final V2i frameTopLeftTile = new V2i(6, 8);
	private final int leftOfFrame = t(frameTopLeftTile.x) - 18;
	private final int belowFrame = t(frameTopLeftTile.y) + 4 * (frameDots.y + 1);
	private final int belowFrameCenterX = t(frameTopLeftTile.x) + 2 * frameDots.x;
	private final float walkSpeed = 1.2f;

	public MsPacManIntroScene(PacManGameSwingUI ui, V2i size, MsPacManAssets assets, PacManGameModel game) {
		this.ui = ui;
		this.size = size;
		this.assets = assets;
		this.game = game;
	}

	@Override
	public V2i size() {
		return size;
	}

	@Override
	public void start() {
		for (Ghost ghost : game.ghosts) {
			ghost.position = new V2f(size.x, belowFrame);
			ghost.speed = 0;
			ghost.dir = LEFT;
		}
		game.pac.position = new V2f(size.x, belowFrame);
		game.pac.speed = 0;
		game.pac.dir = LEFT;
	}

	@Override
	public void end() {
		for (int ghostID = 0; ghostID < 4; ++ghostID) {
			for (Direction dir : Direction.values()) {
				assets.ghostsWalking.get(ghostID).get(dir).restart();
			}
		}
		for (Direction dir : Direction.values()) {
			assets.pacMunching.get(dir).restart();
		}
		game.state.resetTimer();
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
			ui.sounds().ifPresent(sm -> sm.playSound(PacManGameSound.CREDIT));
		}

		drawBlinkingFrame(g, time);
		g.setFont(assets.scoreFont);
		g.setColor(Color.ORANGE);
		drawCenteredText(g, "\"MS PAC-MAN\"", t(5));

		for (Ghost ghost : game.ghosts) {
			if (ghostReachedEndPosition(ghost)) {
				g.drawImage(sprite(ghost), (int) ghost.position.x, (int) ghost.position.y, null);
				continue;
			}
			if (ghost.id == 0) {
				g.setColor(Color.WHITE);
				g.drawString(ui.translation("WITH"), t(8), t(11));
			}
			if (ghost.speed != 0) {
				g.setColor(GHOST_COLORS[ghost.id]);
				drawCenteredText(g, ui.translation("MSPACMAN.GHOST." + ghost.id + ".NICKNAME"), t(14));
				V2f velocity = new V2f(ghost.dir.vec).scaled(ghost.speed);
				ghost.position = ghost.position.sum(velocity);
				if (ghost.position.x <= leftOfFrame) {
					ghost.dir = UP;
				}
			}
			g.drawImage(sprite(ghost), (int) ghost.position.x, (int) ghost.position.y, null);
			if (ghostReachedEndPosition(ghost)) {
				ghost.speed = 0;
				ghost.position = new V2f(ghost.position.x, t(frameTopLeftTile.y) + 16 * ghost.id);
				if (ghost.id < 3) { // start next ghost
					game.ghosts[ghost.id + 1].speed = walkSpeed;
					ui.sounds().ifPresent(sm -> sm.playSound(PacManGameSound.CREDIT));
				} else { // start Pac
					game.pac.speed = walkSpeed;
					ui.sounds().ifPresent(sm -> sm.loopSound(PacManGameSound.PACMAN_MUNCH));
				}
			}
		}

		if (ghostReachedEndPosition(game.ghosts[3])) {
			g.setColor(Color.WHITE);
			g.drawString(ui.translation("STARRING"), t(8), t(11));
			g.setColor(Color.YELLOW);
			g.drawString("MS PAC-MAN", t(11), t(14));
		}

		if (game.pac.speed != 0) {
			V2f velocity = new V2f(game.pac.dir.vec).scaled(game.pac.speed);
			game.pac.position = game.pac.position.sum(velocity);
			if (game.pac.position.x <= belowFrameCenterX) {
				game.pac.speed = 0;
				ui.sounds().ifPresent(sm -> sm.stopAllSounds());
			}
		}
		g.drawImage(pacSprite(), (int) game.pac.position.x, (int) game.pac.position.y, null);

		// Pac animation over?
		if (game.pac.speed == 0 && game.pac.position.x <= belowFrameCenterX) {
			drawPointsAnimation(g, 26, time);
			drawPressKeyToStart(g, time);
		}

		// restart intro after 30 seconds
		if (time == clock.sec(30)) {
			end();
			start();
		}
	}

	private boolean ghostReachedEndPosition(Ghost ghost) {
		return ghost.position.x <= leftOfFrame && ghost.position.y <= t(frameTopLeftTile.y) + ghost.id * 16;
	}

	private BufferedImage pacSprite() {
		return game.pac.speed != 0 ? assets.pacMunching.get(game.pac.dir).currentFrameThenAdvance() : assets.pacMouthOpen.get(game.pac.dir);
	}

	private BufferedImage sprite(Ghost ghost) {
		return ghost.speed != 0 ? assets.ghostsWalking.get(ghost.id).get(ghost.dir).currentFrameThenAdvance()
				: assets.ghostsWalking.get(ghost.id).get(ghost.dir).thing(0);
	}

	private void drawBlinkingFrame(Graphics2D g, long time) {
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
			g.setColor((dot + light) % (frameDots.x / 2) == 0 ? Color.CYAN : Color.RED);
			g.fillRect(t(frameTopLeftTile.x) + 4 * x, t(frameTopLeftTile.y) + 4 * y, 2, 2);
		}
	}

	private void drawPressKeyToStart(Graphics2D g, long time) {
		g.setColor(Color.ORANGE);
		g.setFont(assets.scoreFont);
		if (time % 40 < 20) {
			drawCenteredText(g, ui.translation("PRESS_KEY_TO_PLAY"), size.y - 20);
		}
	}

	private void drawPointsAnimation(Graphics2D g, int yTile, long time) {
		g.setColor(Color.PINK);
		g.fillRect(t(9) + 6, t(yTile - 1) + 2, 2, 2);
		if (time % 40 < 20) {
			g.fillOval(t(9), t(yTile + 1) - 2, 10, 10);
		}
		g.setColor(Color.WHITE);
		g.setFont(assets.scoreFont);
		g.drawString("10", t(12), t(yTile));
		g.drawString("50", t(12), t(yTile + 2));
		g.setFont(assets.scoreFont.deriveFont(6f));
		g.drawString(ui.translation("POINTS"), t(15), t(yTile));
		g.drawString(ui.translation("POINTS"), t(15), t(yTile + 2));
	}

}