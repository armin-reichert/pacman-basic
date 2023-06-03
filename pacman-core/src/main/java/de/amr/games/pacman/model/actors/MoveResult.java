/*
Copyright (c) 2021-2023 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.games.pacman.model.actors;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class MoveResult {
	public boolean moved;
	public boolean tunnelEntered;
	public boolean teleported;
	public final List<String> messages;

	public MoveResult() {
		moved = false;
		tunnelEntered = false;
		teleported = false;
		messages = new ArrayList<>(3);
	}

	public String summary() {
		return messages.stream().collect(Collectors.joining(", "));
	}

	@Override
	public String toString() {
		var sb = new StringBuilder("");
		sb.append(tunnelEntered ? " entered tunnel" : "");
		sb.append(teleported ? " teleported" : "");
		sb.append(moved ? " moved" : "");
		return sb.length() == 0 ? "" : "[" + sb.toString().trim() + "]";
	}
}