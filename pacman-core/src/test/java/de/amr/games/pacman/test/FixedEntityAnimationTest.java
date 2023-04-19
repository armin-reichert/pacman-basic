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

package de.amr.games.pacman.test;

import static org.junit.Assert.assertEquals;

import org.junit.Before;
import org.junit.Test;

import de.amr.games.pacman.lib.anim.FrameSequence;

/**
 * @author Armin Reichert
 */
public class FixedEntityAnimationTest {

	private FrameSequence<String> animation;

	@Before
	public void setUp() {
		animation = new FrameSequence<>("A", "B", "C");
	}

	@Test
	public void testInit() {
		assertEquals(3, animation.numFrames());
		assertEquals(0, animation.frameIndex());
		assertEquals("A", animation.frame());
	}

	@Test
	public void testAnimate() {
		var frame = animation.animate();
		assertEquals(3, animation.numFrames());
		assertEquals(0, animation.frameIndex());
		assertEquals("A", frame);
	}

	@Test
	public void testSetFrameIndex() {
		animation.setFrameIndex(2);
		assertEquals(2, animation.frameIndex());
		assertEquals("C", animation.frame());
	}

	@Test(expected = IllegalArgumentException.class)
	public void testIllegalFrameIndex() {
		animation.setFrameIndex(0);
		animation.setFrameIndex(1);
		animation.setFrameIndex(2);
		animation.setFrameIndex(3);
	}
}
