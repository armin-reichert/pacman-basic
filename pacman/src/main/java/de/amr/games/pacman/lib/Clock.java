package de.amr.games.pacman.lib;

/**
 * A clock producing the ticks driving the game.
 * 
 * @author Armin Reichert
 *
 */
public class Clock {

	public int targetFrequency = 60;
	public long frequency;
	public long ticksTotal;

	private long ticksCounted;
	private long ticksCountStart;

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
		long sleep = Math.max(1_000_000_000 / targetFrequency - duration, 0);
		if (sleep > 0) {
			try {
				Thread.sleep(sleep / 1_000_000); // nanos -> millis
			} catch (InterruptedException x) {
				x.printStackTrace();
			}
		}
	}

	public int sec(double seconds) {
		return (int) (seconds * targetFrequency);
	}

	public int frame(int frameTicks, int numFrames) {
		return (int) (ticksTotal / frameTicks) % numFrames;
	}

	/**
	 * Executes repeatedly either code for specified time and then gets idle for same time.
	 * 
	 * @param durationTicks number of ticks that code is executed
	 * @param code          either code
	 */
	public void runOrBeIdle(int durationTicks, Runnable code) {
		if (ticksTotal % (2 * durationTicks) < durationTicks) {
			code.run();
		}
	}

	/**
	 * Executes alternatively either code for specified time and then other code.
	 * 
	 * @param durationTicks number of ticks that either or other code is executed
	 * @param eitherCode    either code
	 * @param otherCode     other code
	 */
	public void runAlternating(int durationTicks, Runnable eitherCode, Runnable otherCode) {
		if (ticksTotal % (2 * durationTicks) < durationTicks) {
			eitherCode.run();
		} else {
			otherCode.run();
		}
	}

	/**
	 * Executes alternatively either code for specified time and then other code. After each phase the
	 * third code is executed.
	 * 
	 * @param durationTicks number of ticks that either or other code is executed
	 * @param eitherCode    either code
	 * @param otherCode     other code
	 * @param andThen       code executed after one either/other phase has been executed
	 */
	public void runAlternating(int durationTicks, Runnable eitherCode, Runnable otherCode, Runnable andThen) {
		long frame = ticksTotal % (2 * durationTicks);
		if (frame < durationTicks) {
			eitherCode.run();
		} else {
			otherCode.run();
		}
		if (frame == 2 * durationTicks - 1) {
			andThen.run();
		}
	}
}