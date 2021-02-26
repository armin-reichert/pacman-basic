module de.amr.games.pacman.jpms_pacman_core {

	requires java.base;
	requires java.desktop;

	exports de.amr.games.pacman.controller;
	exports de.amr.games.pacman.heaven;
	exports de.amr.games.pacman.lib;
	exports de.amr.games.pacman.model.common;
	exports de.amr.games.pacman.model.mspacman;
	exports de.amr.games.pacman.model.pacman;
	exports de.amr.games.pacman.sound;
	exports de.amr.games.pacman.ui;
	exports de.amr.games.pacman.ui.animation;
	exports de.amr.games.pacman.world;
}