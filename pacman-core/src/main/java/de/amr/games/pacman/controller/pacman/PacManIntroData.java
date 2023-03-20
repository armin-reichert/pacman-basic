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
package de.amr.games.pacman.controller.pacman;

import de.amr.games.pacman.controller.common.GameController;
import de.amr.games.pacman.lib.anim.Pulse;
import de.amr.games.pacman.model.common.actors.Ghost;
import de.amr.games.pacman.model.common.actors.Pac;

/**
 * @author Armin Reichert
 */
public class PacManIntroData {
	public static final float CHASING_SPEED = 1.1f;
	public static final int LEFT_TILE = 4;
	public static final String[] CHARACTERS = { "SHADOW", "SPEEDY", "BASHFUL", "POKEY" };
	public final GameController gameController;
	public final Pulse blinking = new Pulse(10, true);
	public final Pac pacMan = new Pac("Pac-Man");
	public final Ghost[] ghosts = new Ghost[] { //
			new Ghost(Ghost.ID_RED_GHOST, "Blinky"), //
			new Ghost(Ghost.ID_PINK_GHOST, "Pinky"), //
			new Ghost(Ghost.ID_CYAN_GHOST, "Inky"), //
			new Ghost(Ghost.ID_ORANGE_GHOST, "Clyde"), //
	};
	public final boolean[] pictureVisible = { false, false, false, false };
	public final boolean[] nicknameVisible = { false, false, false, false };
	public final boolean[] characterVisible = { false, false, false, false };
	public boolean creditVisible = false;
	public boolean titleVisible = false;
	public int ghostIndex;
	public long ghostKilledTime;

	public PacManIntroData(GameController gameController) {
		this.gameController = gameController;
	}
}