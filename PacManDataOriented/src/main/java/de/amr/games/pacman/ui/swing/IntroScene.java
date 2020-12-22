package de.amr.games.pacman.ui.swing;

import static de.amr.games.pacman.core.World.TS;
import static de.amr.games.pacman.lib.Direction.LEFT;
import static de.amr.games.pacman.lib.Direction.RIGHT;
import static de.amr.games.pacman.lib.Functions.t;
import static de.amr.games.pacman.ui.swing.PacManGameSwingUI.dirIndex;
import static de.amr.games.pacman.ui.swing.PacManGameSwingUI.drawCenteredText;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;

import de.amr.games.pacman.core.Game;
import de.amr.games.pacman.lib.Direction;
import de.amr.games.pacman.lib.V2i;
import de.amr.games.pacman.ui.Sound;

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

	public final Game game;
	public final PacManGameSwingUI ui;
	public final Assets assets;
	public final V2i size;

	private long passed;
	private long mark;
	private float pacManX;
	private float leftmostGhostX;
	private int killedGhost;
	private boolean ghostsChasingPacMan;

	public IntroScene(Game game, PacManGameSwingUI ui, Assets assets, V2i size) {
		this.game = game;
		this.ui = ui;
		this.assets = assets;
		this.size = size;
		reset();
	}

	public void reset() {
		passed = 0;
		mark = 0;
		pacManX = size.x;
		leftmostGhostX = pacManX + 24;
		killedGhost = -1;
		ghostsChasingPacMan = true;
	}

	public void mute() {
		ui.stopSound(Sound.SIREN_1);
		ui.stopSound(Sound.PACMAN_POWER);
		ui.stopSound(Sound.GHOST_DEATH);
	}

	public void draw(Graphics2D g) {
		g.setFont(assets.scoreFont);

		mark = 1;
		if (passed >= game.clock.sec(mark)) {
			int w = assets.imageLogo.getWidth();
			g.drawImage(assets.imageLogo, (size.x - w) / 2, 3, null);
		}

		mark += 1;
		if (passed >= game.clock.sec(mark)) {
			g.setColor(Color.WHITE);
			drawCenteredText(g, "CHARACTER / NICKNAME", 8 * TS, size.x);
		}

		mark += 1;
		for (int ghost = 0; ghost <= 3; ++ghost) {
			int y = t(10 + 3 * ghost);
			if (passed == game.clock.sec(mark)) {
				ui.playSound(Sound.CREDIT);
			}
			if (passed >= game.clock.sec(mark)) {
				BufferedImage ghostLookingRight = assets.sheet(0, 4 + ghost);
				g.drawImage(ghostLookingRight, t(2) - 3, y - 2, null);
			}
			if (passed >= game.clock.sec(mark + 0.5f)) {
				String character = (String) GHOST_INTRO_TEXTS[ghost][0];
				String nickname = (String) GHOST_INTRO_TEXTS[ghost][1];
				Color color = (Color) GHOST_INTRO_TEXTS[ghost][2];
				String text = "-";
				text += passed > game.clock.sec(mark + 1) ? character + "    " + nickname : character;
				g.setColor(color);
				g.drawString(text, t(4), y + 11);
			}
			mark += 2;
		}

		if (passed >= game.clock.sec(mark)) {
			g.setColor(Color.PINK);
			g.fillRect(t(9) + 6, t(27) + 2, 2, 2);
			game.clock.runOrBeIdle(20, () -> {
				g.fillOval(t(9), t(29) - 2, 10, 10);
			});
			g.setColor(Color.WHITE);
			g.drawString("10", t(12), t(28));
			g.drawString("50", t(12), t(30));
			g.setFont(assets.scoreFont.deriveFont(6f));
			g.drawString("PTS", t(15), t(28));
			g.drawString("PTS", t(15), t(30));
		}

		mark += 1;
		if (passed == game.clock.sec(mark)) {
			ui.loopSound(Sound.SIREN_1);
		}

		if (passed >= game.clock.sec(mark)) {
			if (ghostsChasingPacMan) {
				drawGhostsChasingPacMan(g);
			} else {
				drawPacManChasingGhosts(g);
			}
		}

		mark += 1;
		if (passed >= game.clock.sec(mark)) {
			g.setColor(Color.ORANGE);
			g.setFont(assets.scoreFont);
			game.clock.runOrBeIdle(20, () -> {
				drawCenteredText(g, "Press any key to play!", size.y - 20, size.x);
			});
		}

		++passed;
		if (passed == game.clock.sec(30)) {
			reset();
		}
	}

	private void drawGhostsChasingPacMan(Graphics2D g) {
		int y = t(22);
		game.clock.runOrBeIdle(20, () -> {
			g.setColor(Color.PINK);
			g.fillOval(t(2), y + 2, 10, 10);
		});
		g.drawImage(pacManWalking(LEFT), (int) pacManX, y, null);
		for (int ghost = 0; ghost < 4; ++ghost) {
			g.drawImage(ghostWalking(LEFT, ghost), (int) leftmostGhostX + 16 * ghost, y, null);
		}
		if (pacManX > t(2)) {
			pacManX -= 0.8f;
			leftmostGhostX -= 0.8f;
		} else {
			ghostsChasingPacMan = false;
			ui.stopSound(Sound.SIREN_1);
			ui.loopSound(Sound.PACMAN_POWER);
		}
	}

	private void drawPacManChasingGhosts(Graphics2D g) {
		int y = t(22);
		BufferedImage frightenedGhostImage = ghostFrightened();
		for (int ghost = 0; ghost < 4; ++ghost) {
			int x = (int) leftmostGhostX + ghost * 16;
			if (pacManX < x) {
				g.drawImage(frightenedGhostImage, x, y, null);
			} else if (pacManX > x && pacManX <= x + 16) {
				int bounty = (int) Math.pow(2, ghost) * 200;
				g.drawImage(assets.numbers.get(bounty), x, y, null);
				if (killedGhost != ghost) {
					killedGhost++;
					ui.playSound(Sound.GHOST_DEATH);
				}
			}
		}
		g.drawImage(pacManWalking(RIGHT), (int) pacManX, y, null);
		if (pacManX < size.x) {
			pacManX += 0.6f;
			leftmostGhostX += 0.3f;
		} else {
			ui.stopSound(Sound.PACMAN_POWER);
		}
	}

	private BufferedImage pacManWalking(Direction dir) {
		int frame = game.clock.frame(5, 3);
		return frame == 2 ? assets.sheet(frame, 0) : assets.sheet(frame, dirIndex(dir));
	}

	private BufferedImage ghostWalking(Direction dir, int ghost) {
		return assets.sheet(2 * dirIndex(dir) + game.clock.frame(5, 2), 4 + ghost);
	}

	private BufferedImage ghostFrightened() {
		return assets.sheet(8 + game.clock.frame(5, 2), 4);
	}
}