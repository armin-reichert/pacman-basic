package de.amr.games.pacman.ui.swing.rendering.common;

import static de.amr.games.pacman.model.world.PacManGameWorld.TS;

import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.util.Map;

import de.amr.games.pacman.lib.Direction;
import de.amr.games.pacman.lib.TimedSequence;
import de.amr.games.pacman.model.common.Pac;

public class Player2D {

	private final Pac player;
	private Map<Direction, TimedSequence<BufferedImage>> munchingAnimations;
	private TimedSequence<BufferedImage> dyingAnimation;

	public Player2D(Pac pac) {
		this.player = pac;
	}

	public void setRendering(AbstractPacManGameRendering rendering) {
		setMunchingAnimations(rendering.createPlayerMunchingAnimations());
		setDyingAnimation(rendering.createPlayerDyingAnimation());
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

	public void render(Graphics2D g) {
		BufferedImage sprite = currentSprite();
		if (player.visible) {
			int dx = (TS - sprite.getWidth()) / 2, dy = (TS - sprite.getHeight()) / 2;
			g.drawImage(sprite, (int) (player.position.x + dx), (int) (player.position.y + dy), null);
		}
	}

	private BufferedImage currentSprite() {
		if (player.dead) {
			return dyingAnimation.hasStarted() ? dyingAnimation.animate() : munchingAnimations.get(player.dir()).frame();
		}
		if (player.speed == 0) {
			return munchingAnimations.get(player.dir()).frame(0);
		}
		if (player.stuck) {
			return munchingAnimations.get(player.dir()).frame(1);
		}
		return munchingAnimations.get(player.dir()).animate();
	}
}