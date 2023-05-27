/*
Copyright (c) 2021-2023 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.games.pacman.lib.anim;

/**
 * Time-controlled sequence of things, for example of boolean values, numbers, images, spritesheet regions etc.
 * 
 * @param <T> type of "things" to be animated
 * 
 * @author Armin Reichert
 */
public class SimpleAnimation<T> implements Animated {

	public static final int INDEFINITE = -1;

	protected T[] frames;
	protected int repetitions;
	protected long totalRunningTicks;
	protected long frameDurationTicks;
	protected long frameTick;
	protected int frameIndex;
	protected long repetitionIndex;
	protected boolean running;
	protected boolean complete;

	@SafeVarargs
	public SimpleAnimation(T... frames) {
		if (frames.length == 0) {
			throw new IllegalArgumentException("Animation must at least contain one frame");
		}
		this.frames = frames;
		repetitions = 1;
		frameDurationTicks = 6; // 0.1 sec at 60Hz
		reset();
	}

	@Override
	public void reset() {
		totalRunningTicks = 0;
		frameTick = 0;
		frameIndex = 0;
		repetitionIndex = 0;
		running = false;
		complete = false;
	}

	@Override
	public void setFrameDuration(long ticks) {
		frameDurationTicks = ticks;
	}

	public int repetitions() {
		return repetitions;
	}

	@Override
	public void setRepetitions(int n) {
		repetitions = n;
	}

	public void repeatForever() {
		repetitions = INDEFINITE;
	}

	@Override
	public void start() {
		running = true;
	}

	@Override
	public void stop() {
		running = false;
	}

	@Override
	public T animate() {
		T currentThing = frame();
		advance();
		return currentThing;
	}

	private void advance() {
		if (!running) {
			return;
		}
		// continue current frame?
		if (frameTick < frameDurationTicks - 1) {
			++frameTick;
			return;
		}
		// start next frame?
		if (frameIndex < frames.length - 1) {
			++frameIndex;
			frameTick = 0;
			return;
		}
		// loop forever?
		if (repetitions == INDEFINITE) {
			repetitionIndex = 0;
			frameIndex = 0;
			frameTick = 0;
			return;
		}
		// start next repetition?
		if (repetitionIndex < repetitions - 1) {
			++repetitionIndex;
			frameIndex = 0;
			frameTick = 0;
			return;
		}
		complete = true;
		stop();
	}

	@Override
	public T frame() {
		if (frameIndex >= frames.length) {
			throw new IllegalStateException(
					String.format("Trying to access animation frame at index %d but there are only %d frames. %s", frameIndex,
							frames.length, getClass().getName()));
		}
		return frames[frameIndex];
	}

	@Override
	public T frame(int i) {
		return frames[i];
	}

	@Override
	public int frameIndex() {
		return frameIndex;
	}

	@Override
	public void setFrameIndex(int i) {
		if (frameIndex >= frames.length) {
			throw new IllegalStateException(
					String.format("Trying to set animation frame index to %d but there are only %d frames. %s", frameIndex,
							frames.length, getClass().getName()));
		}
		frameIndex = i;
		frameTick = 0;
	}

	public long getFrameDuration() {
		return frameDurationTicks;
	}

	public long duration() {
		return frames.length * frameDurationTicks;
	}

	@Override
	public int numFrames() {
		return frames.length;
	}

	@Override
	public boolean isRunning() {
		return running;
	}

	@Override
	public void ensureRunning() {
		if (!running) {
			complete = false;
			running = true;
		}
	}

	public boolean isComplete() {
		return complete;
	}

	public boolean hasStarted() {
		return running || complete;
	}
}