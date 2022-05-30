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

import static de.amr.games.pacman.lib.Logging.log;

import java.util.Collection;
import java.util.concurrent.ConcurrentLinkedQueue;

import de.amr.games.pacman.lib.V2i;
import de.amr.games.pacman.model.common.GameModel;

/**
 * @author Armin Reichert
 */
public class GameEventSupport {

	public static boolean logPublishEvents = true;

	private final GameModel game;
	private final Collection<GameEventListener> subscribers = new ConcurrentLinkedQueue<>();
	private boolean enabled;

	public GameEventSupport(GameModel game) {
		this.game = game;
	}

	public void setEnabled(boolean eventingEnabled) {
		this.enabled = eventingEnabled;
	}

	public void addEventListener(GameEventListener subscriber) {
		subscribers.add(subscriber);
	}

	public void removeEventListener(GameEventListener subscriber) {
		subscribers.remove(subscriber);
	}

	public void publish(GameEvent gameEvent) {
		if (enabled) {
			if (logPublishEvents && gameEvent.type != GameEventType.PLAYER_FINDS_FOOD) {
				log("%s", gameEvent);
			}
			subscribers.forEach(subscriber -> subscriber.onGameEvent(gameEvent));
		}
	}

	public void publish(GameEventType info, V2i tile) {
		publish(new GameEvent(game, info, null, tile));
	}
}