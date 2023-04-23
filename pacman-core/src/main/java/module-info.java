/*
 * MIT License
 * 
 * Copyright (c) 2021-2023 Armin Reichert
 * 
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated
 * documentation files (the "Software"), to deal in the Software without restriction, including without limitation the
 * rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to
 * permit persons to whom the Software is furnished to do so, subject to the following conditions:
 * 
 * The above copyright notice and this permission notice shall be included in all copies or substantial portions of the
 * Software.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE
 * WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR
 * COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR
 * OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */
module de.amr.games.pacman {

	requires transitive org.apache.logging.log4j;

	exports de.amr.games.pacman.controller.common;
	exports de.amr.games.pacman.controller.mspacman;
	exports de.amr.games.pacman.controller.pacman;
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