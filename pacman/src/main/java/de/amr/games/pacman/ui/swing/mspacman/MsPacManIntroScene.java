package de.amr.games.pacman.ui.swing.mspacman;

import static de.amr.games.pacman.game.worlds.PacManGameWorld.t;
import static de.amr.games.pacman.lib.Direction.UP;
import static de.amr.games.pacman.ui.swing.PacManGameSwingUI.TEXTS;
import static de.amr.games.pacman.ui.swing.mspacman.MsPacManAssets.DIR_INDEX;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.util.Arrays;

import de.amr.games.pacman.game.core.PacManGame;
import de.amr.games.pacman.lib.Direction;
import de.amr.games.pacman.lib.V2i;
import de.amr.games.pacman.ui.api.PacManGameSound;
import de.amr.games.pacman.ui.swing.scene.PacManGameScene;

public class MsPacManIntroScene implements PacManGameScene {

	private static final Color[] GHOST_COLORS = { Color.RED, Color.PINK, Color.CYAN, Color.ORANGE };

	private final PacManGame game;
	private final V2i size;
	private final MsPacManAssets assets;

	public MsPacManIntroScene(PacManGame game, V2i size, MsPacManAssets assets) {
		this.game = game;
		this.size = size;
		this.assets = assets;
	}

	@Override
	public V2i size() {
		return size;
	}

	private BufferedImage ghostWalking(Direction dir, int ghostID, boolean animated) {
		if (animated) {
			int frame = game.clock.frame(5, 2);
			return assets.section(2 * DIR_INDEX.get(dir) + frame, 4 + ghostID);
		} else {
			return assets.section(2 * DIR_INDEX.get(dir), 4 + ghostID);
		}
	}

	private BufferedImage pacWalking(Direction dir, boolean animated) {
		if (animated) {
			int frame = game.clock.frame(5, 3);
			return assets.section(frame, DIR_INDEX.get(dir));
		} else {
			return assets.section(1, DIR_INDEX.get(dir));
		}
	}

	private final long animationStart = 60;
	private final V2i frameSize = new V2i(t(6), t(8));
	private final float speed = 0.5f;
	private final int ghostTargetX = frameSize.x - 24;
	private int[] ghostX = new int[4];
	private int[] ghostY = new int[4];
	private Direction[] ghostDirection = new Direction[4];
	private boolean[] ghostWalking = new boolean[4];
	private boolean[] ghostReachedTarget = new boolean[4];
	private boolean pacWalking;
	private boolean pacReachedTarget;
	private int pacX, pacY;

	@Override
	public void start() {
		Arrays.fill(ghostX, size.x);
		Arrays.fill(ghostY, t(17));
		Arrays.fill(ghostWalking, false);
		Arrays.fill(ghostDirection, Direction.LEFT);
		Arrays.fill(ghostReachedTarget, false);
		pacWalking = false;
		pacReachedTarget = false;
		pacX = size.x;
		pacY = t(17);
		game.state.resetTimer();
	}

	@Override
	public void draw(Graphics2D g, Graphics2D unscaledGC) {

		if (game.state.tick() < animationStart) {
			return;
		}

		// run animation

		long animationTime = game.state.tick() - animationStart;
		if (animationTime == animationStart) {
			ghostWalking[0] = true;
			game.ui.playSound(PacManGameSound.CREDIT);
		}

		g.setFont(assets.scoreFont);
		g.setColor(Color.ORANGE);
		drawCenteredText(g, "\"MS PAC-MAN\"", t(5));
		drawFrame(g, Color.RED, game.clock.frame(4, 8));

		for (int ghost = 0; ghost <= 3; ++ghost) {

			if (ghostReachedTarget[ghost]) {
				g.drawImage(ghostWalking(UP, ghost, false), ghostTargetX, frameSize.y + 16 * ghost, null);
				continue;
			}

			if (ghostWalking[ghost]) {
				g.setFont(assets.scoreFont);

				// display text
				if (ghost == 0) {
					g.setColor(Color.WHITE);
					g.drawString("WITH", t(8), t(11));
				}
				g.setColor(GHOST_COLORS[ghost]);
				drawCenteredText(g, game.world.ghostName(ghost), t(14));

				// walk
				if (ghostDirection[ghost] == Direction.LEFT) {
					ghostX[ghost] -= speed;
				} else if (ghostDirection[ghost] == Direction.UP) {
					ghostY[ghost] -= speed;
				}
				if (ghostX[ghost] == ghostTargetX) {
					ghostDirection[ghost] = Direction.UP;
				}
				BufferedImage sprite = ghostWalking(ghostDirection[ghost], ghost, !ghostReachedTarget[ghost]);
				g.drawImage(sprite, ghostX[ghost], ghostY[ghost], null);

				// target reached?
				if (ghostY[ghost] <= frameSize.y + ghost * 16) {
					ghostReachedTarget[ghost] = true;
					ghostWalking[ghost] = false;
					if (ghost < 3) {
						ghostWalking[ghost + 1] = true;
						game.ui.playSound(PacManGameSound.CREDIT);
					} else {
						pacWalking = true;
						game.ui.loopSound(PacManGameSound.PACMAN_MUNCH);
					}
				}
			}
		}

		if (ghostReachedTarget[3]) {
			g.setColor(Color.WHITE);
			g.drawString("STARRING", t(8), t(11));
			g.setColor(Color.YELLOW);
			g.drawString("MS PAC-MAN", t(11), t(14));
		}

		if (pacWalking) {
			pacX -= speed;
		}
		if (pacX <= t(13)) {
			pacWalking = false;
			pacReachedTarget = true;
		}
		if (pacWalking) {
			g.drawImage(pacWalking(Direction.LEFT, true), pacX, pacY, null);
		} else if (pacReachedTarget) {
			g.drawImage(pacWalking(Direction.LEFT, false), pacX, pacY, null);
		}

		if (pacReachedTarget) {
			drawPointsAnimation(g, 26);
			drawPressKeyToStart(g);
			game.ui.stopAllSounds();
		}

		// restart intro after 30 seconds
		if (animationTime == game.clock.sec(30)) {
			start();
		}
	}

	private void drawFrame(Graphics2D g, Color color, int light) {
		int dotsX = 32, dotsY = 16;
		int dot = light;
		for (int i = 0; i < dotsX; ++i) {
			g.setColor(dot % 8 == 0 ? Color.WHITE : Color.RED);
			g.fillRect(frameSize.x + 4 * i, frameSize.y, 2, 2);
			++dot;
		}
		for (int i = 0; i < dotsY; ++i) {
			g.setColor(dot % 8 == 0 ? Color.WHITE : Color.RED);
			g.fillRect(frameSize.x + 4 * dotsX, frameSize.y + 4 * i, 2, 2);
			++dot;
		}
		for (int i = dotsX; i >= 0; --i) {
			g.setColor(dot % 8 == 0 ? Color.WHITE : Color.RED);
			g.fillRect(frameSize.x + 4 * i, frameSize.y + 4 * dotsY, 2, 2);
			++dot;
		}
		for (int i = dotsY - 1; i >= 0; --i) {
			g.setColor(dot % 8 == 0 ? Color.WHITE : Color.RED);
			g.fillRect(frameSize.x, frameSize.y + 4 * i, 2, 2);
			++dot;
		}
	}

	private void drawPressKeyToStart(Graphics2D g) {
		g.setColor(Color.ORANGE);
		g.setFont(assets.scoreFont);
		game.clock.runOrBeIdle(20, () -> {
			drawCenteredText(g, TEXTS.getString("PRESS_KEY_TO_PLAY"), size.y - 20);
		});
	}

	private void drawPointsAnimation(Graphics2D g, int yTile) {
		g.setColor(Color.PINK);
		g.fillRect(t(9) + 6, t(yTile - 1) + 2, 2, 2);
		game.clock.runOrBeIdle(20, () -> {
			g.fillOval(t(9), t(yTile + 1) - 2, 10, 10);
		});
		g.setColor(Color.WHITE);
		g.setFont(assets.scoreFont);
		g.drawString("10", t(12), t(yTile));
		g.drawString("50", t(12), t(yTile + 2));
		g.setFont(assets.scoreFont.deriveFont(6f));
		g.drawString(TEXTS.getString("POINTS"), t(15), t(yTile));
		g.drawString(TEXTS.getString("POINTS"), t(15), t(yTile + 2));
	}

}