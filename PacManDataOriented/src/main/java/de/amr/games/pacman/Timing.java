package de.amr.games.pacman;

public class Timing {

	public static int targetFPS = 60;
	public static long fps;
	public static long frames;
	public static long framesTotal;
	public static long fpsCountStartTime;
	public static long frameStartTime;
	public static long frameEndTime;
	public static long frameDuration;

	public static int sec(float seconds) {
		return (int) (seconds * targetFPS);
	}

	public static void runFrame(Runnable frame) {
		frameStartTime = System.nanoTime();
		frame.run();
		frameEndTime = System.nanoTime();
		frameDuration = frameEndTime - frameStartTime;
		++frames;
		++framesTotal;
		if (frameEndTime - fpsCountStartTime >= 1_000_000_000) {
			fps = frames;
			frames = 0;
			fpsCountStartTime = System.nanoTime();
		}
		long sleepTime = Math.max(1_000_000_000 / targetFPS - frameDuration, 0);
		if (sleepTime > 0) {
			try {
				Thread.sleep(sleepTime / 1_000_000); // milliseconds
			} catch (InterruptedException x) {
				x.printStackTrace();
			}
		}
	}
}