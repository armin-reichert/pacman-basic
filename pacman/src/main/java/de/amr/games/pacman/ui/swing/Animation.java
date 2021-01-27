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

	private List<BufferedImage> sprites = new ArrayList<>();
	private int frameTicks;
	private int frameIndex;
	private int ticksPassed;
	private boolean running;
	private boolean loop;

	public void addFrame(BufferedImage sprite) {
		sprites.add(sprite);
	}

	public BufferedImage getSprite(int i) {
		return sprites.get(i);
	}

	public int currentFrameIndex() {
		return frameIndex;
	}

	public int getFrameTicks() {
		return frameTicks;
	}

	public void setFrameTicks(int ticks) {
		frameTicks = ticks;
	}

	public int getDuration() {
		return sprites.size() * frameTicks;
	}

	public void setLoop(boolean b) {
		loop = b;
	}

	public boolean isRunning() {
		return running;
	}

	public void reset() {
		frameIndex = 0;
		ticksPassed = 0;
	}

	public void start() {
		running = true;
	}

	public void stop() {
		running = false;
	}

	public BufferedImage frame() {
		BufferedImage currentSprite = sprites.get(frameIndex);
		if (running) {
			if (ticksPassed < frameTicks) {
				++ticksPassed;
			} else {
				if (frameIndex < sprites.size() - 1) {
					ticksPassed = 0;
					++frameIndex;
				} else if (loop) {
					reset();
				}
			}
		}
		return currentSprite;
	}
}
