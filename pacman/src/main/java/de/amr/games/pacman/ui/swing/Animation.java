package de.amr.games.pacman.ui.swing;

import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;

/**
 * Sprite animation.
 * 
 * @author Armin Reichert
 */
public class Animation {

	private int ticksPerFrame;
	private List<BufferedImage> sprites;
	private int frame;
	private int ticks;
	private boolean running;
	private boolean loop;

	public Animation() {
		this(1);
	}

	public Animation(int ticksPerFrame) {
		sprites = new ArrayList<>();
		this.ticksPerFrame = ticksPerFrame;
	}

	public void addFrame(BufferedImage sprite) {
		sprites.add(sprite);
	}

	public void setLoop(boolean b) {
		loop = b;
	}

	public boolean isRunning() {
		return running;
	}

	public void reset() {
		frame = 0;
		ticks = 0;
	}

	public void start() {
		running = true;
	}

	public void stop() {
		running = false;
	}

	public BufferedImage frame() {
		BufferedImage current = sprites.get(frame);
		if (running) {
			if (ticks < ticksPerFrame) {
				++ticks;
			} else if (frame < sprites.size() - 1) {
				ticks = 0;
				++frame;
			} else if (loop && frame == sprites.size() - 1) {
				reset();
			}
		}
		return current;
	}
}
