package de.amr.games.pacman.ui.swing.mspacman;

import static de.amr.games.pacman.model.world.PacManGameWorld.t;
import static de.amr.games.pacman.ui.swing.rendering.standard.MsPacMan_StandardRendering.assets;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics2D;

import de.amr.games.pacman.controller.PacManGameController;
import de.amr.games.pacman.model.common.Ghost;
import de.amr.games.pacman.ui.mspacman.MsPacMan_IntroScene_Controller;
import de.amr.games.pacman.ui.mspacman.MsPacMan_IntroScene_Controller.Phase;
import de.amr.games.pacman.ui.sound.SoundManager;
import de.amr.games.pacman.ui.swing.common.GameScene;
import de.amr.games.pacman.ui.swing.rendering.PacManGameRendering2D;

/**
 * Intro scene of the Ms. Pac-Man game. The ghosts and Ms. Pac-Man are introduced one after another.
 * 
 * @author Armin Reichert
 */
public class MsPacMan_IntroScene extends GameScene {

	private MsPacMan_IntroScene_Controller animation;

	public MsPacMan_IntroScene(PacManGameController controller, Dimension size, PacManGameRendering2D rendering,
			SoundManager sounds) {
		super(controller, size, rendering, sounds);
	}

	@Override
	public void start() {
		animation = new MsPacMan_IntroScene_Controller(controller, rendering, sounds);
		animation.start();
	}

	@Override
	public void update() {
		animation.update();
	}

	@Override
	public void render(Graphics2D g) {
		g.setFont(assets.getScoreFont());
		g.setColor(Color.ORANGE);
		g.drawString("\"MS PAC-MAN\"", t(8), t(5));
		drawAnimatedFrame(g, 32, 16, controller.getGame().state.timer.running());
		for (Ghost ghost : animation.ghosts) {
			rendering.drawGhost(g, ghost, false);
		}
		rendering.drawPlayer(g, animation.msPac);
		presentGhost(g);
		presentMsPacMan(g);
		if (animation.phase == Phase.END) {
			drawPointsAnimation(g, 26);
			drawPressKeyToStart(g, 32);
		}
	}

	private void presentGhost(Graphics2D g) {
		if (animation.currentGhost == null) {
			return;
		}
		g.setColor(Color.WHITE);
		g.setFont(assets.getScoreFont());
		if (animation.currentGhost == animation.ghosts[0]) {
			g.drawString("WITH", t(8), t(11));
		}
		g.setColor(animation.currentGhost.id == 0 ? Color.RED
				: animation.currentGhost.id == 1 ? Color.PINK : animation.currentGhost.id == 2 ? Color.CYAN : Color.ORANGE);
		g.drawString(animation.currentGhost.name.toUpperCase(), t(13 - animation.currentGhost.name.length() / 2), t(14));
	}

	private void presentMsPacMan(Graphics2D g) {
		if (!animation.presentingMsPac) {
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
			g.fillRect(t(animation.frameTopLeftTile.x) + 4 * x, t(animation.frameTopLeftTile.y) + 4 * y, 2, 2);
		}
	}

	private void drawPressKeyToStart(Graphics2D g, int tileY) {
		if (animation.blinking.frame()) {
			String text = "PRESS SPACE TO PLAY";
			g.setColor(Color.ORANGE);
			g.setFont(assets.getScoreFont());
			g.drawString(text, t(13 - text.length() / 2), t(tileY));
		}
	}

	private void drawPointsAnimation(Graphics2D g, int tileY) {
		int x = t(10), y = t(tileY);
		if (animation.blinking.frame()) {
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