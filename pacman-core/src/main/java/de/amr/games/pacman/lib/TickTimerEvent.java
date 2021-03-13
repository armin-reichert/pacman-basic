package de.amr.games.pacman.lib;

public class TickTimerEvent {

	public enum Type {
		RESET, STARTED, STOPPED, EXPIRED, HALF_EXPIRED
	};

	public TickTimerEvent(Type type, long ticks) {
		this.type = type;
		this.ticks = ticks;
	}

	public TickTimerEvent(Type type) {
		this.type = type;
		ticks = 0;
	}

	public final Type type;
	public final long ticks;
}