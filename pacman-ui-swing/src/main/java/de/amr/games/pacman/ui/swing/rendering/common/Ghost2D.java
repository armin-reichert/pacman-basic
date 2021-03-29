package de.amr.games.pacman.ui.swing.rendering.common;

import static de.amr.games.pacman.model.common.GhostState.DEAD;
import static de.amr.games.pacman.model.common.GhostState.ENTERING_HOUSE;
import static de.amr.games.pacman.model.common.GhostState.FRIGHTENED;
import static de.amr.games.pacman.model.common.GhostState.LOCKED;

import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.util.EnumMap;
import java.util.Map;

import de.amr.games.pacman.lib.Direction;
import de.amr.games.pacman.model.common.Ghost;
import de.amr.games.pacman.ui.animation.TimedSequence;

public class Ghost2D {

	public final Ghost ghost;

	private boolean displayFrightened;
	private Map<Direction, TimedSequence<BufferedImage>> kickingAnimations = new EnumMap<>(Direction.class);
	private TimedSequence<BufferedImage> flashingAnimation;
	private TimedSequence<BufferedImage> frightenedAnimation;
	private Map<Direction, TimedSequence<BufferedImage>> returningHomeAnimations = new EnumMap<>(Direction.class);
	private Map<Integer, BufferedImage> numberSprites;

	public Ghost2D(Ghost ghost) {
		this.ghost = ghost;
	}

	public void setDisplayFrightened(boolean displayFrightened) {
		this.displayFrightened = displayFrightened;
	}

	public Map<Direction, TimedSequence<BufferedImage>> getKickingAnimations() {
		return kickingAnimations;
	}

	public void setKickingAnimations(Map<Direction, TimedSequence<BufferedImage>> kickingAnimations) {
		this.kickingAnimations = kickingAnimations;
	}

	public TimedSequence<BufferedImage> getFlashingAnimation() {
		return flashingAnimation;
	}

	public void setFlashingAnimation(TimedSequence<BufferedImage> flashingAnimation) {
		this.flashingAnimation = flashingAnimation;
	}

	public TimedSequence<BufferedImage> getFrightenedAnimation() {
		return frightenedAnimation;
	}

	public void setFrightenedAnimation(TimedSequence<BufferedImage> frightenedAnimation) {
		this.frightenedAnimation = frightenedAnimation;
	}

	public Map<Direction, TimedSequence<BufferedImage>> getReturningHomeAnimations() {
		return returningHomeAnimations;
	}

	public void setReturningHomeAnimations(Map<Direction, TimedSequence<BufferedImage>> returningHomeAnimations) {
		this.returningHomeAnimations = returningHomeAnimations;
	}

	public Map<Integer, BufferedImage> getNumberSprites() {
		return numberSprites;
	}

	public void setNumberSpriteMap(Map<Integer, BufferedImage> numberSprites) {
		this.numberSprites = numberSprites;
	}

	public void render(Graphics2D g) {
		BufferedImage sprite = currentSprite();
		if (ghost.visible) {
			int dx = sprite.getWidth() / 2 - 4, dy = sprite.getHeight() / 2 - 4;
			Graphics2D gc = (Graphics2D) g.create();
			gc.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
			gc.drawImage(sprite, (int) (ghost.position.x - dx), (int) (ghost.position.y - dy), null);
			gc.dispose();
		}
	}

	private BufferedImage currentSprite() {
		if (ghost.bounty > 0) {
			return numberSprites.get(ghost.bounty);
		}
		if (ghost.is(DEAD) || ghost.is(ENTERING_HOUSE)) {
			return returningHomeAnimations.get(ghost.dir).animate();
		}
		if (ghost.is(FRIGHTENED)) {
			return flashingAnimation.isRunning() ? flashingAnimation.animate() : frightenedAnimation.animate();
		}
		if (ghost.is(LOCKED) && displayFrightened) {
			return frightenedAnimation.animate();
		}
		if (ghost.speed == 0) {
			return kickingAnimations.get(ghost.wishDir).frame();
		}
		return kickingAnimations.get(ghost.wishDir).animate(); // Looks towards wish dir!
	}
}