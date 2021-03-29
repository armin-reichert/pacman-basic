package de.amr.games.pacman.ui.swing.scenes.pacman;

import static de.amr.games.pacman.model.common.GameVariant.PACMAN;
import static de.amr.games.pacman.model.world.PacManGameWorld.TS;
import static de.amr.games.pacman.model.world.PacManGameWorld.t;
import static de.amr.games.pacman.ui.pacman.PacMan_IntroScene_Controller.TOP_Y;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.RenderingHints;

import de.amr.games.pacman.controller.PacManGameController;
import de.amr.games.pacman.model.common.Ghost;
import de.amr.games.pacman.ui.pacman.PacMan_IntroScene_Controller;
import de.amr.games.pacman.ui.pacman.PacMan_IntroScene_Controller.GhostPortrait;
import de.amr.games.pacman.ui.pacman.PacMan_IntroScene_Controller.Phase;
import de.amr.games.pacman.ui.swing.PacManGameUI_Swing;
import de.amr.games.pacman.ui.swing.scenes.common.GameScene;

/**
 * Intro scene of the PacMan game.
 * <p>
 * The ghost are presented one after another, then Pac-Man is chased by the ghosts, turns the card
 * and hunts the ghost himself.
 * 
 * @author Armin Reichert
 */
public class PacMan_IntroScene extends GameScene {

	private PacMan_IntroScene_Controller sceneController;

	public PacMan_IntroScene(PacManGameController controller, Dimension size) {
		super(controller, size, PacManGameUI_Swing.RENDERING.get(PACMAN), PacManGameUI_Swing.SOUND.get(PACMAN));
	}

	@Override
	public void start() {
		sceneController = new PacMan_IntroScene_Controller(gameController, rendering);
		sceneController.start();
	}

	@Override
	public void update() {
		sceneController.update();
	}

	@Override
	public void render(Graphics2D g) {
		Graphics2D g2 = (Graphics2D) g.create();
		g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		rendering.drawScore(g, gameController.game(), true);
		drawGallery(g);
		if (sceneController.phase == Phase.CHASING_PAC) {
			if (sceneController.blinking.animate()) {
				g2.setColor(Color.PINK);
				g2.fillOval(t(2), (int) sceneController.pac.position.y, TS, TS);
			}
		}
		drawGuys(g);
		if (sceneController.phase.ordinal() >= Phase.CHASING_GHOSTS.ordinal()) {
			drawPointsAnimation(g2, 11, 26);
		}
		if (sceneController.phase == Phase.READY_TO_PLAY) {
			drawPressKeyToStart(g, 32);
		}
		g2.dispose();
	}

	private void drawGuys(Graphics2D g) {
		for (Ghost ghost : sceneController.ghosts) {
			rendering.drawGhost(g, ghost, false);
		}
		rendering.drawPlayer(g, sceneController.pac);
	}

	private void drawGallery(Graphics2D g) {
		g.setColor(Color.WHITE);
		g.setFont(rendering.getScoreFont());
		g.drawString("CHARACTER", t(6), TOP_Y);
		g.drawString("/", t(16), TOP_Y);
		g.drawString("NICKNAME", t(18), TOP_Y);
		for (int i = 0; i < 4; ++i) {
			GhostPortrait portrait = sceneController.gallery[i];
			if (portrait.ghost.visible) {
				int y = TOP_Y + t(2 + 3 * i);
				rendering.drawGhost(g, sceneController.gallery[i].ghost, false);
				g.setColor(getGhostColor(i));
				g.setFont(rendering.getScoreFont());
				if (portrait.characterVisible) {
					g.drawString("-" + portrait.character, t(6), y + 8);
				}
				if (portrait.nicknameVisible) {
					g.drawString("\"" + portrait.ghost.name + "\"", t(18), y + 8);
				}
			}
		}
	}

	private Color getGhostColor(int i) {
		return i == 0 ? Color.RED : i == 1 ? Color.PINK : i == 2 ? Color.CYAN : Color.ORANGE;
	}

	private void drawPressKeyToStart(Graphics2D g, int yTile) {
		if (sceneController.blinking.frame()) {
			String text = "PRESS SPACE TO PLAY";
			g.setColor(Color.ORANGE);
			g.setFont(rendering.getScoreFont());
			g.drawString(text, t(14 - text.length() / 2), t(yTile));
		}
	}

	private void drawPointsAnimation(Graphics2D g, int tileX, int tileY) {
		if (sceneController.blinking.frame()) {
			g.setColor(Color.PINK);
			g.fillRect(t(tileX) + 6, t(tileY - 1) + 2, 2, 2);
			g.fillOval(t(tileX), t(tileY + 1) - 2, 10, 10);
		}
		g.setColor(Color.WHITE);
		g.setFont(rendering.getScoreFont());
		g.drawString("10", t(tileX + 2), t(tileY));
		g.drawString("50", t(tileX + 2), t(tileY + 2));
		g.setFont(rendering.getScoreFont().deriveFont(6f));
		g.drawString("PTS", t(tileX + 5), t(tileY));
		g.drawString("PTS", t(tileX + 5), t(tileY + 2));
	}
}