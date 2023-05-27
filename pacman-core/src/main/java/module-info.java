/*
 * Copyright (c) 2021-2023 Armin Reichert (MIT License) See file LICENSE in repository root directory for details.
 */
module de.amr.games.pacman {

	requires org.tinylog.api;

	exports de.amr.games.pacman.controller;
	exports de.amr.games.pacman.event;
	exports de.amr.games.pacman.lib;
	exports de.amr.games.pacman.lib.anim;
	exports de.amr.games.pacman.lib.fsm;
	exports de.amr.games.pacman.lib.math;
	exports de.amr.games.pacman.lib.option;
	exports de.amr.games.pacman.lib.steering;
	exports de.amr.games.pacman.lib.timer;
	exports de.amr.games.pacman.model;
	exports de.amr.games.pacman.model.actors;
	exports de.amr.games.pacman.model.world;
}