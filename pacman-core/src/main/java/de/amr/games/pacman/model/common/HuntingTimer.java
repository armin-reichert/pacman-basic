/*
MIT License

Copyright (c) 2022 Armin Reichert

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

package de.amr.games.pacman.model.common;

import static de.amr.games.pacman.lib.Logging.log;

import java.util.List;

import de.amr.games.pacman.lib.TickTimer;

/**
 * @author Armin Reichert
 */
public class HuntingTimer extends TickTimer {

	/** Tells if the current hunting phase is "scattering". */
	public static boolean isScatteringPhase(int phase) {
		return phase % 2 == 0;
	}

	private int phase;

	public HuntingTimer() {
		super("HuntingTimer");
	}

	@Override
	public String toString() {
		int i = isScatteringPhase(phase) ? scatteringPhase() : chasingPhase();
		String whichPhase = List.of("First", "Second", "Third", "Fourth").get(i);
		return "%s: Phase %d (%s %s)".formatted(super.toString(), phase, whichPhase, phaseName());
	}

	public void startPhase(int phase, long duration) {
		this.phase = phase;
		setDurationTicks(duration).start();
		log("%s: started", this);
	}

	public int phase() {
		return phase;
	}

	public int scatteringPhase() {
		return phase % 2 == 0 ? phase / 2 : -1;
	}

	public int chasingPhase() {
		return phase % 2 == 1 ? phase / 2 : -1;
	}

	public String phaseName() {
		return phase % 2 == 0 ? "Scattering" : "Chasing";
	}
}