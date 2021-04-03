package de.amr.games.pacman.ui.swing.rendering.common;

import static de.amr.games.pacman.model.world.PacManGameWorld.TS;

import java.awt.Color;
import java.awt.Graphics2D;

import de.amr.games.pacman.lib.TimedSequence;
import de.amr.games.pacman.lib.V2i;

public class Energizer2D {

	private final V2i tile;
	private TimedSequence<Boolean> blinkingAnimation = TimedSequence.pulse().frameDuration(15);

	public Energizer2D(V2i tile) {
		this.tile = tile;
	}

	public TimedSequence<Boolean> getBlinkingAnimation() {
		return blinkingAnimation;
	}

	public void render(Graphics2D g) {
		if (!blinkingAnimation.animate()) {
			g.setColor(Color.BLACK);
			g.fillRect(tile.x * TS, tile.y * TS, TS, TS);
		}
	}
}