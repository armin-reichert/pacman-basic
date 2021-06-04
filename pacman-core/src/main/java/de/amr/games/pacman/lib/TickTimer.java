package de.amr.games.pacman.lib;

import static de.amr.games.pacman.lib.TickTimer.TickTimerState.EXPIRED;
import static de.amr.games.pacman.lib.TickTimer.TickTimerState.READY;
import static de.amr.games.pacman.lib.TickTimer.TickTimerState.RUNNING;
import static de.amr.games.pacman.lib.TickTimer.TickTimerState.STOPPED;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

import de.amr.games.pacman.lib.TickTimerEvent.Type;

/**
 * A simple, but useful, passive timer counting ticks.
 * 
 * @author Armin Reichert
 */
public class TickTimer {

	public static final long INDEFINITE = Long.MAX_VALUE;

	public static final long sec_to_ticks(double sec) {
		return Math.round(sec * 60);
	}

	public enum TickTimerState {
		READY, RUNNING, STOPPED, EXPIRED;
	}

	public static boolean trace = true;

	private void trace(String msg, Object... args) {
		if (trace) {
			Logging.log(msg, args);
		}
	}

	private final String name;
	private List<Consumer<TickTimerEvent>> subscribers;
	private TickTimerState state;
	private long duration;
	private long t; // 0..(duration - 1)

	public TickTimer(String name) {
		this.name = name;
		reset();
	}

	public String getName() {
		return name;
	}

	@Override
	public String toString() {
		return String.format("TickTimer %s: state=%s t=%d remaining=%d", state, name, t, ticksRemaining());
	}

	public void addEventListener(Consumer<TickTimerEvent> subscriber) {
		if (subscribers == null) {
			subscribers = new ArrayList<>(3);
		}
		subscribers.add(subscriber);
	}

	public void removeEventListener(Consumer<TickTimerEvent> subscriber) {
		if (subscribers != null) {
			subscribers.remove(subscriber);
		}
	}

	private void fireEvent(TickTimerEvent e) {
		if (subscribers != null) {
			subscribers.forEach(subscriber -> subscriber.accept(e));
		}
	}

	public void reset(long ticks) {
		state = READY;
		t = 0;
		duration = ticks;
		trace("%s got reset", this);
		fireEvent(new TickTimerEvent(Type.RESET, duration));
	}

	public void reset() {
		reset(INDEFINITE);
	}

	public void resetSeconds(double seconds) {
		reset((long) (seconds * 60));
	}

	public void start() {
		if (state == RUNNING) {
			trace("%s not started, already running", this);
			return;
		}
		if (state == STOPPED || state == READY) {
			state = RUNNING;
			trace("%s started", this);
			fireEvent(new TickTimerEvent(Type.STARTED));
		} else {
			throw new IllegalStateException(String.format("Timer %s cannot be started when in state %s", this, state));
		}
	}

	public void stop() {
		if (state == STOPPED) {
			trace("%s not stopped, already stopped", this);
			return;
		}
		if (state == RUNNING) {
			state = STOPPED;
			trace("%s stopped", this);
			fireEvent(new TickTimerEvent(Type.STOPPED));
		}
	}

	public void tick() {
		if (state == READY) {
			throw new IllegalStateException(String.format("Timer %s not ticked, is ready", this));
		}
		if (state == EXPIRED) {
			throw new IllegalStateException(String.format("Timer %s has expired", name));
		}
		if (state == STOPPED) {
			trace("%s not ticked, is stopped", this);
			return;
		}
		++t;
		trace("%s ticked", this);
		if (t == duration / 2) {
			fireEvent(new TickTimerEvent(Type.HALF_EXPIRED, t));
		}
		if (t == duration) {
			state = EXPIRED;
			trace("%s expired", this);
			fireEvent(new TickTimerEvent(Type.EXPIRED, t));
			return;
		}
	}

	public void forceExpiration() {
		state = EXPIRED;
		trace("%s forced to expire", this);
		fireEvent(new TickTimerEvent(Type.EXPIRED, t));
	}

	public boolean hasExpired() {
		return state == EXPIRED;
	}

	public boolean isRunning() {
		return state == RUNNING;
	}

	public boolean isStopped() {
		return state == STOPPED;
	}

	public long duration() {
		return duration;
	}

	public long ticked() {
		return t;
	}

	public long ticksRemaining() {
		return duration == INDEFINITE ? INDEFINITE : duration - t;
	}

	public boolean isRunningSeconds(double seconds) {
		return t == (long) (seconds * 60);
	}

	public boolean hasJustStarted() {
		return t == 1;
	}
}