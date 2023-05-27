/*
Copyright (c) 2021-2023 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.games.pacman.lib.anim;

/**
 * @author Armin Reichert
 */
public interface Animated {

	boolean isRunning();

	void ensureRunning();

	Object animate();

	void start();

	void stop();

	void reset();

	/**
	 * Resets and starts the animation.
	 */
	default void restart() {
		reset();
		start();
	}

	void setRepetitions(int n);

	Object frame(int i);

	Object frame();

	int frameIndex();

	void setFrameIndex(int i);

	int numFrames();

	void setFrameDuration(long frameTicks);
}