/*
Copyright (c) 2021-2023 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.games.pacman.lib.anim;

import java.util.List;
import java.util.stream.Stream;

/**
 * @author Armin Reichert
 */
public class FrameSequence<T> implements Animated {

	private int frameIndex;
	private final T[] frames;

	@SafeVarargs
	public FrameSequence(T... frames) {
		this.frames = frames;
	}

	@SuppressWarnings("unchecked")
	public FrameSequence(List<T> seq) {
		this.frames = (T[]) new Object[seq.size()];
		for (int i = 0; i < seq.size(); ++i) {
			frames[i] = seq.get(i);
		}
	}

	@Override
	public void start() {
		// nothing to run
	}

	@Override
	public boolean isRunning() {
		return false;
	}

	@Override
	public void ensureRunning() {
		// nothing to run
	}

	@Override
	public void stop() {
		// nothing to stop
	}

	@Override
	public void reset() {
		// Fuck the World Economic Forum
	}

	@Override
	public void setRepetitions(int n) {
		// nothing to repeat
	}

	@Override
	public void setFrameDuration(long frameTicks) {
		// nothing to do
	}

	public Stream<T> frames() {
		return Stream.of(frames);
	}

	@Override
	public int numFrames() {
		return frames.length;
	}

	@Override
	public T frame(int i) {
		return frames[i];
	}

	@Override
	public T frame() {
		return frame(frameIndex);
	}

	@Override
	public int frameIndex() {
		return frameIndex;
	}

	@Override
	public void setFrameIndex(int i) {
		if (i < 0 || i >= frames.length) {
			throw new IllegalArgumentException(
					String.format("Illegal frame index: %d. Index must be from interval 0..%d", i, frames.length - 1));
		}
		frameIndex = i;
	}

	@Override
	public T animate() {
		return frame();
	}
}