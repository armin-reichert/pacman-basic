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

package de.amr.games.pacman.controller.pacman;

import de.amr.games.pacman.lib.TimedSeq;
import de.amr.games.pacman.model.common.Ghost;
import de.amr.games.pacman.model.common.Pac;

/**
 * @author Armin Reichert
 */
public class IntroContext {

	public static class GhostPortrait {
		public int id;
		public String nickname;
		public String character;
		public boolean pictureVisible = false;
		public boolean characterVisible = false;
		public boolean nicknameVisible = false;
		public int tileX;
		public int tileY;

		public GhostPortrait(int id, String nickname, String character, int tileY) {
			this.id = id;
			this.nickname = nickname;
			this.character = character;
			this.tileX = 4;
			this.tileY = tileY;
		}
	}

	public TimedSeq<Boolean> fastBlinking = TimedSeq.pulse().frameDuration(10);
	public TimedSeq<Boolean> slowBlinking = TimedSeq.pulse().frameDuration(30);
//	public int topY = t(6);
	public GhostPortrait[] portraits;
	public Pac pacMan;
	public Ghost[] ghosts;
	public int ghostIndex;
	public long ghostKilledTime;

	public void selectGhost(int index) {
		ghostIndex = index;
		portraits[ghostIndex].pictureVisible = true;
	}
}