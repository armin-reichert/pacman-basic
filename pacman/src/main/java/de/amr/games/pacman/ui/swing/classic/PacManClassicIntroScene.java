package de.amr.games.pacman.ui.swing.classic;

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
import de.amr.games.pacman.model.PacManGame;
import de.amr.games.pacman.model.creatures.Ghost;
import de.amr.games.pacman.model.creatures.Pac;
import de.amr.games.pacman.ui.api.PacManGameScene;

/**
 * Intro presenting the ghosts and showing the chasing animations.
 * 
 * @author Armin Reichert
 */
public class PacManClassicIntroScene implements PacManGameScene {

	private final V2i size;
	private final PacManClassicRendering rendering;
	private final PacManGame game;

	private final Animation<Boolean> blinking = Animation.pulse().frameDuration(30);
	private final int headingTileY = 6;
	private final int chaseTileY = 20;
	private final int ghostGap = 18;
	private final float pacSpeedChasing = 1.0f;
	private final float pacSpeedFleeing = 0.95f;
	private final float ghostSpeedChasing = 1.0f;
	private final float ghostSpeedFleeing = 0.5f;

	private boolean chasingPac;
	private Ghost ghostDying;
	private long ghostDyingTimer;
	private long completedTime;

	public PacManClassicIntroScene(V2i size, PacManClassicRendering rendering, PacManGame game) {
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
			ghost.position = new V2f(size.x + 32 + ghostGap * ghost.id, t(chaseTileY));
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
		g.setFont(rendering.assets.scoreFont);
		g.drawString(rendering.translator.apply("CHARACTER_NICKNAME"), t(6), t(headingTileY));

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
			rendering.letPacMunch();
			rendering.letGhostsFidget(game.ghosts(), true);
			game.pac.speed = pacSpeedFleeing;
			game.ghosts().forEach(ghost -> ghost.speed = ghostSpeedChasing);
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
		g.drawImage(rendering.assets.ghostsWalking.get(id).get(RIGHT).frame(), t(3), y - 4, null);
		if (showCharacter) {
			String character = rendering.translator.apply("CLASSIC.GHOST." + id + ".CHARACTER");
			Color color = rendering.assets.ghostColors[id];
			g.setColor(color);
			g.setFont(rendering.assets.scoreFont);
			g.drawString("-" + character, t(6), y + 8);
		}
		if (showNickname) {
			String nickname = "\"" + rendering.translator.apply("CLASSIC.GHOST." + id + ".NICKNAME") + "\"";
			Color color = rendering.assets.ghostColors[id];
			g.setColor(color);
			g.setFont(rendering.assets.scoreFont);
			g.drawString(nickname, t(17), y + 8);
		}
	}

	private void showGhostsChasingPacMan(Graphics2D g, Pac pac) {
		if (blinking.animate()) {
			g.setColor(Color.PINK);
			g.fillOval(t(2), t(chaseTileY), TS, TS);
		}
		pac.moveFreely();
		game.ghosts().forEach(Ghost::moveFreely);
		if (differsAtMost(pac.position.x, 2 * TS, 1) && pac.dir == LEFT) {
			// let Pac turn around
			chasingPac = false;
			pac.dir = RIGHT;
			game.pac.speed = pacSpeedChasing;
			game.ghosts().forEach(ghost -> {
				ghost.dir = ghost.wishDir = RIGHT;
				ghost.speed = ghostSpeedFleeing;
				ghost.state = FRIGHTENED;
				rendering.letGhostBeFrightened(ghost, true);
			});
		}
		rendering.drawPac(g, pac);
		game.ghosts().forEach(ghost -> {
			rendering.drawGhost(g, ghost, game);
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
				ghostDyingTimer = clock.sec(0.6);
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
		pac.moveFreely();
		game.ghosts().forEach(Ghost::moveFreely);
		rendering.drawPac(g, pac);
		for (Ghost ghost : game.ghosts) {
			rendering.drawGhost(g, ghost, game);
		}
	}

	private void showPressKeyToStart(Graphics2D g) {
		g.setColor(Color.ORANGE);
		g.setFont(rendering.assets.scoreFont);
		if (blinking.animate()) {
			drawHCenteredText(g, rendering.translator.apply("PRESS_KEY_TO_PLAY"), size.y - 20);
		}
	}

	private void showPointsAnimation(Graphics2D g, int tileY) {
		g.setColor(Color.PINK);
		g.fillRect(t(9) + 6, t(tileY) + 2, 2, 2);
		if (blinking.animate()) {
			g.fillOval(t(9), t(tileY + 2) - 2, 10, 10);
		}
		g.setColor(Color.WHITE);
		g.setFont(rendering.assets.scoreFont);
		g.drawString("10", t(12), t(tileY + 1));
		g.drawString("50", t(12), t(tileY + 3));
		g.setFont(rendering.assets.scoreFont.deriveFont(6f));
		g.drawString(rendering.translator.apply("POINTS"), t(15), t(tileY + 1));
		g.drawString(rendering.translator.apply("POINTS"), t(15), t(tileY + 3));
	}
}