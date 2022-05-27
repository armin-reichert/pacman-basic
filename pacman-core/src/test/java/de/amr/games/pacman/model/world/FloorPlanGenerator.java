package de.amr.games.pacman.model.world;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

import de.amr.games.pacman.model.common.world.FloorPlan;
import de.amr.games.pacman.model.common.world.World;
import de.amr.games.pacman.model.mspacman.MsPacManWorld1;
import de.amr.games.pacman.model.mspacman.MsPacManWorld2;
import de.amr.games.pacman.model.mspacman.MsPacManWorld3;
import de.amr.games.pacman.model.mspacman.MsPacManWorld4;
import de.amr.games.pacman.model.pacman.PacManGame;

/**
 * Test for floor plan generation.
 * 
 * @author Armin Reichert
 */
public class FloorPlanGenerator {

	public static void main(String[] args) {
		int resolution = 8;
		File dir = new File(System.getProperty("user.dir"));
		createFloorPlan(PacManGame.createWorld(), dir, "floorplan-pacman-map1-res-%d.txt", resolution);
		createFloorPlan(new MsPacManWorld1(), dir, "floorplan-mspacman-map1-res-%d.txt", resolution);
		createFloorPlan(new MsPacManWorld2(), dir, "floorplan-mspacman-map2-res-%d.txt", resolution);
		createFloorPlan(new MsPacManWorld3(), dir, "floorplan-mspacman-map3-res-%d.txt", resolution);
		createFloorPlan(new MsPacManWorld4(), dir, "floorplan-mspacman-map4-res-%d.txt", resolution);
	}

	private static void createFloorPlan(World world, File dir, String outputFileNamePattern, int resolution) {
		FloorPlan floorPlan = new FloorPlan(resolution, world);
		File out = new File(dir, String.format(outputFileNamePattern, resolution));
		try (FileWriter w = new FileWriter(out, StandardCharsets.UTF_8)) {
			floorPlan.print(w, true);
			System.out.println("Floor plan " + out.getAbsolutePath() + " created");
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}