package de.amr.games.pacman.ui.swing;

import static de.amr.games.pacman.World.TS;
import static de.amr.games.pacman.common.Direction.LEFT;
import static de.amr.games.pacman.common.Direction.RIGHT;
import static de.amr.games.pacman.entities.Ghost.BLINKY;
import static de.amr.games.pacman.entities.Ghost.CLYDE;
import static de.amr.games.pacman.entities.Ghost.INKY;
import static de.amr.games.pacman.entities.Ghost.PINKY;
import static de.amr.games.pacman.ui.swing.PacManGameSwingUI.dirIndex;
import static de.amr.games.pacman.ui.swing.PacManGameSwingUI.drawCenteredText;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;

import de.amr.games.pacman.PacManGame;
import de.amr.games.pacman.common.Direction;

/**
 * Intro presenting the ghosts and showing the chasing animation.
 * 
 * @author Armin Reichert
 */
class IntroScene {

	//@formatter:off
	static final Object[][] GHOST_INTRO_TEXTS = {
		{ "SHADOW ", "\"BLINKY\"", Color.RED }, 
		{ "SPEEDY ", "\"PINKY\"",  Color.PINK },
		{ "BASHFUL", "\"INKY\"",   Color.CYAN }, 
		{ "POKEY  ", "\"CLYDE\"",  Color.ORANGE }
	};
	//@formatter:on

	public PacManGame game;
	public Assets assets;
	public Dimension size;
	public long passed;
	public long mark;
	public float pacManX;
	public float leftmostGhostX;
	public boolean chasingPacMan;

	public IntroScene(PacManGame game, Assets assets, Dimension size) {
		this.game = game;
		this.assets = assets;
		this.size = size;
		reset();
	}

	public void reset() {
		passed = 0;
		mark = 0;
		pacManX = size.width;
		leftmostGhostX = pacManX + 24;
		chasingPacMan = true;
	}

	public void draw(Graphics2D g) {
		g.setFont(assets.scoreFont);

		mark = 1;
		if (passed >= game.clock.sec(mark)) {
			int w = assets.imageLogo.getWidth();
			g.drawImage(assets.imageLogo, (size.width - w) / 2, 3, null);
		}

		mark += 1;
		if (passed >= game.clock.sec(mark)) {
			g.setColor(Color.WHITE);
			drawCenteredText(g, "CHARACTER / NICKNAME", 8 * TS, size.width);
		}

		mark += 1;
		for (int ghost = 0; ghost <= 3; ++ghost) {
			int y = (10 + 3 * ghost) * TS;
			if (passed >= game.clock.sec(mark)) {
				BufferedImage ghostLookingRight = assets.sheet(0, 4 + ghost);
				g.drawImage(ghostLookingRight, 2 * TS - 3, y - 2, null);
			}
			if (passed >= game.clock.sec(mark + 0.5f)) {
				String character = (String) GHOST_INTRO_TEXTS[ghost][0];
				String nickname = (String) GHOST_INTRO_TEXTS[ghost][1];
				Color color = (Color) GHOST_INTRO_TEXTS[ghost][2];
				String text = "-";
				text += passed > game.clock.sec(mark + 1) ? character + "    " + nickname : character;
				g.setColor(color);
				g.drawString(text, 4 * TS, y + 11);
			}
			mark += 2;
		}

		if (passed >= game.clock.sec(mark)) {
			g.setColor(Color.PINK);
			g.fillRect(9 * TS + 6, 27 * TS + 2, 2, 2);
			drawCenteredText(g, "10 PTS", 28 * TS, size.width);
			game.clock.alternating(20, () -> {
				g.fillOval(9 * TS, 29 * TS - 2, 10, 10);
			});
			drawCenteredText(g, "50 PTS", 30 * TS, size.width);
		}

		mark += 1;
		if (passed >= game.clock.sec(mark)) {
			if (chasingPacMan) {
				drawChasingPacMan(g);
			} else {
				drawChasingGhosts(g);
			}
		}

		mark += 1;
		if (passed >= game.clock.sec(mark)) {
			g.setColor(Color.WHITE);
			game.clock.alternating(20, () -> {
				drawCenteredText(g, "Press SPACE to play!", size.height - 20, size.width);
			});
		}

		++passed;
		if (passed == game.clock.sec(30)) {
			reset();
		}
	}

	private void drawChasingPacMan(Graphics2D g) {
		int y = 22 * TS;
		game.clock.alternating(20, () -> {
			g.setColor(Color.PINK);
			g.fillOval(2 * TS, y + 2, 10, 10);
		});
		g.drawImage(pacManWalking(LEFT), (int) pacManX, y, null);
		g.drawImage(ghostWalking(LEFT, BLINKY), (int) leftmostGhostX, y, null);
		g.drawImage(ghostWalking(LEFT, PINKY), (int) leftmostGhostX + 16, y, null);
		g.drawImage(ghostWalking(LEFT, INKY), (int) leftmostGhostX + 2 * 16, y, null);
		g.drawImage(ghostWalking(LEFT, CLYDE), (int) leftmostGhostX + 3 * 16, y, null);
		if (pacManX > 2 * TS) {
			pacManX -= 1;
			leftmostGhostX -= 1;
		} else {
			chasingPacMan = false;
		}
	}

	private void drawChasingGhosts(Graphics2D g) {
		int y = 22 * TS;
		BufferedImage ghost = ghostFrightened();
		for (int i = 0; i < 4; ++i) {
			int x = (int) leftmostGhostX + i * 16;
			if (pacManX > x && pacManX <= x + 16) {
				int bounty = (int) Math.pow(2, i) * 200;
				g.drawImage(assets.bountyNumbers.get(bounty), x, y, null);
			} else if (pacManX < x) {
				g.drawImage(ghost, x, y, null);
			}
		}
		g.drawImage(pacManWalking(RIGHT), (int) pacManX, y, null);
		if (leftmostGhostX < size.width) {
			pacManX += 0.6f;
			leftmostGhostX += 0.3f;
		}
	}

	private BufferedImage pacManWalking(Direction dir) {
		int mouthFrame = game.clock.frame(5, 3);
		return mouthFrame == 2 ? assets.sheet(mouthFrame, 0) : assets.sheet(mouthFrame, dirIndex(dir));
	}

	private BufferedImage ghostWalking(Direction dir, int ghost) {
		return assets.sheet(2 * dirIndex(dir) + game.clock.frame(5, 2), 4 + ghost);
	}

	private BufferedImage ghostFrightened() {
		return assets.sheet(8 + game.clock.frame(5, 2), 4);
	}
}