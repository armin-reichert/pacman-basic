package de.amr.games.pacman.ui.swing;

import static de.amr.games.pacman.core.World.t;
import static de.amr.games.pacman.lib.Direction.LEFT;
import static de.amr.games.pacman.lib.Direction.RIGHT;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.util.stream.IntStream;

import de.amr.games.pacman.core.Game;
import de.amr.games.pacman.lib.Direction;
import de.amr.games.pacman.ui.Sound;

/**
 * Intro presenting the ghosts and showing the chasing animations.
 * 
 * @author Armin Reichert
 */
class IntroScene extends Scene {

	//@formatter:off
	static final Object[][] GHOST_INTRO_TEXTS = {
		{ "SHADOW ", "\"BLINKY\"", Color.RED }, 
		{ "SPEEDY ", "\"PINKY\"",  Color.PINK },
		{ "BASHFUL", "\"INKY\"",   Color.CYAN }, 
		{ "POKEY  ", "\"CLYDE\"",  Color.ORANGE }
	};
	//@formatter:on

	private float pacManX;
	private float leftmostGhostX;
	private int killedGhost;
	private boolean ghostsChasingPacMan;

	public IntroScene(Game game, Assets assets, Dimension size) {
		super(game, assets, size);
	}

	public void reset() {
		game.state.resetTimer();
		pacManX = size.width;
		leftmostGhostX = pacManX + 24;
		killedGhost = -1;
		ghostsChasingPacMan = true;
	}

	public void mute() {
		game.ui.stopSound(Sound.SIREN_1);
		game.ui.stopSound(Sound.PACMAN_POWER);
		game.ui.stopSound(Sound.GHOST_DEATH);
	}

	public void draw(Graphics2D g) {

		game.state.runAfter(game.clock.sec(1), () -> {
			drawCenteredImage(g, assets.imageLogo, 3);
		});

		game.state.runAfter(game.clock.sec(2), () -> {
			g.setColor(Color.WHITE);
			g.setFont(assets.scoreFont);
			drawCenteredText(g, "CHARACTER / NICKNAME", t(8));
		});

		IntStream.rangeClosed(0, 3).forEach(ghost -> {
			int ghostAnimationStart = 3 + 2 * ghost;
			int y = t(10 + 3 * ghost);
			game.state.runAt(game.clock.sec(ghostAnimationStart), () -> {
				game.ui.playSound(Sound.CREDIT);
			});
			game.state.runAfter(game.clock.sec(ghostAnimationStart), () -> {
				g.drawImage(assets.sheet(0, 4 + ghost), t(2) - 3, y - 2, null);
			});
			game.state.runAfter(game.clock.sec(ghostAnimationStart + 0.5), () -> {
				drawGhostCharacterAndName(g, ghost, y, false);
			});
			game.state.runAfter(game.clock.sec(ghostAnimationStart + 1), () -> {
				drawGhostCharacterAndName(g, ghost, y, true);
			});
		});

		game.state.runAfter(game.clock.sec(12), () -> {
			drawPointsAnimation(g);
		});

		game.state.runAt(game.clock.sec(13), () -> {
			game.ui.loopSound(Sound.SIREN_1);
		});

		game.state.runAfter(game.clock.sec(13), () -> {
			if (ghostsChasingPacMan) {
				drawGhostsChasingPacMan(g);
			} else {
				drawPacManChasingGhosts(g);
			}
		});

		game.state.runAfter(game.clock.sec(14), () -> {
			drawPressKeyToStart(g);
		});

		game.state.runAt(game.clock.sec(30), this::reset);
	}

	private void drawPressKeyToStart(Graphics2D g) {
		g.setColor(Color.ORANGE);
		g.setFont(assets.scoreFont);
		game.clock.runOrBeIdle(20, () -> {
			drawCenteredText(g, "Press any key to play!", size.height - 20);
		});
	}

	private void drawPointsAnimation(Graphics2D g) {
		g.setColor(Color.PINK);
		g.fillRect(t(9) + 6, t(27) + 2, 2, 2);
		game.clock.runOrBeIdle(20, () -> {
			g.fillOval(t(9), t(29) - 2, 10, 10);
		});
		g.setColor(Color.WHITE);
		g.setFont(assets.scoreFont);
		g.drawString("10", t(12), t(28));
		g.drawString("50", t(12), t(30));
		g.setFont(assets.scoreFont.deriveFont(6f));
		g.drawString("PTS", t(15), t(28));
		g.drawString("PTS", t(15), t(30));
	}

	private void drawGhostCharacterAndName(Graphics2D g, int ghostID, int y, boolean both) {
		String character = (String) GHOST_INTRO_TEXTS[ghostID][0];
		String nickname = (String) GHOST_INTRO_TEXTS[ghostID][1];
		Color color = (Color) GHOST_INTRO_TEXTS[ghostID][2];
		String text = both ? character + "    " + nickname : character;
		g.setColor(color);
		g.setFont(assets.scoreFont);
		g.drawString("-" + text, t(4), y + 11);
	}

	private void drawGhostsChasingPacMan(Graphics2D g) {
		int y = t(22);
		game.clock.runOrBeIdle(20, () -> {
			g.setColor(Color.PINK);
			g.fillOval(t(2), y + 2, 10, 10);
		});
		g.drawImage(pacManWalkingSprite(LEFT), (int) pacManX, y, null);
		for (int ghost = 0; ghost < 4; ++ghost) {
			g.drawImage(ghostWalkingSprite(LEFT, ghost), (int) leftmostGhostX + 16 * ghost, y, null);
		}
		if (pacManX > t(2)) {
			pacManX -= 0.8f;
			leftmostGhostX -= 0.8f;
		} else {
			ghostsChasingPacMan = false;
			game.ui.stopSound(Sound.SIREN_1);
			game.ui.loopSound(Sound.PACMAN_POWER);
		}
	}

	private void drawPacManChasingGhosts(Graphics2D g) {
		int y = t(22);
		BufferedImage frightenedGhost = ghostFrightenedSprite();
		for (int ghost = 0; ghost < 4; ++ghost) {
			int x = (int) leftmostGhostX + ghost * 16;
			if (pacManX < x) {
				g.drawImage(frightenedGhost, x, y, null);
			} else if (pacManX > x && pacManX <= x + 16) {
				short bounty = (short) (Math.pow(2, ghost) * 200);
				g.drawImage(assets.numbers.get(bounty), x, y, null);
				if (killedGhost != ghost) {
					killedGhost++;
					game.ui.playSound(Sound.GHOST_DEATH);
				}
			}
		}
		g.drawImage(pacManWalkingSprite(RIGHT), (int) pacManX, y, null);
		if (pacManX < size.width) {
			pacManX += 0.6f;
			leftmostGhostX += 0.3f;
		} else {
			game.ui.stopSound(Sound.PACMAN_POWER);
		}
	}

	private BufferedImage pacManWalkingSprite(Direction dir) {
		int frame = game.clock.frame(5, 3);
		return frame == 2 ? assets.sheet(frame, 0) : assets.sheet(frame, Assets.DIR_INDEX.get(dir));
	}

	private BufferedImage ghostWalkingSprite(Direction dir, int ghost) {
		return assets.sheet(2 * Assets.DIR_INDEX.get(dir) + game.clock.frame(5, 2), 4 + ghost);
	}

	private BufferedImage ghostFrightenedSprite() {
		return assets.sheet(8 + game.clock.frame(5, 2), 4);
	}
}