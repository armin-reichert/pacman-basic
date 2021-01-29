package de.amr.games.pacman.ui.swing;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Animation of things, for example of images.
 * 
 * @author Armin Reichert
 */
public class Animation<T> {

	private List<T> things;
	private int repetitions;
	private int frameDurationTicks;
	private int frameRunningTicks;
	private int frameIndex;
	private int loopIndex;
	private boolean running;
	private boolean complete;

	@SafeVarargs
	public static <TT> Animation<TT> of(TT... images) {
		Animation<TT> a = new Animation<>();
		a.things = images.length > 0 ? Arrays.asList(images) : new ArrayList<>();
		return a;
	}

	private Animation() {
		repetitions = 1;
		frameDurationTicks = 6; // 0.1 sec
		reset();
	}

	public Animation<T> reset() {
		frameRunningTicks = 0;
		frameIndex = 0;
		loopIndex = 0;
		running = false;
		complete = false;
		return this;
	}

	public Animation<T> add(T thing) {
		things.add(thing);
		return this;
	}

	public Animation<T> frameDuration(int ticks) {
		frameDurationTicks = ticks;
		return this;
	}

	public Animation<T> repetitions(int n) {
		repetitions = n;
		return this;
	}

	public Animation<T> endless() {
		repetitions = Integer.MAX_VALUE;
		return this;
	}

	public Animation<T> restart() {
		reset();
		run();
		return this;
	}

	public Animation<T> run() {
		running = true;
		return this;
	}

	public Animation<T> stop() {
		running = false;
		return this;
	}

	public T currentFrameThenAdvance() {
		T currentThing = things.get(frameIndex);
		advance();
		return currentThing;
	}

	public T currentFrame() {
		return things.get(frameIndex);
	}

	public void advance() {
		if (running) {
			if (frameRunningTicks + 1 < frameDurationTicks) {
				frameRunningTicks++;
			} else if (frameIndex + 1 < things.size()) {
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
	}

	public T thing(int i) {
		return things.get(i);
	}

	public int currentFrameIndex() {
		return frameIndex;
	}

	public int getFrameDurationTicks() {
		return frameDurationTicks;
	}

	public int getDuration() {
		return things.size() * frameDurationTicks;
	}

	public boolean isRunning() {
		return running;
	}

	public boolean isComplete() {
		return complete;
	}
}