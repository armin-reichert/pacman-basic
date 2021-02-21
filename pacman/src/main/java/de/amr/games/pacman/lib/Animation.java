package de.amr.games.pacman.lib;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Timed sequence of things, for example of images or spritesheet regions
 * 
 * @author Armin Reichert
 */
public class Animation<T> {

	static class SingleFrameAnimation<TT> extends Animation<TT> {

		public SingleFrameAnimation(TT thing) {
			things = Collections.singletonList(thing);
		}
	}

	@SafeVarargs
	public static <TT> Animation<TT> of(TT... things) {
		if (things.length == 0) {
			throw new IllegalArgumentException("Animation must have at least one frame");
		}
		if (things.length == 1) {
			return new SingleFrameAnimation<TT>(things[0]);
		}
		Animation<TT> a = new Animation<>();
		a.things = Stream.of(things).collect(Collectors.toList());
		return a;
	}

	public static Animation<Boolean> pulse() {
		return Animation.of(true, false).endless();
	}

	protected List<T> things;
	protected int repetitions;
	protected int frameDurationTicks;
	protected int frameRunningTicks;
	protected int frameIndex;
	protected int loopIndex;
	protected boolean running;
	protected boolean complete;

	protected Animation() {
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

	public Animation<T> frameDuration(int ticks) {
		frameDurationTicks = ticks;
		return this;
	}

	public int repetitions() {
		return repetitions;
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

	public T animate() {
		T currentThing = things.get(frameIndex);
		advance();
		return currentThing;
	}

	public T frame() {
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

	public T frame(int i) {
		return things.get(i);
	}

	public int frameIndex() {
		return frameIndex;
	}

	public int getFrameDuration() {
		return frameDurationTicks;
	}

	public int duration() {
		return things.size() * frameDurationTicks;
	}

	public int numFrames() {
		return things.size();
	}

	public boolean isRunning() {
		return running;
	}

	public boolean isComplete() {
		return complete;
	}

	public boolean hasStarted() {
		return running || complete;
	}
}