package de.amr.games.pacman.ui.swing.rendering.mspacman;

import java.awt.Graphics2D;
import java.awt.image.BufferedImage;

import de.amr.games.pacman.lib.TimedSequence;
import de.amr.games.pacman.model.mspacman.Stork;

public class Stork2D {

	private final Stork stork;
	private TimedSequence<BufferedImage> animation;

	public Stork2D(Stork stork) {
		this.stork = stork;
	}

	public TimedSequence<BufferedImage> getAnimation() {
		return animation;
	}

	public void setAnimation(TimedSequence<BufferedImage> animation) {
		this.animation = animation;
	}

	public void render(Graphics2D g) {
		BufferedImage frame = animation.animate();
		g.drawImage(frame, (int) stork.position.x, (int) stork.position.y, null);
	}
}