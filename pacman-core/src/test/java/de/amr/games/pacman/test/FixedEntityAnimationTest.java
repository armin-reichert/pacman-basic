/*
Copyright (c) 2021-2023 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
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
