package de.amr.games.pacman.model.world;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

public class WallMapGenerator {

	public static void main(String[] args) {
		WallMapGenerator gen = new WallMapGenerator();
		int resolution = 8;
		gen.run("/pacman/maps/map1.txt", "pacman-map1-res-%d.txt", resolution);
		gen.run("/mspacman/maps/map1.txt", "mspacman-map1-res-%d.txt", resolution);
		gen.run("/mspacman/maps/map2.txt", "mspacman-map2-res-%d.txt", resolution);
		gen.run("/mspacman/maps/map3.txt", "mspacman-map3-res-%d.txt", resolution);
		gen.run("/mspacman/maps/map4.txt", "mspacman-map4-res-%d.txt", resolution);
	}

	private void run(String mapPath, String outputFileName, int resolution) {
		File dir = new File(System.getProperty("user.dir"));
		File out = new File(dir, String.format(outputFileName, resolution));
		try {
			out.createNewFile();
		} catch (IOException e) {
			e.printStackTrace();
			return;
		}
		WorldMap map = WorldMap.load(mapPath);
		MapBasedPacManGameWorld world = new MapBasedPacManGameWorld();
		world.setMap(map);
		FloorPlan wallMap = new FloorPlanBuilder(resolution).build(world);
		try (FileWriter w = new FileWriter(out, StandardCharsets.UTF_8)) {
			wallMap.print(w, true);
		} catch (IOException e) {
			e.printStackTrace();
		}
		System.out.println("Wall map file " + out.getAbsolutePath() + " created");
	}
}