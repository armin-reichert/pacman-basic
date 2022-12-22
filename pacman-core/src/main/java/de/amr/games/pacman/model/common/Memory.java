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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import de.amr.games.pacman.lib.math.Vector2i;
import de.amr.games.pacman.model.common.actors.Ghost;

/**
 * @author Armin Reichert
 */
public class Memory {
	public boolean lastFoodFound;
	public Optional<Vector2i> foodFoundTile;
	public boolean energizerFound;
	public boolean bonusReached;
	public boolean pacMetKiller;
	public boolean pacPowered;
	public boolean pacPowerLost;
	public boolean pacPowerFading;
	public boolean ghostsKilled;
	public List<Ghost> edibleGhosts;
	public final List<Ghost> killedGhosts = new ArrayList<>(4);
	public Optional<Ghost> unlockedGhost;
	public String unlockReason;

	public Memory() {
		forgetEverything();
	}

	public void forgetEverything() {
		lastFoodFound = false;
		foodFoundTile = Optional.empty();
		energizerFound = false;
		bonusReached = false;
		pacMetKiller = false;
		pacPowered = false;
		pacPowerLost = false;
		pacPowerFading = false;
		ghostsKilled = false;
		edibleGhosts = Collections.emptyList();
		killedGhosts.clear();
		unlockedGhost = Optional.empty();
		unlockReason = null;
	}
}