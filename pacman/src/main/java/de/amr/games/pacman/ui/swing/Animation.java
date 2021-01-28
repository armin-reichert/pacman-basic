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
	private int frameDurationTicks;
	private int frameIndex;
	private int ticksInFrame;
	private boolean running;
	private int repetitions = 1;
	private int loopIndex;

	public void addFrame(BufferedImage sprite) {
		sprites.add(sprite);
	}

	public BufferedImage getSprite(int i) {
		return sprites.get(i);
	}

	public int currentFrameIndex() {
		return frameIndex;
	}

	public int getFrameDurationTicks() {
		return frameDurationTicks;
	}

	public void setFrameDurationTicks(int ticks) {
		frameDurationTicks = ticks;
	}

	public int getDuration() {
		return sprites.size() * frameDurationTicks;
	}

	public void setRepetitions(int n) {
		repetitions = n;
	}

	public boolean isRunning() {
		return running;
	}

	public void reset() {
		ticksInFrame = 0;
		frameIndex = 0;
		loopIndex = 0;
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
			if (ticksInFrame < frameDurationTicks - 1) {
				++ticksInFrame;
			} else if (frameIndex < sprites.size() - 1) {
				ticksInFrame = 0;
				++frameIndex;
			} else if (loopIndex < repetitions - 1) {
				ticksInFrame = 0;
				frameIndex = 0;
				++loopIndex;
			}
		}
		return currentSprite;
	}
}