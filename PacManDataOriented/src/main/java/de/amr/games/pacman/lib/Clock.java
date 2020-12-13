package de.amr.games.pacman.lib;

public class Clock {

	public int targetFPS = 60;
	public long fps;
	public long frames;
	public long framesTotal;
	public long fpsCountStartTime;

	public int sec(float seconds) {
		return (int) (seconds * targetFPS);
	}

	public int frame(int frameTicks, int numFrames) {
		return (int) (framesTotal / frameTicks) % numFrames;
	}

	/**
	 * Executes repeatedly either code for specified time and then gets idle for same time.
	 * 
	 * @param durationTicks number of ticks that code is executed
	 * @param code          either code
	 */
	public void runOrBeIdle(int durationTicks, Runnable code) {
		if (framesTotal % (2 * durationTicks) < durationTicks) {
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
		if (framesTotal % (2 * durationTicks) < durationTicks) {
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
		long frame = framesTotal % (2 * durationTicks);
		if (frame < durationTicks) {
			eitherCode.run();
		} else {
			otherCode.run();
		}
		if (frame == 2 * durationTicks - 1) {
			andThen.run();
		}
	}

	public void tick(Runnable frame) {
		long frameStartTime = System.nanoTime();
		frame.run();
		long frameEndTime = System.nanoTime();
		long frameDuration = frameEndTime - frameStartTime;
		++frames;
		++framesTotal;
		if (frameEndTime - fpsCountStartTime >= 1_000_000_000) {
			fps = frames;
			frames = 0;
			fpsCountStartTime = System.nanoTime();
		}
		long sleepTime = Math.max(1_000_000_000 / targetFPS - frameDuration, 0);
		if (sleepTime > 0) {
			try {
				Thread.sleep(sleepTime / 1_000_000); // milliseconds
			} catch (InterruptedException x) {
				x.printStackTrace();
			}
		}
	}
}