package de.amr.games.pacman.ui.swing.mspacman;

import static de.amr.games.pacman.ui.swing.mspacman.MsPacMan_GameRendering.assets;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;

import de.amr.games.pacman.lib.Animation;
import de.amr.games.pacman.model.guys.GameEntity;
import de.amr.games.pacman.ui.swing.PacManGameSwingUI;

/**
 * The flap used in the intermission scenes.
 * 
 * @author Armin Reichert
 */
class Flap extends GameEntity {

	private final MsPacMan_GameRendering rendering;
	private final Font font;

	public final Animation<BufferedImage> animation;
	public final int sceneNumber;
	public final String sceneTitle;

	public Flap(int number, String title) {
		sceneNumber = number;
		sceneTitle = title;
		rendering = PacManGameSwingUI.RENDERING_MSPACMAN;
		font = new Font(assets.getScoreFont().getName(), Font.PLAIN, 8);
		animation = Animation.of( //
				assets.region(456, 208, 32, 32), //
				assets.region(488, 208, 32, 32), //
				assets.region(520, 208, 32, 32), //
				assets.region(488, 208, 32, 32), //
				assets.region(456, 208, 32, 32)//
		);
		animation.repetitions(1).frameDuration(4);
	}

	public void draw(Graphics2D g) {
		if (visible) {
			animation.animate();
			rendering.drawImage(g, animation.frame(), position.x, position.y, true);
			g.setFont(font);
			g.setColor(new Color(222, 222, 225, 200));
			g.drawString(sceneNumber + "", position.x + 20, position.y + 30);
			g.setFont(assets.getScoreFont());
			g.drawString(sceneTitle, position.x + 40, position.y + 20);
		}
	}
}