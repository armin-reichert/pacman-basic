/*
Copyright (c) 2021-2023 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.games.pacman.model;

import de.amr.games.pacman.lib.Vector2i;
import de.amr.games.pacman.model.actors.Ghost;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

/**
 * @author Armin Reichert
 */
public class Memory {
	public Optional<Vector2i> foodFoundTile;
	public boolean energizerFound;
	public int bonusReachedIndex; // 0=first, 1=second, -1=no bonus
	public boolean pacKilled;
	public boolean pacPowerActive;
	public boolean pacPowerStarts;
	public boolean pacPowerLost;
	public boolean pacPowerFading;
	public List<Ghost> pacPrey;
	public final List<Ghost> killedGhosts = new ArrayList<>(4);

	public Memory() {
		forgetEverything();
	}

	public void forgetEverything() {
		foodFoundTile = Optional.empty();
		energizerFound = false;
		bonusReachedIndex = -1;
		pacKilled = false;
		pacPowerActive = false;
		pacPowerStarts = false;
		pacPowerLost = false;
		pacPowerFading = false;
		pacPrey = Collections.emptyList();
		killedGhosts.clear();
	}

	@Override
	public String toString() {

		var foodText = "";
		if (foodFoundTile.isPresent()) {
			foodText = String.format("%s at %s", energizerFound ? "Energizer" : "Pellet", foodFoundTile.get());
		}

		var bonusText = "";
		if (bonusReachedIndex != -1) {
			bonusText = String.format("Bonus %d reached", bonusReachedIndex);
		}

		var powerText = "";
		if (pacPowerStarts) {
			powerText += " starts";
		}
		if (pacPowerActive) {
			powerText += " active";
		}
		if (pacPowerFading) {
			powerText += " fading";
		}
		if (pacPowerLost) {
			powerText += " lost";
		}
		if (!powerText.isEmpty()) {
			powerText = "Pac power: " + powerText;
		}

		var pacKilledText = pacKilled ? "Pac killed" : "";

		var preyText = "";
		if (!pacPrey.isEmpty()) {
			preyText = String.format("Prey: %s", pacPrey);
		}

		var killedGhostsText = killedGhosts.isEmpty() ? "" : killedGhosts.toString();

		return String.format("%s%s%s%s%s%s", foodText, bonusText, powerText, pacKilledText, preyText, killedGhostsText);
	}

	public boolean edibleGhostsExist() {
		return !pacPrey.isEmpty();
	}
}