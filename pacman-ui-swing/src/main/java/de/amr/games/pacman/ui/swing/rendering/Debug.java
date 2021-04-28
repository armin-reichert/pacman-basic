package de.amr.games.pacman.ui.swing.rendering;

import static de.amr.games.pacman.model.world.PacManGameWorld.HTS;
import static de.amr.games.pacman.model.world.PacManGameWorld.TS;
import static de.amr.games.pacman.model.world.PacManGameWorld.t;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.Polygon;
import java.awt.Stroke;

import de.amr.games.pacman.controller.PacManGameController;
import de.amr.games.pacman.lib.Direction;
import de.amr.games.pacman.lib.V2i;
import de.amr.games.pacman.model.common.AbstractGameModel;
import de.amr.games.pacman.model.common.GameModel;

public class Debug {

	public static boolean on = false;

	public static void drawPlaySceneDebugInfo(Graphics2D g, PacManGameController controller) {
		GameModel game = controller.game();
		final Color[] GHOST_COLORS = { Color.RED, Color.PINK, Color.CYAN, Color.ORANGE };
		long remaining = controller.stateTimer().ticksRemaining();
		String ticksText = remaining == Long.MAX_VALUE ? "forever" : remaining + " ticks remaining";
		String stateText = String.format("%s (%s)", controller.state, ticksText);
		g.setColor(Color.WHITE);
		g.setFont(new Font("Arial", Font.PLAIN, 6));
		g.drawString(stateText, t(1), t(3));
		game.ghosts().forEach(ghost -> {
			g.setColor(Color.WHITE);
			g.drawRect((int) ghost.position.x, (int) ghost.position.y, TS, TS);
			if (ghost.targetTile != null) {
				Color c = GHOST_COLORS[ghost.id];
				g.setColor(c);
				g.fillRect(t(ghost.targetTile.x) + HTS / 2, t(ghost.targetTile.y) + HTS / 2, HTS, HTS);
			}
		});
		if (game.player().targetTile != null) {
			g.setColor(new Color(255, 255, 0, 200));
			g.fillRect(t(game.player().targetTile.x), t(game.player().targetTile.y), TS, TS);
		}
	}

	public static void drawMazeStructure(Graphics2D g, AbstractGameModel game) {
		final Polygon TRIANGLE = new Polygon(new int[] { -4, 4, 0 }, new int[] { 0, 0, 4 }, 3);
		Color dark = new Color(80, 80, 80, 200);
		Stroke thin = new BasicStroke(0.1f);
		g.setColor(dark);
		g.setStroke(thin);
		for (int x = 0; x < game.currentLevel().world.numCols(); ++x) {
			for (int y = 0; y < game.currentLevel().world.numRows(); ++y) {
				V2i tile = new V2i(x, y);
				if (game.currentLevel().world.isIntersection(tile)) {
					for (Direction dir : Direction.values()) {
						V2i neighbor = tile.plus(dir.vec);
						if (game.currentLevel().world.isWall(neighbor)) {
							continue;
						}
						g.drawLine(t(x) + HTS, t(y) + HTS, t(neighbor.x) + HTS, t(neighbor.y) + HTS);
					}
				} else if (game.currentLevel().world.isOneWayDown(tile)) {
					g.translate(t(x) + HTS, t(y));
					g.fillPolygon(TRIANGLE);
					g.translate(-t(x) - HTS, -t(y));
				}
			}
		}
	}
}