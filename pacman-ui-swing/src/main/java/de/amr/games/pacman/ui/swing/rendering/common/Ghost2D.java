package de.amr.games.pacman.ui.swing.rendering.common;

import static de.amr.games.pacman.model.common.GhostState.DEAD;
import static de.amr.games.pacman.model.common.GhostState.ENTERING_HOUSE;
import static de.amr.games.pacman.model.common.GhostState.FRIGHTENED;
import static de.amr.games.pacman.model.common.GhostState.LOCKED;
import static de.amr.games.pacman.model.world.PacManGameWorld.TS;

import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.util.Map;

import de.amr.games.pacman.lib.Direction;
import de.amr.games.pacman.lib.TimedSequence;
import de.amr.games.pacman.model.common.Ghost;

public class Ghost2D {

	public final Ghost ghost;

	private Map<Direction, TimedSequence<BufferedImage>> kickingAnimations;
	private TimedSequence<BufferedImage> flashingAnimation;
	private TimedSequence<BufferedImage> frightenedAnimation;
	private Map<Direction, TimedSequence<BufferedImage>> returningHomeAnimations;
	private Map<Integer, BufferedImage> bountyNumberSprites;

	private boolean displayFrightened;

	public Ghost2D(Ghost ghost) {
		this.ghost = ghost;
	}

	public void setRendering(AbstractPacManGameRendering rendering) {
		setKickingAnimations(rendering.createGhostKickingAnimations(ghost.id));
		setFrightenedAnimation(rendering.createGhostFrightenedAnimation());
		setFlashingAnimation(rendering.createGhostFlashingAnimation());
		setReturningHomeAnimations(rendering.createGhostReturningHomeAnimations());
		setBountyNumberSprites(rendering.getBountyNumberSpritesMap());
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

	public Map<Integer, BufferedImage> getBountyNumberSprites() {
		return bountyNumberSprites;
	}

	public void setBountyNumberSprites(Map<Integer, BufferedImage> sprites) {
		this.bountyNumberSprites = sprites;
	}

	public void render(Graphics2D g) {
		BufferedImage sprite = currentSprite();
		if (ghost.visible) {
			int dx = (TS - sprite.getWidth()) / 2, dy = (TS - sprite.getHeight()) / 2;
			g.drawImage(sprite, (int) (ghost.position.x + dx), (int) (ghost.position.y + dy), null);
		}
	}

	private BufferedImage currentSprite() {
		if (ghost.bounty > 0) {
			return bountyNumberSprites.get(ghost.bounty);
		}
		if (ghost.is(DEAD) || ghost.is(ENTERING_HOUSE)) {
			return returningHomeAnimations.get(ghost.dir()).animate();
		}
		if (ghost.is(FRIGHTENED)) {
			return flashingAnimation.isRunning() ? flashingAnimation.animate() : frightenedAnimation.animate();
		}
		if (ghost.is(LOCKED) && displayFrightened) {
			return frightenedAnimation.animate();
		}
		if (ghost.speed == 0) {
			return kickingAnimations.get(ghost.wishDir()).frame();
		}
		return kickingAnimations.get(ghost.wishDir()).animate(); // Looks towards wish dir!
	}
}