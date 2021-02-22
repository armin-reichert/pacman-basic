package de.amr.games.pacman.ui.swing.rendering;

import static de.amr.games.pacman.model.guys.GhostState.DEAD;
import static de.amr.games.pacman.model.guys.GhostState.ENTERING_HOUSE;
import static de.amr.games.pacman.model.guys.GhostState.FRIGHTENED;
import static de.amr.games.pacman.model.guys.GhostState.LOCKED;
import static de.amr.games.pacman.world.PacManGameWorld.t;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.util.stream.Stream;

import de.amr.games.pacman.lib.Animation;
import de.amr.games.pacman.lib.Direction;
import de.amr.games.pacman.model.GameModel;
import de.amr.games.pacman.model.guys.Bonus;
import de.amr.games.pacman.model.guys.GameEntity;
import de.amr.games.pacman.model.guys.Ghost;
import de.amr.games.pacman.model.guys.Pac;

/**
 * Rendering for the Ms. Pac-Man game.
 * 
 * @author Armin Reichert
 */
public class MsPacMan_Rendering extends DefaultRendering {

	public static final MsPacMan_Assets assets = new MsPacMan_Assets();

	@Override
	public Font getScoreFont() {
		return assets.getScoreFont();
	}

	/**
	 * Note: maze numbers are 1-based, maze index as stored here is 0-based.
	 * 
	 * @param mazeIndex
	 * @return
	 */
	@Override
	public Color getMazeWallColor(int mazeIndex) {
		switch (mazeIndex) {
		case 0:
			return new Color(255, 183, 174);
		case 1:
			return new Color(71, 183, 255);
		case 2:
			return new Color(222, 151, 81);
		case 3:
			return new Color(33, 33, 255);
		case 4:
			return new Color(255, 183, 255);
		case 5:
			return new Color(255, 183, 174);
		default:
			return Color.WHITE;
		}
	}

	/**
	 * Note: maze numbers are 1-based, maze index as stored here is 0-based.
	 * 
	 * @param mazeIndex
	 * @return
	 */
	@Override
	public Color getMazeWallBorderColor(int mazeIndex) {
		switch (mazeIndex) {
		case 0:
			return new Color(255, 0, 0);
		case 1:
			return new Color(222, 222, 255);
		case 2:
			return new Color(222, 222, 255);
		case 3:
			return new Color(255, 183, 81);
		case 4:
			return new Color(255, 255, 0);
		case 5:
			return new Color(255, 0, 0);
		default:
			return Color.WHITE;
		}
	}

	@Override
	public Animation<Boolean> energizerBlinking() {
		return assets.energizerBlinkingAnim;
	}

	@Override
	public Stream<Animation<?>> mazeFlashings() {
		// TODO this is silly
		return assets.mazesFlashingAnims.stream().map(Animation.class::cast);
	}

	@Override
	public Animation<BufferedImage> mazeFlashing(int mazeNumber) {
		return assets.mazesFlashingAnims.get(mazeNumber - 1);
	}

	public BufferedImage bonusSprite(Bonus bonus, GameModel game) {
		if (bonus.edibleTicksLeft > 0) {
			return assets.symbolSprites[bonus.symbol];
		}
		if (bonus.eatenTicksLeft > 0) {
			return assets.bonusValueSprites.get(bonus.points);
		}
		return null;
	}

	@Override
	public BufferedImage lifeSprite() {
		return assets.lifeSprite;
	}

	@Override
	public BufferedImage pacSprite(Pac pac) {
		if (pac.dead) {
			return playerDying().hasStarted() ? playerDying().animate() : playerMunching(pac, pac.dir).frame();
		}
		return pac.speed == 0 || !pac.couldMove ? playerMunching(pac, pac.dir).frame(1)
				: playerMunching(pac, pac.dir).animate();
	}

	@Override
	public BufferedImage ghostSprite(Ghost ghost, boolean frightened) {
		if (ghost.bounty > 0) {
			return assets.bountyNumberSprites.get(ghost.bounty);
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
		return ghostKicking(ghost, ghost.wishDir).animate();
	}

	@Override
	protected BufferedImage bonusSprite(Bonus bonus) {
		if (bonus.edibleTicksLeft > 0) {
			return assets.symbolSprites[bonus.symbol];
		}
		if (bonus.eatenTicksLeft > 0) {
			return assets.bonusValueSprites.get(bonus.points);
		}
		return null;
	}

	@Override
	protected BufferedImage symbolSprite(byte symbol) {
		return assets.symbolSprites[symbol];
	}

	@Override
	public Animation<BufferedImage> playerMunching(Pac pac, Direction dir) {
		return assets.msPacManMunchingAnimByDir.get(dir);
	}

	@Override
	public Animation<?> spouseMunching(Pac spouse, Direction dir) {
		return assets.pacManMunching.get(dir);
	}

	@Override
	public Animation<BufferedImage> playerDying() {
		return assets.msPacManSpinningAnim;
	}

	@Override
	public Animation<BufferedImage> ghostKicking(Ghost ghost, Direction dir) {
		return assets.ghostsKickingAnimsByGhost.get(ghost.id).get(dir);
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
		return assets.ghostEyesAnimByDir.get(dir);
	}

	@Override
	public Animation<?> storkFlying() {
		return Animation.of(//
				assets.region(489, 176, 32, 16), //
				assets.region(521, 176, 32, 16)//
		).endless().frameDuration(10);
	}

	@Override
	public void drawMaze(Graphics2D g, int mazeNumber, int x, int y, boolean flashing) {
		if (flashing) {
			g.drawImage(mazeFlashing(mazeNumber).animate(), x, y, null);
		} else {
			g.drawImage(assets.mazeFullImages.get(mazeNumber - 1), x, y, null);
		}
	}

	@Override
	public void drawLevelCounter(Graphics2D g, GameModel game, int rightX, int y) {
		Graphics2D g2 = smoothGC(g);
		int x = rightX;
		for (int levelNumber = 1; levelNumber <= Math.min(game.currentLevelNumber, 7); ++levelNumber) {
			byte symbol = game.levelSymbols.get(levelNumber - 1);
			g2.drawImage(assets.symbolSprites[symbol], x, y, null);
			x -= t(2);
		}
		g2.dispose();
	}

	@Override
	public void drawLifeCounterSymbol(Graphics2D g, int x, int y) {
		drawSprite(g, assets.lifeSprite, x, y);
	}

	@Override
	public void drawBonus(Graphics2D g, Bonus bonus) {
		// Ms. Pac.Man bonus is jumping while wandering the maze
		int dy = bonus.edibleTicksLeft > 0 ? assets.bonusJumpAnim.animate() : 0;
		g.translate(0, dy);
		drawGuy(g, bonus, bonusSprite(bonus));
		g.translate(0, -dy);
	}

	@Override
	public void drawSpouse(Graphics2D g, Pac pacMan) {
		if (pacMan.visible) {
			Animation<BufferedImage> munching = assets.pacManMunching.get(pacMan.dir);
			drawSprite(g, pacMan.speed > 0 ? munching.animate() : munching.frame(1), pacMan.position.x - 4,
					pacMan.position.y - 4);
		}
	}

	@Override
	public void drawStork(Graphics2D g, GameEntity stork) {
		// TODO Auto-generated method stub

	}

	// TODO
	@Override
	public void drawStorkSprite(Graphics2D g, float x, float y) {
		BufferedImage storkSprite = assets.storkAnim.animate();
		drawSprite(g, storkSprite, x + 4 - storkSprite.getWidth() / 2, y + 4 - storkSprite.getHeight() / 2);
	}

	@Override
	public void drawHeart(Graphics2D g, GameEntity heart) {
		drawGuy(g, heart, assets.s(2, 10));
	}

	@Override
	public void drawJunior(Graphics2D g, GameEntity junior) {
		// TODO Auto-generated method stub

	}

	@Override
	public void drawJuniorSprite(Graphics2D g, float x, float y) {
		BufferedImage juniorSprite = assets.junior;
		drawSprite(g, juniorSprite, x + 4 - juniorSprite.getWidth() / 2, y + 4 - juniorSprite.getHeight() / 2);
	}

	@Override
	public void drawBag(Graphics2D g, GameEntity bag) {
		// TODO Auto-generated method stub

	}

	@Override
	public void drawBlueBagSprite(Graphics2D g, float x, float y) {
		BufferedImage bagSprite = assets.blueBag;
		drawSprite(g, bagSprite, x + 4 - bagSprite.getWidth() / 2, y + 4 - bagSprite.getHeight() / 2);
	}

	// Pac-Man only:
	@Override
	public Animation<?> bigPacMan() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Animation<?> blinkyDamaged() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Animation<?> blinkyNaked() {
		// TODO Auto-generated method stub
		return null;
	}

}