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

import java.util.List;
import java.util.stream.Stream;

/**
 * @author Armin Reichert
 */
public class SpriteArray<THING> implements SpriteAnimation<THING> {

	private int frameIndex;
	private final THING[] things;

	@SafeVarargs
	public SpriteArray(THING... things) {
		this.things = things;
	}

	@SuppressWarnings("unchecked")
	public SpriteArray(List<THING> list) {
		this.things = (THING[]) new Object[list.size()];
		for (int i = 0; i < list.size(); ++i) {
			things[i] = list.get(i);
		}
	}

	public Stream<THING> frames() {
		return Stream.of(things);
	}

	public int numFrames() {
		return things.length;
	}

	@Override
	public THING frame(int i) {
		return things[i];
	}

	@Override
	public THING frame() {
		return frame(frameIndex);
	}

	@Override
	public void setFrameIndex(int i) {
		frameIndex = i;
	}

	@Override
	public THING animate() {
		return frame();
	}
}