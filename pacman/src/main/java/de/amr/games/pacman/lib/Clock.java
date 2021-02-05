package de.amr.games.pacman.lib;

import static de.amr.games.pacman.lib.Logging.log;

/**
 * A clock producing the ticks driving the game.
 * 
 * @author Armin Reichert
 */
public class Clock {

	public int targetFrequency = 60;
	public long frequency;
	public long ticksTotal;

	private long ticksCounted;
	private long ticksCountStart;

	/**
	 * Executes the given work and sleeps a bit to keep the target frequency.
	 * 
	 * @param work some work to do during this frame
	 */
	public void tick(Runnable work) {
		long start, end, duration;
		start = System.nanoTime();
		work.run();
		end = System.nanoTime();
		duration = end - start;
		++ticksTotal;
		++ticksCounted;
		if (end - ticksCountStart >= 1_000_000_000) {
			frequency = ticksCounted;
			ticksCounted = 0;
			ticksCountStart = System.nanoTime();
		}
		long sleep = Math.max(1_000_000_000L / targetFrequency - duration, 0);
		if (sleep > 0) {
			try {
				Thread.sleep(sleep / 1_000_000); // nanos -> millis
			} catch (InterruptedException x) {
				x.printStackTrace();
			}
		}
		log("work time: %.2g, sleep time: %.2g", duration / 1000_000.0, sleep / 1000_000.0);
	}

	/**
	 * @return the number of ticks equivalent to the given amount of seconds wrt. to the current clock
	 *         frequency
	 */
	public int sec(double seconds) {
		return (int) (seconds * targetFrequency);
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
		return (int) (ticksTotal / frameDurationTicks) % numFrames;
	}
}