package de.amr.games.pacman.model.world;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

/**
 * Test for floor plan generation.
 * 
 * @author Armin Reichert
 */
public class FloorPlanGenerator {

	public static void main(String[] args) {
		int resolution = 8;
		File dir = new File(System.getProperty("user.dir"));
		createFloorPlan("/pacman/maps/map1.txt", dir, "floorplan-pacman-map1-res-%d.txt", resolution);
		createFloorPlan("/mspacman/maps/map1.txt", dir, "floorplan-mspacman-map1-res-%d.txt", resolution);
		createFloorPlan("/mspacman/maps/map2.txt", dir, "floorplan-mspacman-map2-res-%d.txt", resolution);
		createFloorPlan("/mspacman/maps/map3.txt", dir, "floorplan-mspacman-map3-res-%d.txt", resolution);
	}

	private static void createFloorPlan(String mapPath, File dir, String outputFileNamePattern, int resolution) {
		MapBasedPacManGameWorld world = new MapBasedPacManGameWorld();
		WorldMap map = WorldMap.load(mapPath);
		world.setMap(map);
		FloorPlan wallMap = new FloorPlanBuilder(resolution).build(world);
		File out = new File(dir, String.format(outputFileNamePattern, resolution));
		try (FileWriter w = new FileWriter(out, StandardCharsets.UTF_8)) {
			wallMap.print(w, true);
			System.out.println("Floor plan " + out.getAbsolutePath() + " created");
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}