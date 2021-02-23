package de.amr.games.pacman.ui.swing.rendering;

import static de.amr.games.pacman.model.guys.GhostState.DEAD;
import static de.amr.games.pacman.model.guys.GhostState.ENTERING_HOUSE;
import static de.amr.games.pacman.model.guys.GhostState.FRIGHTENED;
import static de.amr.games.pacman.model.guys.GhostState.LOCKED;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.util.stream.Stream;

import de.amr.games.pacman.lib.Animation;
import de.amr.games.pacman.lib.Direction;
import de.amr.games.pacman.model.guys.Bonus;
import de.amr.games.pacman.model.guys.Ghost;
import de.amr.games.pacman.model.guys.Pac;

/**
 * Rendering for the classic Pac-Man game.
 * 
 * @author Armin Reichert
 */
public class PacMan_Rendering extends DefaultRendering {

	public final PacMan_Assets assets;

	public PacMan_Rendering() {
		assets = new PacMan_Assets();
	}

	@Override
	public Font getScoreFont() {
		return assets.getScoreFont();
	}

	@Override
	public Color getMazeWallBorderColor(int mazeIndex) {
		return new Color(33, 33, 255);
	}

	@Override
	public Color getMazeWallColor(int mazeIndex) {
		return Color.BLACK;
	}

	// Animations

	@Override
	public Animation<Boolean> energizerBlinking() {
		return assets.energizerBlinkingAnim;
	}

	@Override
	public Stream<Animation<?>> mazeFlashings() {
		return Stream.of(assets.mazeFlashingAnim);
	}

	@Override
	public Animation<BufferedImage> mazeFlashing(int mazeNumber) {
		return assets.mazeFlashingAnim;
	}

	@Override
	public Animation<BufferedImage> playerMunching(Pac pac, Direction dir) {
		return assets.getOrCreatePacMunchingAnimation(pac).get(dir);
	}

	@Override
	public Animation<?> spouseMunching(Pac spouse, Direction dir) {
		return null;
	}

	@Override
	public Animation<BufferedImage> playerDying() {
		return assets.pacCollapsingAnim;
	}

	@Override
	public Animation<BufferedImage> ghostKicking(Ghost ghost, Direction dir) {
		return assets.getOrCreateGhostsWalkingAnimation(ghost).get(dir);
	}

	@Override
	public Animation<BufferedImage> ghostFrightened(Ghost ghost, Direction dir) {
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
	public Animation<?> blinkyDamaged() {
		return assets.blinkyDamaged;
	}

	@Override
	public Animation<?> blinkyNaked() {
		return assets.blinkyNaked;
	}

	@Override
	public Animation<?> storkFlying() {
		return null;
	}

	@Override
	public Animation<?> bigPacMan() {
		return assets.bigPacManAnim;
	}

	// draw functions

	@Override
	public void drawMaze(Graphics2D g, int mazeNumber, int x, int y, boolean flashing) {
		if (flashing) {
			g.drawImage(mazeFlashing(mazeNumber).animate(), x, y, null);
		} else {
			g.drawImage(assets.mazeFullImage, x, y, null);
		}
	}

	@Override
	public void drawLifeCounterSymbol(Graphics2D g, int x, int y) {
		g.drawImage(lifeSprite(), x, y, null);
	}

	// Sprites

	@Override
	public BufferedImage bonusSprite(Bonus bonus) {
		if (bonus.edibleTicksLeft > 0) {
			return assets.symbolSprites[bonus.symbol];
		}
		if (bonus.eatenTicksLeft > 0) {
			return assets.numberSprites.get(bonus.points);
		}
		return null;
	}

	@Override
	public BufferedImage lifeSprite() {
		return assets.sprite(8, 1);
	}

	@Override
	protected BufferedImage symbolSprite(byte symbol) {
		return assets.symbolSprites[symbol];
	}

	@Override
	public BufferedImage pacSprite(Pac pac) {
		if (pac.dead) {
			return playerDying().hasStarted() ? playerDying().animate() : playerMunching(pac, pac.dir).frame();
		}
		if (pac.speed == 0) {
			return playerMunching(pac, pac.dir).frame(0);
		}
		if (!pac.couldMove) {
			return playerMunching(pac, pac.dir).frame(1);
		}
		return playerMunching(pac, pac.dir).animate();
	}

	@Override
	public BufferedImage ghostSprite(Ghost ghost, boolean frightened) {
		if (ghost.bounty > 0) {
			return assets.numberSprites.get(ghost.bounty);
		}
		if (ghost.is(DEAD) || ghost.is(ENTERING_HOUSE)) {
			return ghostReturningHomeToDir(ghost, ghost.dir).animate();
		}
		if (ghost.is(FRIGHTENED)) {
			return ghostFlashing().isRunning() ? ghostFlashing().frame() : ghostFrightened(ghost, ghost.dir).animate();
		}
		if (ghost.is(LOCKED) && frightened) {
			return ghostFrightened(ghost, ghost.dir).animate();
		}
		return ghostKicking(ghost, ghost.wishDir).animate(); // Looks towards wish dir!
	}
}