package de.amr.games.pacman.lib;

public class CountdownTimer {

	private long duration;
	private long running;

	public void tick() {
		if (running < duration) {
			++running;
		}
	}

	public long getDuration() {
		return duration;
	}

	public void setDuration(long ticks) {
		duration = ticks;
		running = 0;
	}

	public long running() {
		return running;

	}

	public long remaining() {
		return duration == Long.MAX_VALUE ? Long.MAX_VALUE : duration - running;
	}

	public boolean expired() {
		return running == duration;
	}
}