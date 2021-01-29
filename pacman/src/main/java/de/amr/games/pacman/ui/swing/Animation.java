package de.amr.games.pacman.ui.swing;

import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Sprite animation.
 * 
 * @author Armin Reichert
 */
public class Animation {

	private List<BufferedImage> sprites;
	private int repetitions;
	private int frameDurationTicks;
	private int frameRunningTicks;
	private int frameIndex;
	private int loopIndex;
	private boolean running;
	private boolean complete;

	public static Animation of(BufferedImage... images) {
		Animation a = new Animation();
		a.sprites = images.length > 0 ? Arrays.asList(images) : new ArrayList<>();
		return a;
	}

	private Animation() {
		repetitions = 1;
		frameDurationTicks = 6; // 0.1 sec
		frameRunningTicks = 0;
		frameIndex = 0;
		loopIndex = 0;
		running = false;
		complete = false;
	}

	public Animation add(BufferedImage sprite) {
		sprites.add(sprite);
		return this;
	}

	public Animation frameDuration(int ticks) {
		frameDurationTicks = ticks;
		return this;
	}

	public Animation repetitions(int n) {
		repetitions = n;
		return this;
	}

	public Animation endless() {
		repetitions = Integer.MAX_VALUE;
		return this;
	}

	public Animation restart() {
		reset();
		run();
		return this;
	}

	public Animation reset() {
		frameRunningTicks = 0;
		frameIndex = 0;
		loopIndex = 0;
		complete = false;
		return this;
	}

	public Animation run() {
		running = true;
		return this;
	}

	public Animation stop() {
		running = false;
		return this;
	}

	public BufferedImage sprite() {
		BufferedImage currentSprite = sprites.get(frameIndex);
		if (running) {
			if (frameRunningTicks + 1 < frameDurationTicks) {
				frameRunningTicks++;
			} else if (frameIndex + 1 < sprites.size()) {
				// start next frame
				frameIndex++;
				frameRunningTicks = 0;
			} else if (loopIndex + 1 < repetitions) {
				// start next loop
				loopIndex++;
				frameIndex = 0;
				frameRunningTicks = 0;
			} else if (repetitions < Integer.MAX_VALUE) {
				// last loop complete
				complete = true;
				stop();
			} else {
				loopIndex = 0;
				frameIndex = 0;
				frameRunningTicks = 0;
			}
		}
		return currentSprite;
	}

	public BufferedImage sprite(int i) {
		return sprites.get(i);
	}

	public int currentFrameIndex() {
		return frameIndex;
	}

	public int getFrameDurationTicks() {
		return frameDurationTicks;
	}

	public int getDuration() {
		return sprites.size() * frameDurationTicks;
	}

	public boolean isRunning() {
		return running;
	}

	public boolean isComplete() {
		return complete;
	}

}