package de.amr.games.pacman.ui.swing.mspacman.entities;

import static de.amr.games.pacman.ui.swing.PacManGameUI_Swing.RENDERING_MSPACMAN;
import static de.amr.games.pacman.ui.swing.rendering.MsPacMan_Rendering.assets;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;

import de.amr.games.pacman.lib.Animation;
import de.amr.games.pacman.model.guys.GameEntity;

/**
 * The flap used in the intermission scenes.
 * 
 * @author Armin Reichert
 */
public class Flap extends GameEntity {

	private final int sceneNumber;
	private final String sceneTitle;

	public Flap(int number, String title) {
		sceneNumber = number;
		sceneTitle = title;
		animation = Animation.of( //
				assets.region(456, 208, 32, 32), //
				assets.region(488, 208, 32, 32), //
				assets.region(520, 208, 32, 32), //
				assets.region(488, 208, 32, 32), //
				assets.region(456, 208, 32, 32)//
		).repetitions(1).frameDuration(4);
	}

	public void draw(Graphics2D g) {
		if (visible) {
			RENDERING_MSPACMAN.drawSprite(g, (BufferedImage) animation.animate(), position.x, position.y);
			g.setFont(new Font(assets.getScoreFont().getName(), Font.PLAIN, 8));
			g.setColor(new Color(222, 222, 225, 192));
			g.drawString(sceneNumber + "", position.x + 20, position.y + 30);
			g.setFont(assets.getScoreFont());
			g.drawString(sceneTitle, position.x + 40, position.y + 20);
		}
	}
}