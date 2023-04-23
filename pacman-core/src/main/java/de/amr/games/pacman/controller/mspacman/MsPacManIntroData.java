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
package de.amr.games.pacman.controller.mspacman;

import static de.amr.games.pacman.lib.Globals.HTS;
import static de.amr.games.pacman.lib.Globals.TS;
import static de.amr.games.pacman.lib.Globals.v2i;

import java.util.List;

import de.amr.games.pacman.controller.common.GameController;
import de.amr.games.pacman.lib.anim.Pulse;
import de.amr.games.pacman.lib.math.Vector2i;
import de.amr.games.pacman.lib.timer.TickTimer;
import de.amr.games.pacman.model.actors.Ghost;
import de.amr.games.pacman.model.actors.Pac;

/**
 * @author Armin Reichert
 */
public class MsPacManIntroData {
	public final GameController gameController;
	public static final float GUYS_SPEED = 1.1f;
	public static final Vector2i BLINKY_END_TILE = v2i(TS * (8), TS * (11));
	public static final Vector2i TURNING_POSITION = v2i(TS * (6), TS * (20)).plus(0, HTS);
	public static final int MS_PACMAN_STOP_X = TS * (15);
	public static final Vector2i TITLE_TILE = v2i(TS * (10), TS * (8));
	public final Pulse blinking = new Pulse(30, true);
	public final TickTimer lightsTimer = new TickTimer("lights-timer");
	public final Pac msPacMan = new Pac("Ms. Pac-Man");
	public final List<Ghost> ghosts = List.of( //
			new Ghost(Ghost.ID_RED_GHOST, "Blinky"), //
			new Ghost(Ghost.ID_PINK_GHOST, "Pinky"), //
			new Ghost(Ghost.ID_CYAN_GHOST, "Inky"), //
			new Ghost(Ghost.ID_ORANGE_GHOST, "Sue") //
	);
	int ghostIndex = 0;

	public MsPacManIntroData(GameController gameController) {
		this.gameController = gameController;
	}

	public int ghostIndex() {
		return ghostIndex;
	}
}