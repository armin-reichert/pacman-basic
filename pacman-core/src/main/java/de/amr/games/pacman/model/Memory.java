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
package de.amr.games.pacman.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import de.amr.games.pacman.lib.math.Vector2i;
import de.amr.games.pacman.model.actors.Ghost;

/**
 * @author Armin Reichert
 */
public class Memory {
	public Optional<Vector2i> foodFoundTile;
	public boolean energizerFound;
	public int bonusReachedIndex; // 0=first, 1=second, -1=no bonus
	public boolean pacKilled;
	public boolean pacPowerGained;
	public boolean pacPowerLost;
	public boolean pacPowerFading;
	public List<Ghost> edibleGhosts;
	public final List<Ghost> killedGhosts = new ArrayList<>(4);
	public Optional<Ghost> unlockedGhost;
	public String unlockReason;

	public Memory() {
		forgetEverything();
	}

	public void forgetEverything() {
		foodFoundTile = Optional.empty();
		energizerFound = false;
		bonusReachedIndex = -1;
		pacKilled = false;
		pacPowerGained = false;
		pacPowerLost = false;
		pacPowerFading = false;
		edibleGhosts = Collections.emptyList();
		killedGhosts.clear();
		unlockedGhost = Optional.empty();
		unlockReason = null;
	}

	public boolean edibleGhostsExist() {
		return !edibleGhosts.isEmpty();
	}
}