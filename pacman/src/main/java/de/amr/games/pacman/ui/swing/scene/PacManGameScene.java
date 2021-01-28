package de.amr.games.pacman.ui.swing.scene;

import static de.amr.games.pacman.game.worlds.PacManGameWorld.HTS;
import static de.amr.games.pacman.game.worlds.PacManGameWorld.TS;
import static de.amr.games.pacman.game.worlds.PacManGameWorld.t;
import static java.lang.Math.round;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.Polygon;
import java.awt.Stroke;
import java.awt.image.BufferedImage;

import de.amr.games.pacman.game.core.PacManGameModel;
import de.amr.games.pacman.game.creatures.Ghost;
import de.amr.games.pacman.lib.Direction;
import de.amr.games.pacman.lib.V2i;
import de.amr.games.pacman.ui.swing.PacManGameSwingUI;

/**
 * Implemented by all scenes of the Pac-Man and Ms. Pac-Man game.
 * 
 * @author Armin Reichert
 */
public interface PacManGameScene {

	V2i size();

	void draw(Graphics2D g);

	default void drawCenteredText(Graphics2D g, String text, int y) {
		g.drawString(text, (size().x - g.getFontMetrics().stringWidth(text)) / 2, y);
	}

	default void drawCenteredImage(Graphics2D g, BufferedImage image, int y) {
		g.drawImage(image, (size().x - image.getWidth()) / 2, y, null);
	}

	default void start() {

	}

	default void end() {

	}

	// debugging help

	default void drawDebugInfo(Graphics2D g, PacManGameModel game) {
		if (PacManGameSwingUI.debugMode) {
			final Color[] GHOST_COLORS = { Color.RED, Color.PINK, Color.CYAN, Color.ORANGE };
			long remaining = game.state.remaining();
			String ticksText = remaining == Long.MAX_VALUE ? "forever" : remaining + " ticks remaining";
			String stateText = String.format("%s (%s)", game.stateDescription(), ticksText);
			g.setColor(Color.WHITE);
			g.setFont(new Font("Arial", Font.PLAIN, 6));
			g.drawString(stateText, t(1), t(3));
			for (Ghost ghost : game.ghosts) {
				g.setColor(Color.WHITE);
				g.drawRect(round(ghost.position.x), round(ghost.position.y), TS, TS);
				if (ghost.targetTile != null) {
					Color c = GHOST_COLORS[ghost.id];
					g.setColor(c);
					g.fillRect(t(ghost.targetTile.x) + HTS / 2, t(ghost.targetTile.y) + HTS / 2, HTS, HTS);
				}
			}
			if (game.pac.targetTile != null) {
				g.setColor(new Color(255, 255, 0, 200));
				g.fillRect(t(game.pac.targetTile.x), t(game.pac.targetTile.y), TS, TS);
			}
		}
	}

	default void drawMazeStructure(Graphics2D g, PacManGameModel game) {
		final Polygon TRIANGLE = new Polygon(new int[] { -4, 4, 0 }, new int[] { 0, 0, 4 }, 3);
		Color dark = new Color(80, 80, 80, 200);
		Stroke thin = new BasicStroke(0.1f);
		g.setColor(dark);
		g.setStroke(thin);
		for (int x = 0; x < game.world.xTiles(); ++x) {
			for (int y = 0; y < game.world.yTiles(); ++y) {
				if (game.world.isIntersection(x, y)) {
					for (Direction dir : Direction.values()) {
						int nx = x + dir.vec.x, ny = y + dir.vec.y;
						if (!game.world.isAccessible(nx, ny)) {
							continue;
						}
						g.drawLine(t(x) + HTS, t(y) + HTS, t(nx) + HTS, t(ny) + HTS);
					}
				} else if (game.world.isUpwardsBlocked(x, y)) {
					g.translate(t(x) + HTS, t(y));
					g.fillPolygon(TRIANGLE);
					g.translate(-t(x) - HTS, -t(y));
				}
			}
		}
	}

}