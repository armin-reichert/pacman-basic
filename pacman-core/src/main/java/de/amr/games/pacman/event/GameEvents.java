/*
Copyright (c) 2021-2023 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.games.pacman.event;

import static de.amr.games.pacman.lib.Globals.checkNotNull;

import java.util.Collection;
import java.util.concurrent.ConcurrentLinkedQueue;

import org.tinylog.Logger;

import de.amr.games.pacman.controller.GameController;
import de.amr.games.pacman.lib.Vector2i;
import de.amr.games.pacman.model.GameModel;

/**
 * @author Armin Reichert
 */
public class GameEvents {

	private static GameController gameController;
	private static Collection<GameEventListener> subscribers = new ConcurrentLinkedQueue<>();
	private static boolean soundEventsEnabled = true;

	private GameEvents() {
	}

	public static void setGameController(GameController gameController) {
		checkNotNull(gameController);
		GameEvents.gameController = gameController;
	}

	public static void setSoundEventsEnabled(boolean enabled) {
		GameEvents.soundEventsEnabled = enabled;
		Logger.info("Sound events {}", enabled ? "enabled" : "disabled");
	}

	public static void addListener(GameEventListener subscriber) {
		checkNotNull(subscriber);
		GameEvents.subscribers.add(subscriber);
	}

	public static void removeListener(GameEventListener subscriber) {
		checkNotNull(subscriber);
		GameEvents.subscribers.remove(subscriber);
	}

	public static void publishGameEvent(GameEvent event) {
		checkNotNull(event);
		Logger.trace("Publish game event: {}", event);
		GameEvents.subscribers.forEach(subscriber -> subscriber.onGameEvent(event));
	}

	public static void publishGameEvent(byte type, Vector2i tile) {
		checkNotNull(tile);
		publishGameEvent(new GameEvent(gameController.game(), type, tile));
	}

	public static void publishGameEventOfType(byte type, GameModel game) {
		publishGameEvent(new GameEvent(game, type, null));
	}

	public static void publishSoundEvent(byte soundEventID, GameModel game) {
		checkNotNull(soundEventID);
		if (GameEvents.soundEventsEnabled) {
			publishGameEvent(new SoundEvent(game, soundEventID));
		}
	}
}