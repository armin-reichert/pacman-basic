/*
MIT License

Copyright (c) 2021-22 Armin Reichert

Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files (the "Software"), to deal
in the Software without restriction, including without limitation the rights
to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
copies of the Software, and to permit persons to whom the Software is
furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all
copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
SOFTWARE.
 */
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

	public enum TickTimerState {
		READY, RUNNING, STOPPED, EXPIRED;
	}

	/** Timer value representing "forever". */
	public static final long INDEFINITE = Long.MAX_VALUE;
	private static final int TICKS_PER_SEC = 60;

	public static boolean trace = false;

	private void trace(String msg, Object... args) {
		if (trace) {
			Logging.log(msg, args);
		}
	}

	/**
	 * @param sec seconds
	 * @return number of ticks representing given seconds at 60Hz
	 */
	public static final long sec_to_ticks(double sec) {
		return Math.round(sec * TICKS_PER_SEC);
	}

	public static String ticksAsString(long ticks) {
		return ticks == INDEFINITE ? "indefinite" : "%d".formatted(ticks);
	}

	private final String name;
	private TickTimerState state;
	private long duration;
	private long tick; // 0..(duration - 1)
	private List<Consumer<TickTimerEvent>> subscribers;

	public TickTimer(String name) {
		this.name = name;
		setDurationIndefinite();
	}

	@Override
	public String toString() {
		return "[%s %s tick:%s remaining:%s]".formatted(name, state, ticksAsString(tick), ticksAsString(remaining()));
	}

	public TickTimerState state() {
		return state;
	}

	public String name() {
		return name;
	}

	/**
	 * Sets timer to given duration and resets timer state to {@link TickTimerState#READY}.
	 * 
	 * @param ticks timer duration in ticks
	 * @return itself
	 */
	public TickTimer setDurationTicks(long ticks) {
		duration = ticks;
		tick = 0;
		state = READY;
		trace("%s set", this);
		fireEvent(new TickTimerEvent(Type.RESET, ticks));
		return this;
	}

	public TickTimer setDurationSeconds(double seconds) {
		return setDurationTicks(sec_to_ticks(seconds));
	}

	/**
	 * Reset the time to run {@link #INDEFINITE}.
	 */
	public TickTimer setDurationIndefinite() {
		return setDurationTicks(INDEFINITE);
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

	public TickTimer start() {
		if (state == RUNNING) {
			trace("%s not started, already running", this);
			return this;
		}
		if (state == STOPPED || state == READY) {
			state = RUNNING;
			trace("%s started", this);
			fireEvent(new TickTimerEvent(Type.STARTED));
			return this;
		} else {
			throw new IllegalStateException(String.format("Timer %s cannot be started when in state %s", this, state));
		}
	}

	public TickTimer stop() {
		if (state == STOPPED) {
			trace("%s not stopped, already stopped", this);
		} else if (state == RUNNING) {
			state = STOPPED;
			trace("%s stopped", this);
			fireEvent(new TickTimerEvent(Type.STOPPED));
		}
		return this;
	}

	public TickTimer advance() {
		if (state == READY) {
			return this; // TODO handle this properly
		}
		if (state == STOPPED || state == EXPIRED) {
			return this;
		}
		++tick;
		if (tick == duration / 2) {
			fireEvent(new TickTimerEvent(Type.HALF_EXPIRED, tick));
		}
		if (tick == duration) {
			expire();
		}
		return this;
	}

	public TickTimer expire() {
		state = EXPIRED;
		trace("%s expired", this);
		fireEvent(new TickTimerEvent(Type.EXPIRED, tick));
		return this;
	}

	public boolean hasExpired() {
		return state == EXPIRED;
	}

	public boolean isReady() {
		return state == READY;
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

	public long tick() {
		return tick;
	}

	public boolean atSecond(double seconds) {
		return tick == sec_to_ticks(seconds);
	}

	public long remaining() {
		return duration == INDEFINITE ? INDEFINITE : duration - tick;
	}
}