package de.amr.games.pacman.ui.swing.mspacman;

import static de.amr.games.pacman.lib.Direction.LEFT;
import static de.amr.games.pacman.lib.Direction.RIGHT;
import static de.amr.games.pacman.ui.swing.mspacman.MsPacManAssets.DIR_INDEX;
import static de.amr.games.pacman.world.PacManGameWorld.t;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.util.ResourceBundle;
import java.util.stream.IntStream;

import de.amr.games.pacman.core.PacManGame;
import de.amr.games.pacman.lib.Direction;
import de.amr.games.pacman.lib.V2i;
import de.amr.games.pacman.ui.api.Sound;
import de.amr.games.pacman.ui.swing.PacManGameScene;

/**
 * Intro presenting the ghosts and showing the chasing animations.
 * 
 * TODO: implement the real Ms. Pac-Man intro
 * 
 * @author Armin Reichert
 */
public class MsPacManIntroScene extends PacManGameScene {

	private final MsPacManAssets assets;
	private final ResourceBundle resources = ResourceBundle.getBundle("localization.translation");
	private float pacManX;
	private float leftmostGhostX;
	private int lastKilledGhost;
	private boolean ghostsChasingPacMan;

	public MsPacManIntroScene(PacManGame game, V2i size, MsPacManAssets assets) {
		super(game, size);
		this.assets = assets;
		bgColor = new Color(10, 10, 10);
	}

	@Override
	public void start() {
		game.state.resetTimer();
		pacManX = size.x;
		leftmostGhostX = pacManX + 24;
		lastKilledGhost = -1;
		ghostsChasingPacMan = true;
	}

	@Override
	public void end() {
		game.ui.stopSound(Sound.SIREN_1);
		game.ui.stopSound(Sound.PACMAN_POWER);
		game.ui.stopSound(Sound.GHOST_DEATH);
	}

	@Override
	public void draw(Graphics2D g, Graphics2D unscaledGC) {
		game.state.runAfter(game.clock.sec(1), () -> drawLogo(unscaledGC, g.getTransform().getScaleX()));

		game.state.runAfter(game.clock.sec(2), () -> {
			g.setColor(Color.WHITE);
			g.setFont(assets.scoreFont);
			drawCenteredText(g, resources.getString("CHARACTER_NICKNAME"), t(8));
		});

		IntStream.rangeClosed(0, 3).forEach(ghost -> {
			int ghostStart = 3 + 2 * ghost;
			int y = t(10 + 3 * ghost);
			game.state.runAt(game.clock.sec(ghostStart), () -> {
				game.ui.playSound(Sound.CREDIT);
			});
			game.state.runAfter(game.clock.sec(ghostStart), () -> {
				g.drawImage(assets.section(0, 4 + ghost), t(2) - 3, y - 2, null);
			});
			game.state.runAfter(game.clock.sec(ghostStart + 0.5), () -> {
				drawGhostCharacterAndName(g, ghost, y, false);
			});
			game.state.runAfter(game.clock.sec(ghostStart + 1), () -> {
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

		game.state.runAt(game.clock.sec(30), this::start);
	}

	private void drawLogo(Graphics2D g, double scaling) {
		g.drawImage(assets.gameLogo, (int) (size.x * scaling - assets.gameLogo.getWidth()) / 2, (int) (3 * scaling), null);
	}

	private void drawPressKeyToStart(Graphics2D g) {
		g.setColor(Color.ORANGE);
		g.setFont(assets.scoreFont);
		game.clock.runOrBeIdle(20, () -> {
			drawCenteredText(g, resources.getString("PRESS_ANY_KEY_TO_PLAY"), size.y - 20);
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
		g.drawString(resources.getString("POINTS"), t(15), t(28));
		g.drawString(resources.getString("POINTS"), t(15), t(30));
	}

	private void drawGhostCharacterAndName(Graphics2D g, int ghostID, int y, boolean both) {
		String character = resources.getString("MSPACMAN.GHOST." + ghostID + ".CHARACTER");
		String nickname = "\"" + resources.getString("MSPACMAN.GHOST." + ghostID + ".NICKNAME") + "\"";
		Color color = GHOST_COLORS[ghostID];
		g.setColor(color);
		g.setFont(assets.scoreFont);
		g.drawString("-" + character, t(4), y + 11);
		if (both) {
			g.drawString(nickname, t(15), y + 11);
		}
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
				int bounty = (int) (Math.pow(2, ghost) * 200);
				g.drawImage(assets.bountyNumbers.get(bounty), x, y, null);
				if (lastKilledGhost != ghost) {
					lastKilledGhost++;
					game.ui.playSound(Sound.GHOST_DEATH);
				}
			}
		}
		g.drawImage(pacManWalkingSprite(RIGHT), (int) pacManX, y, null);
		if (pacManX < size.x) {
			pacManX += 0.6f;
			leftmostGhostX += 0.3f;
		} else {
			game.ui.stopSound(Sound.PACMAN_POWER);
		}
	}

	private BufferedImage pacManWalkingSprite(Direction dir) {
		int frame = game.clock.frame(5, 3);
		return frame == 2 ? assets.section(frame, 0) : assets.section(frame, DIR_INDEX.get(dir));
	}

	private BufferedImage ghostWalkingSprite(Direction dir, int ghost) {
		int frame = game.clock.frame(5, 2);
		return assets.section(2 * DIR_INDEX.get(dir) + frame, 4 + ghost);
	}

	private BufferedImage ghostFrightenedSprite() {
		int frame = game.clock.frame(5, 2);
		return assets.section(8 + frame, 4);
	}
}