/*
MIT License

Copyright (c) 2021 Armin Reichert

Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files (the "Software"), to deal
in the Software without restriction, including without limitation the rights
to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
copies of the Software, and to permit persons to whom the Software is
furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all
copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
SOFTWARE.
 */
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
public class TimedSequence<T> {

	static class SingleFrameAnimation<TT> extends TimedSequence<TT> {

		public SingleFrameAnimation(TT thing) {
			things = Collections.singletonList(thing);
		}
	}

	@SafeVarargs
	public static <TT> TimedSequence<TT> of(TT... things) {
		if (things.length == 0) {
			throw new IllegalArgumentException("Animation must have at least one frame");
		}
		if (things.length == 1) {
			return new SingleFrameAnimation<TT>(things[0]);
		}
		TimedSequence<TT> a = new TimedSequence<>();
		a.things = Stream.of(things).collect(Collectors.toList());
		return a;
	}

	public static TimedSequence<Boolean> pulse() {
		return TimedSequence.of(true, false).endless();
	}

	protected List<T> things;
	protected int repetitions;
	protected long delay;
	protected long delayRemainingTicks;
	protected long totalRunningTicks;
	protected long frameDurationTicks;
	protected long frameRunningTicks;
	protected int frameIndex;
	protected long loopIndex;
	protected boolean running;
	protected boolean complete;
	protected Runnable onStart;

	protected TimedSequence() {
		repetitions = 1;
		frameDurationTicks = 6; // 0.1 sec
		reset();
	}

	public TimedSequence<T> reset() {
		delayRemainingTicks = delay;
		totalRunningTicks = 0;
		frameRunningTicks = 0;
		frameIndex = 0;
		loopIndex = 0;
		running = false;
		complete = false;
		return this;
	}

	public TimedSequence<T> onStart(Runnable code) {
		onStart = code;
		return this;
	}

	public TimedSequence<T> frameDuration(long ticks) {
		frameDurationTicks = ticks;
		return this;
	}

	public long delay() {
		return delay;
	}

	public TimedSequence<T> delay(long ticks) {
		delay = ticks;
		delayRemainingTicks = ticks;
		return this;
	}

	public int repetitions() {
		return repetitions;
	}

	public TimedSequence<T> repetitions(int n) {
		repetitions = n;
		return this;
	}

	public TimedSequence<T> endless() {
		repetitions = Integer.MAX_VALUE;
		return this;
	}

	public TimedSequence<T> restart() {
		reset();
		run();
		return this;
	}

	public TimedSequence<T> run() {
		running = true;
		return this;
	}

	public TimedSequence<T> stop() {
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
			if (delayRemainingTicks > 0) {
				delayRemainingTicks--;
			} else if (totalRunningTicks++ == 0) {
				if (onStart != null) {
					onStart.run();
				}
			} else if (frameRunningTicks + 1 < frameDurationTicks) {
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

	public long getFrameDuration() {
		return frameDurationTicks;
	}

	public long duration() {
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