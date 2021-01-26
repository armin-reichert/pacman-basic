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

import de.amr.games.pacman.game.core.PacManGameController;
import de.amr.games.pacman.game.core.PacManGameModel;
import de.amr.games.pacman.game.creatures.Ghost;
import de.amr.games.pacman.lib.Direction;
import de.amr.games.pacman.lib.V2i;
import de.amr.games.pacman.ui.swing.PacManGameSwingUI;

public abstract class PacManGamePlayScene implements PacManGameScene {

	private static final Color[] GHOST_COLORS = { Color.RED, Color.PINK, Color.CYAN, Color.ORANGE };
	private static final Polygon TRIANGLE = new Polygon(new int[] { -4, 4, 0 }, new int[] { 0, 0, 4 }, 3);

	protected final PacManGameSwingUI ui;
	protected final PacManGameController controller;
	protected final PacManGameModel game;
	protected final V2i size;

	public PacManGamePlayScene(PacManGameSwingUI ui, PacManGameController controller, V2i size) {
		this.ui = ui;
		this.controller = controller;
		this.game = controller.game;
		this.size = size;
	}

	@Override
	public V2i size() {
		return size;
	}

	public void drawDebugInfo(Graphics2D g) {
		if (PacManGameSwingUI.debugMode) {
			long remaining = controller.state.remaining();
			String ticksText = remaining == Long.MAX_VALUE ? "forever" : remaining + " ticks remaining";
			String stateText = String.format("%s (%s)", controller.stateDescription(), ticksText);
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

	public void drawMazeStructure(Graphics2D g) {
		Color dark = new Color(80, 80, 80, 200);
		Stroke thin = new BasicStroke(0.1f);
		g.setColor(dark);
		g.setStroke(thin);
		for (int x = 0; x < game.world.xTiles(); ++x) {
			for (int y = 0; y < game.world.yTiles(); ++y) {
				if (game.world.isIntersection(x, y)) {
					for (Direction dir : Direction.values()) {
						int nx = x + dir.vec.x, ny = y + dir.vec.y;
						if (game.world.isWall(nx, ny)) {
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