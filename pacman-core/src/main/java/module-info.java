module de.amr.games.pacman {

	requires java.base;
	requires java.desktop;

	exports de.amr.games.pacman.controller;
	exports de.amr.games.pacman.lib;
	exports de.amr.games.pacman.model.common;
	exports de.amr.games.pacman.model.mspacman;
	exports de.amr.games.pacman.model.pacman;
	exports de.amr.games.pacman.sound;
	exports de.amr.games.pacman.ui;
	exports de.amr.games.pacman.ui.animation;
	exports de.amr.games.pacman.ui.mspacman;
	exports de.amr.games.pacman.ui.pacman;
	exports de.amr.games.pacman.world;
}