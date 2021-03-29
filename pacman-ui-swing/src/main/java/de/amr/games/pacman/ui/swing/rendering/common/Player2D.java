package de.amr.games.pacman.ui.swing.rendering.common;

import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.util.Map;

import de.amr.games.pacman.lib.Direction;
import de.amr.games.pacman.model.common.Pac;
import de.amr.games.pacman.ui.animation.TimedSequence;

public class Player2D {

	private final Pac player;
	private Map<Direction, TimedSequence<BufferedImage>> munchingAnimations;
	private TimedSequence<BufferedImage> dyingAnimation;

	public Player2D(Pac pac) {
		this.player = pac;
	}

	public TimedSequence<BufferedImage> getDyingAnimation() {
		return dyingAnimation;
	}

	public void setDyingAnimation(TimedSequence<BufferedImage> dyingAnimation) {
		this.dyingAnimation = dyingAnimation;
	}

	public void setMunchingAnimations(Map<Direction, TimedSequence<BufferedImage>> munchingAnimations) {
		this.munchingAnimations = munchingAnimations;
	}

	public Map<Direction, TimedSequence<BufferedImage>> getMunchingAnimations() {
		return munchingAnimations;
	}

	private void drawEntity(Graphics2D g, BufferedImage guySprite) {
		if (player.visible && guySprite != null) {
			int dx = guySprite.getWidth() / 2 - 4, dy = guySprite.getHeight() / 2 - 4;
			Graphics2D gc = (Graphics2D) g.create();
			gc.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
			gc.drawImage(guySprite, (int) (player.position.x - dx), (int) (player.position.y - dy), null);
			gc.dispose();
		}
	}

	private BufferedImage currentSprite() {
		if (player.dead) {
			return dyingAnimation.hasStarted() ? dyingAnimation.animate() : munchingAnimations.get(player.dir).frame();
		}
		if (player.speed == 0) {
			return munchingAnimations.get(player.dir).frame(0);
		}
		if (player.stuck) {
			return munchingAnimations.get(player.dir).frame(1);
		}
		return munchingAnimations.get(player.dir).animate();
	}

	public void render(Graphics2D g) {
		drawEntity(g, currentSprite());
	}
}