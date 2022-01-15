/*
MIT License

Copyright (c) 2021 Armin Reichert

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

	public static final long INDEFINITE = Long.MAX_VALUE;

	public static final long sec_to_ticks(double sec) {
		return Math.round(sec * 60);
	}

	public enum TickTimerState {
		READY, RUNNING, STOPPED, EXPIRED;
	}

	public static boolean trace = false;

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
		return String.format("[%s: state=%s t=%d remaining=%s]", name, state, t,
				(ticksRemaining() == INDEFINITE ? "indefinite" : String.valueOf(ticksRemaining())));
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
			return; // TODO handle this properly
		}
		if (state == STOPPED) {
			return;
		}
		++t;
		if (t == duration / 2) {
			fireEvent(new TickTimerEvent(Type.HALF_EXPIRED, t));
		}
		if (t == duration) {
			expire();
		}
	}

	public void expire() {
		state = EXPIRED;
		trace("%s expired", this);
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