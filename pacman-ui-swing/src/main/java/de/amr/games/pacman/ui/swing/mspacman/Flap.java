package de.amr.games.pacman.ui.swing.mspacman;

import static de.amr.games.pacman.ui.swing.mspacman.MsPacMan_Rendering.assets;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;

import de.amr.games.pacman.lib.Animation;
import de.amr.games.pacman.model.guys.GameEntity;
import de.amr.games.pacman.ui.swing.PacManGameUI_Swing;

/**
 * The flap used in the intermission scenes.
 * 
 * @author Armin Reichert
 */
class Flap extends GameEntity {

	private final MsPacMan_Rendering rendering = PacManGameUI_Swing.RENDERING_MSPACMAN;
	private final Font font = new Font(assets.getScoreFont().getName(), Font.PLAIN, 8);

	public final Animation<BufferedImage> animation = Animation.of( //
			assets.region(456, 208, 32, 32), //
			assets.region(488, 208, 32, 32), //
			assets.region(520, 208, 32, 32), //
			assets.region(488, 208, 32, 32), //
			assets.region(456, 208, 32, 32)//
	).repetitions(1).frameDuration(4);

	public final int sceneNumber;
	public final String sceneTitle;

	public Flap(int number, String title) {
		sceneNumber = number;
		sceneTitle = title;
	}

	public void draw(Graphics2D g) {
		if (visible) {
			animation.animate();
			rendering.drawImage(g, animation.frame(), position.x, position.y, true);
			g.setFont(font);
			g.setColor(new Color(222, 222, 225, 192));
			g.drawString(sceneNumber + "", position.x + 20, position.y + 30);
			g.setFont(assets.getScoreFont());
			g.drawString(sceneTitle, position.x + 40, position.y + 20);
		}
	}
}