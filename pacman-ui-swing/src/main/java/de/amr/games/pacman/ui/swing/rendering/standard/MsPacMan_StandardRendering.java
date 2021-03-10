package de.amr.games.pacman.ui.swing.rendering.standard;

import static de.amr.games.pacman.model.common.GhostState.DEAD;
import static de.amr.games.pacman.model.common.GhostState.ENTERING_HOUSE;
import static de.amr.games.pacman.model.common.GhostState.FRIGHTENED;
import static de.amr.games.pacman.model.common.GhostState.LOCKED;
import static de.amr.games.pacman.model.world.PacManGameWorld.t;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.util.stream.Stream;

import de.amr.games.pacman.lib.Direction;
import de.amr.games.pacman.lib.V2d;
import de.amr.games.pacman.model.common.Flap;
import de.amr.games.pacman.model.common.GameEntity;
import de.amr.games.pacman.model.common.GameModel;
import de.amr.games.pacman.model.common.Ghost;
import de.amr.games.pacman.model.common.Pac;
import de.amr.games.pacman.model.common.Stork;
import de.amr.games.pacman.model.pacman.PacManBonus;
import de.amr.games.pacman.ui.animation.Animation;
import de.amr.games.pacman.ui.animation.GhostAnimations;
import de.amr.games.pacman.ui.animation.MazeAnimations;
import de.amr.games.pacman.ui.animation.PlayerAnimations;

/**
 * Rendering for the Ms. Pac-Man game.
 * 
 * @author Armin Reichert
 */
public class MsPacMan_StandardRendering extends StandardRendering
		implements PlayerAnimations, GhostAnimations, MazeAnimations {

	public static final MsPacMan_StandardAssets assets = new MsPacMan_StandardAssets();

	@Override
	public PlayerAnimations playerAnimations() {
		return this;
	}

	@Override
	public GhostAnimations ghostAnimations() {
		return this;
	}

	@Override
	public MazeAnimations mazeAnimations() {
		return this;
	}

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

	public BufferedImage bonusSprite(PacManBonus bonus, GameModel game) {
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
			return ghostReturningHome(ghost, ghost.dir).animate();
		}
		if (ghost.is(FRIGHTENED)) {
			return ghostFlashing(ghost).isRunning() ? ghostFlashing(ghost).frame()
					: ghostFrightened(ghost, ghost.dir).animate();
		}
		if (ghost.is(LOCKED) && frightened) {
			return ghostFrightened(ghost, ghost.dir).animate();
		}
		return ghostKicking(ghost, ghost.wishDir).animate();
	}

	@Override
	protected BufferedImage bonusSprite(PacManBonus bonus) {
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
	public Animation<BufferedImage> ghostFlashing(Ghost ghost) {
		return assets.ghostFlashingAnim;
	}

	@Override
	public Animation<BufferedImage> ghostReturningHome(Ghost ghost, Direction dir) {
		return assets.ghostEyesAnimByDir.get(dir);
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
		for (int levelNumber = 1; levelNumber <= Math.min(game.levelNumber, 7); ++levelNumber) {
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
	public void drawBonus(Graphics2D g, PacManBonus bonus) {
		// Ms. Pac.Man bonus is jumping while wandering the maze
		int dy = bonus.edibleTicksLeft > 0 ? assets.bonusJumpAnim.animate() : 0;
		g.translate(0, dy);
		drawEntity(g, bonus, bonusSprite(bonus));
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
	public void drawFlap(Graphics2D g, Flap flap) {
		if (flap.visible) {
			drawSprite(g, (BufferedImage) flap.flapping.frame(), flap.position.x, flap.position.y);
			g.setFont(new Font(assets.getScoreFont().getName(), Font.PLAIN, 8));
			g.setColor(new Color(222, 222, 225, 192));
			g.drawString(flap.sceneNumber + "", (int) flap.position.x + 20, (int) flap.position.y + 30);
			g.setFont(assets.getScoreFont());
			g.drawString(flap.sceneTitle, (int) flap.position.x + 40, (int) flap.position.y + 20);
		}
	}

	@Override
	public Animation<?> flapFlapping() {
		return Animation.of( //
				assets.region(456, 208, 32, 32), //
				assets.region(488, 208, 32, 32), //
				assets.region(520, 208, 32, 32), //
				assets.region(488, 208, 32, 32), //
				assets.region(456, 208, 32, 32)//
		).repetitions(1).frameDuration(4);
	}

	@Override
	public void drawStork(Graphics2D g, Stork stork) {
		drawEntity(g, stork, (BufferedImage) stork.flying.frame());
	}

	@Override
	public Animation<?> storkFlying() {
		return Animation.of(//
				assets.region(489, 176, 32, 16), //
				assets.region(521, 176, 32, 16)//
		).endless().frameDuration(10);
	}

	@Override
	public void drawHeart(Graphics2D g, GameEntity heart) {
		drawEntity(g, heart, assets.s(2, 10));
	}

	@Override
	public void drawJuniorBag(Graphics2D g, de.amr.games.pacman.model.common.JuniorBag bag) {
		if (bag.visible) {
			if (bag.open) {
				drawEntity(g, bag, assets.junior);
			} else {
				drawEntity(g, bag, assets.blueBag);
			}
		}
	}

	// Pac-Man only:

	@Override
	public void drawBigPacMan(Graphics2D g, Pac bigPacMan) {
	}

	@Override
	public void drawNail(Graphics2D g, GameEntity nail) {
	}

	@Override
	public void drawBlinkyStretched(Graphics2D g, Ghost ghost, V2d nailPosition, int stretching) {
	}

	@Override
	public void drawBlinkyPatched(Graphics2D g, Ghost blinky) {
	}

	@Override
	public void drawBlinkyNaked(Graphics2D g, Ghost blinky) {
	}
}