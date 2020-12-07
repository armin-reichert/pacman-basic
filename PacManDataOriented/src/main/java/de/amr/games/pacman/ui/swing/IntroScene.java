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
	public long timer;
	public int pacManX;
	public boolean chasingPacMan;

	public IntroScene(PacManGame game, Assets assets, Dimension size) {
		this.game = game;
		this.assets = assets;
		this.size = size;
		reset();
	}

	public void reset() {
		timer = 0;
		pacManX = size.width;
		chasingPacMan = true;
	}

	public void draw(Graphics2D g) {
		g.setFont(assets.scoreFont);

		int time = 1;
		if (timer >= game.clock.sec(time)) {
			int w = assets.imageLogo.getWidth();
			g.drawImage(assets.imageLogo, (size.width - w) / 2, 3, null);
		}

		time += 1;
		if (timer >= game.clock.sec(time)) {
			g.setColor(Color.WHITE);
			drawCenteredText(g, "CHARACTER / NICKNAME", 8 * TS, size.width);
		}

		time += 1;
		for (int ghost = 0; ghost <= 3; ++ghost) {
			int y = (10 + 3 * ghost) * TS;
			if (timer >= game.clock.sec(time)) {
				BufferedImage ghostLookingRight = assets.sheet(0, 4 + ghost);
				g.drawImage(ghostLookingRight, 2 * TS - 3, y - 2, null);
			}
			if (timer >= game.clock.sec(time + 0.5f)) {
				String character = (String) GHOST_INTRO_TEXTS[ghost][0];
				String nickname = (String) GHOST_INTRO_TEXTS[ghost][1];
				Color color = (Color) GHOST_INTRO_TEXTS[ghost][2];
				String text = "-";
				text += timer > game.clock.sec(time + 1) ? character + "    " + nickname : character;
				g.setColor(color);
				g.drawString(text, 4 * TS, y + 11);
			}
			time += 2;
		}

		if (timer >= game.clock.sec(time)) {
			g.setColor(Color.PINK);
			g.fillRect(9 * TS + 6, 27 * TS + 2, 2, 2);
			drawCenteredText(g, "10 PTS", 28 * TS, size.width);
			game.clock.alternating(20, () -> {
				g.fillOval(9 * TS, 29 * TS - 2, 10, 10);
			});
			drawCenteredText(g, "50 PTS", 30 * TS, size.width);
		}

		time += 1;
		if (timer >= game.clock.sec(time)) {
			if (chasingPacMan) {
				drawChasingPacMan(g, time);
			} else {
				drawChasingGhosts(g, time);
			}
		}

		time += 1;
		if (timer >= game.clock.sec(time)) {
			g.setColor(Color.WHITE);
			game.clock.alternating(20, () -> {
				drawCenteredText(g, "Press SPACE to play!", size.height - 20, size.width);
			});
		}

		++timer;
		if (timer == game.clock.sec(30)) {
			reset();
		}
	}

	private void drawChasingPacMan(Graphics2D g, int time) {
		int x = pacManX;
		int y = 22 * TS;
		game.clock.alternating(20, () -> {
			g.setColor(Color.PINK);
			g.fillOval(2 * TS, y + 2, 10, 10);
		});
		g.drawImage(pacManWalking(LEFT), x, y, null);
		x += 24;
		g.drawImage(ghostWalking(LEFT, BLINKY), x, y, null);
		x += 16;
		g.drawImage(ghostWalking(LEFT, PINKY), x, y, null);
		x += 16;
		g.drawImage(ghostWalking(LEFT, INKY), x, y, null);
		x += 16;
		g.drawImage(ghostWalking(LEFT, CLYDE), x, y, null);
		if (pacManX > 2 * TS) {
			pacManX -= 1;
		} else {
			chasingPacMan = false;
		}
	}

	private void drawChasingGhosts(Graphics2D g, int time) {
		int x = pacManX;
		int y = 22 * TS;
		g.drawImage(pacManWalking(RIGHT), x, y, null);
		x += 24;
		BufferedImage ghost = ghostFrightened();
		for (int i = 0; i < 4; ++i) {
			g.drawImage(ghost, x, y, null);
			x += 16;
		}
		if (pacManX < size.width) {
			pacManX += 1;
		} else {
			// TODO what?
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