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
	private boolean complete;
	private int repetitions = 1;
	private int loopIndex;

	public BufferedImage frame() {
		BufferedImage currentSprite = sprites.get(frameIndex);
		if (running) {
			if (ticksInFrame + 1 < frameDurationTicks) {
				ticksInFrame++;
			} else if (frameIndex + 1 < sprites.size()) {
				// start next frame
				frameIndex++;
				ticksInFrame = 0;
			} else if (loopIndex + 1 < repetitions) {
				// start next loop
				loopIndex++;
				frameIndex = 0;
				ticksInFrame = 0;
			} else if (repetitions < Integer.MAX_VALUE) {
				// last loop complete
				complete = true;
				stop();
			} else {
				loopIndex = 0;
				frameIndex = 0;
				ticksInFrame = 0;
			}
		}
		return currentSprite;
	}

	public void add(BufferedImage sprite) {
		sprites.add(sprite);
	}

	public BufferedImage get(int i) {
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

	public boolean isComplete() {
		return complete;
	}

	public void restart() {
		reset();
		run();
	}

	public void reset() {
		ticksInFrame = 0;
		frameIndex = 0;
		loopIndex = 0;
		complete = false;
	}

	public void run() {
		running = true;
	}

	public void stop() {
		running = false;
	}
}