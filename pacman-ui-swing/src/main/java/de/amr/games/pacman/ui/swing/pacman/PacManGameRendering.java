package de.amr.games.pacman.ui.swing.pacman;

import static de.amr.games.pacman.model.GhostState.DEAD;
import static de.amr.games.pacman.model.GhostState.ENTERING_HOUSE;
import static de.amr.games.pacman.model.GhostState.FRIGHTENED;
import static de.amr.games.pacman.model.GhostState.LOCKED;
import static de.amr.games.pacman.world.PacManGameWorld.t;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;

import de.amr.games.pacman.controller.PacManGameState;
import de.amr.games.pacman.lib.Animation;
import de.amr.games.pacman.lib.Direction;
import de.amr.games.pacman.lib.V2i;
import de.amr.games.pacman.model.Bonus;
import de.amr.games.pacman.model.Ghost;
import de.amr.games.pacman.model.Pac;
import de.amr.games.pacman.model.PacManGameModel;
import de.amr.games.pacman.ui.swing.assets.Spritesheet;
import de.amr.games.pacman.ui.swing.rendering.GameRenderingUsingAnimatedSprites;

/**
 * Rendering for the classic Pac-Man game.
 * 
 * @author Armin Reichert
 */
class PacManGameRendering extends GameRenderingUsingAnimatedSprites {

	public final PacManGameAssets assets;

	public PacManGameRendering() {
		assets = new PacManGameAssets();
	}

	@Override
	public Spritesheet spritesheet() {
		return assets;
	}

	@Override
	public Animation<BufferedImage> pacMunchingToDir(Direction dir) {
		return assets.pacMunchingAnimByDir.get(dir);
	}

	@Override
	public Animation<BufferedImage> pacDying() {
		return assets.pacCollapsingAnim;
	}

	@Override
	public Animation<BufferedImage> ghostKickingToDir(Ghost ghost, Direction dir) {
		return assets.ghostsWalkingAnimsByGhost.get(ghost.id).get(dir);
	}

	@Override
	public Animation<BufferedImage> ghostFrightenedToDir(Ghost ghost, Direction dir) {
		return assets.ghostBlueAnim;
	}

	@Override
	public Animation<BufferedImage> ghostFlashing() {
		return assets.ghostFlashingAnim;
	}

	@Override
	public Animation<BufferedImage> ghostReturningHomeToDir(Ghost ghost, Direction dir) {
		return assets.ghostEyesAnimsByDir.get(dir);
	}

	@Override
	public Animation<BufferedImage> mazeFlashing(int mazeNumber) {
		return assets.mazeFlashingAnim;
	}

	@Override
	public Animation<Boolean> energizerBlinking() {
		return assets.energizerBlinkingAnim;
	}

	@Override
	public BufferedImage bonusSprite(Bonus bonus, PacManGameModel game) {
		if (bonus.edibleTicksLeft > 0) {
			return assets.spriteAt(assets.symbolTiles[bonus.symbol]);
		}
		if (bonus.eatenTicksLeft > 0) {
			return assets.numberSprites.get(bonus.points);
		}
		return null;
	}

	@Override
	public BufferedImage lifeSprite() {
		return assets.spriteAt(8, 1);
	}

	@Override
	public BufferedImage pacSprite(Pac pac, PacManGameModel game) {
		if (pac.dead) {
			return pacDying().hasStarted() ? pacDying().animate() : pacMunchingToDir(pac.dir).frame();
		}
		if (pac.speed == 0) {
			return pacMunchingToDir(pac.dir).frame(0);
		}
		if (!pac.couldMove) {
			return pacMunchingToDir(pac.dir).frame(1);
		}
		return pacMunchingToDir(pac.dir).animate();
	}

	@Override
	public BufferedImage ghostSprite(Ghost ghost, PacManGameModel game) {
		if (ghost.bounty > 0) {
			return assets.numberSprites.get(ghost.bounty);
		}
		if (ghost.is(DEAD) || ghost.is(ENTERING_HOUSE)) {
			return ghostReturningHomeToDir(ghost, ghost.dir).animate();
		}
		if (ghost.is(FRIGHTENED)) {
			return ghostFlashing().isRunning() ? ghostFlashing().frame() : ghostFrightenedToDir(ghost, ghost.dir).animate();
		}
		if (ghost.is(LOCKED) && game.pac.powerTicksLeft > 0) {
			return ghostFrightenedToDir(ghost, ghost.dir).animate();
		}
		return ghostKickingToDir(ghost, ghost.wishDir).animate(); // Looks towards wish dir!
	}

	@Override
	public void drawFullMaze(Graphics2D g, PacManGameModel game, int mazeNumber, int x, int y) {
		g.drawImage(assets.mazeFullImage, x, y, null);
	}

	@Override
	public void drawEmptyMaze(Graphics2D g, PacManGameModel game, int mazeNumber, int x, int y) {
		g.drawImage(assets.mazeEmptyImage, x, y, null);
	}

	@Override
	public void drawScore(Graphics2D g, PacManGameModel game, int x, int y) {
		g.setFont(assets.scoreFont);
		g.translate(0, assets.scoreFont.getSize() + 1);
		g.setColor(Color.WHITE);
		g.drawString(translations.getString("SCORE"), x, y);
		g.translate(0, 1);
		if (game.state != PacManGameState.INTRO && !game.attractMode) {
			g.setColor(Color.YELLOW);
			g.drawString(String.format("%08d", game.score), x, y + t(1));
			g.setColor(Color.LIGHT_GRAY);
			g.drawString(String.format("L%02d", game.currentLevelNumber), x + t(8), y + t(1));
		}
		g.translate(0, -(assets.scoreFont.getSize() + 2));
	}

	@Override
	public void drawHiScore(Graphics2D g, PacManGameModel game, int x, int y) {
		g.setFont(assets.scoreFont);
		g.translate(0, assets.scoreFont.getSize() + 1);
		g.setColor(Color.WHITE);
		g.drawString(translations.getString("HI_SCORE"), x, y);
		g.translate(0, 1);
		if (game.state != PacManGameState.INTRO && !game.attractMode) {
			g.setColor(Color.YELLOW);
			g.drawString(String.format("%08d", game.highscorePoints), x, y + t(1));
			g.setColor(Color.LIGHT_GRAY);
			g.drawString(String.format("L%02d", game.highscoreLevel), x + t(8), y + t(1));
		}
		g.translate(0, -(assets.scoreFont.getSize() + 2));
	}

	@Override
	public void drawLevelCounter(Graphics2D g, PacManGameModel game, int rightX, int y) {
		Graphics2D g2 = smoothGC(g);
		int x = rightX;
		int firstLevelNumber = Math.max(1, game.currentLevelNumber - 6);
		for (int levelNumber = firstLevelNumber; levelNumber <= game.currentLevelNumber; ++levelNumber) {
			V2i symbolTile = assets.symbolTiles[game.levelSymbols.get(levelNumber - 1)];
			g2.drawImage(assets.spriteAt(symbolTile), x, y, null);
			x -= t(2);
		}
		g2.dispose();
	}

}