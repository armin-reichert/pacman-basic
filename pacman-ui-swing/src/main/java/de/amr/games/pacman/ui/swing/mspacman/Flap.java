package de.amr.games.pacman.ui.swing.mspacman;

import static de.amr.games.pacman.ui.swing.mspacman.MsPacMan_GameRendering.assets;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;

import de.amr.games.pacman.lib.Animation;
import de.amr.games.pacman.model.guys.GameEntity;
import de.amr.games.pacman.ui.swing.PacManGameSwingUI;

public class Flap extends GameEntity {

	static final MsPacMan_GameRendering rendering = PacManGameSwingUI.msPacManGameRendering;

	public int sceneNumber = 1;
	public String sceneTitle = "Scene #" + sceneNumber;
	public Animation<BufferedImage> animation = assets.flapAnim;
	public Font font = new Font(assets.getScoreFont().getName(), Font.PLAIN, 8);

	public void draw(Graphics2D g) {
		if (visible) {
			animation.animate();
			rendering.drawImage(g, animation.frame(), position.x, position.y, true);
			g.setFont(font);
			g.setColor(new Color(222, 222, 225, 200));
			g.drawString(sceneNumber + "", position.x + 20, position.y + 30);
			if (animation.isRunning()) {
				g.setFont(assets.getScoreFont());
				g.drawString(sceneTitle, position.x + 40, position.y + 20);
			}
		}
	}
}