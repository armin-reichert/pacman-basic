package de.amr.games.pacman.controller;

import java.util.List;

import de.amr.games.pacman.model.common.Ghost;

class AutopilotData {

	static final int MAX_GHOST_AHEAD_DETECTION_DIST = 4; // tiles
	static final int MAX_GHOST_BEHIND_DETECTION_DIST = 2; // tiles
	static final int MAX_GHOST_CHASE_DIST = 10; // tiles
	static final int MAX_BONUS_HARVEST_DIST = 20; // tiles

	Ghost hunterAhead;
	double hunterAheadDistance;
	Ghost hunterBehind;
	double hunterBehindDistance;
	List<Ghost> frightenedGhosts;
	List<Double> frightenedGhostsDistance;

	@Override
	public String toString() {
		String s = "-- Begin autopilot info\n";
		if (hunterAhead != null) {
			s += String.format("Hunter ahead:  %s, distance: %.2g\n", hunterAhead.name, hunterAheadDistance);
		} else {
			s += "No hunter ahead\n";
		}
		if (hunterBehind != null) {
			s += String.format("Hunter behind: %s, distance: %.2g\n", hunterBehind.name, hunterBehindDistance);
		} else {
			s += "No hunter behind\n";
		}
		for (int i = 0; i < frightenedGhosts.size(); ++i) {
			Ghost ghost = frightenedGhosts.get(i);
			s += String.format("Prey: %s, distance: %.2g\n", ghost.name, frightenedGhostsDistance.get(i));
		}
		if (frightenedGhosts.isEmpty()) {
			s += "No prey\n";
		}
		s += "-- End autopilot info";
		return s;
	}
}