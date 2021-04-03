package de.amr.games.pacman.ui.swing.rendering.mspacman;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;

import de.amr.games.pacman.lib.TimedSequence;
import de.amr.games.pacman.model.mspacman.Flap;

public class Flap2D {

	private final Flap flap;
	private TimedSequence<BufferedImage> animation;
	private Font font;

	public Flap2D(Flap flap) {
		this.flap = flap;
	}

	public void setAnimation(TimedSequence<BufferedImage> animation) {
		this.animation = animation;
	}

	public TimedSequence<BufferedImage> getAnimation() {
		return animation;
	}

	public void setFont(Font font) {
		this.font = font;
	}

	public Flap getFlap() {
		return flap;
	}

	public void render(Graphics2D g) {
		if (flap.visible) {
			g.drawImage(animation.animate(), (int) flap.position.x, (int) flap.position.y, null);
			g.setFont(font);
			g.setColor(new Color(222, 222, 225, 192));
			g.drawString(flap.sceneNumber + "", (int) flap.position.x + 20, (int) flap.position.y + 30);
			g.drawString(flap.sceneTitle, (int) flap.position.x + 40, (int) flap.position.y + 20);
		}
	}
}