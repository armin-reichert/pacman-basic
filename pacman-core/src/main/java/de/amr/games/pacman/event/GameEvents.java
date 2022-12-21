/*
MIT License

Copyright (c) 2021-22 Armin Reichert

Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files (the "Software"), to deal
in the Software without restriction, including without limitation the rights
to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
copies of the Software, and to permit persons to whom the Software is
furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all
copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
SOFTWARE.
 */
package de.amr.games.pacman.event;

import java.util.Collection;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.function.Supplier;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import de.amr.games.pacman.lib.Vector2i;
import de.amr.games.pacman.model.common.GameModel;

/**
 * @author Armin Reichert
 */
public class GameEvents {

	private static final Logger LOGGER = LogManager.getFormatterLogger();
	private static final GameEvents IT = new GameEvents();

	private Supplier<GameModel> fnGame;
	private final Collection<GameEventListener> subscribers = new ConcurrentLinkedQueue<>();

	private GameEvents() {
	}

	public static void setGame(Supplier<GameModel> fnGame) {
		IT.fnGame = fnGame;
	}

	public static void addListener(GameEventListener subscriber) {
		IT.subscribers.add(subscriber);
	}

	public static void removeListener(GameEventListener subscriber) {
		IT.subscribers.remove(subscriber);
	}

	public static void publish(GameEvent event) {
		LOGGER.trace("Game event: %s", event);
		IT.subscribers.forEach(subscriber -> subscriber.onGameEvent(event));
	}

	public static void publish(GameEventType type, Vector2i tile) {
		publish(new GameEvent(IT.fnGame.get(), type, null, tile));
	}
}