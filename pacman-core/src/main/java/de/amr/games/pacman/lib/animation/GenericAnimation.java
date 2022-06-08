/*
MIT License

Copyright (c) 2021-22 Armin Reichert

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
package de.amr.games.pacman.lib.animation;

/**
 * Timed sequence ("animation") of things, for example of images or spritesheet regions.
 * 
 * @author Armin Reichert
 */
public class GenericAnimation<T> {

	public static int INDEFINITE = -1;

	/**
	 * Creates a timed sequence (animation) of the given things.
	 * 
	 * @param <TT>   type of things
	 * @param things the things to be animated
	 * @return the animation
	 */
	@SafeVarargs
	public static <TT> GenericAnimation<TT> of(TT... things) {
		if (things.length == 0) {
			throw new IllegalArgumentException("Sequence must have at least contain one thing");
		}
		return new GenericAnimation<>(things);
	}

	/**
	 * @return an endless animation of alternating true/false values
	 */
	public static GenericAnimation<Boolean> pulse() {
		return GenericAnimation.of(true, false).endless();
	}

	protected T[] things;
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

	@SafeVarargs
	public GenericAnimation(T... things) {
		if (things.length == 0) {
			throw new IllegalArgumentException("Sequence must have at least contain one thing");
		}
		this.things = things;
		repetitions = 1;
		frameDurationTicks = 6; // 0.1 sec
		reset();
	}

	/**
	 * Resets the animation to its initial state.
	 * 
	 * @return the animation
	 */
	public GenericAnimation<T> reset() {
		delayRemainingTicks = delay;
		totalRunningTicks = 0;
		frameRunningTicks = 0;
		frameIndex = 0;
		loopIndex = 0;
		running = false;
		complete = false;
		return this;
	}

	/**
	 * @param code code to be run when the animation starts
	 * @return the animation
	 */
	public GenericAnimation<T> onStart(Runnable code) {
		onStart = code;
		return this;
	}

	/**
	 * Sets the frame duration
	 * 
	 * @param ticks frame ticks
	 * @return the animation
	 */
	public GenericAnimation<T> frameDuration(long ticks) {
		frameDurationTicks = ticks;
		return this;
	}

	/**
	 * @return number of ticks before the animation starts
	 */
	public long delay() {
		return delay;
	}

	/**
	 * Sets the number of ticks before the animation starts.
	 * 
	 * @param ticks number of ticks before the animation starts
	 * @return the animation
	 */
	public GenericAnimation<T> delay(long ticks) {
		delay = delayRemainingTicks = ticks;
		return this;
	}

	/**
	 * @return number of times the animation is repeated
	 */
	public int repetitions() {
		return repetitions;
	}

	/**
	 * Sets the number of times the animation is repeated.
	 * 
	 * @param n number of times the animation is repeated
	 * @return the animation
	 */
	public GenericAnimation<T> repetitions(int n) {
		repetitions = n;
		return this;
	}

	/**
	 * Lets the animation repeat endlessly.
	 * 
	 * @return the animation
	 */
	public GenericAnimation<T> endless() {
		repetitions = INDEFINITE;
		return this;
	}

	/**
	 * Resets and starts the animation.
	 * 
	 * @return the animation
	 */
	public GenericAnimation<T> restart() {
		reset();
		run();
		return this;
	}

	/**
	 * Runs the animation.
	 * 
	 * @return the animation
	 */
	public GenericAnimation<T> run() {
		running = true;
		return this;
	}

	/**
	 * Stops the animation.
	 * 
	 * @return the animation
	 */
	public GenericAnimation<T> stop() {
		running = false;
		return this;
	}

	/**
	 * Advances the animation by one step.
	 * 
	 * @return the frame before the animation step
	 */
	public T animate() {
		T currentThing = things[frameIndex];
		advance();
		return currentThing;
	}

	/**
	 * @return the current frame/thing
	 */
	public T frame() {
		return things[frameIndex];
	}

	/**
	 * Advances the animation by a single step.
	 */
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
			} else if (frameIndex + 1 < things.length) {
				// start next frame
				frameIndex++;
				frameRunningTicks = 0;
			} else if (loopIndex + 1 < repetitions) {
				// start next loop
				loopIndex++;
				frameIndex = 0;
				frameRunningTicks = 0;
			} else if (repetitions != INDEFINITE) {
				// last loop complete
				complete = true;
				running = false;
			} else {
				loopIndex = 0;
				frameIndex = 0;
				frameRunningTicks = 0;
			}
		}
	}

	/**
	 * @param i index
	 * @return the thing at the given index
	 */
	public T frame(int i) {
		return things[i];
	}

	/**
	 * @return the current index of the animation
	 */
	public int frameIndex() {
		return frameIndex;
	}

	public void setFrameIndex(int i) {
		frameIndex = i;
		frameRunningTicks = 0;
	}

	/**
	 * @return number of ticks each frame takes
	 */
	public long getFrameDuration() {
		return frameDurationTicks;
	}

	/**
	 * @return number of ticks the complete animation takes
	 */
	public long duration() {
		return things.length * frameDurationTicks;
	}

	/**
	 * @return number of frames of this animation
	 */
	public int numFrames() {
		return things.length;
	}

	/**
	 * @return if the animation is running
	 */
	public boolean isRunning() {
		return running;
	}

	public void ensureRunning() {
		if (!running) {
			complete = false;
			running = true;
		}
	}

	/**
	 * @return if the animation has completed
	 */
	public boolean isComplete() {
		return complete;
	}

	/**
	 * @return if the animation has started
	 */
	public boolean hasStarted() {
		return running || complete;
	}
}