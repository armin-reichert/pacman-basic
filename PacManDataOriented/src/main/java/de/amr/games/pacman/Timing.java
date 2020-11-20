package de.amr.games.pacman;

public class Timing {

	public static int FPS = 60;

	public static int sec(float seconds) {
		return (int) (seconds * FPS);
	}

	public long fps;
	public long frames;
	public long framesTotal;
	public long fpsCountStartTime;
	public long frameStartTime;
	public long frameEndTime;
	public long frameDuration;

	public void beginFrame() {
		frameStartTime = System.nanoTime();
	}

	public void endFrame() {
		frameEndTime = System.nanoTime();
		frameDuration = frameEndTime - frameStartTime;
		++frames;
		++framesTotal;
		if (frameEndTime - fpsCountStartTime >= 1_000_000_000) {
			fps = frames;
			frames = 0;
			fpsCountStartTime = System.nanoTime();
		}
		long sleepTime = Math.max(1_000_000_000 / FPS - frameDuration, 0);
		if (sleepTime > 0) {
			try {
				Thread.sleep(sleepTime / 1_000_000); // milliseconds
			} catch (InterruptedException x) {
				x.printStackTrace();
			}
		}
	}
}