package de.amr.games.pacman.ui.swing.rendering.common;

import static de.amr.games.pacman.model.world.PacManGameWorld.TS;

import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.util.Map;

import de.amr.games.pacman.lib.TimedSequence;
import de.amr.games.pacman.model.pacman.Bonus;

public class Bonus2D {

	private Bonus bonus;
	private Map<String, BufferedImage> symbolSprites;
	private Map<Integer, BufferedImage> numberSprites;
	private TimedSequence<Integer> jumpAnimation;

	public void setRendering(AbstractPacManGameRendering rendering) {
		setJumpAnimation(rendering.createBonusAnimation());
		setSymbolSprites(rendering.getSymbolSpritesMap());
		setNumberSprites(rendering.getBonusNumberSpritesMap());
	}

	public TimedSequence<Integer> getJumpAnimation() {
		return jumpAnimation;
	}

	public void setJumpAnimation(TimedSequence<Integer> jumpAnimation) {
		this.jumpAnimation = jumpAnimation;
	}

	public void setBonus(Bonus bonus) {
		this.bonus = bonus;
	}

	public void setSymbolSprites(Map<String, BufferedImage> symbolSprites) {
		this.symbolSprites = symbolSprites;
	}

	public void setNumberSprites(Map<Integer, BufferedImage> numberSprites) {
		this.numberSprites = numberSprites;
	}

	public void render(Graphics2D g) {
		BufferedImage sprite = currentSprite();
		if (sprite == null || !bonus.visible) {
			return;
		}
		// Ms. Pac.Man bonus is jumping up and down while wandering the maze
		int jump = jumpAnimation != null ? jumpAnimation.animate() : 0;
		int dx = -(sprite.getWidth() - TS) / 2, dy = -(sprite.getHeight() - TS) / 2;
		g.translate(0, jump);
		g.drawImage(sprite, (int) (bonus.position.x + dx), (int) (bonus.position.y + dy), null);
		g.translate(0, -jump);
	}

	private BufferedImage currentSprite() {
		if (bonus == null) {
			return null;
		}
		if (bonus.edibleTicksLeft > 0) {
			return symbolSprites.get(bonus.symbol);
		}
		if (bonus.eatenTicksLeft > 0) {
			return numberSprites.get(bonus.points);
		}
		return null;
	}
}