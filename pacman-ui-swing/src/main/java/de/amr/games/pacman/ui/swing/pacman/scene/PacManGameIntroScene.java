package de.amr.games.pacman.ui.swing.pacman.scene;

import static de.amr.games.pacman.heaven.God.clock;
import static de.amr.games.pacman.heaven.God.differsAtMost;
import static de.amr.games.pacman.lib.Direction.LEFT;
import static de.amr.games.pacman.lib.Direction.RIGHT;
import static de.amr.games.pacman.model.creatures.GhostState.FRIGHTENED;
import static de.amr.games.pacman.world.PacManGameWorld.TS;
import static de.amr.games.pacman.world.PacManGameWorld.t;

import java.awt.Color;
import java.awt.Graphics2D;
import java.util.stream.IntStream;

import de.amr.games.pacman.lib.Animation;
import de.amr.games.pacman.lib.V2f;
import de.amr.games.pacman.lib.V2i;
import de.amr.games.pacman.model.AbstractPacManGame;
import de.amr.games.pacman.model.creatures.Ghost;
import de.amr.games.pacman.model.creatures.Pac;
import de.amr.games.pacman.ui.swing.PacManGameScene;
import de.amr.games.pacman.ui.swing.pacman.rendering.PacManGameSpriteBasedRendering;

/**
 * Intro presenting the ghosts and showing the chasing animations.
 * 
 * @author Armin Reichert
 */
public class PacManGameIntroScene implements PacManGameScene {

	private final V2i size;
	private final PacManGameSpriteBasedRendering rendering;
	private final AbstractPacManGame game;

	private final Animation<Boolean> blinking = Animation.pulse().frameDuration(30);
	private final int headingTileY = 6;
	private final int chaseTileY = 20;
	private final int gapBetweenGhosts = 1;
	private final float pacSpeed = 1.1f;
	private final float ghostSpeedWhenChasing = pacSpeed * 1.07f;
	private final float ghostSpeedWhenFleeing = pacSpeed * 0.5f;
	private final long ghostDyingDuration = 20;

	private boolean chasingPac;
	private Ghost ghostDying;
	private long ghostDyingTimer;
	private long completedTime;

	public PacManGameIntroScene(V2i size, PacManGameSpriteBasedRendering rendering, AbstractPacManGame game) {
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
		chasingPac = true;
		ghostDying = null;
		ghostDyingTimer = 0;

		game.pac.visible = true;
		game.pac.position = new V2f(size.x, t(chaseTileY));
		game.pac.dir = LEFT;
		game.pac.speed = 0;
		game.pac.couldMove = true;
		game.pac.dead = false;

		game.ghosts().forEach(ghost -> {
			ghost.visible = true;
			ghost.position = new V2f(size.x + 32 + (16 + gapBetweenGhosts) * ghost.id, t(chaseTileY));
			ghost.wishDir = ghost.dir = LEFT;
			ghost.speed = 0;
			ghost.couldMove = true;
			ghost.bounty = 0;
			ghost.state = null;
		});

		rendering.resetAllAnimations(game);
	}

	@Override
	public void end() {
		blinking.stop();
		game.state.resetTimer();
	}

	@Override
	public void draw(Graphics2D g) {
		rendering.drawScore(g, game);

		g.setColor(Color.WHITE);
		g.setFont(rendering.assets.getScoreFont());
		g.drawString(rendering.translations.getString("CHARACTER_NICKNAME"), t(6), t(headingTileY));

		// Introduce ghosts
		IntStream.range(0, 4).forEach(id -> {
			int ghostIntroStartSeconds = 1 + 2 * id;
			int ghostY = t(headingTileY + 1 + 3 * id);
			game.state.runAfter(clock.sec(ghostIntroStartSeconds), () -> {
				introduceGhost(g, id, ghostY, false, false);
			});
			game.state.runAfter(clock.sec(ghostIntroStartSeconds + 0.5), () -> {
				introduceGhost(g, id, ghostY, true, false);
			});
			game.state.runAfter(clock.sec(ghostIntroStartSeconds + 1), () -> {
				introduceGhost(g, id, ghostY, true, true);
			});
		});

		game.state.runAfter(clock.sec(10), () -> {
			showPointsAnimation(g, 24);
		});

		game.state.runAt(clock.sec(11), () -> {
			blinking.restart();
			rendering.pacMunching().forEach(Animation::restart);
			rendering.ghostsKicking(game.ghosts()).forEach(Animation::restart);
			game.pac.speed = pacSpeed;
			game.ghosts().forEach(ghost -> ghost.speed = ghostSpeedWhenChasing);
		});

		game.state.runAfter(clock.sec(11), () -> {
			if (chasingPac) {
				showGhostsChasingPacMan(g, game.pac);
			} else if (completedTime < 0) {
				showPacManChasingGhosts(g, game.pac);
			}
		});

		if (completedTime > 0) {
			showPressKeyToStart(g);
			game.state.runAt(completedTime + clock.sec(3), () -> {
				end();
				game.attractMode = true;
			});
		}
	}

	private void introduceGhost(Graphics2D g, int id, int y, boolean showCharacter, boolean showNickname) {
		Graphics2D g2 = rendering.smoothGC(g);
		g2.drawImage(rendering.assets.ghostImage(id, RIGHT), t(3), y - 4, null);
		if (showCharacter) {
			String character = rendering.translations.getString("CLASSIC.GHOST." + id + ".CHARACTER");
			Color color = rendering.assets.ghostColor(id);
			g2.setColor(color);
			g2.setFont(rendering.assets.getScoreFont());
			g2.drawString("-" + character, t(6), y + 8);
		}
		if (showNickname) {
			String nickname = "\"" + rendering.translations.getString("CLASSIC.GHOST." + id + ".NICKNAME") + "\"";
			Color color = rendering.assets.ghostColor(id);
			g2.setColor(color);
			g2.setFont(rendering.assets.getScoreFont());
			g2.drawString(nickname, t(17), y + 8);
		}
		g2.dispose();
	}

	private void showGhostsChasingPacMan(Graphics2D g, Pac pac) {
		if (blinking.animate()) {
			g.setColor(Color.PINK);
			g.fillOval(t(2), t(chaseTileY), TS, TS);
		}
		pac.move();
		game.ghosts().forEach(Ghost::move);
		if (differsAtMost(pac.position.x, 2 * TS, 1) && pac.dir == LEFT) {
			// let Pac turn around
			chasingPac = false;
			pac.dir = RIGHT;
			game.ghosts().forEach(ghost -> {
				ghost.dir = ghost.wishDir = RIGHT;
				ghost.speed = ghostSpeedWhenFleeing;
				ghost.state = FRIGHTENED;
				rendering.ghostFrightened(ghost).forEach(Animation::restart);
			});
		}
		rendering.drawGuy(g, pac, game);
		game.ghosts().forEach(ghost -> {
			rendering.drawGuy(g, ghost, game);
		});
	}

	private void showPacManChasingGhosts(Graphics2D g, Pac pac) {
		if (pac.position.x > size.x + 20) {
			completedTime = game.state.ticksRun();
			return;
		}
		for (Ghost ghost : game.ghosts) {
			if (ghost.bounty == 0 && pac.meets(ghost)) {
				ghostDying = ghost;
				ghostDyingTimer = ghostDyingDuration;
				ghost.bounty = (int) (Math.pow(2, ghost.id) * 200);
				pac.position = new V2f(pac.position.x - 32, pac.position.y);
				pac.visible = false;
				game.ghosts().forEach(gh -> gh.speed = 0f);
				break;
			}
		}
		if (ghostDyingTimer > 0) {
			ghostDyingTimer--;
			if (ghostDyingTimer == 0) {
				pac.visible = true;
				ghostDying.visible = false;
				ghostDying = null;
				game.ghosts().forEach(ghost -> ghost.speed = game.level.ghostSpeedFrightened);
			}
		}
		pac.move();
		game.ghosts().forEach(Ghost::move);
		rendering.drawGuy(g, pac, game);
		for (Ghost ghost : game.ghosts) {
			rendering.drawGuy(g, ghost, game);
		}
	}

	private void showPressKeyToStart(Graphics2D g) {
		g.setColor(Color.ORANGE);
		g.setFont(rendering.assets.getScoreFont());
		if (blinking.animate()) {
			drawHCenteredText(g, rendering.translations.getString("PRESS_KEY_TO_PLAY"), t(31));
		}
	}

	private void showPointsAnimation(Graphics2D g, int tileY) {
		g.setColor(Color.PINK);
		g.fillRect(t(9) + 6, t(tileY) + 2, 2, 2);
		if (blinking.animate()) {
			g.fillOval(t(9), t(tileY + 2) - 2, 10, 10);
		}
		g.setColor(Color.WHITE);
		g.setFont(rendering.assets.getScoreFont());
		g.drawString("10", t(12), t(tileY + 1));
		g.drawString("50", t(12), t(tileY + 3));
		g.setFont(rendering.assets.getScoreFont().deriveFont(6f));
		g.drawString(rendering.translations.getString("POINTS"), t(15), t(tileY + 1));
		g.drawString(rendering.translations.getString("POINTS"), t(15), t(tileY + 3));
	}
}