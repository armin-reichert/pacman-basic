package de.amr.games.pacman.lib;

public class CountdownTimer {

	private long duration;
	private long tick; // 0 .. duration - 1

	public void run() {
		if (!expired()) {
			++tick;
		}
	}

	public boolean expired() {
		return tick == duration - 1;
	}

	public long getDuration() {
		return duration;
	}

	public void setDuration(long ticks) {
		if (ticks < 0) {
			throw new IllegalArgumentException("Duration cannot be negative, but is " + ticks);
		}
		duration = ticks;
		tick = -1;
	}

	public long running() {
		return tick;
	}

	public long remaining() {
		return duration == Long.MAX_VALUE ? Long.MAX_VALUE : duration - tick;
	}
}