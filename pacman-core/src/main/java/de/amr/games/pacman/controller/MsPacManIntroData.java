/*
MIT License

Copyright (c) 2021-2023 Armin Reichert

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
package de.amr.games.pacman.controller;

import static de.amr.games.pacman.lib.Globals.TS;
import static de.amr.games.pacman.lib.Globals.isOdd;
import static de.amr.games.pacman.lib.Globals.v2i;

import java.util.BitSet;
import java.util.List;

import de.amr.games.pacman.lib.anim.Pulse;
import de.amr.games.pacman.lib.math.Vector2i;
import de.amr.games.pacman.lib.timer.TickTimer;
import de.amr.games.pacman.model.GameModel;
import de.amr.games.pacman.model.actors.Ghost;
import de.amr.games.pacman.model.actors.Pac;

/**
 * @author Armin Reichert
 */
public class MsPacManIntroData {
	//@formatter:off
	public GameController gameController;
	public float          speed                = 1.1f;
	public int            stopY                = TS * 11 + 1;
	public int            stopX                = TS * 6 - 3; 
	public int            stopMsPacX           = TS * 15 + 3;
	public Vector2i       titlePosition        = v2i(TS * 10, TS * 8);
	public Pulse          blinking             = new Pulse(30, true);
	public TickTimer      marqueeTimer         = new TickTimer("marquee-timer");
	public int            numBulbs             = 96;
	public int            bulbDistance         = 16;
	public Pac            msPacMan             = new Pac("Ms. Pac-Man");
	public List<Ghost>    ghosts               = List.of(
		new Ghost(GameModel.RED_GHOST,    "Blinky"),
		new Ghost(GameModel.PINK_GHOST,   "Pinky"),
		new Ghost(GameModel.CYAN_GHOST,   "Inky"),
		new Ghost(GameModel.ORANGE_GHOST, "Sue")
		);
	public int ghostIndex = 0;
	//@formatter:on

	public MsPacManIntroData(GameController gameController) {
		this.gameController = gameController;
	}

	private int onIndex(long t, int i) {
		return (int) (i * bulbDistance + t) % numBulbs;
	}

	/**
	 * In the Arcade game, 6 of the 96 bulbs are switched-on every frame, shifting every tick the bulbs in the leftmost
	 * column are switched-off every second frame. Maybe a bug?
	 * 
	 * @return bitset indicating which marquee bulbs are on
	 */
	public BitSet marqueeState() {
		long t = marqueeTimer.tick();
		var on = new BitSet(numBulbs);
		on.set(onIndex(t, 0));
		on.set(onIndex(t, 1));
		on.set(onIndex(t, 2));
		on.set(onIndex(t, 3));
		on.set(onIndex(t, 4));
		on.set(onIndex(t, 5));
		for (int i = 81; i < numBulbs; ++i) {
			if (i >= 81 && isOdd(i)) {
				on.clear(i);
			}
		}
		return on;
	}
}