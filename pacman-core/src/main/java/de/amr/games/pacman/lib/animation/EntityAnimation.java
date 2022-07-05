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
package de.amr.games.pacman.lib.animation;

/**
 * @author Armin Reichert
 */
public interface EntityAnimation {

	default void run() {
	}

	default boolean isRunning() {
		return true;
	}

	default void ensureRunning() {
	}

	default void advance() {
	}

	default Object animate() {
		return null;
	}

	default void stop() {
	}

	default void reset() {
	}

	default void restart() {
		reset();
		run();
	}

	default void repetions(int n) {
	}

	default Object frame(int i) {
		return null; // makes no sense here
	}

	default Object frame() {
		return null; // makes no sense here
	}

	default void setFrameIndex(int i) {
	}
}