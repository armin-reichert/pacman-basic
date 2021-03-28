package de.amr.games.pacman.ui.swing.app;

import java.time.Duration;

/**
 * Controls the speed of the simulation.
 * 
 * @author Armin Reichert
 */
public class SpeedControl {

	private int targetFPS = 60;
	private double frameDurationNanos = 1e9 / targetFPS;

	private long totalFrames;
	private long lastFPS;

	private long framesCountedDuringLastSecond;
	private long framesCountStart;

	/**
	 * Executes the given work and sleeps as long as needed to keep the target frequency.
	 * 
	 * @param work some work to do during this frame
	 */
	public void frame(Runnable work) {
		long workStart, workEnd, workDuration;

		workStart = System.nanoTime();
		work.run();
		workEnd = System.nanoTime();
		workDuration = workEnd - workStart;

		++totalFrames;
		++framesCountedDuringLastSecond;
		if (workEnd - framesCountStart >= Duration.ofSeconds(1).toNanos()) {
			lastFPS = framesCountedDuringLastSecond;
			framesCountedDuringLastSecond = 0;
			framesCountStart = System.nanoTime();
		}

		double sleepMillis = Math.max((frameDurationNanos - workDuration) * 98 / 100, 0) / 1_000_000L;
		if (sleepMillis > 0) {
			try {
				Thread.sleep((long) sleepMillis);
			} catch (InterruptedException x) {
				x.printStackTrace();
			}
		}
	}

	public int getTargetFPS() {
		return targetFPS;
	}

	public void setTargetFPS(int framesPerSecond) {
		targetFPS = framesPerSecond;
		frameDurationNanos = Duration.ofSeconds(1).toNanos() / targetFPS;
	}

	public long getLastFPS() {
		return lastFPS;
	}

	public long getTotalFrames() {
		return totalFrames;
	}

	/**
	 * @return the number of ticks equivalent to the given amount of seconds wrt. to the current clock
	 *         frequency
	 */
	public int sec(double seconds) {
		return (int) (seconds * targetFPS);
	}

	/**
	 * Computes the animation frame at the current clock tick. For example, if each animation frame
	 * shall take 2 ticks and the complete animation consists of 4 different frames, then the call
	 * <code>frame(2, 4)</code> produces repeatedly the sequence <code>0, 0, 1, 1, 2, 2, 3, 3</code>.
	 * 
	 * @param frameDurationTicks duration in ticks of one animation frame
	 * @param numFrames          number of frames of the complete animation
	 * @return animation frame for current clock tick
	 */
	public int frame(int frameDurationTicks, int numFrames) {
		return (int) (totalFrames / frameDurationTicks) % numFrames;
	}
}