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
package de.amr.games.pacman.controller.mspacman;

import static de.amr.games.pacman.lib.math.Vector2i.v2i;
import static de.amr.games.pacman.model.common.world.World.HTS;
import static de.amr.games.pacman.model.common.world.World.t;

import java.util.Arrays;
import java.util.List;

import de.amr.games.pacman.controller.common.GameController;
import de.amr.games.pacman.controller.common.SceneControllerContext;
import de.amr.games.pacman.lib.anim.Pulse;
import de.amr.games.pacman.lib.math.Vector2i;
import de.amr.games.pacman.lib.timer.TickTimer;
import de.amr.games.pacman.model.common.actors.Ghost;
import de.amr.games.pacman.model.common.actors.Pac;

public class MsPacManIntroData extends SceneControllerContext {
	public final Vector2i redGhostEndPosition = v2i(t(8), t(11));
	public final Vector2i turningPoint = v2i(t(6), t(20)).plus(0, HTS);
	public final int msPacManStopX = t(15);
	public final Vector2i titlePosition = v2i(t(10), t(8));
	public final Pulse blinking = new Pulse(30, true);
	public final TickTimer lightsTimer = new TickTimer("lights-timer");
	public final float actorSpeed = 1.1f;
	public final Pac pac;
	public final List<Ghost> ghosts;
	public int ghostIndex = 0;
	public boolean creditVisible = false;

	public MsPacManIntroData(GameController gameController) {
		super(gameController);
		pac = gameController.game().createPac();
		ghosts = Arrays.asList(gameController.game().createGhosts());
	}
}