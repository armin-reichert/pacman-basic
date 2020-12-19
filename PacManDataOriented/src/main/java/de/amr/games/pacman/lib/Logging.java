package de.amr.games.pacman.lib;

import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

public class Logging {

	public static final DateTimeFormatter TIME_FORMAT = DateTimeFormatter.ofPattern("HH:mm:ss.SSS");

	public static void log(String msg, Object... args) {
		String timestamp = TIME_FORMAT.format(LocalTime.now());
		String message = String.format(msg, args);
		System.err.printf("[%s] %s\n", timestamp, message);
	}
}